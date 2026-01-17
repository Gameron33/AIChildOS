/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * Teach Activity - Interface for teaching the AI child.
 */

package com.aichild.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TeachActivity provides an interface for the father to teach the AI child.
 * 
 * Teaching categories:
 * - Words and their meanings
 * - Objects and concepts
 * - Emotions
 * - Custom knowledge
 */
public class TeachActivity extends Activity {
    private static final String TAG = "TeachActivity";
    
    private EditText mWordInput;
    private EditText mMeaningInput;
    private TextView mFeedbackText;
    private Button mTeachButton;
    private Button mCancelButton;
    
    // Teaching categories
    private String mSelectedCategory = "word";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach);
        
        initViews();
    }
    
    private void initViews() {
        mWordInput = findViewById(R.id.word_input);
        mMeaningInput = findViewById(R.id.meaning_input);
        mFeedbackText = findViewById(R.id.feedback_text);
        mTeachButton = findViewById(R.id.teach_button);
        mCancelButton = findViewById(R.id.cancel_button);
        
        // Category buttons
        findViewById(R.id.category_words).setOnClickListener(v -> selectCategory("word"));
        findViewById(R.id.category_objects).setOnClickListener(v -> selectCategory("object"));
        findViewById(R.id.category_emotions).setOnClickListener(v -> selectCategory("emotion"));
        findViewById(R.id.category_custom).setOnClickListener(v -> selectCategory("custom"));
        
        // Teach button
        mTeachButton.setOnClickListener(v -> teachWord());
        
        // Cancel button
        mCancelButton.setOnClickListener(v -> finish());
    }
    
    private void selectCategory(String category) {
        mSelectedCategory = category;
        
        // Update hints based on category
        switch (category) {
            case "word":
                mWordInput.setHint("Enter a word...");
                mMeaningInput.setHint("What does it mean?");
                break;
            case "object":
                mWordInput.setHint("What is it called?");
                mMeaningInput.setHint("Describe it...");
                break;
            case "emotion":
                mWordInput.setHint("Emotion name...");
                mMeaningInput.setHint("When do we feel this?");
                break;
            case "custom":
                mWordInput.setHint("Concept...");
                mMeaningInput.setHint("Explanation...");
                break;
        }
    }
    
    private void teachWord() {
        String word = mWordInput.getText().toString().trim();
        String meaning = mMeaningInput.getText().toString().trim();
        
        if (word.isEmpty()) {
            Toast.makeText(this, "Please enter something to teach!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Send to AI Child service
        Intent intent = new Intent("com.aichild.ACTION_TEACH");
        intent.putExtra("category", mSelectedCategory);
        intent.putExtra("word", word);
        intent.putExtra("meaning", meaning);
        sendBroadcast(intent);
        
        // Show feedback
        mFeedbackText.setText("Teaching your child: \"" + word + "\"");
        mFeedbackText.setVisibility(View.VISIBLE);
        
        // Clear inputs
        mWordInput.setText("");
        mMeaningInput.setText("");
        
        // Show success
        Toast.makeText(this, "You taught your child: " + word + "! 🧒📚", Toast.LENGTH_SHORT).show();
        
        // Close after delay
        mFeedbackText.postDelayed(this::finish, 1500);
    }
}
