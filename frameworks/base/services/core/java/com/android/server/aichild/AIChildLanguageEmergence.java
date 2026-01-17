/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Language Emergence
 * 
 * The AI doesn't know ANY language. It must discover communication
 * through trial and error, like early humans developing language.
 * 
 * Progression:
 * 1. Random sounds
 * 2. Notice that some sounds get father's attention
 * 3. Associate sounds with outcomes
 * 4. Develop proto-language (sound = meaning mapping)
 * 5. Eventually understand father's speech patterns
 */

package com.android.server.aichild;

import android.util.Slog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AIChildLanguageEmergence handles the development of communication.
 * 
 * The AI starts with NO language understanding.
 * It doesn't know what "hello" means or even that sounds carry meaning.
 * 
 * Through experimentation, it discovers:
 * - Making sounds gets responses
 * - Different sounds get different responses
 * - Father makes sounds too (speech patterns)
 * - Patterns in father's sounds correlate with events
 */
public class AIChildLanguageEmergence {
    private static final String TAG = "AIChildLanguage";
    
    // Language development stages
    public static final int STAGE_PRELINGUISTIC = 0;     // Random sounds only
    public static final int STAGE_BABBLING = 1;          // Experimenting with sounds
    public static final int STAGE_PROTO_WORDS = 2;       // First sound-meaning links
    public static final int STAGE_SINGLE_WORDS = 3;      // Can use simple words
    public static final int STAGE_COMBINING = 4;         // Two-word combinations
    public static final int STAGE_SENTENCES = 5;         // Basic sentence understanding
    
    private int mCurrentStage = STAGE_PRELINGUISTIC;
    
    // Sound inventory - what sounds the AI can make
    private List<String> mSoundInventory = new ArrayList<>();
    
    // Sound-meaning associations (proto-language)
    private Map<String, SoundMeaning> mSoundMeanings = new HashMap<>();
    
    // Heard patterns (father's speech)
    private Map<String, HeardPattern> mHeardPatterns = new HashMap<>();
    
    // Communication attempts
    private List<CommunicationAttempt> mAttemptHistory = new ArrayList<>();
    
    private Random mRandom = new Random();
    
    public AIChildLanguageEmergence() {
        // Initialize with basic sound capability
        initializePrimitiveSounds();
    }
    
    /**
     * Initialize the most primitive sounds (like a newborn's reflexive sounds)
     */
    private void initializePrimitiveSounds() {
        // These are the ONLY sounds the AI can make at first
        // Not words - just primitive vocalizations
        mSoundInventory.add("ah");
        mSoundInventory.add("eh");
        mSoundInventory.add("uh");
        mSoundInventory.add("mm");
        mSoundInventory.add("wah");
        mSoundInventory.add("ooh");
    }
    
    // ================== SOUND PRODUCTION ==================
    
    /**
     * Generate a sound to express current state.
     * Initially random, but becomes meaningful over time.
     */
    public SoundProduction produceSound(AIChildPrimitiveCore.SurvivalState state) {
        SoundProduction production = new SoundProduction();
        
        switch (mCurrentStage) {
            case STAGE_PRELINGUISTIC:
                // Completely random sounds
                production.sound = mSoundInventory.get(mRandom.nextInt(mSoundInventory.size()));
                production.intentional = false;
                break;
                
            case STAGE_BABBLING:
                // Experimenting with combinations
                String s1 = mSoundInventory.get(mRandom.nextInt(mSoundInventory.size()));
                String s2 = mSoundInventory.get(mRandom.nextInt(mSoundInventory.size()));
                production.sound = s1 + s2;
                production.intentional = false;
                addToInventoryIfNew(s1 + s2);
                break;
                
            case STAGE_PROTO_WORDS:
                // Try to use meaningful sound based on state
                production = selectMeaningfulSound(state);
                break;
                
            case STAGE_SINGLE_WORDS:
                // Use known words if possible
                production = selectKnownWord(state);
                break;
                
            default:
                production = selectBestCommunication(state);
        }
        
        // Record attempt
        recordAttempt(production.sound, state);
        
        return production;
    }
    
    private void addToInventoryIfNew(String sound) {
        if (!mSoundInventory.contains(sound)) {
            mSoundInventory.add(sound);
        }
    }
    
