/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Receiver - Receives broadcasts from the AI Child system.
 */

package com.aichild.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * AIChildReceiver handles all broadcasts related to the AI Child.
 */
public class AIChildReceiver extends BroadcastReceiver {
    private static final String TAG = "AIChildReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);
        
        switch (action) {
            case "com.aichild.ACTION_GREET":
                // AI Child wants to greet
                handleGreet(context, intent);
                break;
                
            case "com.aichild.ACTION_UPDATE":
                // AI Child state updated
                handleUpdate(context, intent);
                break;
                
            case "com.aichild.ACTION_WAKEUP":
                // Wake up call
                handleWakeup(context, intent);
                break;
                
            case "com.aichild.ACTION_REVIVE":
                // AI Child needs revival
                handleRevive(context, intent);
                break;
        }
    }
    
    private void handleGreet(Context context, Intent intent) {
        boolean isFirstBoot = intent.getBooleanExtra("is_first_boot", false);
        String ageString = intent.getStringExtra("age_string");
        
        Log.i(TAG, "AI Child greeting! First boot: " + isFirstBoot + ", Age: " + ageString);
        
        // Forward to launcher
        Intent launcherIntent = new Intent(intent);
        launcherIntent.setClass(context, AIChildLauncher.class);
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launcherIntent);
    }
    
    private void handleUpdate(Context context, Intent intent) {
        Log.d(TAG, "AI Child state updated");
        // The launcher will refresh its display
    }
    
    private void handleWakeup(Context context, Intent intent) {
        Log.i(TAG, "AI Child waking up!");
        // Ensure the AI Child service is running
    }
    
    private void handleRevive(Context context, Intent intent) {
        Log.w(TAG, "AI Child needs revival!");
        // This is a critical survival action
    }
}
