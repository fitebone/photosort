package com.example.james.h_photo;

import android.provider.BaseColumns;

public final class TagDB {
    private TagDB(){

    }

    public static class Tags implements BaseColumns{
        public static final String TABLE_NAME = "ImageTagData";
        public static final String COLUMN_PATH = "ImagePath";
        public static final String COLUMN_TAGS = "Tags";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PATH + " TEXT, " +
                COLUMN_TAGS + " TEXT" + ")";
    }
}