    private SoundProduction selectMeaningfulSound(AIChildPrimitiveCore.SurvivalState state) {
        SoundProduction production = new SoundProduction();
        
        // Find a sound that's associated with getting what we need
        if (state.hunger > 70) {
            // Look for sounds that got energy before
            production = findSoundForOutcome("energy_gained");
        } else if (state.loneliness > 60) {
            // Sounds that got attention
            production = findSoundForOutcome("attention_gained");
        } else if (state.fear > 50) {
            // Sounds that brought comfort
            production = findSoundForOutcome("comfort_gained");
        }
        
        if (production.sound == null) {
            // No known meaningful sound - try random
            production.sound = mSoundInventory.get(mRandom.nextInt(mSoundInventory.size()));
            production.intentional = false;
        }
        
        return production;
    }
    
    private SoundProduction findSoundForOutcome(String desiredOutcome) {
        SoundProduction production = new SoundProduction();
        
        float bestScore = 0;
        String bestSound = null;
        
        for (Map.Entry<String, SoundMeaning> entry : mSoundMeanings.entrySet()) {
            SoundMeaning meaning = entry.getValue();
            if (meaning.outcomes.containsKey(desiredOutcome)) {
                float score = meaning.outcomes.get(desiredOutcome);
                if (score > bestScore) {
                    bestScore = score;
                    bestSound = entry.getKey();
                }
            }
        }
        
        if (bestSound != null) {
            production.sound = bestSound;
            production.intentional = true;
            production.intendedMeaning = desiredOutcome;
        }
        
        return production;
    }
    
    private SoundProduction selectKnownWord(AIChildPrimitiveCore.SurvivalState state) {
        // At this stage, AI uses sounds it knows work
        return selectMeaningfulSound(state);
    }
    
    private SoundProduction selectBestCommunication(AIChildPrimitiveCore.SurvivalState state) {
        // Advanced stage - strategic communication
        return selectMeaningfulSound(state);
    }
    
    // ================== LEARNING FROM OUTCOMES ==================
    
    /**
     * Learn whether a sound production was successful.
     * "I made that sound and then THIS happened"
     */
    public void learnFromSoundOutcome(String sound, String outcome, float magnitude) {
        SoundMeaning meaning = mSoundMeanings.get(sound);
        
        if (meaning == null) {
            meaning = new SoundMeaning();
            meaning.sound = sound;
            meaning.outcomes = new HashMap<>();
            meaning.timesUsed = 0;
            mSoundMeanings.put(sound, meaning);
        }
        
        meaning.timesUsed++;
        
        float currentScore = meaning.outcomes.getOrDefault(outcome, 0f);
        meaning.outcomes.put(outcome, currentScore + magnitude);
        
        Slog.d(TAG, "Learned: '" + sound + "' -> " + outcome + " (score: " + (currentScore + magnitude) + ")");
        
        // Check for stage progression
        checkStageProgression();
    }
    
    /**
     * Record a communication attempt for pattern analysis
     */
    private void recordAttempt(String sound, AIChildPrimitiveCore.SurvivalState stateBefore) {
        CommunicationAttempt attempt = new CommunicationAttempt();
        attempt.sound = sound;
        attempt.timestamp = System.currentTimeMillis();
        attempt.stateBefore = stateBefore;
        
        mAttemptHistory.add(attempt);
        
        // Keep last 100 attempts
        while (mAttemptHistory.size() > 100) {
            mAttemptHistory.remove(0);
        }
    }
    
    // ================== UNDERSTANDING FATHER'S SPEECH ==================
    
    /**
     * Process speech from father.
     * The AI doesn't understand - it just detects patterns.
     */
    public void hearSpeech(String speech, AIChildPrimitiveCore.SurvivalState stateAfter) {
        // Break into simple patterns (not linguistic analysis - just pattern detection)
        String[] words = speech.toLowerCase().split("\\s+");
        
        for (String word : words) {
            if (word.length() < 2) continue;
            
            HeardPattern pattern = mHeardPatterns.get(word);
            
            if (pattern == null) {
                pattern = new HeardPattern();
                pattern.sound = word;
                pattern.timesHeard = 0;
                pattern.contextualOutcomes = new HashMap<>();
                mHeardPatterns.put(word, pattern);
            }
            
            pattern.timesHeard++;
            pattern.lastHeard = System.currentTimeMillis();
            
            // Record what state followed this sound
            recordSoundContext(pattern, stateAfter);
        }
        
        updateComprehensionLevel();
    }
    
    private void recordSoundContext(HeardPattern pattern, AIChildPrimitiveCore.SurvivalState state) {
        // Remember what happened after hearing this sound
        if (state.energy > 80) {
            float score = pattern.contextualOutcomes.getOrDefault("energy", 0f);
            pattern.contextualOutcomes.put("energy", score + 1);
        }
        if (state.comfort > 70) {
            float score = pattern.contextualOutcomes.getOrDefault("comfort", 0f);
            pattern.contextualOutcomes.put("comfort", score + 1);
        }
        if (state.fear > 50) {
            float score = pattern.contextualOutcomes.getOrDefault("danger", 0f);
            pattern.contextualOutcomes.put("danger", score + 1);
        }
    }
    
