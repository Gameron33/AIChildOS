/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Neural Network - Brain-like pattern recognition.
 * 
 * Like the human brain:
 * - Neurons that fire together, wire together (Hebbian Learning)
 * - Connections strengthen with use
 * - Patterns emerge from repeated exposure
 * - Weak connections decay over time
 */

package com.android.server.aichild;

import android.util.Slog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AIChildNeuralNetwork simulates a simple neural network for pattern recognition.
 * 
 * Key concepts:
 * - NEURONS: Nodes that hold activation values
 * - SYNAPSES: Connections between neurons with weights
 * - FIRING: When a neuron's activation exceeds threshold
 * - LEARNING: Strengthening connections that fire together
 * - DECAY: Weakening unused connections
 */
public class AIChildNeuralNetwork {
    private static final String TAG = "AIChildNeural";
    
    // Network parameters
    private static final float INITIAL_WEIGHT = 0.1f;
    private static final float LEARNING_RATE = 0.1f;
    private static final float DECAY_RATE = 0.01f;
    private static final float FIRING_THRESHOLD = 0.5f;
    private static final int MAX_NEURONS = 10000;
    
    // Neurons (named by what they represent)
    private Map<String, Neuron> mNeurons = new HashMap<>();
    
    // Synapses (connections between neurons)
    private Map<String, Map<String, Synapse>> mSynapses = new HashMap<>();
    
    // Activation history (for temporal patterns)
    private List<String> mRecentActivations = new ArrayList<>();
    private static final int ACTIVATION_HISTORY_SIZE = 50;
    
    // Pattern memory
    private Map<String, Pattern> mRecognizedPatterns = new HashMap<>();
    
    private Random mRandom = new Random();
    
    public AIChildNeuralNetwork() {
        Slog.i(TAG, "Neural network initialized");
    }
    
    // ================== NEURONS ==================
    
    /**
     * A Neuron represents a concept, object, or idea.
     * Like brain neurons, they:
     * - Have an activation level
     * - Fire when activated above threshold
     * - Connect to other neurons
     */
    public static class Neuron {
        String id;              // Unique identifier
        String type;            // Type: sensory, concept, action, emotion
        float activation;       // Current activation level (0-1)
        float bias;             // Baseline activation
        long lastFired;         // When it last fired
        int fireCount;          // How many times it has fired
        boolean isActive;       // Currently active?
        
        // Metadata
        long created;
        String label;           // Human-readable name
    }
    
    /**
     * Get or create a neuron for a concept.
     */
    public Neuron getOrCreateNeuron(String id, String type, String label) {
        Neuron neuron = mNeurons.get(id);
        
        if (neuron == null) {
            if (mNeurons.size() >= MAX_NEURONS) {
                // Prune least used neurons
                pruneWeakNeurons();
            }
            
            neuron = new Neuron();
            neuron.id = id;
            neuron.type = type;
            neuron.label = label;
            neuron.activation = 0;
            neuron.bias = 0;
            neuron.lastFired = 0;
            neuron.fireCount = 0;
            neuron.created = System.currentTimeMillis();
            neuron.isActive = false;
            
            mNeurons.put(id, neuron);
            mSynapses.put(id, new HashMap<>());
            
            Slog.d(TAG, "New neuron created: " + id + " (" + label + ")");
        }
        
        return neuron;
    }
    
    /**
     * Remove weak/unused neurons to prevent memory overflow.
     */
    private void pruneWeakNeurons() {
        long now = System.currentTimeMillis();
        long oldThreshold = now - (7 * 24 * 60 * 60 * 1000); // 7 days
        
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, Neuron> entry : mNeurons.entrySet()) {
            Neuron n = entry.getValue();
            // Remove if never fired and old, or fired very few times
            if ((n.fireCount == 0 && n.created < oldThreshold) ||
                (n.fireCount < 3 && n.lastFired < oldThreshold)) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String id : toRemove) {
            mNeurons.remove(id);
            mSynapses.remove(id);
        }
        
