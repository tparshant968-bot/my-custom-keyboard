package com.example.clipboardkeyboard.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ramayan_clipboard.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tags (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "display_order INTEGER)");

        db.execSQL("CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "content TEXT, " +
                "tag TEXT, " +
                "color INTEGER, " +
                "display_order INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS tags");
        onCreate(db);
    }

    // --- Tag Operations ---
    public long insertTag(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("display_order", System.currentTimeMillis());
        return db.insertWithOnConflict("tags", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<TagItem> getAllTags() {
        List<TagItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, display_order FROM tags ORDER BY display_order ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new TagItem(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateTagName(int tagId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        db.update("tags", cv, "id = ?", new String[]{String.valueOf(tagId)});
    }

    // --- Item Operations ---
    public long insertItem(String title, String content, String tag, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("tag", tag);
        cv.put("color", color);
        cv.put("display_order", System.currentTimeMillis());
        return db.insert("items", null, cv);
    }

    public List<ClipItem> getItemsByTag(String tag) {
        List<ClipItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (tag == null || tag.equals("All List") || tag.isEmpty()) {
            cursor = db.rawQuery("SELECT id, title, content, tag, color, display_order FROM items ORDER BY display_order DESC", null);
        } else {
            cursor = db.rawQuery("SELECT id, title, content, tag, color, display_order FROM items WHERE tag = ? ORDER BY display_order DESC", new String[]{tag});
        }

        if (cursor.moveToFirst()) {
            do {
                list.add(new ClipItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateItem(int id, String title, String content, String tag, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("tag", tag);
        cv.put("color", color);
        db.update("items", cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public void setItemToTop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("display_order", System.currentTimeMillis());
        db.update("items", cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "id = ?", new String[]{String.valueOf(id)});
    }
}
