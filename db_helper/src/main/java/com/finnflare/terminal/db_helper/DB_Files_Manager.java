package com.finnflare.terminal.db_helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

public class DB_Files_Manager extends AppCompatActivity {
    private String TAG = "FF_TERMINAL_LOG";
    private ProgressDialog pd;
    private AlertDialog.Builder aldBuilder;
    private DB_Helper db_helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_files_manager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db_helper = new DB_Helper(this);

        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        aldBuilder = new AlertDialog.Builder(this);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    public void onNumLoadClick(View view){
        new Thread(loadFilesForDB).start();
    }

    public void uploadLeftovers(View view){
        new Thread(saveLeftoversFile).start();
    }

    public void onResetScan(View view){
        db_helper.resetScanRes();
        aldBuilder.setTitle("Сброс")
                .setMessage("Остатки сброшены")
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    public void onClearScan(View view){
        db_helper.clearScanRes();
        aldBuilder.setTitle("Очистка")
                .setMessage("Остатки удалены")
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    public void onClearDB(View view){
        db_helper.clearDB();

        aldBuilder.setTitle("Очистка базы")
                .setMessage("Очистка завершена")
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    private Runnable loadFilesForDB = new Runnable() {

        @Override
        public void run() {
            JSONObject jObject;
            JSONArray jArray;
            Reader reader;

            Bundle bundle;
            Message msg;

            String root = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).
                    getAbsolutePath();

            boolean progressBarIsShown = false, isFileLoaded = false;

            //----- GOODS loading
            try {
                Log.v(TAG, "Start GOODS JSON loading");
                reader = new FileReader(root + File.separator + Tables.GOODS.LOAD_FILE_NAME);

                JSONParser parser = new JSONParser();
                jObject = (JSONObject) parser.parse(reader);

                jArray = (JSONArray) jObject.get(Tables.GOODS.TABLE_NAME);
                if(jArray != null) {
                    Log.v(TAG, String.valueOf(jArray.size()));
                    bundle = new Bundle();
                    bundle.putString("title", Tables.GOODS.LOAD_FILE_PROCESS_TITLE);
                    bundle.putInt("progress", jArray.size());
                    if(!progressBarIsShown) {
                        bundle.putBoolean("start", true);
                        progressBarIsShown = true;
                    }
                    msg = new Message();
                    msg.setData(bundle);
                    dbFileUtilsHandler.sendMessage(msg);

                    int goods_count = 0, goodsIter = (int) Math.ceil(jArray.size() / 100);

                    for (Object o : jArray) {
                        goods_count++;

                        db_helper.putJSONObjectToTable((JSONObject) o, Tables.GOODS.TABLE_NAME);

                        if( goods_count == goodsIter){
                            bundle = new Bundle();
                            bundle.putInt("progress", goodsIter);
                            msg = new Message();
                            msg.setData(bundle);
                            dbFileUtilsHandler.sendMessage(msg);
                            goods_count = 0;
                        }
                    }
                }
                reader.close();
                isFileLoaded = true;
                new File(root + File.separator + Tables.GOODS.LOAD_FILE_NAME).delete();
            } catch (FileNotFoundException e) {
                Log.v(TAG, "Error GOODS file not found : " + e.toString());
            } catch (IOException e) {
                Log.v(TAG, "Error GOODS file reading : " + e.toString());
            } catch (ParseException e) {
                Log.v(TAG, "Error parsing GOODS JSON : " + e.toString());
            }

            //-----  LEFTOVERS loading
            try {
                Log.v(TAG, "Start LEFTOVERS JSON loading");
                reader = new FileReader(root + File.separator + Tables.LEFTOVERS.LOAD_FILE_NAME);

                JSONParser parser = new JSONParser();
                jObject = (JSONObject) parser.parse(reader);

                jArray = (JSONArray) jObject.get(Tables.LEFTOVERS.TABLE_NAME);

                if(jArray != null) {
                    bundle = new Bundle();
                    bundle.putString("title", Tables.LEFTOVERS.LOAD_FILE_PROCESS_TITLE);
                    bundle.putInt("progress", jArray.size());
                    if (!progressBarIsShown) {
                        bundle.putBoolean("start", true);
                        progressBarIsShown = true;
                    }
                    msg = new Message();
                    msg.setData(bundle);
                    dbFileUtilsHandler.sendMessage(msg);

                    db_helper.truncateLEFTOVERS();

                    int leftovers_count = 0, leftoversIter = (int) Math.ceil(jArray.size() / 100);

                    for (Object o : jArray) {
                        leftovers_count++;

                        db_helper.putJSONObjectToTable((JSONObject) o, Tables.LEFTOVERS.TABLE_NAME);

                        if( leftovers_count == leftoversIter){
                            bundle = new Bundle();
                            bundle.putInt("progress", leftoversIter);
                            msg = new Message();
                            msg.setData(bundle);
                            dbFileUtilsHandler.sendMessage(msg);
                            leftovers_count = 0;
                        }
                    }
                }
                reader.close();
                isFileLoaded = true;
                new File(root + File.separator + Tables.LEFTOVERS.LOAD_FILE_NAME).delete();
            } catch (FileNotFoundException e) {
                Log.v(TAG, "Error LEFTOVERS file not found : " + e.toString());
            } catch (IOException e) {
                Log.v(TAG, "Error LEFTOVERS file reading : " + e.toString());
            } catch (ParseException e) {
                Log.v(TAG, "Error parsing LEFTOVERS JSON : " + e.toString());
            }

            //----- MARKING_CODES loading
            try {
                Log.v(TAG, "Start MARKING_CODES JSON loading");
                reader = new FileReader(root + File.separator + Tables.MARKING_CODES.LOAD_FILE_NAME);

                JSONParser parser = new JSONParser();
                jObject = (JSONObject) parser.parse(reader);

                jArray = (JSONArray) jObject.get(Tables.MARKING_CODES.TABLE_NAME);

                if(jArray != null) {

                    bundle = new Bundle();
                    bundle.putString("title", Tables.MARKING_CODES.LOAD_FILE_PROCESS_TITLE);
                    bundle.putInt("progress", jArray.size());
                    if(!progressBarIsShown) {
                        bundle.putBoolean("start", true);
                        progressBarIsShown = true;
                    }
                    msg = new Message();
                    msg.setData(bundle);
                    dbFileUtilsHandler.sendMessage(msg);

                    db_helper.truncateMARCING_CODES();

                    int marking_codes_count = 0, marking_codes_Iter = (int) Math.ceil(jArray.size() / 100);
                    for (Object o : jArray) {
                        marking_codes_count++;

                        db_helper.putJSONObjectToTable((JSONObject) o, Tables.MARKING_CODES.TABLE_NAME);
                        if( marking_codes_count == marking_codes_Iter){
                            bundle = new Bundle();
                            bundle.putInt("progress", marking_codes_Iter);
                            msg = new Message();
                            msg.setData(bundle);
                            dbFileUtilsHandler.sendMessage(msg);
                            marking_codes_count = 0;
                        }
                    }
                }
                isFileLoaded = true;
                reader.close();
                new File(root + File.separator + Tables.MARKING_CODES.LOAD_FILE_NAME).delete();
            } catch (FileNotFoundException e) {
                Log.v(TAG, "Error MARKING_CODES file not found : " + e.toString());
            } catch (IOException e) {
                Log.v(TAG, "Error MARKING_CODES file reading : " + e.toString());
            } catch (ParseException e) {
                Log.v(TAG, "Error parsing MARKING_CODES JSON : " + e.toString());
            }

            //----- STATES loading
            try {
                Log.v(TAG, "Start STATES JSON loading");
                reader = new FileReader(root + File.separator + Tables.STATES.LOAD_FILE_NAME);

                JSONParser parser = new JSONParser();
                jObject = (JSONObject) parser.parse(reader);

                jArray = (JSONArray) jObject.get(Tables.STATES.TABLE_NAME);

                if(jArray != null) {
                    bundle = new Bundle();
                    bundle.putString("title", Tables.STATES.LOAD_FILE_PROCESS_TITLE);
                    bundle.putInt("progress", jArray.size());
                    if (!progressBarIsShown) {
                        bundle.putBoolean("start", true);
                        progressBarIsShown = true;
                    }
                    msg = new Message();
                    msg.setData(bundle);
                    dbFileUtilsHandler.sendMessage(msg);

                    db_helper.truncateSTATES();

                    int leftovers_count = 0, leftoversIter = (int) Math.ceil(jArray.size() / 100);

                    for (Object o : jArray) {
                        leftovers_count++;

                        db_helper.putJSONObjectToTable((JSONObject) o, Tables.STATES.TABLE_NAME);

                        if( leftovers_count == leftoversIter){
                            bundle = new Bundle();
                            bundle.putInt("progress", leftoversIter);
                            msg = new Message();
                            msg.setData(bundle);
                            dbFileUtilsHandler.sendMessage(msg);
                            leftovers_count = 0;
                        }
                    }
                }
                reader.close();
                isFileLoaded = true;
                new File(root + File.separator + Tables.STATES.LOAD_FILE_NAME).delete();
            } catch (FileNotFoundException e) {
                Log.v(TAG, "Error LEFTOVERS file not found : " + e.toString());
            } catch (IOException e) {
                Log.v(TAG, "Error LEFTOVERS file reading : " + e.toString());
            } catch (ParseException e) {
                Log.v(TAG, "Error parsing LEFTOVERS JSON : " + e.toString());
            }

            bundle = new Bundle();
            bundle.putBoolean("stop", true);
            msg = new Message();
            msg.setData(bundle);
            dbFileUtilsHandler.sendMessage(msg);

            bundle = new Bundle();
            bundle.putString("title", "Загрузка");
            if(isFileLoaded)
                bundle.putString("text", "Загрузка файлов завершена");
            else
                bundle.putString("text", "Файлы для загрузки не найдены или повреждены");
            msg = new Message();
            msg.setData(bundle);
            dbFileUtilsEventsHandler.sendMessage(msg);

            Log.v(TAG, "Files loading process finished");
        }
    };

    private Runnable saveLeftoversFile = new Runnable() {

        @Override
        public void run() {
            Cursor cursor = db_helper.getLeftoversForUpload();
            Bundle bundle;
            Message msg;
            boolean isUploaded = false;

            if (cursor.moveToFirst()) {
                Log.v(TAG, "Start LEFTOVERS JSON uploading");

                bundle = new Bundle();
                bundle.putString("title", Tables.LEFTOVERS.UPLOAD_FILE_PROCESS_TITLE);
                bundle.putInt("progress", cursor.getCount());
                bundle.putBoolean("start", true);
                msg = new Message();
                msg.setData(bundle);
                dbFileUtilsHandler.sendMessage(msg);

                String root = Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).
                        getAbsolutePath();

                ArrayList<JSONObject> jArrayList = new ArrayList<>();
                HashMap<String, String> jObjectMap = new HashMap<>();

                int leftovers_count = 0, leftoversIter = (int) Math.ceil(cursor.getCount() / 100);

                while (!cursor.isAfterLast()) {
                    leftovers_count++;

                    jObjectMap.put(Tables.COLUMNS.COLUMN_GUID,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_GUID)));
                    jObjectMap.put(Tables.COLUMNS.COLUMN_GTIN,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_GTIN)));
                    jObjectMap.put(Tables.COLUMNS.COLUMN_SN,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_SN)));
                    jObjectMap.put(Tables.COLUMNS.COLUMN_RFID,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_RFID)));
                    jObjectMap.put(Tables.COLUMNS.COLUMN_QTYIN,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_QTYIN)));
                    jObjectMap.put(Tables.COLUMNS.COLUMN_QTYOUT,
                            cursor.getString(cursor.getColumnIndex(Tables.COLUMNS.COLUMN_QTYOUT)));
                    jArrayList.add(new JSONObject(jObjectMap));

                    cursor.moveToNext();

                    if (leftovers_count == leftoversIter) {
                        bundle = new Bundle();
                        bundle.putInt("progress", leftoversIter);
                        msg = new Message();
                        msg.setData(bundle);
                        dbFileUtilsHandler.sendMessage(msg);
                        leftovers_count = 0;
                    }
                }
                try {
                    FileWriter writer = new FileWriter(root + File.separator + Tables.LEFTOVERS.SAVE_FILE_NAME);
                    HashMap<String, ArrayList> jFile = new HashMap<>();
                    jFile.put(Tables.LEFTOVERS.TABLE_NAME, jArrayList);
                    writer.write(new JSONObject(jFile).toJSONString());
                    writer.flush();

                    isUploaded = true;
                } catch (IOException e) {
                    Log.v(TAG, "Error LEFTOVERS file writing : " + e.getMessage());
                }

                bundle = new Bundle();
                bundle.putBoolean("stop", true);
                msg = new Message();
                msg.setData(bundle);
                dbFileUtilsHandler.sendMessage(msg);
            }
            cursor.close();

            bundle = new Bundle();
            bundle.putString("title", "Выгрузка");
            if(isUploaded)
                bundle.putString("text", "Выгрузка завершена");
            else
                bundle.putString("text", "Ошибка выгрузки");

            msg = new Message();
            msg.setData(bundle);
            dbFileUtilsEventsHandler.sendMessage(msg);
            Log.v(TAG, "LEFTOVERS JSON uploading ended");

        }
    };

    Handler dbFileUtilsHandler = new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    Bundle bundle = msg.getData();

                    if(bundle.getString("title") != null){
                        pd.setMessage(bundle.getString("title"));
                        pd.setMax(bundle.getInt("progress"));
                        pd.setProgress(0);
                    }
                    else{
                        if( (pd.getMax() - pd.getProgress() > bundle.getInt("progress")) &&
                        (pd.getMax() - pd.getProgress() < 2*bundle.getInt("progress")) ){
                            pd.setProgress(pd.getMax());
                        }
                        else
                            pd.incrementProgressBy(bundle.getInt("progress"));
                    }

                    if(bundle.getBoolean("start"))
                        pd.show();

                    if(bundle.getBoolean("stop")) {
                        pd.setProgress(0);
                        pd.dismiss();
                    }
                    return true;
                }
            }
    );

    Handler dbFileUtilsEventsHandler = new Handler(
        new Handler.Callback(){
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();

                aldBuilder.setTitle(bundle.getString("title"))
                        .setMessage(bundle.getString("text"))
                        .setCancelable(false)
                        .setNegativeButton("Закрыть",
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();

                return true;
            }
        }
    );

    @Override
    protected void onDestroy() {
        db_helper.closeDB();
        super.onDestroy();
    }
}
