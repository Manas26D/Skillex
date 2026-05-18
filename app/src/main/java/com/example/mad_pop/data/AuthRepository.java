package com.example.mad_pop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mad_pop.model.User;

import java.util.ArrayList;
import java.util.List;

public class AuthRepository {
    private final SkillexDbHelper dbHelper;

    public AuthRepository(Context context) {
        dbHelper = new SkillexDbHelper(context);
    }

    public User login(String email, String password, String role) {
        if ("ADMIN".equals(role)) {
            if (!"admin".equals(email) || !"admin".equals(password)) {
                return null;
            }

            SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
            ensureAdminAccount(writableDb);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DbContract.Users.TABLE,
                null,
                DbContract.Users.EMAIL + "=? AND " + DbContract.Users.PASSWORD + "=? AND " + DbContract.Users.ROLE + "=?",
                new String[]{email, password, role},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return mapUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private void ensureAdminAccount(SQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO " + DbContract.Users.TABLE + "(" + DbContract.Users.FULL_NAME + ","
                + DbContract.Users.EMAIL + "," + DbContract.Users.PASSWORD + "," + DbContract.Users.ROLE + ") VALUES ('Admin User','admin','admin','ADMIN')");
        db.execSQL("UPDATE " + DbContract.Users.TABLE + " SET " + DbContract.Users.PASSWORD + "='admin', "
                + DbContract.Users.FULL_NAME + "='Admin User' WHERE " + DbContract.Users.EMAIL + "='admin' AND " + DbContract.Users.ROLE + "='ADMIN'");
    }

    public boolean register(String fullName, String email, String password, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Users.FULL_NAME, fullName);
        values.put(DbContract.Users.EMAIL, email);
        values.put(DbContract.Users.PASSWORD, password);
        values.put(DbContract.Users.ROLE, role);
        return db.insert(DbContract.Users.TABLE, null, values) > 0;
    }

    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.Users.TABLE, null, DbContract.Users.ROLE + "=?", new String[]{role}, null, null, DbContract.Users.FULL_NAME + " ASC");
        try {
            while (cursor.moveToNext()) {
                users.add(mapUser(cursor));
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.Users.TABLE, null, null, null, null, null, DbContract.Users.ROLE + " ASC, " + DbContract.Users.FULL_NAME + " ASC");
        try {
            while (cursor.moveToNext()) {
                users.add(mapUser(cursor));
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public boolean deleteUserById(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DbContract.Enrollments.TABLE, DbContract.Enrollments.USER_ID + "=?", new String[]{String.valueOf(userId)});
            db.delete(DbContract.Enrollments.TABLE,
                    DbContract.Enrollments.COURSE_ID + " IN (SELECT " + DbContract.Courses.ID + " FROM " + DbContract.Courses.TABLE + " WHERE " + DbContract.Courses.MENTOR_ID + "=?)",
                    new String[]{String.valueOf(userId)});
            db.delete(DbContract.Messages.TABLE, DbContract.Messages.SENDER_ID + "=? OR " + DbContract.Messages.RECEIVER_ID + "=?", new String[]{String.valueOf(userId), String.valueOf(userId)});
            db.delete(DbContract.Courses.TABLE, DbContract.Courses.MENTOR_ID + "=?", new String[]{String.valueOf(userId)});
            int deleted = db.delete(DbContract.Users.TABLE, DbContract.Users.ID + "=?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
            return deleted > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateUser(long userId, String fullName, String email, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Users.FULL_NAME, fullName);
        values.put(DbContract.Users.EMAIL, email);
        values.put(DbContract.Users.ROLE, role);
        return db.update(DbContract.Users.TABLE, values, DbContract.Users.ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DbContract.Users.TABLE,
                null,
                DbContract.Users.ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return mapUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public boolean updateProfile(long userId, String fullName, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Users.FULL_NAME, fullName);
        values.put(DbContract.Users.EMAIL, email);
        return db.update(DbContract.Users.TABLE, values, DbContract.Users.ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public String exportAllDataForMentor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();

        sb.append("USERS\n");
        Cursor users = db.rawQuery("SELECT id,full_name,email,role FROM users ORDER BY id", null);
        try {
            while (users.moveToNext()) {
                sb.append("#").append(users.getLong(0)).append(" ")
                        .append(users.getString(1)).append(" | ")
                        .append(users.getString(2)).append(" | ")
                        .append(users.getString(3)).append("\n");
            }
        } finally {
            users.close();
        }

        sb.append("\nCOURSES\n");
        Cursor courses = db.rawQuery("SELECT c.id,c.title,c.category,u.full_name,c.start_date,c.end_date,c.price FROM courses c JOIN users u ON c.mentor_id=u.id ORDER BY c.id", null);
        try {
            while (courses.moveToNext()) {
                sb.append("#").append(courses.getLong(0)).append(" ")
                        .append(courses.getString(1)).append(" [")
                        .append(courses.getString(2)).append("] - ")
                        .append(courses.getString(3)).append(" | ")
                        .append(courses.getString(4)).append(" to ")
                        .append(courses.getString(5)).append(" | INR ")
                        .append(courses.getDouble(6)).append("\n");
            }
        } finally {
            courses.close();
        }

        sb.append("\nENROLLMENTS\n");
        Cursor enrollments = db.rawQuery("SELECT e.id,u.full_name,c.title FROM enrollments e JOIN users u ON e.user_id=u.id JOIN courses c ON e.course_id=c.id ORDER BY e.id", null);
        try {
            while (enrollments.moveToNext()) {
                sb.append("#").append(enrollments.getLong(0)).append(" ")
                        .append(enrollments.getString(1)).append(" -> ")
                        .append(enrollments.getString(2)).append("\n");
            }
        } finally {
            enrollments.close();
        }

        sb.append("\nMESSAGES\n");
        Cursor messages = db.rawQuery("SELECT m.id,s.full_name,r.full_name,m.body FROM messages m JOIN users s ON m.sender_id=s.id JOIN users r ON m.receiver_id=r.id ORDER BY m.id", null);
        try {
            while (messages.moveToNext()) {
                sb.append("#").append(messages.getLong(0)).append(" ")
                        .append(messages.getString(1)).append(" -> ")
                        .append(messages.getString(2)).append(": ")
                        .append(messages.getString(3)).append("\n");
            }
        } finally {
            messages.close();
        }

        return sb.toString();
    }

    private User mapUser(Cursor cursor) {
        return new User(
                cursor.getLong(cursor.getColumnIndexOrThrow(DbContract.Users.ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Users.FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Users.EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Users.ROLE))
        );
    }
}

