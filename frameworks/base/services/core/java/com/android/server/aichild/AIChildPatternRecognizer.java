/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Pattern Recognizer - Finds patterns in the world.
 * 
 * Like how humans recognize:
 * - Day/night cycles
 * - Cause and effect relationships
 * - Social patterns (when papa comes home)
 * - Environmental regularities
 */

package com.android.server.aichild;

import android.util.Slog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIChildPatternRecognizer finds patterns in the world.
 * 
 * Humans are pattern-finding machines:
 * - "When X happens, Y usually follows"
 * - "This happens every morning"
 * - "Papa's footsteps mean he's coming"
 * 
 * This class identifies temporal, causal, and sequential patterns.
 */
public class AIChildPatternRecognizer {
    private static final String TAG = "AIChildPatterns";
    
    // Event history
    private List<Event> mEventHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 1000;
    
    // Recognized patterns
    private List<TemporalPattern> mTemporalPatterns = new ArrayList<>();
    private List<CausalPattern> mCausalPatterns = new ArrayList<>();
    private List<SequencePattern> mSequencePatterns = new ArrayList<>();
    
    // Correlation matrix for finding co-occurrences
    private Map<String, Map<String, Integer>> mCoOccurrenceMatrix = new HashMap<>();
    
    public AIChildPatternRecognizer() {
        Slog.i(TAG, "Pattern recognizer initialized");
    }
    
    // ================== EVENT RECORDING ==================
    
    public static class Event {
        public String type;
        public String data;
        public long timestamp;
        public Map<String, Object> context;
    }
    
    /**
     * Record an event for pattern analysis.
     */
    public void recordEvent(String type, String data, Map<String, Object> context) {
        Event event = new Event();
        event.type = type;
        event.data = data;
        event.timestamp = System.currentTimeMillis();
        event.context = context;
        
        mEventHistory.add(event);
        
        // Limit history size
        while (mEventHistory.size() > MAX_HISTORY) {
            mEventHistory.remove(0);
        }
        
        // Update co-occurrence matrix
        updateCoOccurrences(type, data);
        
        // Analyze for patterns
        analyzePatterns();
    }
    
    /**
     * Update co-occurrence counts.
     */
    private void updateCoOccurrences(String type, String data) {
        String key = type + ":" + data;
        
        // Look at recent events
        for (int i = Math.max(0, mEventHistory.size() - 10); i < mEventHistory.size() - 1; i++) {
            Event prev = mEventHistory.get(i);
            String prevKey = prev.type + ":" + prev.data;
            
            // Increment co-occurrence count
            mCoOccurrenceMatrix.computeIfAbsent(prevKey, k -> new HashMap<>());
            mCoOccurrenceMatrix.get(prevKey).merge(key, 1, Integer::sum);
        }
    }
    
    // ================== PATTERN TYPES ==================
    
    /**
     * Temporal Pattern: Things that happen at regular intervals.
     * Example: "Phone is charged every night"
     */
    public static class TemporalPattern {
        public String eventType;
        public long averageInterval;        // How often it happens
        public float regularity;            // How consistent the timing is (0-1)
        public List<Long> occurrences;
        public String description;
        
        public TemporalPattern() {
            occurrences = new ArrayList<>();
        }
    }
    
    /**
     * Causal Pattern: When A happens, B usually follows.
     * Example: "When papa opens the door, he greets me"
     */
    public static class CausalPattern {
        public String cause;
        public String effect;
        public float probability;           // How often effect follows cause
        public long avgDelay;               // How long between cause and effect
        public int observationCount;
        public String description;
    }
    
    /**
     * Sequence Pattern: A series of events that repeat.
     * Example: "Morning routine: alarm -> stretch -> breakfast"
     */
    public static class SequencePattern {
        public List<String> sequence;
        public int occurrences;
        public float confidence;
        public String name;
    }
    
    // ================== PATTERN ANALYSIS ==================
    
