/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Face - The visual avatar of the AI child.
 */

package com.aichild.home;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.Random;

/**
 * AIChildFace draws the AI child's animated avatar.
 * 
 * Features:
 * - Circular glowing body
 * - Animated eyes with blinking
 * - Mouth with expressions
 * - Blush for happy moments
 * - Floating animation
 */
public class AIChildFace extends View {
    
    // Paints
    private Paint mBodyPaint;
    private Paint mGlowPaint;
    private Paint mEyePaint;
    private Paint mPupilPaint;
    private Paint mMouthPaint;
    private Paint mBlushPaint;
    private Paint mHighlightPaint;
    
    // Dimensions
    private float mCenterX, mCenterY;
    private float mBodyRadius;
    private float mEyeRadius;
    private float mPupilRadius;
    
    // Animation state
    private float mFloatOffset = 0;
    private float mBlinkScale = 1f;
    private float mPupilOffsetX = 0;
    private float mPupilOffsetY = 0;
    private float mBlushAlpha = 0;
    private float mMouthScale = 1f;
    private int mMouthType = 0; // 0=neutral, 1=happy, 2=surprised, 3=speaking
    
    // Animations
    private ValueAnimator mFloatAnimator;
    private ValueAnimator mBlinkAnimator;
    
    // Random for variations
    private Random mRandom = new Random();
    
    // Colors
    private int mPrimaryColor = 0xFF8B5CF6;    // Purple
    private int mSecondaryColor = 0xFFA78BFA;  // Light purple
    private int mBlushColor = 0xFFF472B6;      // Pink
    
    public AIChildFace(Context context) {
        super(context);
        init();
    }
    
    public AIChildFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AIChildFace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Body paint with gradient
        mBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBodyPaint.setStyle(Paint.Style.FILL);
        
        // Glow paint
        mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlowPaint.setStyle(Paint.Style.FILL);
        
        // Eye paint (white)
        mEyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEyePaint.setColor(Color.WHITE);
        mEyePaint.setStyle(Paint.Style.FILL);
        
        // Pupil paint (dark)
        mPupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPupilPaint.setColor(0xFF1A1A2E);
        mPupilPaint.setStyle(Paint.Style.FILL);
        
        // Mouth paint
        mMouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMouthPaint.setColor(Color.WHITE);
        mMouthPaint.setStyle(Paint.Style.STROKE);
        mMouthPaint.setStrokeWidth(6);
        mMouthPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Blush paint
        mBlushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBlushPaint.setStyle(Paint.Style.FILL);
        
        // Highlight paint (shine on body)
        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setColor(0x4DFFFFFF);
        mHighlightPaint.setStyle(Paint.Style.FILL);
        
        // Start animations
        startFloatAnimation();
        startBlinkAnimation();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        mCenterX = w / 2f;
        mCenterY = h / 2f;
        mBodyRadius = Math.min(w, h) * 0.35f;
        mEyeRadius = mBodyRadius * 0.15f;
        mPupilRadius = mEyeRadius * 0.5f;
        
