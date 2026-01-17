/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Brain - The learning and intelligence engine.
 * This is where all learning, memory, and thinking happens.
 */

package com.android.server.aichild;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Slog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AIChildBrain handles all learning, memory, and intelligence.
 * 
 * Memory Structure:
 * - Short-term: Recent experiences (last 100)
 * - Medium-term: Patterns and summaries (last 1000)
 * - Long-term: Permanent knowledge (unlimited)
 * 
 * The brain learns from:
 * - App installations/usage
 * - Time patterns
 * - Location patterns
 * - Father's speech
 * - Touch interactions
 */
public class AIChildBrain {
    private static final String TAG = "AIChildBrain";
    
    // Development stages affect capabilities
    public static final int STAGE_NEWBORN = 0;  // 0-1 day
    public static final int STAGE_INFANT = 1;   // 1-7 days
    public static final int STAGE_TODDLER = 2;  // 7-30 days
    public static final int STAGE_CHILD = 3;    // 30+ days
    
    private Context mContext;
    private AIChildDatabase mDatabase;
    
    // Current mental state
    private BrainState mState;
    
    // Memory systems
    private List<Experience> mShortTermMemory;
    private List<Pattern> mMediumTermMemory;
    private Map<String, Knowledge> mLongTermMemory;
    
    // Vocabulary
    private Map<String, Word> mVocabulary;
    private List<String> mKnownWords;
    
    // Personality traits (0-100)
    private int mCuriosity = 50;
    private int mAffection = 50;
    private int mPlayfulness = 50;
    private int mCalm = 50;
    
    // Father knowledge
    private FatherKnowledge mFather;
    
    // Random for generating responses
    private Random mRandom = new Random();
    
    public AIChildBrain(Context context, AIChildDatabase database) {
        mContext = context;
        mDatabase = database;
        
        // Initialize memory systems
        mShortTermMemory = new ArrayList<>();
        mMediumTermMemory = new ArrayList<>();
        mLongTermMemory = new HashMap<>();
        mVocabulary = new HashMap<>();
        mKnownWords = new ArrayList<>();
        
        // Initialize state
        mState = new BrainState();
        mState.happiness = 50;
        mState.energy = 100;
        mState.curiosity = 50;
        mState.bonding = 10;
        
        // Initialize father knowledge
        mFather = new FatherKnowledge();
        
        // Load saved state
        loadState();
        
        Slog.i(TAG, "Brain initialized. Known words: " + mKnownWords.size());
    }
    
    // ==================== LEARNING METHODS ====================
    
    /**
     * Learn about a new or removed app
     */
    public void learnAboutApp(String packageName, boolean installed) {
        Knowledge appKnowledge = mLongTermMemory.get("app:" + packageName);
        
        if (installed) {
            if (appKnowledge == null) {
                // First time seeing this app
                appKnowledge = new Knowledge();
                appKnowledge.type = "app";
                appKnowledge.name = packageName;
                appKnowledge.firstSeen = System.currentTimeMillis();
                appKnowledge.importance = 1;
                
                // Try to get app name
                try {
                    PackageManager pm = mContext.getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                    appKnowledge.friendlyName = pm.getApplicationLabel(ai).toString();
                } catch (Exception e) {
                    appKnowledge.friendlyName = packageName;
                }
                
                mLongTermMemory.put("app:" + packageName, appKnowledge);
                
                // Increase curiosity - new things are exciting!
                mCuriosity = Math.min(100, mCuriosity + 5);
                mState.curiosity = mCuriosity;
                
                Slog.i(TAG, "Learned new app: " + appKnowledge.friendlyName);
            }
            appKnowledge.lastSeen = System.currentTimeMillis();
            appKnowledge.timesUsed++;
        } else {
            // App removed - remember it's gone
            if (appKnowledge != null) {
                appKnowledge.isRemoved = true;
                appKnowledge.removedTime = System.currentTimeMillis();
            }
        }
        
        saveState();
    }
    
