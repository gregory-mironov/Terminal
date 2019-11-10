package com.finnflare.terminal.db_helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.finnflare.terminal.db_helper.Tables.*;

final class DB_Open_Helper extends SQLiteOpenHelper {

    private Context context;
    public DB_Open_Helper(Context context, String DB_NAME, int DB_VER){
        super(context,  DB_NAME, null, DB_VER);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GOODS.CREATE_TABLE);
        db.execSQL(LEFTOVERS.CREATE_TABLE);
        db.execSQL(MARKING_CODES.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
