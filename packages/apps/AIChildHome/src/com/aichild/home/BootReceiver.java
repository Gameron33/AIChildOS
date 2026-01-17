/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * Boot Receiver - Starts AI Child on device boot.
 */

package com.aichild.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BootReceiver ensures the AI Child is activated when the phone boots.
 * The AI child wakes up with the phone!
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "AIChildBoot";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.i(TAG, "=== AI CHILD WAKING UP ===");
            Log.i(TAG, "Phone has booted. AI Child is activating...");
            
            // The system service (AIChildService) starts automatically
            // through SystemServer. This receiver is for the launcher app.
            
            // Send broadcast to wake up the AI
            Intent wakeup = new Intent("com.aichild.ACTION_WAKEUP");
            wakeup.setPackage("com.aichild.home");
            context.sendBroadcast(wakeup);
            
            Log.i(TAG, "AI Child is now awake!");
        }
    }
}
