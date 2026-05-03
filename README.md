# LIE-FYI

**Deception Detection through Linguistic & Behavioral Analysis**

A sophisticated lie detection application that analyzes text for linguistic and behavioral indicators commonly associated with dishonesty. Uses forensic linguistics, behavioral psychology, and cognitive science research to identify potential deception tells.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Node](https://img.shields.io/badge/node-%3E%3D14.0.0-brightgreen)

## 🎯 Features

### Comprehensive Analysis
- **Linguistic Analysis (35% weight)**: Examines pronoun usage, negation patterns, hedging language, temporal references, and specificity
- **Behavioral Analysis (35% weight)**: Detects stress indicators, sentence fragmentation, repetition, backtracking, and emotional defense
- **Red Flag Indicators (30% weight)**: Identifies defensive language, blame-shifting, objections, deflection, and distance markers

### Smart Comparison
- Compare baseline vs. current statements to detect changing patterns
- Track volatility in linguistic and behavioral markers
- Analyze trends across multiple statements

### Risk Assessment
- Deception score from 0-100
- Six risk levels: MINIMAL, LOW, MODERATE, HIGH, VERY_HIGH, CRITICAL
- Confidence assessment and detailed recommendations

## 📦 Installation

### Prerequisites
- Node.js 14+ 
- npm or yarn

### Setup

```bash
# Clone the repository
git clone https://github.com/ravensshad/LIE-FYI.git
cd LIE-FYI

# Install dependencies
npm install

# Start the server
npm start
```

The application will be available at `http://localhost:3000`

### Development Mode

```bash
npm run dev  # Uses nodemon for auto-restart
```

## 🚀 Usage

### Web Interface

1. Open `http://localhost:3000` in your browser
2. Choose analysis type:
   - **Single Analysis**: Analyze one text for deception indicators
   - **Compare Statements**: Compare two statements to detect pattern changes
3. Enter text and click analyze
4. Review detailed breakdown of linguistic, behavioral, and red flag indicators

### API Endpoints

#### Analyze Single Text
```bash
curl -X POST http://localhost:3000/api/analyze \
  -H "Content-Type: application/json" \
  -d '{"text": "Your text here"}'
```

**Response:**
```json
{
  "score": 42.5,
  "riskLevel": "MODERATE",
  "timestamp": "2026-05-03T12:00:00.000Z",
  "analysis": {
    "linguistic": { ... },
    "behavioral": { ... },
    "redFlags": { ... }
  },
  "summary": {
    "riskFactors": [...],
    "confidence": "MEDIUM-HIGH",
    "recommendation": "..."
  }
}
```

#### Compare Two Statements
```bash
curl -X POST http://localhost:3000/api/compare \
  -H "Content-Type: application/json" \
  -d '{
    "baseline": "First statement",
    "current": "Second statement"
  }'
```

#### Get Risk Levels
```bash
curl http://localhost:3000/api/risk-levels
```

#### Get Documentation
```bash
curl http://localhost:3000/api/documentation
```

## 📊 Analysis Breakdown

### Linguistic Indicators
- **First-Person Pronouns**: Liars tend to use fewer first-person pronouns
- **Negation Frequency**: Excessive negations indicate internal conflict
- **Hedging Language**: "Maybe," "might," "seem" indicate uncertainty
- **Filler Words**: "Um," "uh," "like" suggest cognitive load
- **Temporal References**: Specific times/dates vs. vague references
- **Absolute Language**: "Always," "never" can indicate overstatement

### Behavioral Indicators
- **Stress Indicators**: Repetition, contracted negatives, qualifiers
- **Sentence Fragmentation**: Incomplete thoughts suggest cognitive overload
- **Repetition Rate**: Repeated words indicate processing difficulty
- **Backtracking**: Self-corrections and parenthetical statements
- **Emotional Defense**: Exclamations and capitalization
- **Deflecting Behavior**: Turning statements into questions

### Red Flags
- **Defensive Language**: "I swear," "believe me," direct denials
- **Blame Shifting**: Externalizing responsibility
- **Objections/Denials**: Excessive contradictions
- **Deflection**: Counter-questions instead of direct answers
- **Repair Attempts**: Multiple corrections
- **Distance Markers**: Third-person references, passive voice

## 🎓 Risk Levels

| Level | Score | Interpretation |
|-------|-------|-----------------|
| **MINIMAL** | 0-20 | Very likely truthful |
| **LOW** | 20-35 | Likely truthful with minor indicators |
| **MODERATE** | 35-50 | Mixed indicators, further investigation recommended |
| **HIGH** | 50-65 | Notable deception indicators detected |
| **VERY HIGH** | 65-80 | Strong deception indicators |
| **CRITICAL** | 80-100 | Critical deception indicators |

## 📁 Project Structure

```
LIE-FYI/
├── server.js              # Express server and API endpoints
├── public/
│   ├── index.html        # Frontend interface
│   ├── app.js            # Frontend logic
│   └── styles.css        # Styling
├── src/
│   ├── analyzer.js       # Main analysis orchestrator
│   └── modules/
│       ├── linguistics.js # Linguistic pattern analysis
│       ├── behavioral.js  # Behavioral analysis
│       └── indicators.js  # Red flag detection
├── package.json          # Dependencies
└── README.md            # Documentation
```

## ⚠️ Important Disclaimer

This tool analyzes linguistic and behavioral patterns commonly associated with deception based on forensic linguistics research. However:

- **Should NOT be used as sole evidence** in legal or investigative proceedings
- **Results should be considered** alongside other investigative methods
- **Professional judgment** is always required
- **Context matters**: Cultural, neurological, and personality factors can influence language patterns
- **Not 100% accurate**: Deception detection is not an exact science

This is an **analytical aid**, not a definitive truth detector.

## 🔬 Research Foundation

The analysis is based on research from:
- Forensic linguistics and stylometry
- Cognitive psychology and deception detection
- Behavioral analysis frameworks
- Linguistic analysis of truth vs. deception
- Stress indicators in speech patterns

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see LICENSE file for details.

## 👤 Author

Created by [@ravensshad](https://github.com/ravensshad)

## 🙏 Acknowledgments

- Forensic linguistics research communities
- Behavioral analysis frameworks
- Deception detection researchers
- Open-source community

## 📮 Support

For issues, suggestions, or questions:
- Open an issue on GitHub
- Check existing documentation
- Review the API documentation at `/api/documentation`

---

**Disclaimer**: This tool is for analytical and educational purposes. Always verify information through multiple sources and consult appropriate professionals for important decisions.
