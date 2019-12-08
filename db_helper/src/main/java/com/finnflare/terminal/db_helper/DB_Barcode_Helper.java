package com.finnflare.terminal.db_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.finnflare.terminal.db_helper.Tables.BARCODE_LEFTOVERS;
import com.finnflare.terminal.db_helper.Tables.COLUMNS;
import com.finnflare.terminal.db_helper.Tables.GOODS;
import com.finnflare.terminal.db_helper.Tables.STATES;

import java.util.HashMap;

public final class DB_Barcode_Helper {
    private String TAG = "FF_TERMINAL_LOG";

    private SQLiteDatabase db;
    private DB_Scan_Decoder decoder;

    public DB_Barcode_Helper(Context context){
        db = new DB_Open_Helper(context, Tables.DATABASE.DATABASE_NAME, Tables.DATABASE.DATABASE_VERSION).getWritableDatabase();
        decoder = new DB_Scan_Decoder();
        Log.v(TAG, "DataBase opened/created");
    }

    public HashMap<String, String> decodeScanResult(String scanResult){
        return decoder.decodeScanResult(scanResult);
    }

    public HashMap<String, String> getGoodInfo(HashMap<String, String> _data){
        HashMap<String, String> goodInfo = new HashMap<>();
        goodInfo.put(Tables.COLUMNS.COLUMN_GUID, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_NAME, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_MODEL, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_SIZE, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_STATE, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_STATE_NAME, "");
        goodInfo.put(Tables.COLUMNS.COLUMN_COLOR, "");
        goodInfo.put(
                Tables.COLUMNS.COLUMN_GTIN,
                _data.get(Tables.COLUMNS.COLUMN_GTIN)
        );
        goodInfo.put(
                Tables.COLUMNS.COLUMN_SN,
                _data.get(Tables.COLUMNS.COLUMN_SN)
        );


        Cursor cursor = db.query(Tables.MARKING_CODES.TABLE_NAME,
                new String[]{
                        Tables.COLUMNS.COLUMN_GUID,
                        Tables.COLUMNS.COLUMN_STATE
                },
                Tables.COLUMNS.COLUMN_GTIN + " = ? and " +
                        Tables.COLUMNS.COLUMN_SN + " = ?",
                new String[]{
                        _data.get(Tables.COLUMNS.COLUMN_GTIN),
                        _data.get(Tables.COLUMNS.COLUMN_SN)
                },
                null,
                null,
                null
        );

        if(!cursor.moveToFirst()){
            cursor = db.query(Tables.MARKING_CODES.TABLE_NAME,
                    new String[]{
                            Tables.COLUMNS.COLUMN_GUID
                    },
                    Tables.COLUMNS.COLUMN_GTIN + " = ?",
                    new String[]{
                            _data.get(Tables.COLUMNS.COLUMN_GTIN),
                    },
                    null,
                    null,
                    null
            );
        }

        if(cursor.moveToFirst()){
            goodInfo.put(Tables.COLUMNS.COLUMN_GUID,
                    cursor.getString(
                            cursor.getColumnIndex(
                                    Tables.COLUMNS.COLUMN_GUID
                            )
                    )
            );

            if(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_STATE) != -1) {
                goodInfo.put(Tables.COLUMNS.COLUMN_STATE,
                        cursor.getString(
                                cursor.getColumnIndex(
                                        Tables.COLUMNS.COLUMN_STATE
                                )
                        )
                );
            }

            if(goodInfo.get(Tables.COLUMNS.COLUMN_SN).equals("")) {
                cursor = db.rawQuery(
                        "SELECT " + Tables.COLUMNS.COLUMN_GTIN + " FROM " +
                                Tables.MARKING_CODES.TABLE_NAME + " WHERE " +
                                Tables.COLUMNS.COLUMN_GUID + " = '" + goodInfo.get(Tables.COLUMNS.COLUMN_GUID) + "' AND " +
                                Tables.COLUMNS.COLUMN_STATE + " = '" + goodInfo.get(Tables.COLUMNS.COLUMN_STATE) +
                                "' ORDER BY " + Tables.COLUMNS.COLUMN_GTIN + " DESC LIMIT 1;",
                        null);

                if(cursor.moveToFirst()){
                    goodInfo.put(Tables.COLUMNS.COLUMN_GTIN,
                            cursor.getString(
                                    cursor.getColumnIndex(
                                            Tables.COLUMNS.COLUMN_GTIN
                                    )
                            )
                    );
                }
            }

            cursor = db.query(Tables.GOODS.TABLE_NAME,
                    null,
                    Tables.COLUMNS.COLUMN_GUID + " = ?",
                    new String[]{goodInfo.get(Tables.COLUMNS.COLUMN_GUID)},
                    null,
                    null,
                    null
            );

            if(cursor.moveToFirst()) {
                goodInfo.put(
                        Tables.COLUMNS.COLUMN_NAME,
                        cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_NAME))
                );
                goodInfo.put(
                        Tables.COLUMNS.COLUMN_MODEL,
                        cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_MODEL))
                );
                goodInfo.put(
                        Tables.COLUMNS.COLUMN_SIZE,
                        cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_SIZE))
                );
                goodInfo.put(
                        Tables.COLUMNS.COLUMN_COLOR,
                        cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_COLOR))
                );

                if(!goodInfo.get(Tables.COLUMNS.COLUMN_STATE).equals("")){
                    cursor = db.query(Tables.STATES.TABLE_NAME,
                            new String[]{Tables.COLUMNS.COLUMN_STATE_NAME},
                            Tables.COLUMNS.COLUMN_STATE + " = ?",
                            new String[]{goodInfo.get(Tables.COLUMNS.COLUMN_STATE)},
                            null,
                            null,
                            null
                    );
                    if(cursor.moveToFirst()){
                        goodInfo.put(Tables.COLUMNS.COLUMN_STATE_NAME,
                                cursor.getString(
                                        cursor.getColumnIndex(
                                                Tables.COLUMNS.COLUMN_STATE_NAME
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

    public int increaseGoodLeftoversCount(HashMap<String, String> good) {
        Cursor cursor = db.query(BARCODE_LEFTOVERS.TABLE_NAME,
                new String[]{Tables.COLUMNS.COLUMN_QTYOUT, Tables.COLUMNS.COLUMN_QTYIN},
                Tables.COLUMNS.COLUMN_GUID + " = ? and " +
                        Tables.COLUMNS.COLUMN_SN + " = ? and " +
                        Tables.COLUMNS.COLUMN_GTIN + " = ? and " +
                        Tables.COLUMNS.COLUMN_STATE + " = ?",
                new String[]{
                        good.get(Tables.COLUMNS.COLUMN_GUID),
                        good.get(Tables.COLUMNS.COLUMN_SN),
                        good.get(Tables.COLUMNS.COLUMN_GTIN),
                        good.get(Tables.COLUMNS.COLUMN_STATE)},
                null,
                null,
                null
        );

        int result = 3;

        if(cursor.moveToFirst()){
            result = 2;

            int _qtyout = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_QTYOUT))
            );
            int _qtyin = Integer.parseInt(
                    cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_QTYIN))
            );
            if( _qtyout == 0 || good.get(Tables.COLUMNS.COLUMN_SN).equals("") ){

                result = (_qtyout < _qtyin) ? 1 : 2;

                ContentValues content = new ContentValues();
                content.put(
                        Tables.COLUMNS.COLUMN_QTYOUT,
                        Integer.toString(_qtyout + 1)
                );

                long idRow = db.update(
                        BARCODE_LEFTOVERS.TABLE_NAME,
                        content,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ? and " +
                                COLUMNS.COLUMN_STATE + " = ?",
                        new String[]{
                                good.get(Tables.COLUMNS.COLUMN_GUID),
                                good.get(Tables.COLUMNS.COLUMN_SN),
                                good.get(Tables.COLUMNS.COLUMN_GTIN),
                                good.get(Tables.COLUMNS.COLUMN_STATE)
                        });
            }
            else {
                result = 0;
            }
        }

        if(result == 3){
            ContentValues content = new ContentValues();
            content.put(Tables.COLUMNS.COLUMN_GUID, good.get(Tables.COLUMNS.COLUMN_GUID));
            content.put(Tables.COLUMNS.COLUMN_GTIN, good.get(Tables.COLUMNS.COLUMN_GTIN));
            content.put(Tables.COLUMNS.COLUMN_SN, good.get(Tables.COLUMNS.COLUMN_SN));
            content.put(Tables.COLUMNS.COLUMN_STATE, good.get(Tables.COLUMNS.COLUMN_STATE));
            content.put(Tables.COLUMNS.COLUMN_QTYOUT, Integer.toString(1));
            content.put(Tables.COLUMNS.COLUMN_TIME, "");
            int idRow = (int) db.insert(BARCODE_LEFTOVERS.TABLE_NAME,
                    null,
                    content
            );
            result = 2;
        }

        cursor.close();
        return result;
    }

    public void decreaseGoodLeftoverCount(HashMap<String, String> good){
        Cursor cursor = db.query(BARCODE_LEFTOVERS.TABLE_NAME,
                new String[]{COLUMNS.COLUMN_QTYOUT, COLUMNS.COLUMN_QTYIN},
                COLUMNS.COLUMN_GUID + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ? and " +
                        COLUMNS.COLUMN_STATE + " = ? and " +
                        COLUMNS.COLUMN_GTIN + " = ?",
                new String[]{
                        good.get(COLUMNS.COLUMN_GUID),
                        good.get(COLUMNS.COLUMN_SN),
                        good.get(COLUMNS.COLUMN_STATE),
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
                db.delete(BARCODE_LEFTOVERS.TABLE_NAME,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ?",
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_SN),
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
                        BARCODE_LEFTOVERS.TABLE_NAME,
                        content,
                        COLUMNS.COLUMN_GUID + " = ? and " +
                                COLUMNS.COLUMN_SN + " = ? and " +
                                COLUMNS.COLUMN_GTIN + " = ?",
                        new String[]{
                                good.get(COLUMNS.COLUMN_GUID),
                                good.get(COLUMNS.COLUMN_SN),
                                good.get(COLUMNS.COLUMN_GTIN)
                        });
            }
        }
        cursor.close();
    }

    public HashMap<String, Long> getGoodLeftoverCount(HashMap<String, String> good){
        Cursor cursor = db.query(BARCODE_LEFTOVERS.TABLE_NAME,
                new String[]{COLUMNS.COLUMN_QTYOUT, COLUMNS.COLUMN_QTYIN},
                COLUMNS.COLUMN_GUID + " = ? and " +
                        COLUMNS.COLUMN_SN + " = ? and " +
                        COLUMNS.COLUMN_STATE + " = ? and " +
                        COLUMNS.COLUMN_GTIN + " = ?",
                new String[]{
                        good.get(COLUMNS.COLUMN_GUID),
                        good.get(COLUMNS.COLUMN_SN),
                        good.get(COLUMNS.COLUMN_STATE),
                        good.get(COLUMNS.COLUMN_GTIN)
                },
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

    public HashMap<String, Long> getLeftoversCount(){
        HashMap<String, Long> res = new HashMap<>();

        Cursor cursor = db.rawQuery(
                "SELECT SUM( " + COLUMNS.COLUMN_QTYIN + " ) as total," +
                        " SUM( CASE WHEN " + COLUMNS.COLUMN_QTYOUT + " > " + COLUMNS.COLUMN_QTYIN +
                        " THEN " + COLUMNS.COLUMN_QTYOUT + " - " + COLUMNS.COLUMN_QTYIN +" ELSE 0 END)" +
                        " as wrong, " +
                        " SUM( CASE WHEN " + COLUMNS.COLUMN_QTYIN + " <= " + COLUMNS.COLUMN_QTYOUT +
                        " THEN " + COLUMNS.COLUMN_QTYIN + " ELSE " + COLUMNS.COLUMN_QTYOUT + " END)" +
                        " as correct FROM " + BARCODE_LEFTOVERS.TABLE_NAME
                , null);


        if(cursor.moveToFirst()) {
            res.put("total", cursor.getLong(cursor.getColumnIndex("total")));
            res.put("correct", cursor.getLong(cursor.getColumnIndex("correct")));
            res.put("wrong", cursor.getLong(cursor.getColumnIndex("wrong")));
        }
        cursor.close();

        return res;
    }

    public Cursor getWrongScansList(){
        return db.rawQuery(
                "SELECT g." + COLUMNS.COLUMN_NAME + ", s." + COLUMNS.COLUMN_STATE_NAME + "," +
                " l." + COLUMNS.COLUMN_QTYIN + ", l." + COLUMNS.COLUMN_QTYOUT + "," +
                " l." + COLUMNS.COLUMN_GTIN +
                " FROM " + BARCODE_LEFTOVERS.TABLE_NAME + " as l" +
                " LEFT JOIN " + STATES.TABLE_NAME + " as s" +
                " ON s." + COLUMNS.COLUMN_STATE + " = l." + COLUMNS.COLUMN_STATE +
                " LEFT JOIN " + GOODS.TABLE_NAME + " as g" +
                " ON g." + COLUMNS.COLUMN_GUID + " = l." + COLUMNS.COLUMN_GUID +
                " WHERE l." + COLUMNS.COLUMN_QTYOUT + " > l." +COLUMNS.COLUMN_QTYIN + ";",
                null);
    }

    public Cursor getRemainingScansList(){
        return db.rawQuery(
                "SELECT g." + COLUMNS.COLUMN_NAME + ", s." + COLUMNS.COLUMN_STATE_NAME + ", " +
                "l." + COLUMNS.COLUMN_QTYIN + ", l." + COLUMNS.COLUMN_QTYOUT + " " +
                "FROM " + BARCODE_LEFTOVERS.TABLE_NAME + " as l " +
                "LEFT JOIN " + STATES.TABLE_NAME + " as s " +
                "ON s." + COLUMNS.COLUMN_STATE + " = l." + COLUMNS.COLUMN_STATE + " " +
                "LEFT JOIN " + GOODS.TABLE_NAME + " as g " +
                "ON g." + COLUMNS.COLUMN_GUID + " = l." + COLUMNS.COLUMN_GUID + " " +
                "WHERE l." + COLUMNS.COLUMN_QTYOUT + " < l." +COLUMNS.COLUMN_QTYIN + ";",
                null);

    }

    public void closeDB(){
        db.close();
        Log.v(TAG, "DataBase closed");
    }
}
