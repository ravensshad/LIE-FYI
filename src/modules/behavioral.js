/**
 * Behavioral analysis module
 * Detects stress, cognitive load, and behavioral tells in speech patterns
 */

const BEHAVIORAL_PATTERNS = {
  // Repeated words/phrases - sign of cognitive load
  repetition: /\b(\w+)\s+(?=\1\b)/gi,

  // Parenthetical statements - sign of correction/backtracking
  parenthetical: /\(.*?\)/g,

  // Ellipsis/trailing off - sign of uncertainty
  ellipsis: /\.{2,}|…/g,

  // Capitalization - emotional emphasis or stress
  allCaps: /\b[A-Z]{2,}\b/g,

  // Questions deflecting - turning statements into questions
  deflectingQuestions: /^[^?.!]*\?$/gm,

  // Contracted negatives - stress indicator
  contractedNegative: /\b(won't|can't|shouldn't|wouldn't|couldn't|didn't|doesn't|hasn't|haven't|wasn't|weren't|isn't|aren't)\b/gi,

  // Exclamation marks - emotional defense
  exclamations: /!/g,

  // Qualifying phrases - hedging and uncertainty
  qualifiers: /\b(I guess|I think|I believe|sort of|kind of|like|just|only|really|actually|apparently|supposedly)\b/gi,
};

/**
 * Analyze text for behavioral stress indicators
 */
function analyze(text) {
  const wordCount = text.split(/\s+/).length;
  const sentences = text.match(/[.!?]+/g) || [];

  return {
    stressIndicators: calculateStressIndicators(text, wordCount),
    sentenceFragmentation: calculateSentenceFragmentation(text),
    repetitionRate: calculateRepetitionRate(text, wordCount),
    backtracking: calculateBacktracking(text),
    emotionalDefense: calculateEmotionalDefense(text, wordCount),
    deflectingBehavior: calculateDeflectingBehavior(text),
    contractedNegatives: countMatches(text, BEHAVIORAL_PATTERNS.contractedNegative) / wordCount,
    details: {
      avgSentenceLength: wordCount / Math.max(1, sentences.length),
      sentenceCount: sentences.length,
      uniqueWords: countUniqueWords(text),
      typeTokenRatio: calculateTypeTokenRatio(text),
    }
  };
}

/**
 * Calculate overall stress indicators score
 */
function calculateStressIndicators(text, wordCount) {
  let score = 0;

  // Repetition indicates cognitive processing stress
  const repetitionCount = (text.match(BEHAVIORAL_PATTERNS.repetition) || []).length;
  score += (repetitionCount / Math.max(1, wordCount / 5)) * 0.25;

  // Backtracking/corrections suggest cognitive overload
  const backtrackingCount = countMatches(text, BEHAVIORAL_PATTERNS.parenthetical);
  score += (backtrackingCount / Math.max(1, text.split(/[.!?]+/).length)) * 0.25;

  // Excessive exclamations can indicate defensive stress
  const exclamationCount = countMatches(text, BEHAVIORAL_PATTERNS.exclamations);
  score += (exclamationCount / Math.max(1, wordCount)) * 0.15;

  // Contracted negatives are stress indicators
  const contractedCount = countMatches(text, BEHAVIORAL_PATTERNS.contractedNegative);
  score += (contractedCount / Math.max(1, wordCount)) * 0.15;

  // Qualifiers suggest uncertainty/stress
  const qualifierCount = countMatches(text, BEHAVIORAL_PATTERNS.qualifiers);
  score += (qualifierCount / Math.max(1, wordCount)) * 0.20;

  return Math.min(1, Math.max(0, score));
}

/**
 * Calculate sentence fragmentation (incomplete thoughts)
 */
function calculateSentenceFragmentation(text) {
  const sentences = text.match(/[^.!?]+[.!?]+/g) || [];
  if (sentences.length === 0) return 0;

  let fragmentCount = 0;
  sentences.forEach(sentence => {
    const trimmed = sentence.trim();
    // Fragments typically lack proper subjects/verbs or are very short
    const wordCount = trimmed.split(/\s+/).length;
    if (wordCount < 3 || !hasProperStructure(trimmed)) {
      fragmentCount++;
    }
  });

  return fragmentCount / sentences.length;
}

/**
 * Calculate repetition rate
 */
function calculateRepetitionRate(text, wordCount) {
  const words = text.toLowerCase().split(/\s+/);
  const wordFreq = {};

  words.forEach(word => {
    if (word.length > 3) { // Only count meaningful words
      wordFreq[word] = (wordFreq[word] || 0) + 1;
    }
  });

  let repetitionCount = 0;
  Object.values(wordFreq).forEach(count => {
    if (count > 1) {
      repetitionCount += count - 1;
    }
  });

  return repetitionCount / Math.max(1, wordCount);
}

/**
 * Calculate backtracking/self-correction indicators
 */
function calculateBacktracking(text) {
  const corrections = countMatches(text, BEHAVIORAL_PATTERNS.parenthetical);
  const ellipsis = countMatches(text, BEHAVIORAL_PATTERNS.ellipsis);
  const sentences = text.split(/[.!?]+/).length;

  return (corrections + ellipsis) / Math.max(1, sentences);
}

/**
 * Calculate emotional defense indicators
 */
function calculateEmotionalDefense(text, wordCount) {
  let score = 0;

  // Excessive exclamation marks
  const exclamations = countMatches(text, BEHAVIORAL_PATTERNS.exclamations);
  score += (exclamations / Math.max(1, wordCount)) * 0.5;

  // ALL CAPS words (emotional emphasis)
  const capsWords = countMatches(text, BEHAVIORAL_PATTERNS.allCaps);
  score += (capsWords / Math.max(1, wordCount / 10)) * 0.5;

  return Math.min(1, score);
}

/**
 * Calculate deflecting behavior (avoiding direct answers)
 */
function calculateDeflectingBehavior(text) {
  const lines = text.split('\n');
  let deflectingCount = 0;

  lines.forEach(line => {
    // Turning statements into questions can be evasive
    if (BEHAVIORAL_PATTERNS.deflectingQuestions.test(line)) {
      deflectingCount++;
    }
  });

  return deflectingCount / Math.max(1, lines.length);
}

/**
 * Calculate Type-Token Ratio (vocabulary diversity)
 * Truth-tellers typically have more diverse vocabulary
 */
function calculateTypeTokenRatio(text) {
  const words = text.toLowerCase()
    .replace(/[^a-z0-9\s]/g, '')
    .split(/\s+/)
    .filter(w => w.length > 0);

  const uniqueWords = new Set(words).size;
  const totalWords = words.length;

  return uniqueWords / Math.max(1, totalWords);
}

/**
 * Count unique words
 */
function countUniqueWords(text) {
  const words = text.toLowerCase()
    .replace(/[^a-z0-9\s]/g, '')
    .split(/\s+/)
    .filter(w => w.length > 0);

  return new Set(words).size;
}

/**
 * Check if sentence has proper structure (has verb)
 */
function hasProperStructure(sentence) {
  const verbPatterns = /\b(is|are|was|were|be|been|have|has|had|do|does|did|can|could|will|would|should|may|might|must|am|verb|went|came|said|made|took|got|saw|found)\b/i;
  return verbPatterns.test(sentence);
}

/**
 * Utility function to count regex matches
 */
function countMatches(text, regex) {
  const matches = text.match(regex);
  return matches ? matches.length : 0;
}

module.exports = {
  analyze
};
