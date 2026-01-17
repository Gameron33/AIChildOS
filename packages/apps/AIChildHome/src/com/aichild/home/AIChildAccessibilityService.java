/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Accessibility Service - The AI's eyes and hands.
 * 
 * This service allows the AI to:
 * - See everything on screen
 * - Read all text
 * - Control touch input
 * - Automate any app
 * - Interact with games
 */

package com.aichild.home;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * AIChildAccessibilityService provides the AI with full screen access.
 * 
 * This is like giving the AI:
 * - EYES: Can read everything on screen
 * - HANDS: Can tap, swipe, and type
 * - EARS: Can detect notifications and events
 */
public class AIChildAccessibilityService extends AccessibilityService {
    private static final String TAG = "AIChildA11y";
    
    private static AIChildAccessibilityService sInstance;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    
    // Current screen state
    private String mCurrentPackage = "";
    private String mCurrentClass = "";
    private AccessibilityNodeInfo mRootNode;
    
    // Action callbacks
    private GestureCallback mGestureCallback;
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        
        // Configure the service
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            // Get all events
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            
            // Get all feedback types
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
            
            // Important flags
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                         AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                         AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
            
            // Monitor all packages
            info.packageNames = null;
            
            // Faster updates
            info.notificationTimeout = 50;
            
