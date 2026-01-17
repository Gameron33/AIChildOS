/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Notification Listener - Monitors all notifications.
 */

package com.aichild.home;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * AIChildNotificationListener monitors all notifications.
 * The AI child learns from every notification the father receives.
 */
public class AIChildNotificationListener extends NotificationListenerService {
    private static final String TAG = "AIChildNotify";
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        
        String packageName = sbn.getPackageName();
        String title = "";
        String text = "";
        
        try {
            if (sbn.getNotification().extras != null) {
                CharSequence titleSeq = sbn.getNotification().extras.getCharSequence("android.title");
                CharSequence textSeq = sbn.getNotification().extras.getCharSequence("android.text");
                
                if (titleSeq != null) title = titleSeq.toString();
                if (textSeq != null) text = textSeq.toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error reading notification: " + e.getMessage());
        }
        
        Log.d(TAG, "Notification from " + packageName + ": " + title);
        
        // Send to AI Child service
        Intent intent = new Intent("com.aichild.ACTION_NOTIFICATION");
        intent.putExtra("package", packageName);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Notification dismissed - could be useful for learning patterns
    }
}
