package com.finnflare.terminal.db_helper;

import android.provider.BaseColumns;

final class Tables {
    static abstract  class  DATABASE{
        static final String DATABASE_NAME = "FF_Terminal_DB";
        static final int DATABASE_VERSION = 1;
    }

    static abstract  class COLUMNS implements BaseColumns{
        static final String COLUMN_GUID = "_GUID";
        static final String COLUMN_SN = "_SN";
        static final String COLUMN_GTIN = "_GTIN";
        static final String COLUMN_RFID = "_RFID";

        static final String COLUMN_NAME = "_NAME";
        static final String COLUMN_MODEL = "_MODEL";
        static final String COLUMN_COLOR = "_COLOR";
        static final String COLUMN_SIZE = "_SIZE";
        static final String COLUMN_STATE = "_STATE";
        static final String COLUMN_STATE_NAME = "_STATE_NAME";

        static final String COLUMN_QTYIN = "_QTYIN";
        static final String COLUMN_QTYOUT = "_QTYOUT";
        static final String COLUMN_TIME = "_TRANSACTION_TIMESTAMP";
    }

    static abstract class GOODS implements BaseColumns {
        static final String TABLE_NAME = "GOODS";
        static final  String TABLE_PK = "PK_GOODS";

        static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ( " +
                COLUMNS.COLUMN_GUID +  " VARCHAR (36), " +
                COLUMNS.COLUMN_NAME +  " VARCHAR (200), " +
                COLUMNS.COLUMN_MODEL + " VARCHAR (50), " +
                COLUMNS.COLUMN_COLOR + " VARCHAR (50), " +
                COLUMNS.COLUMN_SIZE +  " VARCHAR (50), " +
                "CONSTRAINT " + TABLE_PK + " PRIMARY KEY ( " +
                COLUMNS.COLUMN_GUID +
                " ) " +
                "ON CONFLICT ROLLBACK );";
        static final String TRUNCATE_TABLE = "DELETE FROM " + TABLE_NAME + ";";

        static final String LOAD_FILE_NAME = "ff.inv.goods.json";
        static final String LOAD_FILE_PROCESS_TITLE = "Загрузка информации о товарах";
    }

    static abstract class LEFTOVERS implements BaseColumns{
        static final String TABLE_NAME = "LEFTOVERS";
        static final  String TABLE_PK = "PK_LEFTOVERS";

        static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ( " +
                COLUMNS.COLUMN_GUID +  " VARCHAR (36) NOT NULL, " +
                COLUMNS.COLUMN_GTIN +  " VARCHAR (50), " +
                COLUMNS.COLUMN_SN + " VARCHAR (20), " +
                COLUMNS.COLUMN_STATE + " VARCHAR (36), " +
                COLUMNS.COLUMN_RFID + " VARCHAR (40), " +
                COLUMNS.COLUMN_QTYIN + " INTEGER DEFAULT (0), " +
                COLUMNS.COLUMN_QTYOUT +  " INTEGER DEFAULT (0), " +
                COLUMNS.COLUMN_TIME + " DATETIME, " +
                "CONSTRAINT " + TABLE_PK + " PRIMARY KEY ( " +
                COLUMNS.COLUMN_GUID + ", " +
                COLUMNS.COLUMN_GTIN + ", " +
                COLUMNS.COLUMN_SN + ", " +
                COLUMNS.COLUMN_RFID +
                " ) " +
                "ON CONFLICT ROLLBACK );";
        static final String TRUNCATE_TABLE = "DELETE FROM " + TABLE_NAME + ";";

        static final String LOAD_FILE_NAME = "ff.inv.leftovers.json";
        static final String SAVE_FILE_NAME = "ff.inv.result.json";
        static final String LOAD_FILE_PROCESS_TITLE = "Загрузка остатков товаров";
        static final String UPLOAD_FILE_PROCESS_TITLE = "Выгрузка остатков товаров";
    }

    static abstract class MARKING_CODES implements BaseColumns{
        static final String TABLE_NAME = "MARKING_CODES";
        static final  String TABLE_PK = "PK_MARKING_CODES";

        static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ( " +
                COLUMNS.COLUMN_GTIN +  " VARCHAR (50), " +
                COLUMNS.COLUMN_SN +  " VARCHAR (20), " +
                COLUMNS.COLUMN_STATE +  " VARCHAR (36), " +
                COLUMNS.COLUMN_GUID + " VARCHAR (36), " +
                COLUMNS.COLUMN_RFID + " VARCHAR (40), " +
                "CONSTRAINT " + TABLE_PK + " PRIMARY KEY ( " +
                COLUMNS.COLUMN_GTIN + ", " +
                COLUMNS.COLUMN_SN +
                " ) " +
                "ON CONFLICT ROLLBACK );";

        static final String TRUNCATE_TABLE = "DELETE FROM " + TABLE_NAME + ";";
        static final String LOAD_FILE_NAME = "ff.inv.marking_codes.json";
        static final String LOAD_FILE_PROCESS_TITLE = "Загрузка кодов товаров";
    }

    static abstract class STATES implements BaseColumns{
        static final String TABLE_NAME = "STATES";

        static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ( " +
                COLUMNS.COLUMN_STATE + " VARCHAR (36) PRIMARY KEY, " +
                COLUMNS.COLUMN_STATE_NAME + " VARCHAR (80) );";

        static final String TRUNCATE_TABLE = "DELETE FROM " + TABLE_NAME + ";";
        static final String LOAD_FILE_NAME = "ff.inv.states.json";
        static final String LOAD_FILE_PROCESS_TITLE = "Загрузка характеристик товаров";
    }
}