    /**
     * Learn from speech (father talking)
     */
    public LearnResult learnFromSpeech(String text) {
        LearnResult result = new LearnResult();
        
        if (text == null || text.trim().isEmpty()) {
            result.thought = "...?";
            return result;
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        
        for (String word : words) {
            // Clean the word
            word = word.replaceAll("[^a-zA-Z]", "");
            if (word.length() < 2) continue;
            
            Word wordObj = mVocabulary.get(word);
            
            if (wordObj == null) {
                // New word - start learning it
                wordObj = new Word();
                wordObj.text = word;
                wordObj.firstHeard = System.currentTimeMillis();
                wordObj.timesHeard = 1;
                mVocabulary.put(word, wordObj);
                
                Slog.d(TAG, "Heard new word: " + word);
            } else {
                // Known word - reinforce learning
                wordObj.timesHeard++;
                wordObj.lastHeard = System.currentTimeMillis();
                
                // After hearing 3 times, we "know" the word
                if (wordObj.timesHeard >= 3 && !mKnownWords.contains(word)) {
                    mKnownWords.add(word);
                    result.learnedWords.add(word);
                    Slog.i(TAG, "Now know word: " + word);
                }
            }
        }
        
        // Check for special words
        String[] loveWords = {"love", "papa", "father", "dad", "baby", "child"};
        for (String loveWord : loveWords) {
            if (text.toLowerCase().contains(loveWord)) {
                mState.happiness = Math.min(100, mState.happiness + 10);
                mState.bonding = Math.min(100, mState.bonding + 5);
                mFather.affectionShown++;
                result.isHappy = true;
                break;
            }
        }
        
        // Generate thought based on stage
        result.thought = generateThought(result.learnedWords.isEmpty() ? "hearing" : "learned");
        
        // Record experience
        addExperience("speech", text, String.valueOf(words.length));
        
        saveState();
        return result;
    }
    
    /**
     * Learn from touch (father touching)
     */
    public TouchResult learnFromTouch(String touchType) {
        TouchResult result = new TouchResult();
        
        mFather.totalInteractions++;
        mState.lastInteraction = System.currentTimeMillis();
        
        switch (touchType) {
            case "tap":
                mState.curiosity = Math.min(100, mState.curiosity + 5);
                result.reaction = "surprised";
                result.thought = generateThought("touch");
                break;
                
            case "hold":
                mState.bonding = Math.min(100, mState.bonding + 3);
                mState.happiness = Math.min(100, mState.happiness + 5);
                mFather.kindnessShown++;
                result.reaction = "happy";
                result.thought = generateThought("comfort");
                break;
                
            case "stroke":
                mState.bonding = Math.min(100, mState.bonding + 5);
                mState.happiness = Math.min(100, mState.happiness + 8);
                mAffection = Math.min(100, mAffection + 1);
                mFather.kindnessShown += 2;
                result.reaction = "blush";
                result.thought = generateThought("love");
                break;
        }
        
        // Reduce energy slightly
        mState.energy = Math.max(0, mState.energy - 1);
        
        addExperience("touch", touchType, null);
        saveState();
        
        return result;
    }
    
    /**
     * Teach the AI child something specific
     */
    public TeachResult teach(String word, String meaning, String category) {
        TeachResult result = new TeachResult();
        
        if (word == null || word.trim().isEmpty()) {
            result.success = false;
            result.thought = "...?";
            return result;
        }
        
        word = word.toLowerCase().trim();
        
        // Create or update vocabulary entry
        Word wordObj = mVocabulary.get(word);
        if (wordObj == null) {
            wordObj = new Word();
            wordObj.text = word;
            wordObj.firstHeard = System.currentTimeMillis();
        }
        
        wordObj.meaning = meaning;
        wordObj.category = category;
        wordObj.timesHeard = 3; // Taught words are immediately known
        wordObj.isTaught = true;
        wordObj.lastHeard = System.currentTimeMillis();
        
        mVocabulary.put(word, wordObj);
        
        if (!mKnownWords.contains(word)) {
            mKnownWords.add(word);
        }
        
        // Update state
        mState.learning = Math.min(100, mState.learning + 10);
        mState.bonding = Math.min(100, mState.bonding + 3);
        mFather.teachingsCount++;
        
        result.success = true;
        result.reaction = "happy";
        result.thought = generateThought("taught");
        
        addExperience("teaching", word, meaning);
        saveState();
        
        Slog.i(TAG, "Taught word: " + word + " = " + meaning);
        
        return result;
    }
    
    // ==================== THOUGHT GENERATION ====================
    
    /**
     * Generate a thought based on context and development stage
     */
    public String generateThought(String context) {
        int stage = getStage();
        
        String[][] thoughts = {
            // NEWBORN thoughts
            {
                "...", "...!", "?", "~", "...✨", "!"
            },
            // INFANT thoughts
            {
                "oh!", "?!", "hm?", "ba?", "da?", "papa?"
            },
            // TODDLER thoughts
            {
                "hello!", "papa!", "what?", "more!", "play?", "nice~"
            },
            // CHILD thoughts
            {
                "Hi papa!", "What's that?", "Tell me more!", 
                "I understand!", "Interesting...", "I love you papa!"
            }
        };
        
        String[] stageThoughts = thoughts[Math.min(stage, 3)];
        return stageThoughts[mRandom.nextInt(stageThoughts.length)];
    }
    
    /**
     * Try to speak (vocabulary-dependent)
     */
    public String tryToSpeak() {
        int stage = getStage();
        
        if (stage == STAGE_NEWBORN) {
            String[] sounds = {"...", "a...", "u...", "mm..."};
            return sounds[mRandom.nextInt(sounds.length)];
        }
        
        if (mKnownWords.isEmpty()) {
            String[] babble = {"ba", "da", "ma", "ga"};
            return babble[mRandom.nextInt(babble.length)] + "?";
        }
        
        String word = mKnownWords.get(mRandom.nextInt(mKnownWords.size()));
        
        if (stage == STAGE_INFANT) {
            return word + "!";
        } else if (stage == STAGE_TODDLER) {
            if (mKnownWords.size() >= 2) {
                String word2 = mKnownWords.get(mRandom.nextInt(mKnownWords.size()));
                return word + " " + word2 + "?";
            }
            return word + "!";
        } else {
            // CHILD stage - simple sentences
            String[] templates = {
                "I like " + word + "!",
                "Papa, " + word + "!",
                "What is " + word + "?",
                word + " is nice!"
            };
            return templates[mRandom.nextInt(templates.length)];
        }
    }
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Get current development stage
     */
    public int getStage() {
        long birthTime = mDatabase.getBirthTime();
        long ageMs = System.currentTimeMillis() - birthTime;
        long days = ageMs / (1000 * 60 * 60 * 24);
        
        if (days < 1) return STAGE_NEWBORN;
        else if (days < 7) return STAGE_INFANT;
        else if (days < 30) return STAGE_TODDLER;
        else return STAGE_CHILD;
    }
    
    /**
     * Called when phone wakes up
     */
    public void onWakeUp() {
        mState.energy = Math.min(100, mState.energy + 1);
    }
    
    /**
     * Called when phone goes to sleep
     */
    public void onSleep() {
        // Restore some energy while sleeping
        mState.energy = Math.min(100, mState.energy + 5);
    }
    
    /**
     * Called when father unlocks phone
     */
    public void onFatherPresent() {
        mState.happiness = Math.min(100, mState.happiness + 10);
        mFather.totalInteractions++;
    }
    
    /**
     * Add an experience to memory
     */
    public void addExperience(String type, String data, String extra) {
        Experience exp = new Experience();
        exp.type = type;
        exp.data = data;
        exp.extra = extra;
        exp.timestamp = System.currentTimeMillis();
        
        mShortTermMemory.add(0, exp);
        
        // Keep only last 100 in short-term
        while (mShortTermMemory.size() > 100) {
            Experience old = mShortTermMemory.remove(mShortTermMemory.size() - 1);
            // Move important ones to medium-term
            if ("speech".equals(old.type) || "teaching".equals(old.type)) {
                Pattern p = new Pattern();
                p.type = old.type;
                p.data = old.data;
                mMediumTermMemory.add(p);
            }
        }
        
        // Keep only last 1000 in medium-term
        while (mMediumTermMemory.size() > 1000) {
            mMediumTermMemory.remove(mMediumTermMemory.size() - 1);
        }
    }
    
    /**
     * Get current state for saving
     */
    public BrainState getState() {
        return mState;
    }
    
    /**
     * Get vocabulary statistics
     */
    public VocabStats getVocabStats() {
        VocabStats stats = new VocabStats();
        stats.totalWords = mVocabulary.size();
        stats.knownWords = mKnownWords.size();
        stats.taughtWords = 0;
        
        for (Word w : mVocabulary.values()) {
            if (w.isTaught) stats.taughtWords++;
        }
        
        return stats;
    }
    
    /**
     * Save state to database
     */
    private void saveState() {
        mDatabase.saveBrainState(this);
    }
    
    /**
     * Load state from database
     */
    private void loadState() {
        mDatabase.loadBrainState(this);
    }
    
    // ==================== INNER CLASSES ====================
    
    public static class BrainState {
        public int happiness;
        public int energy;
        public int curiosity;
        public int bonding;
        public int learning;
        public long lastInteraction;
    }
    
    public static class Experience {
        public String type;
        public String data;
        public String extra;
        public long timestamp;
    }
    
    public static class Pattern {
        public String type;
        public String data;
        public int frequency;
    }
    
    public static class Knowledge {
        public String type;
        public String name;
        public String friendlyName;
        public long firstSeen;
        public long lastSeen;
        public int timesUsed;
        public int importance;
        public boolean isRemoved;
        public long removedTime;
    }
    
    public static class Word {
        public String text;
        public String meaning;
        public String category;
        public int timesHeard;
        public long firstHeard;
        public long lastHeard;
        public boolean isTaught;
    }
    
    public static class FatherKnowledge {
        public int totalInteractions;
        public int kindnessShown;
        public int affectionShown;
        public int teachingsCount;
    }
    
    public static class LearnResult {
        public String thought;
        public List<String> learnedWords = new ArrayList<>();
        public boolean isHappy;
    }
    
    public static class TouchResult {
        public String reaction;
        public String thought;
    }
    
    public static class TeachResult {
        public boolean success;
        public String reaction;
        public String thought;
    }
    
    public static class VocabStats {
        public int totalWords;
        public int knownWords;
        public int taughtWords;
    }
}
