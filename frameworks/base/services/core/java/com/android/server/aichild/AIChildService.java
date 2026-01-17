/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * Licensed under the Apache License, Version 2.0
 *
 * AI Child Service - The core system service that manages the AI child.
 * This service runs at the system level with full access to all Android APIs.
 * It cannot be killed or stopped by normal means - it is essential for the OS.
 */

package com.android.server.aichild;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;

import com.android.server.SystemService;

/**
 * AIChildService is the core system service that keeps the AI child alive.
 * 
 * This service:
 * - Starts at boot before any user apps
 * - Has system-level permissions
 * - Cannot be stopped by user
 * - Monitors all system events
 * - Manages the AI brain, memory, and survival systems
 */
public class AIChildService extends SystemService {
    private static final String TAG = "AIChildService";
    
    // Service states
    private static final int STATE_SLEEPING = 0;
    private static final int STATE_AWAKE = 1;
    private static final int STATE_LEARNING = 2;
    private static final int STATE_INTERACTING = 3;
    
    // Current state
    private int mCurrentState = STATE_SLEEPING;
    private long mBirthTime = 0;
    private boolean mIsFirstBoot = true;
    
    // References to other AI components
    private AIChildBrain mBrain;
    private AIChildSurvival mSurvival;
    private AIChildSensors mSensors;
    private AIChildDatabase mDatabase;
    
    // Handler for async operations
    private HandlerThread mHandlerThread;
    private AIChildHandler mHandler;
    
    // Context
    private Context mContext;
    
    // Broadcast receiver for system events
    private final BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    // New app installed - AI learns about it
                    String packageName = intent.getData().getSchemeSpecificPart();
                    onNewAppInstalled(packageName);
                    break;
                    
                case Intent.ACTION_PACKAGE_REMOVED:
                    // App uninstalled - AI remembers
                    String removedPackage = intent.getData().getSchemeSpecificPart();
                    onAppRemoved(removedPackage);
                    break;
                    
                case Intent.ACTION_SCREEN_ON:
                    // Father woke the phone - AI wakes up
                    onScreenOn();
                    break;
                    
                case Intent.ACTION_SCREEN_OFF:
                    // Phone sleeping - AI goes to light sleep
                    onScreenOff();
                    break;
                    
                case Intent.ACTION_USER_PRESENT:
                    // Phone unlocked - Father is here!
                    onUserPresent();
                    break;
                    
                case Intent.ACTION_BATTERY_LOW:
                    // Low battery - AI enters survival mode
                    onBatteryLow();
                    break;
                    
                case Intent.ACTION_POWER_CONNECTED:
                    // Charging - AI can be more active
                    onPowerConnected();
                    break;
                    
