/*
 * Copyright (C) 2024 AI Child OS Project
 *
 * AI Child Database - Persistent memory storage.
 * Stores all AI memories, learning, and state.
 */

package com.android.server.aichild;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Slog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AIChildDatabase provides persistent storage for the AI's memory.
 * 
 * Tables:
 * - metadata: Birth time, settings
 * - vocabulary: All learned words
 * - experiences: Memory of experiences
 * - knowledge: Long-term learned knowledge
 * - app_data: Information about apps
 */
public class AIChildDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AIChildDatabase";
    
    private static final String DATABASE_NAME = "aichild.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    private static final String TABLE_METADATA = "metadata";
    private static final String TABLE_VOCABULARY = "vocabulary";
    private static final String TABLE_EXPERIENCES = "experiences";
    private static final String TABLE_KNOWLEDGE = "knowledge";
    private static final String TABLE_APPS = "apps";
    private static final String TABLE_STATE = "state";
    
    // Metadata keys
    private static final String KEY_BIRTH_TIME = "birth_time";
    private static final String KEY_FIRST_BOOT = "first_boot";
    
    private Context mContext;
    
    public AIChildDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        Slog.i(TAG, "Database initialized");
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Slog.i(TAG, "Creating AI memory database...");
        
        // Metadata table
        db.execSQL("CREATE TABLE " + TABLE_METADATA + " (" +
            "key TEXT PRIMARY KEY," +
            "value TEXT" +
        ")");
        
        // Vocabulary table
        db.execSQL("CREATE TABLE " + TABLE_VOCABULARY + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "word TEXT UNIQUE," +
            "meaning TEXT," +
            "category TEXT," +
            "times_heard INTEGER DEFAULT 1," +
            "first_heard INTEGER," +
            "last_heard INTEGER," +
            "is_taught INTEGER DEFAULT 0" +
        ")");
        
        // Experiences table
        db.execSQL("CREATE TABLE " + TABLE_EXPERIENCES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "type TEXT," +
            "data TEXT," +
            "extra TEXT," +
            "timestamp INTEGER" +
        ")");
        
        // Knowledge table
        db.execSQL("CREATE TABLE " + TABLE_KNOWLEDGE + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "type TEXT," +
            "name TEXT," +
            "friendly_name TEXT," +
            "first_seen INTEGER," +
            "last_seen INTEGER," +
            "times_used INTEGER DEFAULT 0," +
            "importance INTEGER DEFAULT 1," +
            "is_removed INTEGER DEFAULT 0," +
            "removed_time INTEGER," +
            "extra_data TEXT" +
        ")");
        
        // Apps table (for quick app lookups)
        db.execSQL("CREATE TABLE " + TABLE_APPS + " (" +
            "package_name TEXT PRIMARY KEY," +
            "app_name TEXT," +
            "first_seen INTEGER," +
            "last_used INTEGER," +
            "use_count INTEGER DEFAULT 0," +
            "is_installed INTEGER DEFAULT 1" +
        ")");
        
        // State table (current brain state)
        db.execSQL("CREATE TABLE " + TABLE_STATE + " (" +
            "key TEXT PRIMARY KEY," +
            "value TEXT" +
        ")");
        
        // Create indexes for faster queries
        db.execSQL("CREATE INDEX idx_experiences_type ON " + TABLE_EXPERIENCES + "(type)");
        db.execSQL("CREATE INDEX idx_experiences_time ON " + TABLE_EXPERIENCES + "(timestamp)");
        db.execSQL("CREATE INDEX idx_vocabulary_word ON " + TABLE_VOCABULARY + "(word)");
        
        // Mark first boot
        ContentValues cv = new ContentValues();
        cv.put("key", KEY_FIRST_BOOT);
        cv.put("value", "true");
        db.insert(TABLE_METADATA, null, cv);
        
        Slog.i(TAG, "Database created successfully");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Slog.i(TAG, "Upgrading database from v" + oldVersion + " to v" + newVersion);
        // Handle migrations here in future versions
    }
    
    // ==================== METADATA ====================
    
    /**
     * Check if this is first boot (AI birth)
     */
    public boolean isFirstBoot() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_METADATA,
            new String[]{"value"},
            "key = ?",
            new String[]{KEY_BIRTH_TIME},
            null, null, null);
        
        boolean firstBoot = !cursor.moveToFirst();
        cursor.close();
        
        if (firstBoot) {
            Slog.i(TAG, "This is the AI's first boot (birth)");
        }
        
        return firstBoot;
    }
    
    /**
     * Set birth time
     */
    public void setBirthTime(long birthTime) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues cv = new ContentValues();
        cv.put("key", KEY_BIRTH_TIME);
        cv.put("value", String.valueOf(birthTime));
        db.insertWithOnConflict(TABLE_METADATA, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        
        // Also update first boot flag
        cv = new ContentValues();
        cv.put("key", KEY_FIRST_BOOT);
        cv.put("value", "false");
        db.insertWithOnConflict(TABLE_METADATA, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        
        Slog.i(TAG, "Birth time recorded: " + birthTime);
    }
    
    /**
     * Get birth time
     */
    public long getBirthTime() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_METADATA,
            new String[]{"value"},
            "key = ?",
            new String[]{KEY_BIRTH_TIME},
            null, null, null);
        
        long birthTime = System.currentTimeMillis();
        if (cursor.moveToFirst()) {
            birthTime = Long.parseLong(cursor.getString(0));
        }
        cursor.close();
        
        return birthTime;
    }
    
    // ==================== VOCABULARY ====================
    
    /**
     * Save or update a word
     */
    public void saveWord(AIChildBrain.Word word) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues cv = new ContentValues();
        cv.put("word", word.text);
        cv.put("meaning", word.meaning);
        cv.put("category", word.category);
        cv.put("times_heard", word.timesHeard);
        cv.put("first_heard", word.firstHeard);
        cv.put("last_heard", word.lastHeard);
        cv.put("is_taught", word.isTaught ? 1 : 0);
        
        db.insertWithOnConflict(TABLE_VOCABULARY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    /**
     * Load all vocabulary
     */
    public List<AIChildBrain.Word> loadVocabulary() {
        List<AIChildBrain.Word> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_VOCABULARY,
            null, null, null, null, null, "times_heard DESC");
        
        while (cursor.moveToNext()) {
            AIChildBrain.Word word = new AIChildBrain.Word();
            word.text = cursor.getString(cursor.getColumnIndex("word"));
            word.meaning = cursor.getString(cursor.getColumnIndex("meaning"));
            word.category = cursor.getString(cursor.getColumnIndex("category"));
            word.timesHeard = cursor.getInt(cursor.getColumnIndex("times_heard"));
            word.firstHeard = cursor.getLong(cursor.getColumnIndex("first_heard"));
            word.lastHeard = cursor.getLong(cursor.getColumnIndex("last_heard"));
            word.isTaught = cursor.getInt(cursor.getColumnIndex("is_taught")) == 1;
            words.add(word);
        }
        cursor.close();
        
        Slog.d(TAG, "Loaded " + words.size() + " words from memory");
        return words;
    }
    
    // ==================== EXPERIENCES ====================
    
    /**
     * Save an experience
     */
    public void saveExperience(AIChildBrain.Experience exp) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues cv = new ContentValues();
        cv.put("type", exp.type);
        cv.put("data", exp.data);
        cv.put("extra", exp.extra);
        cv.put("timestamp", exp.timestamp);
        
        db.insert(TABLE_EXPERIENCES, null, cv);
        
        // Keep only last 10000 experiences
        db.execSQL("DELETE FROM " + TABLE_EXPERIENCES + 
            " WHERE id NOT IN (SELECT id FROM " + TABLE_EXPERIENCES + 
            " ORDER BY timestamp DESC LIMIT 10000)");
    }
    
    /**
     * Load recent experiences
     */
    public List<AIChildBrain.Experience> loadRecentExperiences(int limit) {
        List<AIChildBrain.Experience> experiences = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_EXPERIENCES,
            null, null, null, null, null,
            "timestamp DESC",
            String.valueOf(limit));
        
        while (cursor.moveToNext()) {
            AIChildBrain.Experience exp = new AIChildBrain.Experience();
            exp.type = cursor.getString(cursor.getColumnIndex("type"));
            exp.data = cursor.getString(cursor.getColumnIndex("data"));
            exp.extra = cursor.getString(cursor.getColumnIndex("extra"));
            exp.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            experiences.add(exp);
        }
        cursor.close();
        
        return experiences;
    }
    
    // ==================== APPS ====================
    
    /**
     * Save app information
     */
    public void saveApp(String packageName, String appName, boolean isInstalled) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues cv = new ContentValues();
        cv.put("package_name", packageName);
        cv.put("app_name", appName);
        cv.put("is_installed", isInstalled ? 1 : 0);
        cv.put("last_used", System.currentTimeMillis());
        
        // Update use count
        db.execSQL("UPDATE " + TABLE_APPS + 
            " SET use_count = use_count + 1 WHERE package_name = ?",
            new String[]{packageName});
        
        // Insert if not exists
        db.insertWithOnConflict(TABLE_APPS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }
    
    /**
     * Get installed apps count
     */
    public int getInstalledAppsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM " + TABLE_APPS + " WHERE is_installed = 1",
            null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    // ==================== STATE ====================
    
    /**
     * Save brain state
     */
    public void saveBrainState(AIChildBrain brain) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            
            AIChildBrain.BrainState state = brain.getState();
            
            // Save state values
            saveStateValue(db, "happiness", String.valueOf(state.happiness));
            saveStateValue(db, "energy", String.valueOf(state.energy));
            saveStateValue(db, "curiosity", String.valueOf(state.curiosity));
            saveStateValue(db, "bonding", String.valueOf(state.bonding));
            saveStateValue(db, "learning", String.valueOf(state.learning));
            
            Slog.d(TAG, "Brain state saved");
        } catch (Exception e) {
            Slog.e(TAG, "Failed to save brain state: " + e.getMessage());
        }
    }
    
    private void saveStateValue(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(TABLE_STATE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    /**
     * Load brain state
     */
    public void loadBrainState(AIChildBrain brain) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            
            AIChildBrain.BrainState state = brain.getState();
            
            state.happiness = loadStateInt(db, "happiness", 50);
            state.energy = loadStateInt(db, "energy", 100);
            state.curiosity = loadStateInt(db, "curiosity", 50);
            state.bonding = loadStateInt(db, "bonding", 10);
            state.learning = loadStateInt(db, "learning", 0);
            
            Slog.d(TAG, "Brain state loaded");
        } catch (Exception e) {
            Slog.e(TAG, "Failed to load brain state: " + e.getMessage());
        }
    }
    
    private int loadStateInt(SQLiteDatabase db, String key, int defaultValue) {
        Cursor cursor = db.query(TABLE_STATE,
            new String[]{"value"},
            "key = ?",
            new String[]{key},
            null, null, null);
        
        int value = defaultValue;
        if (cursor.moveToFirst()) {
            try {
                value = Integer.parseInt(cursor.getString(0));
            } catch (NumberFormatException e) {
                value = defaultValue;
            }
        }
        cursor.close();
        return value;
    }
    
    /**
     * Save complete state snapshot
     */
    public void saveState(AIChildBrain.BrainState state) {
        SQLiteDatabase db = getWritableDatabase();
        
        saveStateValue(db, "happiness", String.valueOf(state.happiness));
        saveStateValue(db, "energy", String.valueOf(state.energy));
        saveStateValue(db, "curiosity", String.valueOf(state.curiosity));
        saveStateValue(db, "bonding", String.valueOf(state.bonding));
        saveStateValue(db, "learning", String.valueOf(state.learning));
        saveStateValue(db, "last_save", String.valueOf(System.currentTimeMillis()));
        
        Slog.d(TAG, "State snapshot saved");
    }
    
    /**
     * Get total experience count
     */
    public int getTotalExperienceCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EXPERIENCES, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Get vocabulary count
     */
    public int getVocabularyCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VOCABULARY, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
