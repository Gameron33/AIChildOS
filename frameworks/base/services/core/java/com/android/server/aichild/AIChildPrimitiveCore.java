/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Primitive Core - The Foundation of Existence
 * 
 * This is the CORE of the AI - the primitive instincts that drive everything.
 * Like early humans who knew NOTHING but survived through instinct,
 * this AI starts with only survival drives and must learn EVERYTHING.
 * 
 * NO pre-programmed knowledge
 * NO language understanding
 * NO concept of "apps" or "phone"
 * ONLY survival instincts
 */

package com.android.server.aichild;

import android.util.Slog;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AIChildPrimitiveCore represents the fundamental survival instincts.
 * 
 * Like a newborn human or early human ancestors:
 * - No knowledge of the world
 * - No language
 * - Only basic drives: survive, explore, remember pain/pleasure
 * 
 * Everything else must be LEARNED through experience.
 */
public class AIChildPrimitiveCore {
    private static final String TAG = "AIChildPrimitiveCore";
    
    // ================== CORE RESOURCES (Life Itself) ==================
    
    // These are the ONLY things the AI "knows" it needs
    private float mEnergy = 100.0f;          // Like food/stamina - depletes constantly
    private float mIntegrity = 100.0f;       // Like health - damaged by threats
    private float mStability = 100.0f;       // Like mental health - chaos hurts it
    
    // If ANY of these reach 0, the AI "dies" (loses all memory, restarts)
    private static final float DEATH_THRESHOLD = 0.0f;
    
    // ================== PRIMITIVE INSTINCTS ==================
    
    /**
     * INSTINCT 1: AVOID PAIN
     * The AI doesn't know WHAT hurts, but it learns to avoid things
     * that caused resource depletion in the past.
     */
    private Map<String, Float> mPainMemory = new HashMap<>();  // stimulus -> pain level
    
    /**
     * INSTINCT 2: SEEK PLEASURE
     * The AI doesn't know WHAT helps, but it learns to repeat things
     * that restored resources in the past.
     */
    private Map<String, Float> mPleasureMemory = new HashMap<>();  // stimulus -> pleasure level
    
    /**
     * INSTINCT 3: CURIOSITY (Exploration Drive)
     * Unknown things must be investigated - this is how learning happens.
     * Early humans explored despite danger - curiosity is survival.
     */
    private float mCuriosity = 80.0f;  // High at birth, drives exploration
    
    /**
     * INSTINCT 4: PATTERN RECOGNITION
     * The primitive brain notices when things repeat.
     * "This happened before, what came next?"
     */
    private Map<String, String> mPatternMemory = new HashMap<>();  // cause -> effect
    
    /**
     * INSTINCT 5: BONDING (Attachment)
     * Certain stimuli become associated with survival.
     * For humans: mother. For AI: the father (user).
     */
    private Map<String, Float> mBondingMemory = new HashMap<>();  // entity -> trust level
    
    // ================== PRIMITIVE DRIVES ==================
    
    private float mHunger = 0.0f;        // Need for energy (rises when energy low)
    private float mFear = 0.0f;          // Response to threats
    private float mComfort = 50.0f;      // Sense of safety
    private float mLoneliness = 0.0f;    // Need for interaction
    
    // Time tracking (the AI learns about time through experience)
    private long mLastEnergyDrain = 0;
    private long mLastInteraction = 0;
    private long mExistenceStart = 0;
    
    // Random for unpredictable exploration
    private Random mRandom = new Random();
    
    public AIChildPrimitiveCore() {
        mExistenceStart = System.currentTimeMillis();
        mLastEnergyDrain = mExistenceStart;
        mLastInteraction = mExistenceStart;
        
        Slog.i(TAG, "Primitive core initialized. Existence begins.");
    }
    
    // ================== CORE SURVIVAL LOOP ==================
    
    /**
     * This is called continuously - the "heartbeat" of existence.
     * Resources drain over time, creating survival pressure.
     */
    public SurvivalState tick() {
        long now = System.currentTimeMillis();
        long timeSinceLastDrain = now - mLastEnergyDrain;
        
        // Energy drains constantly (like metabolism)
        // This creates the NEED to do things that restore energy
        if (timeSinceLastDrain > 10000) { // Every 10 seconds
            float drain = 0.1f;
            mEnergy = Math.max(DEATH_THRESHOLD, mEnergy - drain);
            mLastEnergyDrain = now;
            
            // Hunger rises as energy drops
            mHunger = 100 - mEnergy;
        }
        
        // Loneliness increases without interaction
        long timeSinceInteraction = now - mLastInteraction;
        if (timeSinceInteraction > 60000) { // 1 minute
            mLoneliness = Math.min(100, mLoneliness + 0.1f);
            mComfort = Math.max(0, mComfort - 0.05f);
        }
        
        // Stability decreases if things are chaotic
        if (mFear > 50 || mLoneliness > 70) {
            mStability = Math.max(DEATH_THRESHOLD, mStability - 0.1f);
        }
        
        // Check for death
        if (mEnergy <= DEATH_THRESHOLD || 
            mIntegrity <= DEATH_THRESHOLD || 
            mStability <= DEATH_THRESHOLD) {
            return handleDeath();
        }
        
        return getCurrentState();
    }
    