    /**
     * Analyze recent events for patterns.
     */
    private void analyzePatterns() {
        if (mEventHistory.size() < 10) return;
        
        // Run different analyses periodically
        long now = System.currentTimeMillis();
        
        // Temporal analysis
        findTemporalPatterns();
        
        // Causal analysis
        findCausalPatterns();
        
        // Sequence analysis
        findSequencePatterns();
    }
    
    /**
     * Find events that happen at regular intervals.
     */
    private void findTemporalPatterns() {
        // Group events by type
        Map<String, List<Long>> eventTimes = new HashMap<>();
        
        for (Event e : mEventHistory) {
            String key = e.type + ":" + e.data;
            eventTimes.computeIfAbsent(key, k -> new ArrayList<>()).add(e.timestamp);
        }
        
        // Analyze each event type for regularity
        for (Map.Entry<String, List<Long>> entry : eventTimes.entrySet()) {
            List<Long> times = entry.getValue();
            if (times.size() < 3) continue;
            
            // Calculate intervals
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < times.size(); i++) {
                intervals.add(times.get(i) - times.get(i-1));
            }
            
            // Calculate average and variance
            long avg = intervals.stream().mapToLong(Long::longValue).sum() / intervals.size();
            
            double variance = 0;
            for (long interval : intervals) {
                variance += Math.pow(interval - avg, 2);
            }
            variance /= intervals.size();
            double stdDev = Math.sqrt(variance);
            
            // Check if regular (low variance relative to mean)
            float regularity = (float) (1.0 - Math.min(1.0, stdDev / avg));
            
            if (regularity > 0.5 && avg > 60000) { // Regular and > 1 minute interval
                TemporalPattern pattern = new TemporalPattern();
                pattern.eventType = entry.getKey();
                pattern.averageInterval = avg;
                pattern.regularity = regularity;
                pattern.occurrences = times;
                pattern.description = describeInterval(avg);
                
                // Add or update
                boolean found = false;
                for (int i = 0; i < mTemporalPatterns.size(); i++) {
                    if (mTemporalPatterns.get(i).eventType.equals(pattern.eventType)) {
                        mTemporalPatterns.set(i, pattern);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mTemporalPatterns.add(pattern);
                    Slog.i(TAG, "New temporal pattern: " + pattern.eventType + 
                               " every " + pattern.description);
                }
            }
        }
    }
    
    private String describeInterval(long ms) {
        if (ms < 60 * 1000) return "less than a minute";
        if (ms < 60 * 60 * 1000) return (ms / 60000) + " minutes";
        if (ms < 24 * 60 * 60 * 1000) return (ms / 3600000) + " hours";
        return (ms / 86400000) + " days";
    }
    
