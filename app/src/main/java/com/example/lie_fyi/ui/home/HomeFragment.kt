package com.example.lie_fyi.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lie_fyi.R
import com.example.lie_fyi.analysis.PitchAnalyzer
import com.example.lie_fyi.analysis.RealtimeDispatcher
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var rootView: View? = null
    private var buttonStartAnalysis: Button? = null
    private var textAnalysisStatus: TextView? = null
    private var textTranscript: TextView? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var baselinePitch: Float = 0f

    private lateinit var pitchAnalyzer: PitchAnalyzer
    private var realtimeDispatcher: RealtimeDispatcher? = null
    private var originalBackground: Drawable? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private val transcript = StringBuilder()
    private val lieEvents = mutableListOf<String>()

    private enum class State {
        IDLE,
        CALIBRATING,
        LISTENING
    }
    private var currentState = State.IDLE

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                enterState(State.CALIBRATING)
            } else {
                Toast.makeText(requireContext(), "Permission denied. The lie detector feature is unavailable.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(com.example.lie_fyi.R.layout.fragment_home, container, false)
        buttonStartAnalysis = rootView?.findViewById(com.example.lie_fyi.R.id.buttonStartAnalysis)
        textAnalysisStatus = rootView?.findViewById(com.example.lie_fyi.R.id.textAnalysisStatus)
        textTranscript = rootView?.findViewById(com.example.lie_fyi.R.id.textTranscript)

        pitchAnalyzer = PitchAnalyzer()
        originalBackground = rootView?.background
        setupSpeechRecognizer()
        buttonStartAnalysis?.setOnClickListener { onButtonClicked() }
        enterState(State.IDLE)
        return rootView
    }

    private fun onButtonClicked() {
        when (currentState) {
            State.IDLE -> {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        enterState(State.CALIBRATING)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
            State.LISTENING -> {
                stopRealtimePitchDetection()
                stopSpeechRecognition()

                val transcriptText = transcript.toString().trim()
                val timestampsArray = lieEvents.toTypedArray()
                val audioPath = audioFile?.absolutePath.orEmpty()

                // Navigate to report screen using NavController arguments.
                // If Safe Args classes are not generated yet, we fall back to navigateUp.
                try {
                    val navController = androidx.navigation.fragment.findNavController(this)
                    val action = com.example.liefyi.R.id.action_nav_home_to_nav_report

                    val bundle = android.os.Bundle().apply {
                        putString("transcript", transcriptText)
                        putStringArray("lieTimestamps", timestampsArray)
                        putString("audioFilePath", audioPath)
                    }

                    navController.navigate(action, bundle)
                } catch (_: Throwable) {
                    Toast.makeText(requireContext(), "Analysis complete — report ready", Toast.LENGTH_LONG).show()
                    androidx.navigation.fragment.findNavController(this).navigateUp()
                }

                enterState(State.IDLE)
            }
            State.CALIBRATING -> {}
        }
    }

    private fun enterState(newState: State) {
        if (currentState == State.LISTENING) {
            stopRealtimePitchDetection()
            stopSpeechRecognition()
        }

        currentState = newState
        when (currentState) {
            State.IDLE -> {
                rootView?.background = originalBackground
                buttonStartAnalysis?.text = "Start Analysis"
                buttonStartAnalysis?.isEnabled = true
                textAnalysisStatus?.text = "Press the button to start"
                textTranscript?.text = ""
                transcript.clear()
                lieEvents.clear()
                baselinePitch = 0f
            }
            State.CALIBRATING -> {
                buttonStartAnalysis?.text = "Calibrating..."
                buttonStartAnalysis?.isEnabled = false
                textAnalysisStatus?.text = "To calibrate, please state your full name."
                startRecording()

                Handler(Looper.getMainLooper()).postDelayed({
                    stopRecording()
                    audioFile?.let { file ->
                        thread {
                            val pitch = pitchAnalyzer.getAveragePitch(file)
                            activity?.runOnUiThread {
                                if (pitch > 0) {
                                    baselinePitch = pitch
                                    enterState(State.LISTENING)
                                } else {
                                    textAnalysisStatus?.text = "Calibration failed. Please try again."
                                    enterState(State.IDLE)
                                }
                            }
                        }
                    }
                }, 5000)
            }
            State.LISTENING -> {
                buttonStartAnalysis?.text = "Stop Analysis"
                buttonStartAnalysis?.isEnabled = true
                textAnalysisStatus?.text = "Listening... (Baseline: %.2f Hz)".format(baselinePitch)
                startRealtimePitchDetection()
                startSpeechRecognition()
            }
        }
    }

    private fun startRecording() {
        audioFile = File(requireContext().cacheDir, "calibration_audio.3gp")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Recording failed to start", Toast.LENGTH_SHORT).show()
                enterState(State.IDLE)
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                 // It might have already been released
            }
        }
        mediaRecorder = null
    }

    private fun startRealtimePitchDetection() {
        val handler: (Float) -> Unit = { pitchInHz ->
            if (isAdded) {
                activity?.runOnUiThread {
                    if (currentState == State.LISTENING && pitchInHz != -1.0f && baselinePitch > 0) {
                        textAnalysisStatus?.text = "Listening... (Live: %.2f Hz)".format(pitchInHz)
                        if (pitchInHz > baselinePitch * 1.25) {
                            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            lieEvents.add("Pitch spike detected at $timestamp (Live: %.2f Hz, Baseline: %.2f Hz)".format(pitchInHz, baselinePitch))
                            rootView?.setBackgroundColor(Color.RED)
                            Handler(Looper.getMainLooper()).postDelayed({ rootView?.background = originalBackground }, 500)
                        }
                    }
                }
            }
        }
        try {
            realtimeDispatcher = pitchAnalyzer.startRealtimePitchDetection(handler)
        } catch (ise: IllegalStateException) {
            Toast.makeText(requireContext(), ise.message ?: "Microphone unavailable.", Toast.LENGTH_LONG).show()
            realtimeDispatcher = null
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to start realtime pitch detection.", Toast.LENGTH_LONG).show()
            realtimeDispatcher = null
        }
    }

    private fun stopRealtimePitchDetection() {
        realtimeDispatcher?.stop()
        realtimeDispatcher = null
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    transcript.append(matches[0]).append(". ")
                    textTranscript?.text = transcript.toString()
                }
                if (currentState == State.LISTENING) startSpeechRecognition() // Listen for the next utterance
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val sentenceSoFar = transcript.toString().substringBeforeLast(". ", "")
                    val text = if(sentenceSoFar.isEmpty()) matches[0] else "$sentenceSoFar. ${matches[0]}"
                    textTranscript?.text = text
                }
            }

            override fun onEndOfSpeech() { }

            override fun onError(error: Int) {
                if(error == SpeechRecognizer.ERROR_NO_MATCH && currentState == State.LISTENING){
                    startSpeechRecognition()
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startSpeechRecognition() {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    private fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopRecording()
        stopRealtimePitchDetection()
        speechRecognizer?.destroy()
        // _binding was removed because this fragment uses manual view lookups
        rootView = null
    }
}