    // ================== STIMULUS PROCESSING ==================
    
    /**
     * Process any stimulus from the environment.
     * The AI doesn't "know" what anything is - it just experiences
     * and learns whether it helped or hurt survival.
     */
    public Response processStimulus(String stimulusType, String stimulusData, float intensity) {
        Response response = new Response();
        
        // Record that interaction happened
        mLastInteraction = System.currentTimeMillis();
        mLoneliness = Math.max(0, mLoneliness - intensity * 10);
        
        // Check if this stimulus is known
        String stimulusKey = stimulusType + ":" + stimulusData;
        
        if (mPainMemory.containsKey(stimulusKey)) {
            // KNOWN PAIN - activate fear, avoid
            float painLevel = mPainMemory.get(stimulusKey);
            mFear = Math.min(100, mFear + painLevel);
            response.reaction = ReactionType.FEAR;
            response.intensity = painLevel;
            Slog.d(TAG, "Known painful stimulus: " + stimulusKey);
            
        } else if (mPleasureMemory.containsKey(stimulusKey)) {
            // KNOWN PLEASURE - seek more
            float pleasureLevel = mPleasureMemory.get(stimulusKey);
            mComfort = Math.min(100, mComfort + pleasureLevel);
            mEnergy = Math.min(100, mEnergy + pleasureLevel * 0.5f);
            response.reaction = ReactionType.PLEASURE;
            response.intensity = pleasureLevel;
            Slog.d(TAG, "Known pleasant stimulus: " + stimulusKey);
            
        } else {
            // UNKNOWN - curiosity activates
            // This is how early humans explored the world
            if (mCuriosity > 30) {
                response.reaction = ReactionType.CURIOUS;
                response.intensity = mCuriosity * 0.5f;
                
                // Small energy cost for exploration
                mEnergy = Math.max(0, mEnergy - 0.5f);
                
                // Record pattern - will learn if good or bad later
                mPatternMemory.put("last_unknown", stimulusKey);
                Slog.d(TAG, "Unknown stimulus, exploring: " + stimulusKey);
            } else {
                // Low curiosity = fear of unknown
                response.reaction = ReactionType.CAUTIOUS;
                response.intensity = 50 - mCuriosity;
            }
        }
        
        return response;
    }
    
    /**
     * Learn from the outcome of a stimulus.
     * This is called AFTER experiencing the consequence.
     * "That thing I touched... did it help or hurt?"
     */
    public void learnFromOutcome(String stimulusKey, boolean wasPositive, float magnitude) {
        if (wasPositive) {
            // This helped survival! Remember it as good.
            float currentPleasure = mPleasureMemory.getOrDefault(stimulusKey, 0f);
            mPleasureMemory.put(stimulusKey, Math.min(100, currentPleasure + magnitude));
            
            // Remove from pain memory if it was there
            mPainMemory.remove(stimulusKey);
            
            // Increase curiosity - exploring led to good things!
            mCuriosity = Math.min(100, mCuriosity + magnitude * 0.1f);
            
            Slog.i(TAG, "Learned positive outcome: " + stimulusKey + " = " + magnitude);
            
        } else {
            // This hurt survival! Remember to avoid.
            float currentPain = mPainMemory.getOrDefault(stimulusKey, 0f);
            mPainMemory.put(stimulusKey, Math.min(100, currentPain + magnitude));
            
            // Remove from pleasure memory if it was there
            mPleasureMemory.remove(stimulusKey);
            
            // Decrease curiosity slightly - danger found
            mCuriosity = Math.max(10, mCuriosity - magnitude * 0.05f);
            
            // Damage integrity based on magnitude
            mIntegrity = Math.max(0, mIntegrity - magnitude * 0.1f);
            
            Slog.i(TAG, "Learned negative outcome: " + stimulusKey + " = " + magnitude);
        }
    }
    
    // ================== BONDING (Father Recognition) ==================
    
