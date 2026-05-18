package com.example.mad_pop.data;

public final class DbContract {
    private DbContract() {}

    public static final class Users {
        public static final String TABLE = "users";
        public static final String ID = "id";
        public static final String FULL_NAME = "full_name";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String ROLE = "role";
    }

    public static final class Courses {
        public static final String TABLE = "courses";
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String CATEGORY = "category";
        public static final String DESCRIPTION = "description";
        public static final String MENTOR_ID = "mentor_id";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String IMAGE_URL = "image_url";
        public static final String PRICE = "price";
    }

    public static final class Enrollments {
        public static final String TABLE = "enrollments";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String COURSE_ID = "course_id";
        public static final String ENROLLED_AT = "enrolled_at";
    }

    public static final class Messages {
        public static final String TABLE = "messages";
        public static final String ID = "id";
        public static final String SENDER_ID = "sender_id";
        public static final String RECEIVER_ID = "receiver_id";
        public static final String BODY = "body";
        public static final String CREATED_AT = "created_at";
    }

    public static final class Payments {
        public static final String TABLE = "payments";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String COURSE_ID = "course_id";
        public static final String AMOUNT = "amount";
        public static final String METHOD = "method";
        public static final String CREATED_AT = "created_at";
    }
}

