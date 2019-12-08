package com.finnflare.terminal.db_helper;

import java.util.HashMap;
import java.util.regex.Pattern;


final class DB_Scan_Decoder {
    private final String EAN_13 = "^\\d{13}$";
    private final String DataMatrix = "^.?01\\d{14}21.{13}" + (char)29 + "?.*$";
    private final String RFID = "^.{24}$";

    HashMap<String, String> decodeScanResult(String scanResult){
        HashMap<String, String> res = new HashMap<>();

        if(Pattern.matches(EAN_13, scanResult)){
            res.put(Tables.COLUMNS.COLUMN_GTIN, scanResult);
            res.put(Tables.COLUMNS.COLUMN_SN, "");
            res.put(Tables.COLUMNS.COLUMN_RFID, "");
        }
        else
        if(Pattern.matches(DataMatrix, scanResult)){
            int barcodeIndexStart = scanResult.indexOf("01") + 2;
            res.put(Tables.COLUMNS.COLUMN_GTIN,
                    scanResult.substring(
                            barcodeIndexStart,
                            barcodeIndexStart + 14
                    )
            );
            res.put(Tables.COLUMNS.COLUMN_SN,
                    scanResult.substring(
                            barcodeIndexStart + 16,
                            barcodeIndexStart + 29
                    )
            );
            res.put(Tables.COLUMNS.COLUMN_RFID, "");
        }
        else
        if(Pattern.matches(RFID, scanResult)){
            res.put(Tables.COLUMNS.COLUMN_GTIN,
                    Long.parseLong(
                            scanResult.substring(0, 10),
                            16
                    ) +
                    controlNumberGTIN(
                            String.valueOf(
                                    Long.parseLong(
                                            scanResult.substring(0, 10),
                                            16
                                    )
                            ).toCharArray()
                    )
            );
            res.put(Tables.COLUMNS.COLUMN_SN, "");
            res.put(Tables.COLUMNS.COLUMN_RFID, scanResult);
        }
        else
            res = null;

        return res;
    }

    private String controlNumberGTIN(char[] str){
        int ch = 0, nch = 0;
        for(int i = 0; i < str.length; i+=2){
            ch += Character.digit(str[i], 10);
        }
        for(int i = 1; i < str.length; i+=2){
            nch += Character.digit(str[i], 10);
        }

        return String.valueOf((10 - (ch + 3*nch) % 10) % 10);
    }
}
