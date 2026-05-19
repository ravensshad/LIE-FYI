package com.example.liefyi.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.liefyi.R

class ReportFragment : Fragment() {

    private fun getArgsFromBundle(): Triple<String, Array<String>?, String> {
        val argsBundle = arguments
        val transcript = argsBundle?.getString("transcript").orEmpty()
        val lieTimestamps = argsBundle?.getStringArray("lieTimestamps")
        val audioFilePath = argsBundle?.getString("audioFilePath").orEmpty()
        return Triple(transcript, lieTimestamps, audioFilePath)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textTranscript = view.findViewById<TextView>(R.id.text_transcript)
        val textLieEvents = view.findViewById<TextView>(R.id.text_lie_events)
        val textAudioPath = view.findViewById<TextView>(R.id.text_audio_path)
        val buttonBackHome = view.findViewById<Button>(R.id.button_back_home)

        val (transcriptText, lieTimestamps, audioFilePathRaw) = getArgsFromBundle()
        val lieEventsText = lieTimestamps?.joinToString(separator = "\n") { it } ?: "(none)"
        val audioFilePath = audioFilePathRaw.ifBlank { "(not available)" }

        textTranscript.text = transcriptText.ifBlank { "(no transcript captured)" }
        textLieEvents.text = lieEventsText
        textAudioPath.text = audioFilePath

        buttonBackHome.setOnClickListener {
            findNavController(this).navigateUp()
        }
    }
}

