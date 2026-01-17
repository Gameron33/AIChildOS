/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Sensors - System-wide monitoring.
 * Monitors everything happening on the phone.
 */

package com.android.server.aichild;

import android.app.Notification;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Slog;

import java.util.HashMap;
import java.util.Map;

/**
 * AIChildSensors monitors everything happening on the phone.
 * 
 * Monitors:
 * - App usage and switches
 * - Notifications
 * - Location
 * - Time patterns
 * - System events
 */
public class AIChildSensors {
    private static final String TAG = "AIChildSensors";
    
    private Context mContext;
    private AIChildBrain mBrain;
    
    // System managers
    private UsageStatsManager mUsageStatsManager;
    private LocationManager mLocationManager;
    private PackageManager mPackageManager;
    
    // Tracking
    private String mLastApp = null;
    private long mLastAppSwitchTime = 0;
    private Map<String, Long> mAppUsageTime = new HashMap<>();
    
    // Location
    private Location mLastLocation = null;
    private LocationListener mLocationListener;
    
    public AIChildSensors(Context context, AIChildBrain brain) {
        mContext = context;
        mBrain = brain;
        mPackageManager = context.getPackageManager();
        
        Slog.i(TAG, "Sensors initialized");
    }
    
    /**
     * Connect to system services after they're ready
     */
    public void connectToSystemServices() {
        Slog.i(TAG, "Connecting to system services...");
        
        // Usage stats
        mUsageStatsManager = (UsageStatsManager) 
            mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        
        // Location
        mLocationManager = (LocationManager) 
            mContext.getSystemService(Context.LOCATION_SERVICE);
        
        // Start monitoring
        startUsageMonitoring();
        startLocationMonitoring();
        
        Slog.i(TAG, "Connected to system services");
    }
    
    /**
     * Start monitoring app usage
     */
    private void startUsageMonitoring() {
        if (mUsageStatsManager == null) {
            Slog.w(TAG, "UsageStatsManager not available");
            return;
        }
        
        Slog.d(TAG, "Starting usage monitoring");
        // Usage events will be queried periodically
    }
    
    /**
     * Check recent app usage
     */
    public void checkAppUsage() {
        if (mUsageStatsManager == null) return;
        
        try {
            long now = System.currentTimeMillis();
            long startTime = now - (60 * 1000); // Last minute
            
            UsageEvents events = mUsageStatsManager.queryEvents(startTime, now);
            UsageEvents.Event event = new UsageEvents.Event();
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    String packageName = event.getPackageName();
                    
                    if (!packageName.equals(mLastApp)) {
                        onAppSwitch(mLastApp, packageName);
                        mLastApp = packageName;
                    }
                }
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to query usage: " + e.getMessage());
        }
    }
    
    /**
     * Called when user switches apps
     */
    private void onAppSwitch(String fromApp, String toApp) {
        long now = System.currentTimeMillis();
        
        // Calculate time spent in previous app
        if (fromApp != null && mLastAppSwitchTime > 0) {
            long duration = now - mLastAppSwitchTime;
            Long total = mAppUsageTime.get(fromApp);
            mAppUsageTime.put(fromApp, (total == null ? 0 : total) + duration);
            
            // Let brain learn about app usage
            mBrain.addExperience("app_usage", fromApp, String.valueOf(duration / 1000));
        }
        
        mLastAppSwitchTime = now;
        
        // Log the switch
        String toAppName = getAppName(toApp);
        Slog.d(TAG, "App switch: " + fromApp + " -> " + toApp + " (" + toAppName + ")");
        
        // Update brain's knowledge about this app
        mBrain.learnAboutApp(toApp, true);
    }
    
    /**
     * Start location monitoring
     */
    private void startLocationMonitoring() {
        if (mLocationManager == null) {
            Slog.w(TAG, "LocationManager not available");
            return;
        }
        
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                onNewLocation(location);
            }
            
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            
            @Override
            public void onProviderEnabled(String provider) {}
            
            @Override
            public void onProviderDisabled(String provider) {}
        };
        
        try {
            // Request location updates (passive to save battery)
            mLocationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                5 * 60 * 1000,  // 5 minutes
                100,           // 100 meters
                mLocationListener
            );
            
            Slog.d(TAG, "Location monitoring started (passive)");
        } catch (SecurityException e) {
            Slog.w(TAG, "No location permission: " + e.getMessage());
        }
    }
    
    /**
     * Called when location changes
     */
    private void onNewLocation(Location location) {
        if (location == null) return;
        
        // Check if this is a significant move
        if (mLastLocation != null) {
            float distance = mLastLocation.distanceTo(location);
            
            if (distance > 500) { // More than 500 meters
                Slog.d(TAG, "Significant location change: " + distance + "m");
                
                // Let brain learn about movement
                mBrain.addExperience("location", 
                    location.getLatitude() + "," + location.getLongitude(),
                    String.valueOf(distance));
            }
        }
        
        mLastLocation = location;
    }
    
    /**
     * Process notification (called by notification listener)
     */
    public void onNotificationPosted(String packageName, String title, String text) {
        Slog.d(TAG, "Notification from " + packageName + ": " + title);
        
        // Learn about notification
        mBrain.addExperience("notification", packageName, title);
        
        // Update app knowledge
        mBrain.learnAboutApp(packageName, true);
    }
    
    /**
     * Get app name from package
     */
    private String getAppName(String packageName) {
        if (packageName == null) return "Unknown";
        
        try {
            return mPackageManager.getApplicationLabel(
                mPackageManager.getApplicationInfo(packageName, 0)
            ).toString();
        } catch (Exception e) {
            return packageName;
        }
    }
    
    /**
     * Get current location (if available)
     */
    public Location getLastLocation() {
        return mLastLocation;
    }
    
    /**
     * Get app usage statistics
     */
    public Map<String, Long> getAppUsageStats() {
        return new HashMap<>(mAppUsageTime);
    }
    
    /**
     * Get most used app
     */
    public String getMostUsedApp() {
        String mostUsed = null;
        long maxTime = 0;
        
        for (Map.Entry<String, Long> entry : mAppUsageTime.entrySet()) {
            if (entry.getValue() > maxTime) {
                maxTime = entry.getValue();
                mostUsed = entry.getKey();
            }
        }
        
        return mostUsed;
    }
}
