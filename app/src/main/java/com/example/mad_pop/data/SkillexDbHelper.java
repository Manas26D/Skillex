package com.example.mad_pop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SkillexDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "skillex.db";
    private static final int DB_VERSION = 7;

    public SkillexDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DbContract.Users.TABLE + " ("
                + DbContract.Users.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Users.FULL_NAME + " TEXT NOT NULL,"
                + DbContract.Users.EMAIL + " TEXT NOT NULL UNIQUE,"
                + DbContract.Users.PASSWORD + " TEXT NOT NULL,"
                + DbContract.Users.ROLE + " TEXT NOT NULL CHECK(" + DbContract.Users.ROLE + " IN ('ADMIN','MENTOR','MENTEE')))");

        db.execSQL("CREATE TABLE " + DbContract.Courses.TABLE + " ("
                + DbContract.Courses.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Courses.TITLE + " TEXT NOT NULL,"
                + DbContract.Courses.CATEGORY + " TEXT NOT NULL,"
                + DbContract.Courses.DESCRIPTION + " TEXT NOT NULL,"
                + DbContract.Courses.MENTOR_ID + " INTEGER NOT NULL,"
                + DbContract.Courses.START_DATE + " TEXT NOT NULL,"
                + DbContract.Courses.END_DATE + " TEXT NOT NULL,"
                + DbContract.Courses.IMAGE_URL + " TEXT,"
                + DbContract.Courses.PRICE + " REAL NOT NULL DEFAULT 0,"
                + "FOREIGN KEY(" + DbContract.Courses.MENTOR_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "))");

        db.execSQL("CREATE TABLE " + DbContract.Enrollments.TABLE + " ("
                + DbContract.Enrollments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Enrollments.USER_ID + " INTEGER NOT NULL,"
                + DbContract.Enrollments.COURSE_ID + " INTEGER NOT NULL,"
                + DbContract.Enrollments.ENROLLED_AT + " INTEGER NOT NULL,"
                + "UNIQUE(" + DbContract.Enrollments.USER_ID + "," + DbContract.Enrollments.COURSE_ID + "),"
                + "FOREIGN KEY(" + DbContract.Enrollments.USER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "),"
                + "FOREIGN KEY(" + DbContract.Enrollments.COURSE_ID + ") REFERENCES " + DbContract.Courses.TABLE + "(" + DbContract.Courses.ID + "))");

        db.execSQL("CREATE TABLE " + DbContract.Messages.TABLE + " ("
                + DbContract.Messages.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Messages.SENDER_ID + " INTEGER NOT NULL,"
                + DbContract.Messages.RECEIVER_ID + " INTEGER NOT NULL,"
                + DbContract.Messages.BODY + " TEXT NOT NULL,"
                + DbContract.Messages.CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Messages.SENDER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "),"
                + "FOREIGN KEY(" + DbContract.Messages.RECEIVER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "))");

        db.execSQL("CREATE TABLE " + DbContract.Payments.TABLE + " ("
                + DbContract.Payments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Payments.USER_ID + " INTEGER NOT NULL,"
                + DbContract.Payments.COURSE_ID + " INTEGER NOT NULL,"
                + DbContract.Payments.AMOUNT + " REAL NOT NULL,"
                + DbContract.Payments.METHOD + " TEXT NOT NULL,"
                + DbContract.Payments.CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Payments.USER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "),"
                + "FOREIGN KEY(" + DbContract.Payments.COURSE_ID + ") REFERENCES " + DbContract.Courses.TABLE + "(" + DbContract.Courses.ID + "))");

        seed(db);
    }

    private void seed(SQLiteDatabase db) {
        db.execSQL("DELETE FROM users WHERE role='ADMIN' AND email<>'admin'");
        db.execSQL("INSERT OR IGNORE INTO users(full_name,email,password,role) VALUES "
                + "('Admin User','admin','admin','ADMIN'),"
                + "('Aarav Mentor','mentor@skillex.com','mentor123','MENTOR'),"
                + "('Riya Mentor','riya@skillex.com','mentor123','MENTOR'),"
                + "('Maya Mentee','mentee@skillex.com','mentee123','MENTEE')");
        db.execSQL("UPDATE users SET full_name='Admin User',password='admin' WHERE role='ADMIN' AND email='admin'");

        db.execSQL("INSERT INTO courses(title,category,description,mentor_id,start_date,end_date,image_url,price) "
                + "SELECT 'Android Career Starter','Android','Roadmap for Java Android development.',"
                + "(SELECT id FROM users WHERE email='mentor@skillex.com'),'2026-05-01','2026-07-31','demo_android',2499 "
                + "WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title='Android Career Starter')");

        db.execSQL("INSERT INTO courses(title,category,description,mentor_id,start_date,end_date,image_url,price) "
                + "SELECT 'SQL Mastery for Apps','Database','Practical SQL patterns for mobile features.',"
                + "(SELECT id FROM users WHERE email='mentor@skillex.com'),'2026-06-10','2026-08-10','demo_sql',1999 "
                + "WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title='SQL Mastery for Apps')");

        db.execSQL("INSERT INTO courses(title,category,description,mentor_id,start_date,end_date,image_url,price) "
                + "SELECT 'Demo: Intro to UI/UX','Design','Basics of user interface and experience design.',"
                + "(SELECT id FROM users WHERE email='riya@skillex.com'),'2026-05-15','2026-06-30','demo_uiux',0 "
                + "WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title='Demo: Intro to UI/UX')");

        db.execSQL("INSERT INTO courses(title,category,description,mentor_id,start_date,end_date,image_url,price) "
                + "SELECT 'Demo: Python for Data Science','Data Science','Practical Python for analytics and ML.',"
                + "(SELECT id FROM users WHERE email='riya@skillex.com'),'2026-07-01','2026-09-01','demo_python',0 "
                + "WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title='Demo: Python for Data Science')");

        long now = System.currentTimeMillis();
        db.execSQL("INSERT OR IGNORE INTO enrollments(user_id,course_id,enrolled_at) "
                + "SELECT (SELECT id FROM users WHERE email='mentee@skillex.com'),"
                + "(SELECT id FROM courses WHERE title='Android Career Starter')," + now);

        db.execSQL("INSERT INTO messages(sender_id,receiver_id,body,created_at) "
                + "SELECT (SELECT id FROM users WHERE email='mentee@skillex.com'),"
                + "(SELECT id FROM users WHERE email='mentor@skillex.com'),"
                + "'Hi mentor, I am interested in Android Career Starter.'," + now + " "
                + "WHERE NOT EXISTS (SELECT 1 FROM messages)");

        db.execSQL("INSERT INTO messages(sender_id,receiver_id,body,created_at) "
                + "SELECT (SELECT id FROM users WHERE email='mentor@skillex.com'),"
                + "(SELECT id FROM users WHERE email='mentee@skillex.com'),"
                + "'Great, I can help you set up a weekly learning plan.'," + (now + 1) + " "
                + "WHERE (SELECT COUNT(*) FROM messages)=1");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + DbContract.Courses.TABLE + " ADD COLUMN " + DbContract.Courses.START_DATE + " TEXT NOT NULL DEFAULT '2026-01-01'");
            db.execSQL("ALTER TABLE " + DbContract.Courses.TABLE + " ADD COLUMN " + DbContract.Courses.END_DATE + " TEXT NOT NULL DEFAULT '2026-02-01'");
            db.execSQL("ALTER TABLE " + DbContract.Courses.TABLE + " ADD COLUMN " + DbContract.Courses.IMAGE_URL + " TEXT");
        }

        if (oldVersion < 4) {
            db.execSQL("PRAGMA foreign_keys=OFF");
            db.execSQL("CREATE TABLE users_new ("
                    + DbContract.Users.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DbContract.Users.FULL_NAME + " TEXT NOT NULL,"
                    + DbContract.Users.EMAIL + " TEXT NOT NULL UNIQUE,"
                    + DbContract.Users.PASSWORD + " TEXT NOT NULL,"
                    + DbContract.Users.ROLE + " TEXT NOT NULL CHECK(" + DbContract.Users.ROLE + " IN ('ADMIN','MENTOR','MENTEE')))");
            db.execSQL("INSERT INTO users_new(" + DbContract.Users.ID + "," + DbContract.Users.FULL_NAME + "," + DbContract.Users.EMAIL + "," + DbContract.Users.PASSWORD + "," + DbContract.Users.ROLE + ") "
                    + "SELECT " + DbContract.Users.ID + "," + DbContract.Users.FULL_NAME + "," + DbContract.Users.EMAIL + "," + DbContract.Users.PASSWORD + ","
                    + "CASE WHEN " + DbContract.Users.ROLE + " IN ('ADMIN','MENTOR','MENTEE') THEN " + DbContract.Users.ROLE + " ELSE 'MENTOR' END"
                    + " FROM " + DbContract.Users.TABLE);
            db.execSQL("DROP TABLE " + DbContract.Users.TABLE);
            db.execSQL("ALTER TABLE users_new RENAME TO " + DbContract.Users.TABLE);
            db.execSQL("PRAGMA foreign_keys=ON");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DbContract.Enrollments.TABLE + " ("
                    + DbContract.Enrollments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DbContract.Enrollments.USER_ID + " INTEGER NOT NULL,"
                    + DbContract.Enrollments.COURSE_ID + " INTEGER NOT NULL,"
                    + DbContract.Enrollments.ENROLLED_AT + " INTEGER NOT NULL,"
                    + "UNIQUE(" + DbContract.Enrollments.USER_ID + "," + DbContract.Enrollments.COURSE_ID + "),"
                    + "FOREIGN KEY(" + DbContract.Enrollments.USER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "),"
                    + "FOREIGN KEY(" + DbContract.Enrollments.COURSE_ID + ") REFERENCES " + DbContract.Courses.TABLE + "(" + DbContract.Courses.ID + "))");
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + DbContract.Courses.TABLE + " ADD COLUMN " + DbContract.Courses.PRICE + " REAL NOT NULL DEFAULT 0");
        }

        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DbContract.Payments.TABLE + " ("
                    + DbContract.Payments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DbContract.Payments.USER_ID + " INTEGER NOT NULL,"
                    + DbContract.Payments.COURSE_ID + " INTEGER NOT NULL,"
                    + DbContract.Payments.AMOUNT + " REAL NOT NULL,"
                    + DbContract.Payments.METHOD + " TEXT NOT NULL,"
                    + DbContract.Payments.CREATED_AT + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + DbContract.Payments.USER_ID + ") REFERENCES " + DbContract.Users.TABLE + "(" + DbContract.Users.ID + "),"
                    + "FOREIGN KEY(" + DbContract.Payments.COURSE_ID + ") REFERENCES " + DbContract.Courses.TABLE + "(" + DbContract.Courses.ID + "))");
        }

        seed(db);
    }
}
