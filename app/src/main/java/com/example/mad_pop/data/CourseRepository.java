package com.example.mad_pop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mad_pop.model.Course;
import com.example.mad_pop.model.MentorAnalytics;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {
    private final SkillexDbHelper dbHelper;

    public CourseRepository(Context context) {
        dbHelper = new SkillexDbHelper(context);
    }

    public List<Course> getAllCourses() {
        return findCourses(null, null);
    }

    public List<Course> getCoursesForMentor(long mentorId) {
        return findCourses("c.mentor_id=?", new String[]{String.valueOf(mentorId)});
    }

    public List<Course> searchCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findCourses(null, null);
        }
        String where = "c.title LIKE ? OR c.category LIKE ?";
        String wildcard = "%" + query.trim() + "%";
        return findCourses(where, new String[]{wildcard, wildcard});
    }

    public boolean addCourse(String title, String category, String description, long mentorId) {
        return addCourse(title, category, description, mentorId, "2026-01-01", "2026-02-01", null, 0);
    }

    public boolean addCourse(String title, String category, String description, long mentorId, String startDate, String endDate, String imageUrl) {
        return addCourse(title, category, description, mentorId, startDate, endDate, imageUrl, 0);
    }

    public boolean addCourse(String title, String category, String description, long mentorId, String startDate, String endDate, String imageUrl, double price) {
        if (!isValidCourseDateRange(startDate, endDate)) {
            return false;
        }
        if (!isValidCoursePrice(price)) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Courses.TITLE, title);
        values.put(DbContract.Courses.CATEGORY, category);
        values.put(DbContract.Courses.DESCRIPTION, description);
        values.put(DbContract.Courses.MENTOR_ID, mentorId);
        values.put(DbContract.Courses.START_DATE, startDate);
        values.put(DbContract.Courses.END_DATE, endDate);
        values.put(DbContract.Courses.IMAGE_URL, imageUrl);
        values.put(DbContract.Courses.PRICE, price);
        return db.insert(DbContract.Courses.TABLE, null, values) > 0;
    }

    public boolean updateCourse(long courseId, String title, String category, String description, String startDate, String endDate, String imageUrl) {
        return updateCourse(courseId, title, category, description, startDate, endDate, imageUrl, 0);
    }

    public boolean updateCourse(long courseId, String title, String category, String description, String startDate, String endDate, String imageUrl, double price) {
        if (!isValidCourseDateRange(startDate, endDate)) {
            return false;
        }
        if (!isValidCoursePrice(price)) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Courses.TITLE, title);
        values.put(DbContract.Courses.CATEGORY, category);
        values.put(DbContract.Courses.DESCRIPTION, description);
        values.put(DbContract.Courses.START_DATE, startDate);
        values.put(DbContract.Courses.END_DATE, endDate);
        values.put(DbContract.Courses.IMAGE_URL, imageUrl);
        values.put(DbContract.Courses.PRICE, price);
        return db.update(DbContract.Courses.TABLE, values, DbContract.Courses.ID + "=?", new String[]{String.valueOf(courseId)}) > 0;
    }

    public boolean deleteCourse(long courseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DbContract.Enrollments.TABLE, DbContract.Enrollments.COURSE_ID + "=?", new String[]{String.valueOf(courseId)});
            int deleted = db.delete(DbContract.Courses.TABLE, DbContract.Courses.ID + "=?", new String[]{String.valueOf(courseId)});
            db.setTransactionSuccessful();
            return deleted > 0;
        } finally {
            db.endTransaction();
        }
    }

    public Course getCourseById(long courseId) {
        List<Course> items = findCourses("c.id=?", new String[]{String.valueOf(courseId)});
        return items.isEmpty() ? null : items.get(0);
    }

    public boolean enrollInCourse(long userId, long courseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Enrollments.USER_ID, userId);
        values.put(DbContract.Enrollments.COURSE_ID, courseId);
        values.put(DbContract.Enrollments.ENROLLED_AT, System.currentTimeMillis());
        return db.insertWithOnConflict(DbContract.Enrollments.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE) > 0;
    }

    public boolean enrollInCourseWithPayment(long userId, long courseId, String paymentMethod) {
        Course course = getCourseById(courseId);
        if (course == null) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues enrollmentValues = new ContentValues();
            enrollmentValues.put(DbContract.Enrollments.USER_ID, userId);
            enrollmentValues.put(DbContract.Enrollments.COURSE_ID, courseId);
            enrollmentValues.put(DbContract.Enrollments.ENROLLED_AT, System.currentTimeMillis());

            long enrollmentId = db.insertWithOnConflict(
                    DbContract.Enrollments.TABLE,
                    null,
                    enrollmentValues,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
            if (enrollmentId <= 0) {
                return false;
            }

            ContentValues paymentValues = new ContentValues();
            paymentValues.put(DbContract.Payments.USER_ID, userId);
            paymentValues.put(DbContract.Payments.COURSE_ID, courseId);
            paymentValues.put(DbContract.Payments.AMOUNT, course.getPrice());
            paymentValues.put(DbContract.Payments.METHOD, paymentMethod == null ? "UNKNOWN" : paymentMethod);
            paymentValues.put(DbContract.Payments.CREATED_AT, System.currentTimeMillis());
            if (db.insert(DbContract.Payments.TABLE, null, paymentValues) <= 0) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean isUserEnrolled(long userId, long courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DbContract.Enrollments.TABLE + " WHERE "
                        + DbContract.Enrollments.USER_ID + "=? AND " + DbContract.Enrollments.COURSE_ID + "=? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(courseId)}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public int getEnrollmentCountForCourse(long courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DbContract.Enrollments.TABLE + " WHERE " + DbContract.Enrollments.COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public double getTotalEarningsForMentor(long mentorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(p." + DbContract.Payments.AMOUNT + "),0) FROM " + DbContract.Payments.TABLE + " p "
                        + "JOIN " + DbContract.Courses.TABLE + " c ON p." + DbContract.Payments.COURSE_ID + "=c." + DbContract.Courses.ID + " "
                        + "WHERE c." + DbContract.Courses.MENTOR_ID + "=?",
                new String[]{String.valueOf(mentorId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public List<Course> getEnrolledCoursesForMentee(long userId) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT c.id,c.title,c.category,c.description,c.mentor_id,u.full_name,c.start_date,c.end_date,c.image_url,c.price "
                        + "FROM " + DbContract.Enrollments.TABLE + " e "
                        + "JOIN " + DbContract.Courses.TABLE + " c ON e." + DbContract.Enrollments.COURSE_ID + "=c." + DbContract.Courses.ID + " "
                        + "JOIN " + DbContract.Users.TABLE + " u ON c." + DbContract.Courses.MENTOR_ID + "=u." + DbContract.Users.ID + " "
                        + "WHERE e." + DbContract.Enrollments.USER_ID + "=? "
                        + "ORDER BY e." + DbContract.Enrollments.ENROLLED_AT + " DESC",
                new String[]{String.valueOf(userId)}
        );
        try {
            while (cursor.moveToNext()) {
                courses.add(new Course(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getDouble(9)
                ));
            }
        } finally {
            cursor.close();
        }
        return courses;
    }

    public MentorAnalytics getMentorAnalytics(long mentorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int totalCourses = 0;
        Cursor coursesCursor = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Courses.TABLE + " WHERE " + DbContract.Courses.MENTOR_ID + "=?", new String[]{String.valueOf(mentorId)});
        try {
            if (coursesCursor.moveToFirst()) {
                totalCourses = coursesCursor.getInt(0);
            }
        } finally {
            coursesCursor.close();
        }

        int totalEnrollments = 0;
        Cursor enrollCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DbContract.Enrollments.TABLE + " e JOIN " + DbContract.Courses.TABLE + " c"
                        + " ON e." + DbContract.Enrollments.COURSE_ID + "=c." + DbContract.Courses.ID
                        + " WHERE c." + DbContract.Courses.MENTOR_ID + "=?",
                new String[]{String.valueOf(mentorId)}
        );
        try {
            if (enrollCursor.moveToFirst()) {
                totalEnrollments = enrollCursor.getInt(0);
            }
        } finally {
            enrollCursor.close();
        }

        String topCourseTitle = "No enrollments yet";
        int topCourseEnrollments = 0;
        Cursor topCursor = db.rawQuery(
                "SELECT c." + DbContract.Courses.TITLE + ", COUNT(e." + DbContract.Enrollments.ID + ") AS cnt"
                        + " FROM " + DbContract.Courses.TABLE + " c LEFT JOIN " + DbContract.Enrollments.TABLE + " e"
                        + " ON c." + DbContract.Courses.ID + "=e." + DbContract.Enrollments.COURSE_ID
                        + " WHERE c." + DbContract.Courses.MENTOR_ID + "=?"
                        + " GROUP BY c." + DbContract.Courses.ID
                        + " ORDER BY cnt DESC, c." + DbContract.Courses.ID + " DESC LIMIT 1",
                new String[]{String.valueOf(mentorId)}
        );
        try {
            if (topCursor.moveToFirst()) {
                topCourseTitle = topCursor.getString(0);
                topCourseEnrollments = topCursor.getInt(1);
            }
        } finally {
            topCursor.close();
        }

        StringBuilder studentSummary = new StringBuilder();
        Cursor studentCursor = db.rawQuery(
                "SELECT c." + DbContract.Courses.TITLE + ", GROUP_CONCAT(DISTINCT u." + DbContract.Users.FULL_NAME + ") "
                        + "FROM " + DbContract.Courses.TABLE + " c "
                        + "LEFT JOIN " + DbContract.Enrollments.TABLE + " e ON c." + DbContract.Courses.ID + "=e." + DbContract.Enrollments.COURSE_ID + " "
                        + "LEFT JOIN " + DbContract.Users.TABLE + " u ON u." + DbContract.Users.ID + "=e." + DbContract.Enrollments.USER_ID + " "
                        + "WHERE c." + DbContract.Courses.MENTOR_ID + "=? "
                        + "GROUP BY c." + DbContract.Courses.ID + " "
                        + "ORDER BY c." + DbContract.Courses.ID + " DESC",
                new String[]{String.valueOf(mentorId)}
        );
        try {
            while (studentCursor.moveToNext()) {
                String title = studentCursor.getString(0);
                String students = studentCursor.getString(1);
                if (studentSummary.length() > 0) {
                    studentSummary.append("\n");
                }
                if (students == null || students.trim().isEmpty()) {
                    studentSummary.append(title).append(": No students enrolled");
                } else {
                    studentSummary.append(title).append(": ").append(students);
                }
            }
        } finally {
            studentCursor.close();
        }

        if (studentSummary.length() == 0) {
            studentSummary.append("No courses yet");
        }

        return new MentorAnalytics(totalCourses, totalEnrollments, topCourseTitle, topCourseEnrollments, studentSummary.toString());
    }

    public boolean isValidCourseDateRange(String startDate, String endDate) {
        if (startDate == null || endDate == null || startDate.trim().isEmpty() || endDate.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return !end.isBefore(start);
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    public boolean isValidCoursePrice(double price) {
        return price >= 0;
    }

    private List<Course> findCourses(String where, String[] args) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.id,c.title,c.category,c.description,c.mentor_id,u.full_name,c.start_date,c.end_date,c.image_url,c.price "
                + "FROM courses c JOIN users u ON c.mentor_id=u.id";
        if (where != null) {
            sql += " WHERE " + where;
        }
        sql += " ORDER BY c.id DESC";

        Cursor cursor = db.rawQuery(sql, args);
        try {
            while (cursor.moveToNext()) {
                courses.add(new Course(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getDouble(9)
                ));
            }
        } finally {
            cursor.close();
        }
        return courses;
    }
}

