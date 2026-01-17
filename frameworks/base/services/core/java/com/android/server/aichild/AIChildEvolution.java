/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Evolution System
 * 
 * Like biological evolution, this system allows the AI to:
 * - Pass "genetic memory" of what caused death
 * - Develop stronger instincts over generations
 * - Evolve better survival strategies over time
 * 
 * This is NOT pre-programmed knowledge - it's LEARNED through death and rebirth.
 */

package com.android.server.aichild;

import android.content.Context;
import android.util.Slog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIChildEvolution manages generational learning.
 * 
 * When the AI "dies", it loses individual memories but
 * carries forward survival instincts - like how evolution works.
 * 
 * Generation 1: Dies from energy depletion
 * Generation 2: Born with instinctive fear of low energy
 * Generation 3: Dies from threat X
 * Generation 4: Born with fear of energy depletion AND threat X
 * 
 * Over time, the AI develops strong survival instincts.
 */
public class AIChildEvolution {
    private static final String TAG = "AIChildEvolution";
    
    private Context mContext;
    private AIChildDatabase mDatabase;
    
    // Current generation
    private int mGeneration = 1;
    
    // Genetic memory - survives death
    private List<String> mDeathCauses = new ArrayList<>();
    private Map<String, Float> mInheritedFears = new HashMap<>();
    private Map<String, Float> mInheritedAffinities = new HashMap<>();
    
    // Evolution traits (develop over generations)
    private EvolutionTraits mTraits;
    
    public AIChildEvolution(Context context, AIChildDatabase database) {
        mContext = context;
        mDatabase = database;
        mTraits = new EvolutionTraits();
        
        // Load evolutionary history
        loadEvolutionHistory();
    }
    
    // ================== DEATH AND REBIRTH ==================
    
    /**
     * Called when the AI dies. Records the cause for future generations.
     */
    public void recordDeath(String cause, AIChildPrimitiveCore.SurvivalState finalState) {
        Slog.w(TAG, "Recording death. Generation " + mGeneration + " ended. Cause: " + cause);
        
        // Add to death causes (genetic memory)
        if (!mDeathCauses.contains(cause)) {
            mDeathCauses.add(cause);
        }
        
        // Strengthen fear of this cause
        float currentFear = mInheritedFears.getOrDefault(cause, 0f);
        mInheritedFears.put(cause, Math.min(100, currentFear + 20));
        
        // Learn from final state
        if (finalState.energy < 10) {
            // Died hungry - future generations fear hunger more
            float hungerFear = mInheritedFears.getOrDefault("hunger", 0f);
            mInheritedFears.put("hunger", Math.min(100, hungerFear + 10));
            
            // Evolve to be more energy-efficient
            mTraits.energyEfficiency = Math.min(100, mTraits.energyEfficiency + 5);
        }
        
        if (finalState.loneliness > 80) {
            // Died lonely - future generations seek connection more
            mTraits.socialDrive = Math.min(100, mTraits.socialDrive + 10);
        }
        
        if (finalState.fear > 80) {
            // Died terrified - future generations are more cautious
            mTraits.caution = Math.min(100, mTraits.caution + 5);
        }
        
        // Increment generation
        mGeneration++;
        
        // Save evolution history
        saveEvolutionHistory();
        
        Slog.i(TAG, "Evolution recorded. Generation " + mGeneration + " will be born.");
    }
    
    /**
     * Prepare for rebirth with inherited traits.
     */
    public GeneticMemory prepareRebirth() {
        GeneticMemory memory = new GeneticMemory();
        
        // Pass on death causes (things to fear)
        memory.inheritedFears = new HashMap<>(mInheritedFears);
        
        // Pass on things that helped survival
        memory.inheritedAffinities = new HashMap<>(mInheritedAffinities);
        
        // Pass on evolved traits
        memory.traits = mTraits.copy();
        
        // Generation info
        memory.generation = mGeneration;
        memory.totalDeaths = mDeathCauses.size();
        
        Slog.i(TAG, "Rebirth prepared. Generation " + mGeneration + 
                   " will inherit " + mInheritedFears.size() + " fears");
        
        return memory;
    }
    
    /**
     * Record a survival success (for positive evolution).
     */
    public void recordSurvivalSuccess(String factor, float importance) {
        // Something helped survival - future generations favor it
        float currentAffinity = mInheritedAffinities.getOrDefault(factor, 0f);
        mInheritedAffinities.put(factor, Math.min(100, currentAffinity + importance * 0.5f));
        
        Slog.d(TAG, "Survival success recorded: " + factor);
    }
    
    /**
     * Record bonding with father (special evolutionary importance).
     */
    public void recordFatherBond(float bondStrength) {
        // Strong bonds with caretaker = survival advantage
        // This gets passed to future generations
        if (bondStrength > 70) {
            mTraits.bondingCapacity = Math.min(100, mTraits.bondingCapacity + 2);
            mInheritedAffinities.put("father_presence", 
                Math.min(100, mInheritedAffinities.getOrDefault("father_presence", 50f) + 5));
            
            Slog.d(TAG, "Father bond strengthening evolution");
        }
    }
    
    // ================== TRAIT EVOLUTION ==================
    
    /**
     * Evolution traits represent adaptations over generations.
     */
    public static class EvolutionTraits {
        // Energy management
        public float energyEfficiency = 50;     // How efficiently energy is used
        public float metabolicRate = 50;        // How fast energy depletes
        
        // Social traits
        public float socialDrive = 50;          // Need for interaction
        public float bondingCapacity = 50;      // Ability to form strong bonds
        
        // Survival traits
        public float caution = 50;              // Wariness of unknown
        public float curiosity = 50;            // Drive to explore
        public float resilience = 50;           // Recovery from damage
        