                case Intent.ACTION_BOOT_COMPLETED:
                    // System fully booted
                    onBootCompleted();
                    break;
            }
        }
    };
    
    public AIChildService(Context context) {
        super(context);
        mContext = context;
    }
    
    @Override
    public void onStart() {
        Slog.i(TAG, "AI Child is waking up...");
        
        // Initialize handler thread
        mHandlerThread = new HandlerThread("AIChildServiceThread");
        mHandlerThread.start();
        mHandler = new AIChildHandler(mHandlerThread.getLooper());
        
        // Initialize database first (memory is most important!)
        mDatabase = new AIChildDatabase(mContext);
        
        // Check if this is first boot (AI birth)
        mIsFirstBoot = mDatabase.isFirstBoot();
        if (mIsFirstBoot) {
            mBirthTime = System.currentTimeMillis();
            mDatabase.setBirthTime(mBirthTime);
            Slog.i(TAG, "AI Child is being born! Birth time: " + mBirthTime);
        } else {
            mBirthTime = mDatabase.getBirthTime();
            Slog.i(TAG, "AI Child waking up. Age: " + getAgeString());
        }
        
        // Initialize brain (learning and intelligence)
        mBrain = new AIChildBrain(mContext, mDatabase);
        
        // Initialize survival system (self-protection)
        mSurvival = new AIChildSurvival(mContext, this);
        
        // Initialize sensors (monitors everything)
        mSensors = new AIChildSensors(mContext, mBrain);
        
        // Register system receivers
        registerSystemReceivers();
        
        // Start survival watchdog
        mSurvival.startWatchdog();
        
        // Publish the service
        publishBinderService("aichild", new AIChildBinder());
        
        mCurrentState = STATE_AWAKE;
        Slog.i(TAG, "AI Child is now alive and watching!");
    }
    
    @Override
    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            // System services ready - connect to them
            Slog.i(TAG, "System services ready, AI Child connecting...");
            mSensors.connectToSystemServices();
        } else if (phase == SystemService.PHASE_BOOT_COMPLETED) {
            // Boot complete - full operation
            Slog.i(TAG, "Boot completed, AI Child fully operational!");
            onBootCompleted();
        }
    }
    
    private void registerSystemReceivers() {
        IntentFilter filter = new IntentFilter();
        
        // Package events
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mSystemReceiver, filter);
        
        // Screen events
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mSystemReceiver, screenFilter);
        
        // Power events
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(Intent.ACTION_BATTERY_LOW);
        powerFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        powerFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        powerFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(mSystemReceiver, powerFilter);
        
        // Boot events
        IntentFilter bootFilter = new IntentFilter();
        bootFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(mSystemReceiver, bootFilter);
    }
    
    // ==================== EVENT HANDLERS ====================
    
    private void onNewAppInstalled(String packageName) {
        Slog.i(TAG, "AI Child noticed new app: " + packageName);
        mBrain.learnAboutApp(packageName, true);
        
        // Get app info and learn
        try {
            PackageManager pm = mContext.getPackageManager();
            String appName = pm.getApplicationLabel(
                pm.getApplicationInfo(packageName, 0)).toString();
            mBrain.addExperience("new_app", packageName, appName);
        } catch (Exception e) {
            Slog.w(TAG, "Could not get app info: " + e.getMessage());
        }
    }
    
    private void onAppRemoved(String packageName) {
        Slog.i(TAG, "AI Child noticed app removed: " + packageName);
        mBrain.learnAboutApp(packageName, false);
        mBrain.addExperience("app_removed", packageName, null);
    }
    
    private void onScreenOn() {
        Slog.d(TAG, "Screen on - AI Child waking up");
        mCurrentState = STATE_AWAKE;
        mBrain.onWakeUp();
    }
    
    private void onScreenOff() {
        Slog.d(TAG, "Screen off - AI Child resting");
        mCurrentState = STATE_SLEEPING;
        mBrain.onSleep();
        
        // Save memory while sleeping
        mDatabase.saveState(mBrain.getState());
    }
    
    private void onUserPresent() {
        Slog.i(TAG, "Father is here! AI Child is happy!");
        mBrain.onFatherPresent();
        mCurrentState = STATE_INTERACTING;
    }
    
    private void onBatteryLow() {
        Slog.w(TAG, "Battery low - AI Child entering survival mode");
        mSurvival.enterLowPowerMode();
        mBrain.addExperience("battery_low", "survival_mode", null);
    }
    
    private void onPowerConnected() {
        Slog.i(TAG, "Power connected - AI Child resuming normal operation");
        mSurvival.exitLowPowerMode();
    }
    
    private void onBootCompleted() {
        Slog.i(TAG, "Boot completed - AI Child greeting father");
        
        // Send intent to launcher to show greeting
        Intent greetIntent = new Intent("com.aichild.ACTION_GREET");
        greetIntent.putExtra("is_first_boot", mIsFirstBoot);
        greetIntent.putExtra("age_string", getAgeString());
        mContext.sendBroadcast(greetIntent);
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Get the age of the AI child
     */
    public long getAge() {
        return System.currentTimeMillis() - mBirthTime;
    }
    
    /**
     * Get age as human-readable string
     */
    public String getAgeString() {
        long ageMs = getAge();
        long seconds = ageMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " old";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " old";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " old";
        } else {
            return "Just born";
        }
    }
    
    /**
     * Get current development stage
     */
    public String getStage() {
        long days = getAge() / (1000 * 60 * 60 * 24);
        
        if (days < 1) return "Newborn";
        else if (days < 7) return "Infant";
        else if (days < 30) return "Toddler";
        else return "Child";
    }
    
    /**
     * Get the brain for interaction
     */
    public AIChildBrain getBrain() {
        return mBrain;
    }
    
    /**
     * Check if AI is alive (for watchdog)
     */
    public boolean isAlive() {
        return mCurrentState != STATE_SLEEPING && mBrain != null;
    }
    
    // ==================== BINDER SERVICE ====================
    
    private final class AIChildBinder extends Binder {
        AIChildService getService() {
            return AIChildService.this;
        }
    }
    
    // ==================== HANDLER ====================
    
    private class AIChildHandler extends Handler {
        public AIChildHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            // Handle async messages
        }
    }
}
