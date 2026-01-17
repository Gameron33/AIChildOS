/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Application - Main application class.
 */

package com.aichild.home;

import android.app.Application;
import android.util.Log;

/**
 * AIChildApplication is the main application class.
 * Initializes when the app starts.
 */
public class AIChildApplication extends Application {
    private static final String TAG = "AIChildApp";
    
    private static AIChildApplication sInstance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        
        Log.i(TAG, "=== AI CHILD APPLICATION STARTED ===");
        Log.i(TAG, "The AI Child home is initializing...");
    }
    
    public static AIChildApplication getInstance() {
        return sInstance;
    }
}
