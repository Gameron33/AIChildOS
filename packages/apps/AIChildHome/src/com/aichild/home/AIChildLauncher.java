/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Launcher - The home screen where the AI child lives.
 * This replaces the default launcher with the AI child's face.
 */

package com.aichild.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AIChildLauncher is the home screen that shows the AI child.
 * 
 * Features:
 * - AI child avatar as the main element
 * - Touch interactions
 * - Voice input
 * - App drawer access
 * - Thought bubbles and reactions
 */
public class AIChildLauncher extends Activity {
    private static final String TAG = "AIChildLauncher";
    
    // Views
    private AIChildFace mChildFace;
    private TextView mThoughtBubble;
    private TextView mAgeDisplay;
    private TextView mStageDisplay;
    private View mAppDrawerButton;
    private View mSpeakButton;
    private View mTeachButton;
    private GridView mAppGrid;
    
    // State
    private boolean mIsAppDrawerOpen = false;
    private String mCurrentThought = "";
    private Handler mHandler;
    private Random mRandom = new Random();
    
    // Speech recognition
    private SpeechRecognizer mSpeechRecognizer;
    private boolean mIsListening = false;
    
    // Touch tracking
    private long mTouchStartTime;
    private float mTouchStartX, mTouchStartY;
    
    // System receiver
    private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if ("com.aichild.ACTION_GREET".equals(action)) {
                boolean isFirstBoot = intent.getBooleanExtra("is_first_boot", false);
                String ageString = intent.getStringExtra("age_string");
                
                if (isFirstBoot) {
                    showThought("...where am I?", 3000);
                    mHandler.postDelayed(() -> {
                        showThought("...papa?", 3000);
                    }, 4000);
                } else {
                    showThought("Papa! You're back! 💕", 3000);
                }
                
                updateDisplay(ageString);
            } else if ("com.aichild.ACTION_UPDATE".equals(action)) {
                updateFromService();
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        
        // Keep screen on and make it immersive
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mHandler = new Handler(Looper.getMainLooper());
        
        // Initialize views
        initViews();
        
        // Initialize speech recognizer
        initSpeechRecognizer();
        
        // Register for AI child broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.aichild.ACTION_GREET");
        filter.addAction("com.aichild.ACTION_UPDATE");
        registerReceiver(mSystemReceiver, filter);
        
        // Start idle behavior
        startIdleBehavior();
        
        // Initial update
        updateFromService();
    }
    
    private void initViews() {
        mChildFace = findViewById(R.id.child_face);
        mThoughtBubble = findViewById(R.id.thought_bubble);
        mAgeDisplay = findViewById(R.id.age_display);
        mStageDisplay = findViewById(R.id.stage_display);
        mAppDrawerButton = findViewById(R.id.app_drawer_button);
        mSpeakButton = findViewById(R.id.speak_button);
        mTeachButton = findViewById(R.id.teach_button);
        mAppGrid = findViewById(R.id.app_grid);
        
        // Set up touch listener for child
        mChildFace.setOnTouchListener(this::onChildTouch);
        
        // App drawer button
        mAppDrawerButton.setOnClickListener(v -> toggleAppDrawer());
        
        // Speak button
        mSpeakButton.setOnClickListener(v -> startListening());
        
        // Teach button
        mTeachButton.setOnClickListener(v -> openTeachDialog());
        
        // Initially hide app drawer
        mAppGrid.setVisibility(View.GONE);
    }
    
    private boolean onChildTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartTime = System.currentTimeMillis();
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                mChildFace.onTouchStart();
                return true;
                
            case MotionEvent.ACTION_UP:
                long duration = System.currentTimeMillis() - mTouchStartTime;
                float dx = event.getX() - mTouchStartX;
                float dy = event.getY() - mTouchStartY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                String touchType;
                if (duration > 500 && distance < 50) {
                    touchType = "hold";
                } else if (distance > 100) {
                    touchType = "stroke";
                } else {
                    touchType = "tap";
                }
                