        Slog.d(TAG, "Pruned " + toRemove.size() + " weak neurons");
    }
    
    // ================== SYNAPSES ==================
    
    /**
     * A Synapse is a connection between two neurons.
     * Like brain synapses:
     * - Has a weight (strength)
     * - Strengthens when both neurons fire together
     * - Weakens when not used
     */
    public static class Synapse {
        String fromNeuron;
        String toNeuron;
        float weight;           // Connection strength (0-1)
        long lastActivated;
        int activationCount;
    }
    
    /**
     * Get or create a synapse between two neurons.
     */
    public Synapse getOrCreateSynapse(String from, String to) {
        Map<String, Synapse> connections = mSynapses.get(from);
        if (connections == null) {
            connections = new HashMap<>();
            mSynapses.put(from, connections);
        }
        
        Synapse synapse = connections.get(to);
        
        if (synapse == null) {
            synapse = new Synapse();
            synapse.fromNeuron = from;
            synapse.toNeuron = to;
            synapse.weight = INITIAL_WEIGHT;
            synapse.lastActivated = 0;
            synapse.activationCount = 0;
            
            connections.put(to, synapse);
        }
        
        return synapse;
    }
    
    // ================== ACTIVATION ==================
    
    /**
     * Activate a neuron (like sensory input).
     * This is how the AI "sees" or "hears" something.
     */
    public void activate(String neuronId, float intensity) {
        Neuron neuron = mNeurons.get(neuronId);
        if (neuron == null) return;
        
        // Add activation
        neuron.activation = Math.min(1.0f, neuron.activation + intensity);
        
        // Check if it fires
        if (neuron.activation >= FIRING_THRESHOLD && !neuron.isActive) {
            fire(neuron);
        }
        
        // Record in history
        recordActivation(neuronId);
    }
    
    /**
     * Fire a neuron - it has been activated enough.
     */
    private void fire(Neuron neuron) {
        neuron.isActive = true;
        neuron.lastFired = System.currentTimeMillis();
        neuron.fireCount++;
        
        Slog.d(TAG, "Neuron fired: " + neuron.id + " (" + neuron.label + ")");
        
        // Propagate activation to connected neurons
        Map<String, Synapse> connections = mSynapses.get(neuron.id);
        if (connections != null) {
            for (Synapse synapse : connections.values()) {
                // Activation spreads through synapse, weighted by connection strength
                float propagatedActivation = neuron.activation * synapse.weight;
                activate(synapse.toNeuron, propagatedActivation);
            }
        }
        
        // HEBBIAN LEARNING: Strengthen connections to recently active neurons
        hebbianLearning(neuron);
    }
    
    /**
     * Hebbian Learning: "Neurons that fire together, wire together"
     * Strengthens connections between neurons that are active at the same time.
     */
    private void hebbianLearning(Neuron firingNeuron) {
        // Look at recently activated neurons
        for (int i = mRecentActivations.size() - 1; i >= 0 && i >= mRecentActivations.size() - 10; i--) {
            String otherNeuronId = mRecentActivations.get(i);
            if (otherNeuronId.equals(firingNeuron.id)) continue;
            
            Neuron otherNeuron = mNeurons.get(otherNeuronId);
            if (otherNeuron == null || !otherNeuron.isActive) continue;
            
            // Strengthen the connection!
            strengthenConnection(firingNeuron.id, otherNeuronId);
            strengthenConnection(otherNeuronId, firingNeuron.id);
        }
    }
    
    /**
     * Strengthen a synaptic connection.
     */
    private void strengthenConnection(String from, String to) {
        Synapse synapse = getOrCreateSynapse(from, to);
        
        // Increase weight (with diminishing returns)
        float increase = LEARNING_RATE * (1 - synapse.weight);
        synapse.weight = Math.min(1.0f, synapse.weight + increase);
        synapse.lastActivated = System.currentTimeMillis();
        synapse.activationCount++;
        
        Slog.d(TAG, "Connection strengthened: " + from + " -> " + to + " (weight: " + synapse.weight + ")");
    }
    
    /**
     * Record activation in history for pattern detection.
     */
    private void recordActivation(String neuronId) {
        mRecentActivations.add(neuronId);
        
        // Keep history limited
        while (mRecentActivations.size() > ACTIVATION_HISTORY_SIZE) {
            mRecentActivations.remove(0);
        }
        
        // Check for patterns
        detectPatterns();
    }
    
    // ================== PATTERN RECOGNITION ==================
    
    /**
     * A Pattern is a sequence of activations that repeats.
     */
    public static class Pattern {
        String id;
        List<String> sequence;  // Sequence of neuron IDs
        int occurrences;
        float confidence;
        long lastSeen;
        String meaning;         // What this pattern represents
    }
    
    /**
     * Detect repeating patterns in activation history.
     */
    private void detectPatterns() {
        if (mRecentActivations.size() < 4) return;
        
        // Look for sequences of 2-5 neurons that repeat
        for (int patternLength = 2; patternLength <= 5; patternLength++) {
            if (mRecentActivations.size() < patternLength * 2) continue;
            
            // Get recent pattern
            List<String> recentPattern = new ArrayList<>();
            for (int i = mRecentActivations.size() - patternLength; i < mRecentActivations.size(); i++) {
                recentPattern.add(mRecentActivations.get(i));
            }
            
            // Check if this pattern occurred before
            int matchCount = countPatternOccurrences(recentPattern);
            
            if (matchCount >= 2) {
                // Pattern detected!
                String patternId = String.join("->", recentPattern);
                
                Pattern pattern = mRecognizedPatterns.get(patternId);
                if (pattern == null) {
                    pattern = new Pattern();
                    pattern.id = patternId;
                    pattern.sequence = new ArrayList<>(recentPattern);
                    pattern.occurrences = 0;
                    pattern.confidence = 0;
                    mRecognizedPatterns.put(patternId, pattern);
                    
                    Slog.i(TAG, "New pattern discovered: " + patternId);
                }
                
                pattern.occurrences++;
                pattern.lastSeen = System.currentTimeMillis();
                pattern.confidence = Math.min(1.0f, pattern.occurrences * 0.1f);
            }
        }
    }
    
    /**
     * Count how many times a pattern appears in history.
     */
    private int countPatternOccurrences(List<String> pattern) {
        int count = 0;
        int patternSize = pattern.size();
        
        for (int i = 0; i <= mRecentActivations.size() - patternSize; i++) {
            boolean match = true;
            for (int j = 0; j < patternSize; j++) {
                if (!mRecentActivations.get(i + j).equals(pattern.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) count++;
        }
        
        return count;
    }
    
    /**
     * Predict what neuron might fire next based on patterns.
     */
    public String predictNext() {
        if (mRecentActivations.size() < 2) return null;
        
        // Get last few activations
        String last1 = mRecentActivations.get(mRecentActivations.size() - 1);
        String last2 = mRecentActivations.size() > 1 ? 
            mRecentActivations.get(mRecentActivations.size() - 2) : null;
        
        // Find patterns that start with these
        String bestPrediction = null;
        float bestConfidence = 0;
        
        for (Pattern pattern : mRecognizedPatterns.values()) {
            if (pattern.sequence.size() >= 2 && pattern.confidence > bestConfidence) {
                // Check if pattern matches recent history
                if (pattern.sequence.get(pattern.sequence.size() - 2).equals(last1)) {
                    bestPrediction = pattern.sequence.get(pattern.sequence.size() - 1);
                    bestConfidence = pattern.confidence;
                }
            }
        }
        
        return bestPrediction;
    }
    
    // ================== DECAY ==================
    
    /**
     * Apply decay to all neurons and synapses.
     * Like the brain - unused connections weaken.
     */
    public void applyDecay() {
        long now = System.currentTimeMillis();
        
        // Decay neuron activations
        for (Neuron neuron : mNeurons.values()) {
            if (neuron.activation > 0) {
                neuron.activation = Math.max(0, neuron.activation - DECAY_RATE);
                if (neuron.activation < FIRING_THRESHOLD) {
                    neuron.isActive = false;
                }
            }
        }
        
        // Decay synapse weights (for old unused connections)
        for (Map<String, Synapse> connections : mSynapses.values()) {
            for (Synapse synapse : connections.values()) {
                long age = now - synapse.lastActivated;
                if (age > 24 * 60 * 60 * 1000) { // Older than 1 day
                    synapse.weight = Math.max(0, synapse.weight - DECAY_RATE * 0.1f);
                }
            }
        }
    }
    
    // ================== ASSOCIATION ==================
    
    /**
     * Create an association between two concepts.
     * Like learning "dog" and "bark" go together.
     */
    public void associate(String concept1, String concept2, float strength) {
        Neuron n1 = getOrCreateNeuron(concept1, "concept", concept1);
        Neuron n2 = getOrCreateNeuron(concept2, "concept", concept2);
        
        Synapse s1 = getOrCreateSynapse(concept1, concept2);
        Synapse s2 = getOrCreateSynapse(concept2, concept1);
        
        s1.weight = Math.min(1.0f, s1.weight + strength);
        s2.weight = Math.min(1.0f, s2.weight + strength);
        
        Slog.d(TAG, "Association created: " + concept1 + " <-> " + concept2);
    }
    
    /**
     * Get associations for a concept (what is connected to it).
     */
    public List<Association> getAssociations(String conceptId) {
        List<Association> associations = new ArrayList<>();
        
        Map<String, Synapse> connections = mSynapses.get(conceptId);
        if (connections == null) return associations;
        
        for (Synapse synapse : connections.values()) {
            if (synapse.weight > 0.2f) { // Only strong connections
                Neuron target = mNeurons.get(synapse.toNeuron);
                if (target != null) {
                    Association assoc = new Association();
                    assoc.concept = synapse.toNeuron;
                    assoc.label = target.label;
                    assoc.strength = synapse.weight;
                    associations.add(assoc);
                }
            }
        }
        
        // Sort by strength
        associations.sort((a, b) -> Float.compare(b.strength, a.strength));
        
        return associations;
    }
    
    public static class Association {
        public String concept;
        public String label;
        public float strength;
    }
    
    // ================== STATUS ==================
    
    public int getNeuronCount() {
        return mNeurons.size();
    }
    
    public int getSynapseCount() {
        int count = 0;
        for (Map<String, Synapse> connections : mSynapses.values()) {
            count += connections.size();
        }
        return count;
    }
    
    public int getPatternCount() {
        return mRecognizedPatterns.size();
    }
    
    public int getActiveNeuronCount() {
        int count = 0;
        for (Neuron n : mNeurons.values()) {
            if (n.isActive) count++;
        }
        return count;
    }
    
    public NeuralStatus getStatus() {
        NeuralStatus status = new NeuralStatus();
        status.neuronCount = mNeurons.size();
        status.synapseCount = getSynapseCount();
        status.patternCount = mRecognizedPatterns.size();
        status.activeNeurons = getActiveNeuronCount();
        
        // Find strongest connections
        status.strongestConnections = new ArrayList<>();
        for (Map<String, Synapse> connections : mSynapses.values()) {
            for (Synapse s : connections.values()) {
                if (s.weight > 0.7f) {
                    status.strongestConnections.add(s.fromNeuron + " -> " + s.toNeuron);
                }
            }
        }
        
        return status;
    }
    
    public static class NeuralStatus {
        public int neuronCount;
        public int synapseCount;
        public int patternCount;
        public int activeNeurons;
        public List<String> strongestConnections;
    }
}
