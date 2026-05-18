package com.example.mad_pop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mad_pop.model.Message;
import com.example.mad_pop.model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {
    private final SkillexDbHelper dbHelper;

    public ChatRepository(Context context) {
        dbHelper = new SkillexDbHelper(context);
    }

    public List<Message> getConversation(long userId, long otherUserId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT m.id,m.sender_id,m.receiver_id,m.body,u.full_name,m.created_at "
                + "FROM messages m JOIN users u ON m.sender_id=u.id "
                + "WHERE (m.sender_id=? AND m.receiver_id=?) OR (m.sender_id=? AND m.receiver_id=?) "
                + "ORDER BY m.created_at ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(userId),
                String.valueOf(otherUserId),
                String.valueOf(otherUserId),
                String.valueOf(userId)
        });
        try {
            while (cursor.moveToNext()) {
                messages.add(new Message(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5)
                ));
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

    public boolean sendMessage(long senderId, long receiverId, String message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Messages.SENDER_ID, senderId);
        values.put(DbContract.Messages.RECEIVER_ID, receiverId);
        values.put(DbContract.Messages.BODY, message);
        values.put(DbContract.Messages.CREATED_AT, System.currentTimeMillis());
        return db.insert(DbContract.Messages.TABLE, null, values) > 0;
    }

    public List<User> getChatPartners(long userId, String expectedRole) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT DISTINCT u.id,u.full_name,u.email,u.role "
                + "FROM users u "
                + "LEFT JOIN messages m ON (m.sender_id=u.id AND m.receiver_id=?) OR (m.receiver_id=u.id AND m.sender_id=?) "
                + "WHERE u.id != ? AND u.role=? "
                + "ORDER BY CASE WHEN m.id IS NULL THEN 1 ELSE 0 END, u.full_name ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(userId), expectedRole});
        try {
            while (cursor.moveToNext()) {
                users.add(new User(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            }
        } finally {
            cursor.close();
        }
        return users;
    }
}