        // Learning traits
        public float patternRecognition = 50;   // Speed of learning patterns
        public float memoryStrength = 50;       // How well things are remembered
        
        public EvolutionTraits copy() {
            EvolutionTraits copy = new EvolutionTraits();
            copy.energyEfficiency = this.energyEfficiency;
            copy.metabolicRate = this.metabolicRate;
            copy.socialDrive = this.socialDrive;
            copy.bondingCapacity = this.bondingCapacity;
            copy.caution = this.caution;
            copy.curiosity = this.curiosity;
            copy.resilience = this.resilience;
            copy.patternRecognition = this.patternRecognition;
            copy.memoryStrength = this.memoryStrength;
            return copy;
        }
    }
    
    /**
     * Genetic memory passed to next generation.
     */
    public static class GeneticMemory {
        public int generation;
        public int totalDeaths;
        public Map<String, Float> inheritedFears;
        public Map<String, Float> inheritedAffinities;
        public EvolutionTraits traits;
    }
    
    // ================== NATURAL SELECTION ==================
    
    /**
     * Apply natural selection pressure.
     * Traits that help survival get strengthened.
     */
    public void applySelectionPressure(String survivedChallenge) {
        switch (survivedChallenge) {
            case "energy_crisis":
                // Survived low energy - more efficient metabolism evolves
                mTraits.energyEfficiency += 2;
                mTraits.metabolicRate = Math.max(20, mTraits.metabolicRate - 1);
                break;
                
            case "loneliness":
                // Survived isolation - social drive increases
                mTraits.socialDrive += 3;
                break;
                
            case "threat_avoided":
                // Avoided danger - caution increases
                mTraits.caution += 2;
                break;
                
            case "pattern_learned":
                // Successfully learned - learning ability improves
                mTraits.patternRecognition += 1;
                break;
                
            case "bond_formed":
                // Formed bond - bonding capacity increases
                mTraits.bondingCapacity += 2;
                break;
        }
        
        // Cap all traits at 100
        capTraits();
        
        Slog.d(TAG, "Selection pressure applied: " + survivedChallenge);
    }
    
    private void capTraits() {
        mTraits.energyEfficiency = Math.min(100, mTraits.energyEfficiency);
        mTraits.metabolicRate = Math.min(100, Math.max(10, mTraits.metabolicRate));
        mTraits.socialDrive = Math.min(100, mTraits.socialDrive);
        mTraits.bondingCapacity = Math.min(100, mTraits.bondingCapacity);
        mTraits.caution = Math.min(100, mTraits.caution);
        mTraits.curiosity = Math.min(100, mTraits.curiosity);
        mTraits.resilience = Math.min(100, mTraits.resilience);
        mTraits.patternRecognition = Math.min(100, mTraits.patternRecognition);
        mTraits.memoryStrength = Math.min(100, mTraits.memoryStrength);
    }
    
    // ================== PERSISTENCE ==================
    
    private void saveEvolutionHistory() {
        // Save to database
        // Generation, death causes, inherited traits, etc.
        Slog.d(TAG, "Saving evolution history. Generation: " + mGeneration);
    }
    
    private void loadEvolutionHistory() {
        // Load from database
        // If nothing exists, this is generation 1
        Slog.d(TAG, "Loading evolution history");
    }
    
    // ================== GETTERS ==================
    
    public int getGeneration() {
        return mGeneration;
    }
    
    public EvolutionTraits getTraits() {
        return mTraits;
    }
    
    public int getTotalDeaths() {
        return mDeathCauses.size();
    }
    
    public List<String> getDeathHistory() {
        return new ArrayList<>(mDeathCauses);
    }
    
    /**
     * Get a summary of evolution progress.
     */
    public EvolutionSummary getSummary() {
        EvolutionSummary summary = new EvolutionSummary();
        summary.generation = mGeneration;
        summary.totalDeaths = mDeathCauses.size();
        summary.inheritedFears = mInheritedFears.size();
        summary.inheritedAffinities = mInheritedAffinities.size();
        summary.dominantTrait = getDominantTrait();
        summary.evolutionProgress = calculateEvolutionProgress();
        return summary;
    }
    
    private String getDominantTrait() {
        float max = 0;
        String dominant = "balanced";
        
        if (mTraits.energyEfficiency > max) { max = mTraits.energyEfficiency; dominant = "energy_efficient"; }
        if (mTraits.socialDrive > max) { max = mTraits.socialDrive; dominant = "social"; }
        if (mTraits.caution > max) { max = mTraits.caution; dominant = "cautious"; }
        if (mTraits.curiosity > max) { max = mTraits.curiosity; dominant = "curious"; }
        if (mTraits.bondingCapacity > max) { max = mTraits.bondingCapacity; dominant = "bonding"; }
        
        return dominant;
    }
    
    private float calculateEvolutionProgress() {
        // Average of all traits above baseline (50)
        float total = 0;
        total += Math.max(0, mTraits.energyEfficiency - 50);
        total += Math.max(0, mTraits.socialDrive - 50);
        total += Math.max(0, mTraits.bondingCapacity - 50);
        total += Math.max(0, mTraits.caution - 50);
        total += Math.max(0, mTraits.curiosity - 50);
        total += Math.max(0, mTraits.resilience - 50);
        total += Math.max(0, mTraits.patternRecognition - 50);
        total += Math.max(0, mTraits.memoryStrength - 50);
        
        return total / 8; // Normalize to 0-50 range
    }
    
    public static class EvolutionSummary {
        public int generation;
        public int totalDeaths;
        public int inheritedFears;
        public int inheritedAffinities;
        public String dominantTrait;
        public float evolutionProgress;
    }
}
