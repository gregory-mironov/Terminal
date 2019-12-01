package com.finnflare.terminal.db_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.simple.JSONObject;

import java.util.HashMap;

import com.finnflare.terminal.db_helper.Tables.*;

public final class DB_Helper {
    private String TAG = "FF_TERMINAL_LOG";
    private Context context;

    private SQLiteDatabase db;
    private DB_Scan_Decoder decoder;

    DB_Helper(Context context){
        this.context = context;
        db = new DB_Open_Helper(context, DATABASE.DATABASE_NAME, DATABASE.DATABASE_VERSION).getWritableDatabase();
        decoder = new DB_Scan_Decoder();
        Log.v(TAG, "DataBase opened/created");
    }

    Cursor getBCLeftoversForUpload(){
        return db.rawQuery("SELECT * FROM " + BARCODE_LEFTOVERS.TABLE_NAME + " WHERE " +
                COLUMNS.COLUMN_QTYOUT + " > 0", null);
    }
    Cursor getRFLeftoversForUpload(){
        return db.rawQuery("SELECT * FROM " + RFID_LEFTOVERS.TABLE_NAME + " WHERE " +
                COLUMNS.COLUMN_QTYOUT + " > 0", null);
    }

    void putJSONObjectToTable(JSONObject jObject, String Table) {
        ContentValues content = new ContentValues();
        switch (Table){
            case GOODS.TABLE_NAME: {
                content.put(COLUMNS.COLUMN_GUID, (String) jObject.get(COLUMNS.COLUMN_GUID));
                content.put(COLUMNS.COLUMN_NAME, (String) jObject.get(COLUMNS.COLUMN_NAME));
                content.put(COLUMNS.COLUMN_MODEL,
                        jObject.get(COLUMNS.COLUMN_MODEL) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_MODEL)
                );
                content.put(COLUMNS.COLUMN_COLOR,
                        jObject.get(COLUMNS.COLUMN_COLOR) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_COLOR)
                );
                content.put(COLUMNS.COLUMN_SIZE,
                        jObject.get(COLUMNS.COLUMN_SIZE) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_SIZE)
                );
                int idRow = (int) db.insert(GOODS.TABLE_NAME,
                        null,
                        content
                );

                break;
            }

            case BARCODE_LEFTOVERS.TABLE_NAME:{
                content = new ContentValues();
                content.put(COLUMNS.COLUMN_GUID, (String) jObject.get(COLUMNS.COLUMN_GUID));
                content.put(COLUMNS.COLUMN_GTIN,
                        jObject.get(Tables.COLUMNS.COLUMN_GTIN) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_GTIN)
                );
                content.put(Tables.COLUMNS.COLUMN_SN,
                        jObject.get(COLUMNS.COLUMN_SN) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_SN));
                content.put(COLUMNS.COLUMN_STATE,
                        jObject.get(COLUMNS.COLUMN_STATE) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_STATE));
                content.put(COLUMNS.COLUMN_QTYIN, Long.toString((Long) jObject.get(COLUMNS.COLUMN_QTYIN)));

                int idRow = (int) db.insert(Tables.BARCODE_LEFTOVERS.TABLE_NAME,
                        null,
                        content
                );

                break;
            }

            case RFID_LEFTOVERS.TABLE_NAME:{
                content = new ContentValues();
                content.put(COLUMNS.COLUMN_GUID, (String) jObject.get(COLUMNS.COLUMN_GUID));
                content.put(COLUMNS.COLUMN_GTIN,
                        jObject.get(Tables.COLUMNS.COLUMN_GTIN) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_GTIN)
                );
                content.put(COLUMNS.COLUMN_STATE,
                        jObject.get(COLUMNS.COLUMN_STATE) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_STATE));
                content.put(COLUMNS.COLUMN_RFID,
                        jObject.get(COLUMNS.COLUMN_RFID) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_RFID));
                content.put(COLUMNS.COLUMN_QTYIN, Long.toString((Long) jObject.get(COLUMNS.COLUMN_QTYIN)));

                int idRow = (int) db.insert(Tables.RFID_LEFTOVERS.TABLE_NAME,
                        null,
                        content
                );

                break;
            }

            case MARKING_CODES.TABLE_NAME:{
                content = new ContentValues();

                content.put(Tables.COLUMNS.COLUMN_GUID, (String) jObject.get(Tables.COLUMNS.COLUMN_GUID));
                content.put(Tables.COLUMNS.COLUMN_GTIN,
                        jObject.get(Tables.COLUMNS.COLUMN_GTIN) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_GTIN));
                content.put(Tables.COLUMNS.COLUMN_SN,
                        jObject.get(Tables.COLUMNS.COLUMN_SN) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_SN));
                content.put(COLUMNS.COLUMN_STATE,
                        jObject.get(COLUMNS.COLUMN_STATE) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_STATE));
                content.put(Tables.COLUMNS.COLUMN_RFID,
                        jObject.get(Tables.COLUMNS.COLUMN_RFID) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_RFID)
                );
                int idRow = (int) db.insert(Tables.MARKING_CODES.TABLE_NAME,
                        null,
                        content
                );

                break;
            }

            case STATES.TABLE_NAME:{
                content = new ContentValues();

                content.put(COLUMNS.COLUMN_STATE, (String) jObject.get(COLUMNS.COLUMN_STATE));
                content.put(COLUMNS.COLUMN_STATE_NAME, jObject.get(COLUMNS.COLUMN_STATE_NAME) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_STATE_NAME)
                );
                int idRow = (int) db.insert(Tables.STATES.TABLE_NAME,
                        null,
                        content
                );

                break;
            }
        }
    }

    void clearScanRes(){
        truncateLEFTOVERS();
    }

    void clearDB(){
        db.execSQL(GOODS.TRUNCATE_TABLE);
        db.execSQL(BARCODE_LEFTOVERS.TRUNCATE_TABLE);
        db.execSQL(RFID_LEFTOVERS.TRUNCATE_TABLE);
        db.execSQL(MARKING_CODES.TRUNCATE_TABLE);
        db.execSQL(STATES.TRUNCATE_TABLE);
    }

    void truncateLEFTOVERS(){
        db.execSQL(BARCODE_LEFTOVERS.TRUNCATE_TABLE);
        db.execSQL(RFID_LEFTOVERS.TRUNCATE_TABLE);
        //db.execSQL(LEFTOVERS.TRUNCATE_TABLE);
    }

    void truncateMARCING_CODES(){
        db.execSQL(MARKING_CODES.TRUNCATE_TABLE);
    }

    void truncateSTATES(){db.execSQL(STATES.TRUNCATE_TABLE);}

    void resetScanRes(){
        ContentValues content = new ContentValues();
        content.put(COLUMNS.COLUMN_QTYOUT, 0);
        db.delete(BARCODE_LEFTOVERS.TABLE_NAME,
                COLUMNS.COLUMN_QTYIN + " = 0",
                null);

        db.update(BARCODE_LEFTOVERS.TABLE_NAME,
                content,
                COLUMNS.COLUMN_QTYOUT + " <> 0",
                null);

        db.delete(RFID_LEFTOVERS.TABLE_NAME,
                COLUMNS.COLUMN_QTYIN + " = 0",
                null);
    }

    void closeDB(){
        db.close();
        Log.v(TAG, "DataBase closed");
    }
}