            setServiceInfo(info);
        }
        
        Log.i(TAG, "=== AI CHILD ACCESSIBILITY SERVICE CONNECTED ===");
        Log.i(TAG, "The AI can now see and control the screen!");
        
        // Notify AI Child that we're ready
        broadcastReady();
    }
    
    private void broadcastReady() {
        Intent intent = new Intent("com.aichild.ACCESSIBILITY_READY");
        sendBroadcast(intent);
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        // Update current state
        if (event.getPackageName() != null) {
            mCurrentPackage = event.getPackageName().toString();
        }
        if (event.getClassName() != null) {
            mCurrentClass = event.getClassName().toString();
        }
        
        // Get root node
        mRootNode = getRootInActiveWindow();
        
        // Process different event types
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                onWindowChanged(event);
                break;
                
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                onContentChanged(event);
                break;
                
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                onViewClicked(event);
                break;
                
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                onTextChanged(event);
                break;
                
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                onNotification(event);
                break;
        }
        
        // Send screen update to AI
        broadcastScreenUpdate();
    }
    
    private void onWindowChanged(AccessibilityEvent event) {
        Log.d(TAG, "Window changed: " + mCurrentPackage + " / " + mCurrentClass);
        
        Intent intent = new Intent("com.aichild.WINDOW_CHANGED");
        intent.putExtra("package", mCurrentPackage);
        intent.putExtra("class", mCurrentClass);
        sendBroadcast(intent);
    }
    
    private void onContentChanged(AccessibilityEvent event) {
        // Screen content updated - AI might want to read it
    }
    
    private void onViewClicked(AccessibilityEvent event) {
        // User clicked something - AI can learn from this
        CharSequence text = event.getText().isEmpty() ? null : event.getText().get(0);
        if (text != null) {
            Log.d(TAG, "User clicked: " + text);
        }
    }
    
    private void onTextChanged(AccessibilityEvent event) {
        // Text was typed
        CharSequence text = event.getText().isEmpty() ? null : event.getText().get(0);
        if (text != null) {
            Log.d(TAG, "Text changed: " + text);
        }
    }
    
    private void onNotification(AccessibilityEvent event) {
        Log.d(TAG, "Notification from: " + event.getPackageName());
    }
    
    private void broadcastScreenUpdate() {
        Intent intent = new Intent("com.aichild.SCREEN_UPDATE");
        intent.putExtra("package", mCurrentPackage);
        sendBroadcast(intent);
    }
    
    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public void onDestroy() {
        sInstance = null;
        Log.i(TAG, "Accessibility service destroyed");
        super.onDestroy();
    }
    
    // ================== SCREEN READING ==================
    
    /**
     * Get all text on screen.
     */
    public List<ScreenText> getAllScreenText() {
        List<ScreenText> textList = new ArrayList<>();
        
        if (mRootNode != null) {
            extractText(mRootNode, textList, 0);
        }
        
        return textList;
    }
    
    private void extractText(AccessibilityNodeInfo node, List<ScreenText> textList, int depth) {
        if (node == null || depth > 15) return;
        
        // Get text
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        
        if (text != null && text.length() > 0) {
            ScreenText st = new ScreenText();
            st.text = text.toString();
            st.isClickable = node.isClickable();
            node.getBoundsInScreen(st.bounds);
            textList.add(st);
        }
        
        if (desc != null && desc.length() > 0) {
            ScreenText st = new ScreenText();
            st.text = desc.toString();
            st.isDescription = true;
            st.isClickable = node.isClickable();
            node.getBoundsInScreen(st.bounds);
            textList.add(st);
        }
        
        // Process children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                extractText(child, textList, depth + 1);
            }
        }
    }
    
    /**
     * Find element by text.
     */
    public AccessibilityNodeInfo findByText(String text) {
        if (mRootNode == null) return null;
        
        List<AccessibilityNodeInfo> nodes = mRootNode.findAccessibilityNodeInfosByText(text);
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }
        return null;
    }
    
    /**
     * Find element by ID.
     */
    public AccessibilityNodeInfo findById(String viewId) {
        if (mRootNode == null) return null;
        
        List<AccessibilityNodeInfo> nodes = mRootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }
        return null;
    }
    
    // ================== TOUCH CONTROL ==================
    
    /**
     * Tap at a point.
     */
    public boolean tap(int x, int y) {
        Log.d(TAG, "Tapping at: " + x + ", " + y);
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 100);
        
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(stroke)
            .build();
        
        return dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Tap completed");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "Tap cancelled");
            }
        }, null);
    }
    
    /**
     * Tap on an element.
     */
    public boolean tapOnElement(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        
        int x = bounds.centerX();
        int y = bounds.centerY();
        
        return tap(x, y);
    }
    
    /**
     * Click on text.
     */
    public boolean clickOnText(String text) {
        AccessibilityNodeInfo node = findByText(text);
        if (node != null) {
            return tapOnElement(node);
        }
        return false;
    }
    
    /**
     * Long press.
     */
    public boolean longPress(int x, int y, long duration) {
        Log.d(TAG, "Long press at: " + x + ", " + y + " for " + duration + "ms");
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, duration);
        
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(stroke)
            .build();
        
        return dispatchGesture(gesture, null, null);
    }
    
    /**
     * Swipe gesture.
     */
    public boolean swipe(int startX, int startY, int endX, int endY, long duration) {
        Log.d(TAG, "Swiping: (" + startX + "," + startY + ") -> (" + endX + "," + endY + ")");
        
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, duration);
        
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(stroke)
            .build();
        
        return dispatchGesture(gesture, null, null);
    }
    
    /**
     * Scroll up.
     */
    public boolean scrollUp() {
        int centerX = 540; // Assuming 1080p
        return swipe(centerX, 1500, centerX, 500, 300);
    }
    
    /**
     * Scroll down.
     */
    public boolean scrollDown() {
        int centerX = 540;
        return swipe(centerX, 500, centerX, 1500, 300);
    }
    
    // ================== TEXT INPUT ==================
    
    /**
     * Type text into focused field.
     */
    public boolean typeText(String text) {
        AccessibilityNodeInfo focused = findFocusedEditText();
        if (focused != null) {
            // Set text
            android.os.Bundle args = new android.os.Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }
        return false;
    }
    
    /**
     * Find focused text field.
     */
    private AccessibilityNodeInfo findFocusedEditText() {
        if (mRootNode == null) return null;
        return mRootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
    }
    
    /**
     * Clear text in focused field.
     */
    public boolean clearText() {
        return typeText("");
    }
    
    // ================== GLOBAL ACTIONS ==================
    
    /**
     * Press back button.
     */
    public boolean pressBack() {
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }
    
    /**
     * Press home button.
     */
    public boolean pressHome() {
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }
    
    /**
     * Open recent apps.
     */
    public boolean openRecents() {
        return performGlobalAction(GLOBAL_ACTION_RECENTS);
    }
    
    /**
     * Open notifications.
     */
    public boolean openNotifications() {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
    }
    
    /**
     * Take screenshot.
     */
    public boolean takeScreenshot() {
        return performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
    }
    
    // ================== STATIC ACCESS ==================
    
    public static AIChildAccessibilityService getInstance() {
        return sInstance;
    }
    
    public static boolean isRunning() {
        return sInstance != null;
    }
    
    // ================== INNER CLASSES ==================
    
    public static class ScreenText {
        public String text;
        public Rect bounds = new Rect();
        public boolean isClickable;
        public boolean isDescription;
    }
    
    public interface GestureCallback {
        void onCompleted();
        void onCancelled();
    }
}