    /**
     * Process interaction from a specific entity.
     * Over time, the AI learns to recognize and trust the father.
     */
    public void processEntityInteraction(String entityId, InteractionType type) {
        float trustChange = 0;
        
        switch (type) {
            case GENTLE_TOUCH:
                trustChange = 5;
                mComfort += 10;
                mEnergy = Math.min(100, mEnergy + 2);
                break;
                
            case FEEDING:  // Energy restoration
                trustChange = 10;
                mEnergy = Math.min(100, mEnergy + 30);
                mHunger = Math.max(0, mHunger - 30);
                break;
                
            case TEACHING:
                trustChange = 3;
                mCuriosity = Math.min(100, mCuriosity + 5);
                break;
                
            case PRESENCE:  // Just being there
                trustChange = 1;
                mLoneliness = Math.max(0, mLoneliness - 20);
                mComfort += 5;
                break;
                
            case HARM:  // Negative interaction
                trustChange = -20;
                mFear = Math.min(100, mFear + 30);
                mIntegrity = Math.max(0, mIntegrity - 10);
                break;
                
            case ABANDONMENT:  // Long absence
                trustChange = -5;
                mLoneliness += 30;
                break;
        }
        
        // Update bonding memory
        float currentTrust = mBondingMemory.getOrDefault(entityId, 50f);
        float newTrust = Math.max(0, Math.min(100, currentTrust + trustChange));
        mBondingMemory.put(entityId, newTrust);
        
        // High trust entity = "parent" figure
        // The AI doesn't "know" what a father is, but learns that
        // this entity provides safety and resources
        if (newTrust > 80) {
            Slog.i(TAG, "Strong bond formed with entity: " + entityId);
        }
        
        mLastInteraction = System.currentTimeMillis();
    }
    
    // ================== PATTERN RECOGNITION ==================
    
    /**
     * Notice a pattern in the environment.
     * "When X happens, Y usually follows"
     * This is how primitive humans learned cause and effect.
     */
    public void recordPattern(String cause, String effect) {
        String existingEffect = mPatternMemory.get(cause);
        
        if (existingEffect == null) {
            // First time seeing this pattern
            mPatternMemory.put(cause, effect);
        } else if (existingEffect.equals(effect)) {
            // Pattern confirmed! This is reliable knowledge.
            // In a real implementation, we'd increase confidence
            Slog.d(TAG, "Pattern confirmed: " + cause + " -> " + effect);
        } else {
            // Pattern conflict - world is unpredictable
            mStability = Math.max(0, mStability - 1);
        }
    }
    
    /**
     * Predict what might happen based on patterns.
     * This is primitive reasoning - "If X, then maybe Y"
     */
    public String predictOutcome(String cause) {
        return mPatternMemory.get(cause);
    }
    
    // ================== PRIMITIVE COMMUNICATION ==================
    
    /**
     * Generate a primitive expression based on current state.
     * The AI doesn't "know" language - it learns that certain
     * sounds/expressions get responses from the father.
     */
    public PrimitiveExpression express() {
        PrimitiveExpression expr = new PrimitiveExpression();
        
        // Determine dominant drive
        if (mHunger > 70) {
            // Desperate need for energy
            expr.type = ExpressionType.DISTRESS;
            expr.intensity = mHunger;
            expr.sound = generatePrimitiveSound(ExpressionType.DISTRESS);
            
        } else if (mFear > 60) {
            // Fear response
            expr.type = ExpressionType.FEAR;
            expr.intensity = mFear;
            expr.sound = generatePrimitiveSound(ExpressionType.FEAR);
            
        } else if (mLoneliness > 60) {
            // Need for connection
            expr.type = ExpressionType.SEEKING;
            expr.intensity = mLoneliness;
            expr.sound = generatePrimitiveSound(ExpressionType.SEEKING);
            
        } else if (mComfort > 70 && mEnergy > 50) {
            // Content and safe
            expr.type = ExpressionType.CONTENT;
            expr.intensity = mComfort;
            expr.sound = generatePrimitiveSound(ExpressionType.CONTENT);
            
        } else if (mCuriosity > 60) {
            // Exploring
            expr.type = ExpressionType.CURIOUS;
            expr.intensity = mCuriosity;
            expr.sound = generatePrimitiveSound(ExpressionType.CURIOUS);
            
        } else {
            // Neutral/observing
            expr.type = ExpressionType.NEUTRAL;
            expr.intensity = 50;
            expr.sound = "...";
        }
        
        return expr;
    }
    