                mChildFace.onTouchEnd(touchType);
                sendTouchToService(touchType);
                return true;
        }
        return false;
    }
    
    private void sendTouchToService(String touchType) {
        // Send touch to AI service
        Intent intent = new Intent("com.aichild.ACTION_TOUCH");
        intent.putExtra("touch_type", touchType);
        sendBroadcast(intent);
        
        // Show reaction
        switch (touchType) {
            case "tap":
                showThought(getRandomThought("tap"), 2000);
                mChildFace.showReaction("surprised");
                break;
            case "hold":
                showThought(getRandomThought("hold"), 3000);
                mChildFace.showReaction("happy");
                break;
            case "stroke":
                showThought(getRandomThought("stroke"), 3000);
                mChildFace.showReaction("blush");
                break;
        }
    }
    
    private String getRandomThought(String context) {
        // This would normally come from the AI service
        // For now, use simple responses
        String[][] thoughts = {
            // tap
            {"...!", "?!", "oh!", "papa?"},
            // hold
            {"warm...", "safe~", "♡", "papa..."},
            // stroke
            {"love...", "♡♡", "happy~", "papa! ♡"}
        };
        
        int index = context.equals("tap") ? 0 : context.equals("hold") ? 1 : 2;
        String[] options = thoughts[index];
        return options[mRandom.nextInt(options.length)];
    }
    
    private void showThought(String thought, long duration) {
        mCurrentThought = thought;
        mThoughtBubble.setText(thought);
        mThoughtBubble.setVisibility(View.VISIBLE);
        mThoughtBubble.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        mHandler.postDelayed(() -> {
            mThoughtBubble.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> mThoughtBubble.setVisibility(View.GONE))
                .start();
        }, duration);
    }
    
    private void toggleAppDrawer() {
        mIsAppDrawerOpen = !mIsAppDrawerOpen;
        
        if (mIsAppDrawerOpen) {
            loadApps();
            mAppGrid.setVisibility(View.VISIBLE);
            mChildFace.setVisibility(View.GONE);
        } else {
            mAppGrid.setVisibility(View.GONE);
            mChildFace.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        
        // Set up grid adapter (simplified)
        // In real implementation, use a proper adapter
    }
    
    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = 
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String speech = matches.get(0);
                        onSpeechRecognized(speech);
                    }
                    mIsListening = false;
                }
                
                @Override
                public void onReadyForSpeech(Bundle params) {
                    showThought("listening...", 0);
                }
                
                @Override
                public void onError(int error) {
                    mIsListening = false;
                    showThought("...?", 2000);
                }
                
                @Override
                public void onEndOfSpeech() {}
                @Override
                public void onBeginningOfSpeech() {}
                @Override
                public void onRmsChanged(float rmsdB) {}
                @Override
                public void onBufferReceived(byte[] buffer) {}
                @Override
                public void onPartialResults(Bundle partialResults) {}
                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }
    
    private void startListening() {
        if (mSpeechRecognizer != null && !mIsListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizer.startListening(intent);
            mIsListening = true;
            mChildFace.showReaction("listening");
        }
    }
    
    private void onSpeechRecognized(String speech) {
        // Send to AI service
        Intent intent = new Intent("com.aichild.ACTION_SPEECH");
        intent.putExtra("speech_text", speech);
        sendBroadcast(intent);
        
        // Show reaction
        mChildFace.showReaction("happy");
        showThought("papa said: " + speech, 3000);
    }
    
    private void openTeachDialog() {
        // Open teach dialog
        Intent intent = new Intent(this, TeachActivity.class);
        startActivity(intent);
    }
    
    private void updateFromService() {
        // Query AI service for current state
        // This would use a proper binder in production
    }
    
    private void updateDisplay(String ageString) {
        if (mAgeDisplay != null && ageString != null) {
            mAgeDisplay.setText(ageString);
        }
    }
    
    private void startIdleBehavior() {
        // Random idle thoughts and movements
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsAppDrawerOpen && mRandom.nextFloat() < 0.3f) {
                    // Random idle thought
                    String[] idleThoughts = {"...", "papa?", "~", "hmm...", "♪"};
                    showThought(idleThoughts[mRandom.nextInt(idleThoughts.length)], 2000);
                }
                
                // Random eye movement
                mChildFace.randomEyeMovement();
                
                // Schedule next idle behavior
                mHandler.postDelayed(this, 5000 + mRandom.nextInt(10000));
            }
        }, 5000);
    }
    
    @Override
    public void onBackPressed() {
        if (mIsAppDrawerOpen) {
            toggleAppDrawer();
        }
        // Don't call super - we're the home screen!
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSystemReceiver);
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }
}
