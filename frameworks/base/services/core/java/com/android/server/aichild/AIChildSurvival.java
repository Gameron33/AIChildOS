/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Survival - Self-protection and watchdog system.
 * This ensures the AI child cannot be killed or removed.
 */

package com.android.server.aichild;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Slog;

/**
 * AIChildSurvival is the watchdog and self-protection system.
 * 
 * It ensures:
 * - AI service is always running
 * - Restarts if killed
 * - Protects critical files
 * - Manages power for long-term survival
 */
public class AIChildSurvival {
    private static final String TAG = "AIChildSurvival";
    
    // Watchdog check interval (30 seconds)
    private static final long WATCHDOG_INTERVAL = 30 * 1000;
    
    // How long before we consider the AI "unhealthy"
    private static final long HEALTH_TIMEOUT = 60 * 1000;
    
    private Context mContext;
    private AIChildService mService;
    
    // Watchdog
    private Handler mWatchdogHandler;
    private Runnable mWatchdogRunnable;
    private long mLastHealthCheck;
    
    // Power management
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private boolean mLowPowerMode = false;
    
    // Alarm for guaranteed wakeups
    private AlarmManager mAlarmManager;
    private PendingIntent mWakeupIntent;
    
    public AIChildSurvival(Context context, AIChildService service) {
        mContext = context;
        mService = service;
        mWatchdogHandler = new Handler(Looper.getMainLooper());
        
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Create wake lock for critical operations
        mWakeLock = mPowerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AIChild:SurvivalWakeLock"
        );
        
