package com.finnflare.terminal.db_helper;

import com.finnflare.terminal.db_helper.Tables.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

public class DB_RFID_Helper {
    private String TAG = "FF_TERMINAL_LOG";
    private Context context;

    private SQLiteDatabase db;
    private DB_Scan_Decoder decoder;

    public DB_RFID_Helper(Context context){
        this.context = context;
        db = new DB_Open_Helper(context, DATABASE.DATABASE_NAME, DATABASE.DATABASE_VERSION).getWritableDatabase();
        decoder = new DB_Scan_Decoder();
        Log.v(TAG, "DataBase opened/created");
    }

    public HashMap<String, String> decodeScanResult(String scanResult){
        return decoder.decodeScanResult(scanResult);
    }

    public HashMap<String, String> getGoodInfo(HashMap<String, String> _data){
        HashMap<String, String> goodInfo = new HashMap<>();
        goodInfo.put(COLUMNS.COLUMN_GUID, "");
        goodInfo.put(COLUMNS.COLUMN_NAME, "");
        goodInfo.put(COLUMNS.COLUMN_MODEL, "");
        goodInfo.put(COLUMNS.COLUMN_SIZE, "");
        goodInfo.put(COLUMNS.COLUMN_STATE, "");
        goodInfo.put(COLUMNS.COLUMN_STATE_NAME, "");
        goodInfo.put(COLUMNS.COLUMN_COLOR, "");
        goodInfo.put(
                COLUMNS.COLUMN_GTIN,
                _data.get(COLUMNS.COLUMN_GTIN)
        );
        goodInfo.put(
                COLUMNS.COLUMN_RFID,
                _data.get(COLUMNS.COLUMN_RFID)
        );


        Cursor cursor = db.query(MARKING_CODES.TABLE_NAME,
                new String[]{
                        COLUMNS.COLUMN_GUID,
                        COLUMNS.COLUMN_STATE
                },
                COLUMNS.COLUMN_GTIN + " = ? and " +
                        COLUMNS.COLUMN_RFID + " = ?",
                new String[]{
                        _data.get(COLUMNS.COLUMN_GTIN),
                        _data.get(COLUMNS.COLUMN_RFID)
                },
                null,
                null,
                null
        );

        if(!cursor.moveToFirst()){
            cursor = db.query(MARKING_CODES.TABLE_NAME,
                    new String[]{
                            COLUMNS.COLUMN_GUID
                    },
                    COLUMNS.COLUMN_GTIN + " = ?",
                    new String[]{
                            _data.get(COLUMNS.COLUMN_GTIN),
                    },
                    null,
                    null,
                    null
            );
        }

        if(cursor.moveToFirst()){
            goodInfo.put(COLUMNS.COLUMN_GUID,
                    cursor.getString(
                            cursor.getColumnIndex(
                                    COLUMNS.COLUMN_GUID
                            )
                    )
            );

            if(cursor.getColumnIndex(COLUMNS.COLUMN_STATE) != -1) {
                goodInfo.put(COLUMNS.COLUMN_STATE,
                        cursor.getString(
                                cursor.getColumnIndex(
                                        COLUMNS.COLUMN_STATE
                                )
                        )
                );
            }

            if(goodInfo.get(COLUMNS.COLUMN_SN).equals("")) {
                cursor = db.rawQuery(
                        "SELECT " + COLUMNS.COLUMN_GTIN + " FROM " +
                                MARKING_CODES.TABLE_NAME + " WHERE " +
                                COLUMNS.COLUMN_GUID + " = '" + goodInfo.get(COLUMNS.COLUMN_GUID) + "' AND " +
                                COLUMNS.COLUMN_STATE + " = '" + goodInfo.get(COLUMNS.COLUMN_STATE) +
                                "' ORDER BY " + COLUMNS.COLUMN_GTIN + " DESC LIMIT 1;",
                        null);

                if(cursor.moveToFirst()){
                    goodInfo.put(COLUMNS.COLUMN_GTIN,
                            cursor.getString(
                                    cursor.getColumnIndex(
                                            COLUMNS.COLUMN_GTIN
                                    )
                            )
                    );
                }
            }

            cursor = db.query(GOODS.TABLE_NAME,
                    null,
                    COLUMNS.COLUMN_GUID + " = ?",
                    new String[]{goodInfo.get(COLUMNS.COLUMN_GUID)},
                    null,
                    null,
                    null
            );
            if(cursor.moveToFirst()) {
                goodInfo.put(
                        COLUMNS.COLUMN_NAME,
                        cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_NAME))
                );
                goodInfo.put(
                        COLUMNS.COLUMN_MODEL,
                        cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_MODEL))
                );
                goodInfo.put(
                        COLUMNS.COLUMN_SIZE,
                        cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_SIZE))
                );
                goodInfo.put(
                        COLUMNS.COLUMN_COLOR,
                        cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_COLOR))
                );

                if(!goodInfo.get(COLUMNS.COLUMN_STATE).equals("")){
                    cursor = db.query(STATES.TABLE_NAME,
                            new String[]{COLUMNS.COLUMN_STATE_NAME},
                            COLUMNS.COLUMN_STATE + " = ?",
                            new String[]{goodInfo.get(COLUMNS.COLUMN_STATE)},
                            null,
                            null,
                            null
                    );
                    if(cursor.moveToFirst()){
                        goodInfo.put(COLUMNS.COLUMN_STATE_NAME,
                                cursor.getString(
                                        cursor.getColumnIndex(
                                                COLUMNS.COLUMN_STATE_NAME
                                        )
                                )
                        );
                    }
                }
            }

        }

        cursor.close();
        return goodInfo;
    }

    public int increaseGoodLeftoversCount(HashMap<String, String> good){
        Cursor cursor = db.query(RFID_LEFTOVERS.TABLE_NAME,
                null,
                COLUMNS.COLUMN_RFID + " = ?",
                new String[]{good.get(COLUMNS.COLUMN_RFID)},
                null,
                null,
                null);

        int result = 0;

        try {
            if (!cursor.moveToFirst()) {
                cursor = db.rawQuery(
                        "SELECT SUM( CASE WHEN " +
                                COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ? and " +
                                COLUMNS.COLUMN_STATE + " = ? " +
                                " THEN " + COLUMNS.COLUMN_QTYIN + " ELSE 0 END) as qtyin, " +
                                " SUM( CASE WHEN " +
                                COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ? and " +
                                COLUMNS.COLUMN_STATE + " = ? " +
                                " THEN " + COLUMNS.COLUMN_QTYOUT + " ELSE 0 END) as qtyout " +
                                " FROM " + RFID_LEFTOVERS.TABLE_NAME
                        ,
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_GTIN),
                                good.get(COLUMNS.COLUMN_STATE),
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_GTIN),
                                good.get(COLUMNS.COLUMN_STATE),
                        });
                cursor.moveToFirst();

                long _qtyin = cursor.getLong(cursor.getColumnIndex("qtyin"));
                long _qtyout = cursor.getLong(cursor.getColumnIndex("qtyout"));

                result = (_qtyout < _qtyin) ? 1 : 2;

                ContentValues content = new ContentValues();
                content.put(COLUMNS.COLUMN_GUID, good.get(COLUMNS.COLUMN_GUID));
                content.put(COLUMNS.COLUMN_GTIN, good.get(COLUMNS.COLUMN_GTIN));
                content.put(COLUMNS.COLUMN_STATE, good.get(COLUMNS.COLUMN_STATE));
                content.put(COLUMNS.COLUMN_RFID, good.get(COLUMNS.COLUMN_RFID));
                content.put(COLUMNS.COLUMN_QTYOUT, Integer.toString(1));
                content.put(COLUMNS.COLUMN_TIME, "");
                int idRow = (int) db.insert(RFID_LEFTOVERS.TABLE_NAME,
                        null,
                        content
                );
            }
        }
        catch(Exception e){
            Log.v(TAG, e.toString());
        }
        cursor.close();
        return result;
    }

    public HashMap<String, Long> getLeftoversCount(){
        HashMap<String, Long> res = new HashMap<>();

        Cursor cursor = db.rawQuery(
                "SELECT SUM( " + COLUMNS.COLUMN_QTYIN + " ) as total," +
                        " SUM( CASE WHEN " + COLUMNS.COLUMN_QTYOUT + " > " + COLUMNS.COLUMN_QTYIN +
                        " THEN " + COLUMNS.COLUMN_QTYOUT + " - " + COLUMNS.COLUMN_QTYIN +" ELSE 0 END)" +
                        " as wrong, " +
                        " SUM( CASE WHEN " + COLUMNS.COLUMN_QTYIN + " <= " + COLUMNS.COLUMN_QTYOUT +
                        " THEN " + COLUMNS.COLUMN_QTYIN + " ELSE " + COLUMNS.COLUMN_QTYOUT + " END)" +
                        " as correct FROM " + RFID_LEFTOVERS.TABLE_NAME
                , null);


        if(cursor.moveToFirst()) {
            res.put("total", cursor.getLong(cursor.getColumnIndex("total")));
            res.put("correct", cursor.getLong(cursor.getColumnIndex("correct")));
            res.put("wrong", cursor.getLong(cursor.getColumnIndex("wrong")));
        }
        cursor.close();

        return res;
    }

    public void closeDB(){
        db.close();
        Log.v(TAG, "DataBase closed");
    }
}