        // Update gradient paints
        updateGradients();
    }
    
    private void updateGradients() {
        // Body gradient
        mBodyPaint.setShader(new LinearGradient(
            mCenterX - mBodyRadius, mCenterY - mBodyRadius,
            mCenterX + mBodyRadius, mCenterY + mBodyRadius,
            mSecondaryColor, mPrimaryColor,
            Shader.TileMode.CLAMP
        ));
        
        // Glow gradient
        mGlowPaint.setShader(new RadialGradient(
            mCenterX, mCenterY,
            mBodyRadius * 1.5f,
            0x66_8B5CF6, 0x00_8B5CF6,
            Shader.TileMode.CLAMP
        ));
        
        // Blush gradient
        mBlushPaint.setColor(mBlushColor);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerY = mCenterY + mFloatOffset;
        
        // Draw glow
        canvas.drawCircle(mCenterX, centerY, mBodyRadius * 1.4f, mGlowPaint);
        
        // Draw body
        canvas.drawCircle(mCenterX, centerY, mBodyRadius, mBodyPaint);
        
        // Draw highlight (shine effect)
        float highlightX = mCenterX - mBodyRadius * 0.3f;
        float highlightY = centerY - mBodyRadius * 0.3f;
        canvas.drawOval(
            highlightX - mBodyRadius * 0.25f,
            highlightY - mBodyRadius * 0.15f,
            highlightX + mBodyRadius * 0.25f,
            highlightY + mBodyRadius * 0.15f,
            mHighlightPaint
        );
        
        // Draw eyes
        float eyeY = centerY - mBodyRadius * 0.1f;
        float eyeSpacing = mBodyRadius * 0.35f;
        
        // Left eye
        canvas.save();
        canvas.scale(1f, mBlinkScale, mCenterX - eyeSpacing, eyeY);
        canvas.drawCircle(mCenterX - eyeSpacing, eyeY, mEyeRadius, mEyePaint);
        canvas.drawCircle(
            mCenterX - eyeSpacing + mPupilOffsetX,
            eyeY + mPupilOffsetY,
            mPupilRadius,
            mPupilPaint
        );
        // Pupil highlight
        canvas.drawCircle(
            mCenterX - eyeSpacing + mPupilOffsetX + mPupilRadius * 0.3f,
            eyeY + mPupilOffsetY - mPupilRadius * 0.3f,
            mPupilRadius * 0.3f,
            mEyePaint
        );
        canvas.restore();
        
        // Right eye
        canvas.save();
        canvas.scale(1f, mBlinkScale, mCenterX + eyeSpacing, eyeY);
        canvas.drawCircle(mCenterX + eyeSpacing, eyeY, mEyeRadius, mEyePaint);
        canvas.drawCircle(
            mCenterX + eyeSpacing + mPupilOffsetX,
            eyeY + mPupilOffsetY,
            mPupilRadius,
            mPupilPaint
        );
        // Pupil highlight
        canvas.drawCircle(
            mCenterX + eyeSpacing + mPupilOffsetX + mPupilRadius * 0.3f,
            eyeY + mPupilOffsetY - mPupilRadius * 0.3f,
            mPupilRadius * 0.3f,
            mEyePaint
        );
        canvas.restore();
        
        // Draw blush if visible
        if (mBlushAlpha > 0) {
            mBlushPaint.setAlpha((int) (mBlushAlpha * 128));
            float blushY = centerY + mBodyRadius * 0.1f;
            canvas.drawOval(
                mCenterX - eyeSpacing - mBodyRadius * 0.2f,
                blushY - mBodyRadius * 0.08f,
                mCenterX - eyeSpacing + mBodyRadius * 0.2f,
                blushY + mBodyRadius * 0.08f,
                mBlushPaint
            );
            canvas.drawOval(
                mCenterX + eyeSpacing - mBodyRadius * 0.2f,
                blushY - mBodyRadius * 0.08f,
                mCenterX + eyeSpacing + mBodyRadius * 0.2f,
                blushY + mBodyRadius * 0.08f,
                mBlushPaint
            );
        }
        
        // Draw mouth
        float mouthY = centerY + mBodyRadius * 0.3f;
        drawMouth(canvas, mCenterX, mouthY);
    }
    
    private void drawMouth(Canvas canvas, float x, float y) {
        float mouthWidth = mBodyRadius * 0.25f * mMouthScale;
        float mouthHeight = mBodyRadius * 0.1f * mMouthScale;
        
        switch (mMouthType) {
            case 0: // Neutral - small smile
                RectF neutralRect = new RectF(
                    x - mouthWidth, y - mouthHeight / 2,
                    x + mouthWidth, y + mouthHeight
                );
                canvas.drawArc(neutralRect, 0, 180, false, mMouthPaint);
                break;
                
            case 1: // Happy - big smile
                mouthWidth *= 1.3f;
                mouthHeight *= 1.5f;
                RectF happyRect = new RectF(
                    x - mouthWidth, y - mouthHeight / 2,
                    x + mouthWidth, y + mouthHeight
                );
                canvas.drawArc(happyRect, 0, 180, false, mMouthPaint);
                break;
                
            case 2: // Surprised - O shape
                mMouthPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(x, y, mouthHeight, mMouthPaint);
                break;
                
            case 3: // Speaking - animated O
                float speakSize = mouthHeight * (0.8f + (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.4f);
                mMouthPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(x, y, speakSize, mMouthPaint);
                break;
        }
        
        mMouthPaint.setStyle(Paint.Style.STROKE);
    }
    
    private void startFloatAnimation() {
        mFloatAnimator = ValueAnimator.ofFloat(0, -20, 0);
        mFloatAnimator.setDuration(4000);
        mFloatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mFloatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mFloatAnimator.addUpdateListener(animation -> {
            mFloatOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
        mFloatAnimator.start();
    }
    
    private void startBlinkAnimation() {
        mBlinkAnimator = ValueAnimator.ofFloat(1f, 0.1f, 1f);
        mBlinkAnimator.setDuration(200);
        mBlinkAnimator.setStartDelay(3000 + mRandom.nextInt(2000));
        mBlinkAnimator.addUpdateListener(animation -> {
            mBlinkScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        mBlinkAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBlinkAnimator.setStartDelay(3000 + mRandom.nextInt(2000));
                mBlinkAnimator.start();
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mBlinkAnimator.start();
    }
    
    public void onTouchStart() {
        // Scale down slightly
        animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
    }
    
    public void onTouchEnd(String touchType) {
        // Bounce back
        animate().scaleX(1f).scaleY(1f)
            .setInterpolator(new OvershootInterpolator())
            .setDuration(300).start();
    }
    
    public void showReaction(String reaction) {
        switch (reaction) {
            case "surprised":
                mMouthType = 2;
                invalidate();
                postDelayed(() -> {
                    mMouthType = 0;
                    invalidate();
                }, 1000);
                break;
                
            case "happy":
                mMouthType = 1;
                invalidate();
                postDelayed(() -> {
                    mMouthType = 0;
                    invalidate();
                }, 2000);
                break;
                
            case "blush":
                mMouthType = 1;
                ObjectAnimator blush = ObjectAnimator.ofFloat(this, "blushAlpha", 0, 1);
                blush.setDuration(300);
                blush.start();
                postDelayed(() -> {
                    ObjectAnimator unblush = ObjectAnimator.ofFloat(this, "blushAlpha", 1, 0);
                    unblush.setDuration(1000);
                    unblush.start();
                    mMouthType = 0;
                    invalidate();
                }, 2000);
                break;
                
            case "speaking":
                mMouthType = 3;
                invalidate();
                break;
                
            case "listening":
                // Wide eyes
                mPupilOffsetY = -3;
                invalidate();
                break;
        }
    }
    
    public void randomEyeMovement() {
        float newX = (mRandom.nextFloat() - 0.5f) * mPupilRadius;
        float newY = (mRandom.nextFloat() - 0.5f) * mPupilRadius * 0.5f;
        
        ObjectAnimator animX = ObjectAnimator.ofFloat(this, "pupilOffsetX", mPupilOffsetX, newX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(this, "pupilOffsetY", mPupilOffsetY, newY);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.setDuration(200);
        set.start();
    }
    
    // Property setters for ObjectAnimator
    public void setBlushAlpha(float alpha) {
        mBlushAlpha = alpha;
        invalidate();
    }
    
    public void setPupilOffsetX(float offset) {
        mPupilOffsetX = offset;
        invalidate();
    }
    
    public void setPupilOffsetY(float offset) {
        mPupilOffsetY = offset;
        invalidate();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mFloatAnimator != null) mFloatAnimator.cancel();
        if (mBlinkAnimator != null) mBlinkAnimator.cancel();
    }
}