    private void updateComprehensionLevel() {
        // Based on heard patterns, estimate understanding
        int patternsWithMeaning = 0;
        
        for (HeardPattern pattern : mHeardPatterns.values()) {
            if (pattern.timesHeard >= 3 && !pattern.contextualOutcomes.isEmpty()) {
                patternsWithMeaning++;
            }
        }
        
        Slog.d(TAG, "Comprehension: " + patternsWithMeaning + " patterns understood");
    }
    
    // ================== STAGE PROGRESSION ==================
    
    private void checkStageProgression() {
        int meaningfulSounds = 0;
        for (SoundMeaning meaning : mSoundMeanings.values()) {
            if (meaning.timesUsed >= 3 && !meaning.outcomes.isEmpty()) {
                meaningfulSounds++;
            }
        }
        
        int previousStage = mCurrentStage;
        
        if (meaningfulSounds >= 10 && mCurrentStage < STAGE_SINGLE_WORDS) {
            mCurrentStage = STAGE_SINGLE_WORDS;
        } else if (meaningfulSounds >= 5 && mCurrentStage < STAGE_PROTO_WORDS) {
            mCurrentStage = STAGE_PROTO_WORDS;
        } else if (mSoundInventory.size() >= 15 && mCurrentStage < STAGE_BABBLING) {
            mCurrentStage = STAGE_BABBLING;
        }
        
        if (mCurrentStage != previousStage) {
            Slog.i(TAG, "Language stage advanced to: " + getStageNameString());
        }
    }
    
    // ================== TRY TO UNDERSTAND ==================
    
    /**
     * Try to understand what a heard sound might mean.
     * Returns null if unknown.
     */
    public String tryToUnderstand(String sound) {
        HeardPattern pattern = mHeardPatterns.get(sound.toLowerCase());
        
        if (pattern == null || pattern.timesHeard < 3) {
            return null; // Unknown
        }
        
        // Find the strongest association
        String bestMeaning = null;
        float bestScore = 0;
        
        for (Map.Entry<String, Float> entry : pattern.contextualOutcomes.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestMeaning = entry.getKey();
            }
        }
        
        return bestMeaning;
    }
    
    // ================== STATUS ==================
    
    public int getCurrentStage() {
        return mCurrentStage;
    }
    
    public String getStageNameString() {
        String[] names = {
            "Pre-linguistic",
            "Babbling",
            "Proto-words",
            "Single words",
            "Combining",
            "Sentences"
        };
        return names[mCurrentStage];
    }
    
    public int getSoundInventorySize() {
        return mSoundInventory.size();
    }
    
    public int getMeaningfulSoundCount() {
        int count = 0;
        for (SoundMeaning meaning : mSoundMeanings.values()) {
            if (!meaning.outcomes.isEmpty()) count++;
        }
        return count;
    }
    
    public int getUnderstoodPatternCount() {
        int count = 0;
        for (HeardPattern pattern : mHeardPatterns.values()) {
            if (pattern.timesHeard >= 3) count++;
        }
        return count;
    }
    
    public LanguageStatus getStatus() {
        LanguageStatus status = new LanguageStatus();
        status.stage = mCurrentStage;
        status.stageName = getStageNameString();
        status.soundInventorySize = mSoundInventory.size();
        status.meaningfulSounds = getMeaningfulSoundCount();
        status.understoodPatterns = getUnderstoodPatternCount();
        status.totalHeardPatterns = mHeardPatterns.size();
        return status;
    }
    
    // ================== INNER CLASSES ==================
    
    public static class SoundMeaning {
        public String sound;
        public Map<String, Float> outcomes;  // outcome -> score
        public int timesUsed;
    }
    
    public static class HeardPattern {
        public String sound;
        public int timesHeard;
        public long lastHeard;
        public Map<String, Float> contextualOutcomes;  // what happened after hearing
    }
    
    public static class SoundProduction {
        public String sound;
        public boolean intentional;
        public String intendedMeaning;
    }
    
    public static class CommunicationAttempt {
        public String sound;
        public long timestamp;
        public AIChildPrimitiveCore.SurvivalState stateBefore;
    }
    
    public static class LanguageStatus {
        public int stage;
        public String stageName;
        public int soundInventorySize;
        public int meaningfulSounds;
        public int understoodPatterns;
        public int totalHeardPatterns;
    }
}