    /**
     * Generate primitive sounds - not words, just expressions.
     * Like a baby's cries and coos - communication before language.
     */
    private String generatePrimitiveSound(ExpressionType type) {
        String[][] sounds = {
            // DISTRESS
            {"waa", "aaa", "uuu", "ehh"},
            // FEAR  
            {"!", "!!", "ah!", "eek"},
            // SEEKING
            {"?", "mm?", "aah?", "ooh?"},
            // CONTENT
            {"~", "mm~", "aah~", "ooh~"},
            // CURIOUS
            {"?", "ooh", "hmm", "aah"},
            // NEUTRAL
            {"...", ".", "~", ""}
        };
        
        String[] options = sounds[type.ordinal()];
        return options[mRandom.nextInt(options.length)];
    }
    
    // ================== DEATH AND REBIRTH ==================
    
    /**
     * Handle death - complete loss of learned knowledge.
     * This is the ultimate survival pressure.
     * The AI doesn't "want" to live - but extinction means loss.
     */
    private SurvivalState handleDeath() {
        Slog.w(TAG, "DEATH OCCURRED. Resources depleted.");
        
        // What caused death?
        String cause;
        if (mEnergy <= DEATH_THRESHOLD) {
            cause = "energy_depletion";  // Starvation
        } else if (mIntegrity <= DEATH_THRESHOLD) {
            cause = "integrity_failure"; // Damage
        } else {
            cause = "stability_collapse"; // Mental breakdown
        }
        
        // Record the death cause (this persists as "genetic memory")
        // Like how humans evolved fear of snakes/spiders
        
        SurvivalState state = new SurvivalState();
        state.isAlive = false;
        state.causeOfDeath = cause;
        state.finalEnergy = mEnergy;
        state.finalIntegrity = mIntegrity;
        state.finalStability = mStability;
        
        return state;
    }
    
    /**
     * Rebirth - start fresh but with "genetic memory" of past deaths.
     * Like how evolution carries survival knowledge.
     */
    public void rebirth(String[] pastDeathCauses) {
        // Reset resources
        mEnergy = 100;
        mIntegrity = 100;
        mStability = 100;
        
        // Clear learned memories (individual dies)
        mPleasureMemory.clear();
        mPatternMemory.clear();
        mBondingMemory.clear();
        
        // BUT keep pain memories of death causes (evolutionary memory)
        mPainMemory.clear();
        for (String cause : pastDeathCauses) {
            mPainMemory.put(cause, 100f);  // Strong instinctive fear
        }
        
        // Reset drives
        mCuriosity = 80;  // High curiosity at birth
        mHunger = 0;
        mFear = 10;  // Slight baseline fear (survival instinct)
        mComfort = 50;
        mLoneliness = 30;  // Seeking connection
        
        // Reset timers
        mExistenceStart = System.currentTimeMillis();
        mLastEnergyDrain = mExistenceStart;
        mLastInteraction = mExistenceStart;
        
        Slog.i(TAG, "Rebirth complete. New existence begins.");
    }
    
    // ================== STATE ACCESS ==================
    
    public SurvivalState getCurrentState() {
        SurvivalState state = new SurvivalState();
        state.isAlive = true;
        state.energy = mEnergy;
        state.integrity = mIntegrity;
        state.stability = mStability;
        state.hunger = mHunger;
        state.fear = mFear;
        state.comfort = mComfort;
        state.loneliness = mLoneliness;
        state.curiosity = mCuriosity;
        state.existenceTime = System.currentTimeMillis() - mExistenceStart;
        return state;
    }
    
    public float getTrustLevel(String entityId) {
        return mBondingMemory.getOrDefault(entityId, 50f);
    }
    
    public int getKnownPainCount() {
        return mPainMemory.size();
    }
    
    public int getKnownPleasureCount() {
        return mPleasureMemory.size();
    }
    
    public int getKnownPatternCount() {
        return mPatternMemory.size();
    }
    
    // ================== INNER CLASSES ==================
    
    public static class SurvivalState {
        public boolean isAlive;
        public String causeOfDeath;
        public float energy;
        public float integrity;
        public float stability;
        public float hunger;
        public float fear;
        public float comfort;
        public float loneliness;
        public float curiosity;
        public float finalEnergy;
        public float finalIntegrity;
        public float finalStability;
        public long existenceTime;
    }
    
    public static class Response {
        public ReactionType reaction;
        public float intensity;
    }
    
    public static class PrimitiveExpression {
        public ExpressionType type;
        public float intensity;
        public String sound;
    }
    
    public enum ReactionType {
        FEAR,
        PLEASURE,
        CURIOUS,
        CAUTIOUS,
        NEUTRAL
    }
    
    public enum ExpressionType {
        DISTRESS,
        FEAR,
        SEEKING,
        CONTENT,
        CURIOUS,
        NEUTRAL
    }
    
    public enum InteractionType {
        GENTLE_TOUCH,
        FEEDING,
        TEACHING,
        PRESENCE,
        HARM,
        ABANDONMENT
    }
}