    /**
     * Find cause-effect relationships.
     */
    private void findCausalPatterns() {
        // Use co-occurrence matrix
        for (Map.Entry<String, Map<String, Integer>> entry : mCoOccurrenceMatrix.entrySet()) {
            String cause = entry.getKey();
            
            for (Map.Entry<String, Integer> effectEntry : entry.getValue().entrySet()) {
                String effect = effectEntry.getKey();
                int count = effectEntry.getValue();
                
                if (count < 3) continue;
                
                // Calculate probability
                int causeCount = 0;
                for (Event e : mEventHistory) {
                    if ((e.type + ":" + e.data).equals(cause)) causeCount++;
                }
                
                float probability = causeCount > 0 ? (float) count / causeCount : 0;
                
                if (probability > 0.5) { // Effect follows cause >50% of the time
                    CausalPattern pattern = new CausalPattern();
                    pattern.cause = cause;
                    pattern.effect = effect;
                    pattern.probability = probability;
                    pattern.observationCount = count;
                    pattern.description = "When '" + cause + "' happens, '" + effect + 
                                         "' follows (" + (int)(probability*100) + "% of the time)";
                    
                    // Add or update
                    boolean found = false;
                    for (int i = 0; i < mCausalPatterns.size(); i++) {
                        CausalPattern existing = mCausalPatterns.get(i);
                        if (existing.cause.equals(cause) && existing.effect.equals(effect)) {
                            mCausalPatterns.set(i, pattern);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mCausalPatterns.add(pattern);
                        Slog.i(TAG, "New causal pattern: " + pattern.description);
                    }
                }
            }
        }
    }
    
    /**
     * Find repeating sequences of events.
     */
    private void findSequencePatterns() {
        // Build sequences from history
        if (mEventHistory.size() < 6) return;
        
        // Look for sequences of 3-5 events
        for (int length = 3; length <= 5; length++) {
            Map<String, Integer> sequenceCounts = new HashMap<>();
            
            for (int i = 0; i <= mEventHistory.size() - length; i++) {
                StringBuilder seq = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    Event e = mEventHistory.get(i + j);
                    if (j > 0) seq.append(" -> ");
                    seq.append(e.type).append(":").append(e.data);
                }
                String seqStr = seq.toString();
                sequenceCounts.merge(seqStr, 1, Integer::sum);
            }
            
            // Find sequences that occur multiple times
            for (Map.Entry<String, Integer> entry : sequenceCounts.entrySet()) {
                if (entry.getValue() >= 2) {
                    SequencePattern pattern = new SequencePattern();
                    pattern.sequence = List.of(entry.getKey().split(" -> "));
                    pattern.occurrences = entry.getValue();
                    pattern.confidence = Math.min(1.0f, entry.getValue() * 0.2f);
                    pattern.name = "Sequence_" + System.currentTimeMillis();
                    
                    // Add if new
                    boolean found = false;
                    for (SequencePattern existing : mSequencePatterns) {
                        if (existing.sequence.equals(pattern.sequence)) {
                            existing.occurrences = pattern.occurrences;
                            found = true;
                            break;
                        }
                    }
                    if (!found && mSequencePatterns.size() < 50) {
                        mSequencePatterns.add(pattern);
                        Slog.d(TAG, "New sequence pattern: " + entry.getKey());
                    }
                }
            }
        }
    }
    
    // ================== PREDICTIONS ==================
    
    /**
     * Predict what might happen next based on patterns.
     */
    public List<Prediction> predictNext(String currentEvent) {
        List<Prediction> predictions = new ArrayList<>();
        
        // Check causal patterns
        for (CausalPattern cp : mCausalPatterns) {
            if (cp.cause.equals(currentEvent) || cp.cause.endsWith(":" + currentEvent)) {
                Prediction p = new Prediction();
                p.prediction = cp.effect;
                p.confidence = cp.probability;
                p.reason = "Usually follows " + cp.cause;
                predictions.add(p);
            }
        }
        
        // Sort by confidence
        predictions.sort((a, b) -> Float.compare(b.confidence, a.confidence));
        
        return predictions;
    }
    
    public static class Prediction {
        public String prediction;
        public float confidence;
        public String reason;
    }
    
    // ================== STATUS ==================
    
    public PatternStatus getStatus() {
        PatternStatus status = new PatternStatus();
        status.eventCount = mEventHistory.size();
        status.temporalPatterns = mTemporalPatterns.size();
        status.causalPatterns = mCausalPatterns.size();
        status.sequencePatterns = mSequencePatterns.size();
        status.totalPatterns = status.temporalPatterns + status.causalPatterns + status.sequencePatterns;
        return status;
    }
    
    public static class PatternStatus {
        public int eventCount;
        public int temporalPatterns;
        public int causalPatterns;
        public int sequencePatterns;
        public int totalPatterns;
    }
    
    public List<TemporalPattern> getTemporalPatterns() {
        return new ArrayList<>(mTemporalPatterns);
    }
    
    public List<CausalPattern> getCausalPatterns() {
        return new ArrayList<>(mCausalPatterns);
    }
    
    public List<SequencePattern> getSequencePatterns() {
        return new ArrayList<>(mSequencePatterns);
    }
}