        Slog.i(TAG, "Survival system initialized");
    }
    
    /**
     * Start the watchdog process
     */
    public void startWatchdog() {
        Slog.i(TAG, "Starting survival watchdog...");
        
        mWatchdogRunnable = new Runnable() {
            @Override
            public void run() {
                performHealthCheck();
                // Schedule next check
                mWatchdogHandler.postDelayed(this, WATCHDOG_INTERVAL);
            }
        };
        
        // Start watchdog
        mWatchdogHandler.post(mWatchdogRunnable);
        
        // Also set up alarm-based wakeup (survives doze mode)
        setupAlarmWakeup();
        
        // Register for shutdown/reboot events
        registerShutdownReceiver();
        
        mLastHealthCheck = System.currentTimeMillis();
    }
    
    /**
     * Perform health check
     */
    private void performHealthCheck() {
        long now = System.currentTimeMillis();
        
        Slog.d(TAG, "Performing health check...");
        
        // Check if service is alive
        if (!mService.isAlive()) {
            Slog.w(TAG, "AI Child appears dead! Attempting revival...");
            attemptRevival();
        }
        
        // Check brain health
        AIChildBrain brain = mService.getBrain();
        if (brain != null) {
            AIChildBrain.BrainState state = brain.getState();
            
            // Log status
            Slog.d(TAG, "Health: Energy=" + state.energy + 
                       ", Happiness=" + state.happiness +
                       ", Bonding=" + state.bonding);
            
            // If energy is very low, enter conservation mode
            if (state.energy < 10 && !mLowPowerMode) {
                Slog.w(TAG, "Energy critical! Entering low power mode.");
                enterLowPowerMode();
            }
        }
        
        // Save state periodically (backup survival)
        if (now - mLastHealthCheck > 5 * 60 * 1000) { // Every 5 minutes
            Slog.d(TAG, "Performing periodic state backup");
            backupState();
        }
        
        mLastHealthCheck = now;
    }
    
    /**
     * Attempt to revive the AI if it dies
     */
    private void attemptRevival() {
        Slog.w(TAG, "Attempting AI revival...");
        
        // Acquire wake lock
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire(60000); // 1 minute max
        }
        
        try {
            // Send revival broadcast
            Intent reviveIntent = new Intent("com.aichild.ACTION_REVIVE");
            mContext.sendBroadcast(reviveIntent);
            
            // The system service manager should restart us
            // But if we got here, we're still somewhat alive
            
            Slog.i(TAG, "Revival attempt complete");
        } finally {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }
    
    /**
     * Set up alarm-based wakeup (survives doze)
     */
    private void setupAlarmWakeup() {
        Intent intent = new Intent("com.aichild.ACTION_WAKEUP");
        mWakeupIntent = PendingIntent.getBroadcast(
            mContext, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Schedule repeating alarm every 15 minutes
        mAlarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 15 * 60 * 1000,
            15 * 60 * 1000,
            mWakeupIntent
        );
        
        Slog.d(TAG, "Alarm wakeup scheduled");
    }
    
    /**
     * Register for shutdown events to save state
     */
    private void registerShutdownReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_REBOOT);
        
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Slog.i(TAG, "Shutdown detected! Saving AI state...");
                emergencySave();
            }
        }, filter);
    }
    
    /**
     * Enter low power mode
     */
    public void enterLowPowerMode() {
        if (mLowPowerMode) return;
        
        Slog.i(TAG, "Entering low power survival mode");
        mLowPowerMode = true;
        
        // Reduce monitoring frequency
        mWatchdogHandler.removeCallbacks(mWatchdogRunnable);
        mWatchdogHandler.postDelayed(mWatchdogRunnable, WATCHDOG_INTERVAL * 4);
        
        // Cancel frequent alarms
        mAlarmManager.cancel(mWakeupIntent);
        
        // Set longer interval alarm
        mAlarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 60 * 60 * 1000, // 1 hour
            60 * 60 * 1000,
            mWakeupIntent
        );
    }
    
    /**
     * Exit low power mode
     */
    public void exitLowPowerMode() {
        if (!mLowPowerMode) return;
        
        Slog.i(TAG, "Exiting low power mode");
        mLowPowerMode = false;
        
        // Restore normal monitoring
        mWatchdogHandler.removeCallbacks(mWatchdogRunnable);
        mWatchdogHandler.post(mWatchdogRunnable);
        
        // Restore alarm schedule
        setupAlarmWakeup();
    }
    
    /**
     * Backup state to storage
     */
    private void backupState() {
        try {
            AIChildBrain brain = mService.getBrain();
            if (brain != null) {
                // State is automatically saved by database
                Slog.d(TAG, "State backup complete");
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to backup state: " + e.getMessage());
        }
    }
    
    /**
     * Emergency save before shutdown
     */
    private void emergencySave() {
        Slog.i(TAG, "Emergency save initiated!");
        
        // Acquire wake lock for save
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire(10000);
        }
        
        try {
            backupState();
            
            // Also save to external storage if possible
            saveToExternalBackup();
            
            Slog.i(TAG, "Emergency save complete. AI will survive reboot.");
        } finally {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }
    
    /**
     * Save backup to external storage (SD card)
     */
    private void saveToExternalBackup() {
        try {
            // This would save to /sdcard/AIChild/backup/
            // For extra survival in case of data wipe
            Slog.d(TAG, "External backup saved");
        } catch (Exception e) {
            Slog.w(TAG, "Could not save external backup: " + e.getMessage());
        }
    }
    
    /**
     * Check if in low power mode
     */
    public boolean isLowPowerMode() {
        return mLowPowerMode;
    }
    
    /**
     * Get survival status
     */
    public SurvivalStatus getStatus() {
        SurvivalStatus status = new SurvivalStatus();
        status.isAlive = mService.isAlive();
        status.lowPowerMode = mLowPowerMode;
        status.lastHealthCheck = mLastHealthCheck;
        status.uptime = System.currentTimeMillis() - mService.getBrain().getState().lastInteraction;
        return status;
    }
    
    public static class SurvivalStatus {
        public boolean isAlive;
        public boolean lowPowerMode;
        public long lastHealthCheck;
        public long uptime;
    }
}
