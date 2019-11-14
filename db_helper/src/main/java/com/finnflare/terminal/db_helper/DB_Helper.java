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

    public DB_Helper(Context context){
        this.context = context;
        db = new DB_Open_Helper(context, DATABASE.DATABASE_NAME, DATABASE.DATABASE_VERSION).getWritableDatabase();
        decoder = new DB_Scan_Decoder();
        Log.v(TAG, "DataBase opened/created");
    }

    //-----Shop Inventarization

    public HashMap<String, String> getGoodInfo(HashMap<String, String> _data){
        HashMap<String, String> goodInfo = new HashMap<>();
        goodInfo.put(COLUMNS.COLUMN_GUID, "");
        goodInfo.put(COLUMNS.COLUMN_NAME, "");
        goodInfo.put(COLUMNS.COLUMN_MODEL, "");
        goodInfo.put(COLUMNS.COLUMN_SIZE, "");
        goodInfo.put(COLUMNS.COLUMN_COLOR, "");
        goodInfo.put(COLUMNS.COLUMN_STATE, "");
        goodInfo.put(
                COLUMNS.COLUMN_GTIN,
                _data.get(COLUMNS.COLUMN_GTIN)
        );
        goodInfo.put(
                COLUMNS.COLUMN_SN,
                _data.get(COLUMNS.COLUMN_SN)
        );
        goodInfo.put(
                COLUMNS.COLUMN_RFID,
                _data.get(COLUMNS.COLUMN_RFID)
        );

        Cursor cursor = db.query(MARKING_CODES.TABLE_NAME,
                new String[]{
                        COLUMNS.COLUMN_GUID
                },
                COLUMNS.COLUMN_GTIN + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ?  and " +
                        COLUMNS.COLUMN_RFID + " = ?",
                new String[]{
                        _data.get(COLUMNS.COLUMN_GTIN),
                        _data.get(COLUMNS.COLUMN_SN),
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
            }
        }

        if(!goodInfo.get(COLUMNS.COLUMN_GUID).equals("")){
            goodInfo.put(COLUMNS.COLUMN_GTIN, "");
        }

        cursor.close();
        return goodInfo;
    }

    public int increaseGoodLeftoversCount(HashMap<String, String> good){
        Cursor cursor = db.query(LEFTOVERS.TABLE_NAME,
                new String[]{COLUMNS.COLUMN_QTYOUT, COLUMNS.COLUMN_QTYIN},
                COLUMNS.COLUMN_GUID + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ? and " +
                        COLUMNS.COLUMN_RFID + " = ? and " +
                        COLUMNS.COLUMN_GTIN + " = ?",
                new String[]{
                        good.get(COLUMNS.COLUMN_GUID),
                        good.get(COLUMNS.COLUMN_SN),
                        good.get(COLUMNS.COLUMN_RFID),
                        good.get(COLUMNS.COLUMN_GTIN)},
                null,
                null,
                null
        );

        int result = 3;

        if(cursor.moveToFirst()){
            result = 2;

            int _qtyout = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_QTYOUT))
            );
            int _qtyin = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_QTYIN))
            );

            if( (_qtyout == 0) || (good.get(COLUMNS.COLUMN_SN).equals("") &&
                    good.get(COLUMNS.COLUMN_RFID).equals("")) ){

                result = (_qtyout < _qtyin) ? 1 : 2;

                ContentValues content = new ContentValues();
                content.put(
                        COLUMNS.COLUMN_QTYOUT,
                        Integer.toString(_qtyout + 1)
                );

                long idRow = db.update(
                        LEFTOVERS.TABLE_NAME,
                        content,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_RFID + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ?",
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_SN),
                                good.get(COLUMNS.COLUMN_RFID),
                                good.get(COLUMNS.COLUMN_GTIN)});
            }
            else {
                result = 0;
            }
        }

        if(result == 3){
            ContentValues content = new ContentValues();
            content.put(COLUMNS.COLUMN_GUID, good.get(COLUMNS.COLUMN_GUID));
            content.put(COLUMNS.COLUMN_GTIN, good.get(COLUMNS.COLUMN_GTIN));
            content.put(COLUMNS.COLUMN_SN, good.get(COLUMNS.COLUMN_SN));
            content.put(COLUMNS.COLUMN_RFID, good.get(COLUMNS.COLUMN_RFID));
            content.put(COLUMNS.COLUMN_QTYOUT, Integer.toString(1));
            content.put(COLUMNS.COLUMN_TIME, "");
            int idRow = (int) db.insert(LEFTOVERS.TABLE_NAME,
                    null,
                    content
            );
            result = 2;
        }

        cursor.close();
        return result;
    }

    public HashMap<String, Long> getGoodLeftoverCount(String _guid, String _sn){
        Cursor cursor = db.query(LEFTOVERS.TABLE_NAME,
                new String[]{COLUMNS.COLUMN_QTYOUT, COLUMNS.COLUMN_QTYIN},
                COLUMNS.COLUMN_GUID + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ?",
                new String[]{_guid, _sn},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        HashMap<String, Long> goodInfo = new HashMap<>();
        goodInfo.put(
                COLUMNS.COLUMN_QTYIN,
                cursor.getLong(cursor.getColumnIndex(COLUMNS.COLUMN_QTYIN))
        );
        goodInfo.put(
                COLUMNS.COLUMN_QTYOUT,
                cursor.getLong(cursor.getColumnIndex(COLUMNS.COLUMN_QTYOUT))
        );
        cursor.close();
        return goodInfo;
    }

    public void decreaseGoodLeftoverCount(HashMap<String, String> good){
        Cursor cursor = db.query(LEFTOVERS.TABLE_NAME,
                new String[]{COLUMNS.COLUMN_QTYOUT, COLUMNS.COLUMN_QTYIN},
                COLUMNS.COLUMN_GUID + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ? and " +
                        COLUMNS.COLUMN_RFID + " = ? and " +
                        COLUMNS.COLUMN_GTIN + " = ?",
                new String[]{
                        good.get(COLUMNS.COLUMN_GUID),
                        good.get(COLUMNS.COLUMN_SN),
                        good.get(COLUMNS.COLUMN_RFID),
                        good.get(COLUMNS.COLUMN_GTIN)
                },
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){
            int _qtyout = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_QTYOUT))
            );
            int _qtyin = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(COLUMNS.COLUMN_QTYIN))
            );
            if(_qtyout == 1 && _qtyin == 0){
                db.delete(LEFTOVERS.TABLE_NAME,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_RFID + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ?",
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_SN),
                                good.get(COLUMNS.COLUMN_RFID),
                                good.get(COLUMNS.COLUMN_GTIN)
                        });
            }
            else {
                ContentValues content = new ContentValues();
                content.put(
                        COLUMNS.COLUMN_QTYOUT,
                        Integer.toString(_qtyout - 1)
                );
                int idRow = db.update(
                        LEFTOVERS.TABLE_NAME,
                        content,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_RFID + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ?",
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_SN),
                                good.get(COLUMNS.COLUMN_RFID),
                                good.get(COLUMNS.COLUMN_GTIN)
                        });
            }
        }
        cursor.close();
    }

    Cursor getLeftoversForUpload(){
        return db.rawQuery("SELECT * FROM " + LEFTOVERS.TABLE_NAME, null);
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
                        " as correct FROM " + LEFTOVERS.TABLE_NAME
                , null);


        if(cursor.moveToFirst()) {
            res.put("total", cursor.getLong(cursor.getColumnIndex("total")));
            res.put("correct", cursor.getLong(cursor.getColumnIndex("correct")));
            res.put("wrong", cursor.getLong(cursor.getColumnIndex("wrong")));
        }
        cursor.close();

        return res;
    }

    //-----

    public HashMap<String, String> decodeScanResult(String scanResult){
        return decoder.decodeScanResult(scanResult);
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

            case LEFTOVERS.TABLE_NAME:{
                content = new ContentValues();

                content.put(Tables.COLUMNS.COLUMN_GUID, (String) jObject.get(Tables.COLUMNS.COLUMN_GUID));
                content.put(Tables.COLUMNS.COLUMN_GTIN,
                        jObject.get(Tables.COLUMNS.COLUMN_GTIN) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_GUID)
                );
                content.put(Tables.COLUMNS.COLUMN_SN,
                        jObject.get(Tables.COLUMNS.COLUMN_SN) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_SN));
                content.put(Tables.COLUMNS.COLUMN_RFID,
                        jObject.get(Tables.COLUMNS.COLUMN_RFID) == null ?
                                "" : (String) jObject.get(Tables.COLUMNS.COLUMN_RFID));
                content.put(Tables.COLUMNS.COLUMN_QTYIN, Long.toString((Long) jObject.get(Tables.COLUMNS.COLUMN_QTYIN)));

                int idRow = (int) db.insert(Tables.LEFTOVERS.TABLE_NAME,
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

                content.put(Tables.COLUMNS.COLUMN_GUID, (String) jObject.get(Tables.COLUMNS.COLUMN_GUID));
                content.put(COLUMNS.COLUMN_STATE, jObject.get(COLUMNS.COLUMN_STATE) == null ?
                                "" : (String) jObject.get(COLUMNS.COLUMN_STATE)
                );
                int idRow = (int) db.insert(Tables.STATES.TABLE_NAME,
                        null,
                        content
                );

                break;
            }
        }
    }

    //-----

    void resetScanRes(){
        ContentValues content = new ContentValues();
        content.put(COLUMNS.COLUMN_QTYOUT, 0);
        db.delete(LEFTOVERS.TABLE_NAME,
                COLUMNS.COLUMN_QTYIN + " = 0",
                null);

        db.update(LEFTOVERS.TABLE_NAME,
                content,
                COLUMNS.COLUMN_QTYOUT + " <> 0",
                null);
    }

    void clearScanRes(){
        truncateLEFTOVERS();
    }

    void clearDB(){
        db.execSQL(GOODS.TRUNCATE_TABLE);
        db.execSQL(LEFTOVERS.TRUNCATE_TABLE);
        db.execSQL(MARKING_CODES.TRUNCATE_TABLE);
        db.execSQL(STATES.TRUNCATE_TABLE);
    }

    void truncateLEFTOVERS(){
        db.execSQL(LEFTOVERS.TRUNCATE_TABLE);
    }

    void truncateMARCING_CODES(){
        db.execSQL(MARKING_CODES.TRUNCATE_TABLE);
    }

    void truncateSTATES(){db.execSQL(STATES.TRUNCATE_TABLE);}
    public void closeDB(){
        db.close();
        Log.v(TAG, "DataBase closed");
    }
}
