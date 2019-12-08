package com.finnflare.terminal.alien.scanner.driver;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alien.common.KeyCode;
import com.alien.rfid.RFIDCallback;
import com.alien.rfid.Tag;
import com.finnflare.terminal.db_helper.DB_RFID_Helper;

import java.util.HashMap;

public class Alien_RFID_Scanner extends AppCompatActivity implements RFIDCallback {
    private boolean scanningInProcess = false;

    private Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            TextView txtview;
            if(bundle.getString("increment") != null) {
                if (bundle.getString("increment").equals("correct"))
                    txtview = findViewById(R.id.correctScanNum);
                else
                    txtview = findViewById(R.id.wrongScanNum);
                txtview.setText(
                        String.valueOf(
                                Integer.parseInt(
                                        txtview.getText().toString()
                                ) + 1
                        ));
            }
            return true;
        }
    });

    private long LastScanTime = 0;

    private Alien_RFID_Scanner_Utils scanner;
    private DB_RFID_Helper db_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien_rfid_scanner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db_helper = new DB_RFID_Helper(this);

        HashMap<String, Long> Counts = db_helper.getLeftoversCount();

        TextView txtview = findViewById(R.id.planScanNum);
        txtview.setText(String.valueOf(Counts.get("total")));

        txtview = findViewById(R.id.correctScanNum);
        txtview.setText(String.valueOf(Counts.get("correct")));

        txtview = findViewById(R.id.wrongScanNum);
        txtview.setText(String.valueOf(Counts.get("wrong")));

        if (scanner == null)
            scanner = new Alien_RFID_Scanner_Utils(this);
        else
            scanner.setListener(this);
    }

    private void startScan() {
        if (!scanner.isScanning()) {
            LinearLayout clickScanListLayout = findViewById(R.id.clickRFIDScanListLayout);
            if( clickScanListLayout.getVisibility() == View.VISIBLE) {
                clickScanListLayout.removeAllViews();
                clickScanListLayout.setVisibility(View.GONE);
            }
            scanningInProcess = true;
            Button scanBut = findViewById(R.id.scanRFIDProcessButton);
            scanBut.setText(R.string.scanButElStopText);
            scanner.scan();
        }
    }

    public synchronized void stopScan() {
        if (scanner.isScanning()) {
            scanner.stop();
            scanningInProcess = false;
            Button scanBut = findViewById(R.id.scanRFIDProcessButton);
            scanBut.setText(R.string.scanButElStartText);
        }
    }

    public void onTagRead(Tag tag) {
        double rssi = tag.getRSSI();
        HashMap<String, String> scanData = db_helper.decodeScanResult(tag.getEPC());
        if(scanData != null){
            HashMap<String, String> good = db_helper.getGoodInfo(scanData);
            Bundle bundle = new Bundle();

            int incr = db_helper.increaseGoodLeftoversCount(good);
            switch(incr){
                case 1:{
                    bundle.putString("increment", "correct");
                    if ( System.currentTimeMillis() - LastScanTime >= 1500) {
                        LastScanTime = System.currentTimeMillis();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if( v != null){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(500,
                                        VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                v.vibrate(500);
                            }
                        }
                        try {
                            Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case 2:{
                    bundle.putString("increment", "wrong");
                    if ( System.currentTimeMillis() - LastScanTime >= 1500) {
                        LastScanTime = System.currentTimeMillis();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if( v != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                v.vibrate(500);
                            }
                        }
                        try {
                            Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                }
            }
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyCode.ALR_H450.SCAN || event.getRepeatCount() != 0) {
            return super.onKeyDown(keyCode, event);
        }
        startScan();
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != KeyCode.ALR_H450.SCAN) {
            return super.onKeyUp(keyCode, event);
        }
        stopScan();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    public void onStartStopClick(View view){
        if(scanningInProcess)
            stopScan();
        else
            startScan();
    }

    public void onWrongScansClick(View view){
        try{
        if (scanner.isScanning()) {
            scanner.stop();
        }

        LinearLayout clickScanListLayout = findViewById(R.id.clickRFIDScanListLayout);

        if( clickScanListLayout.getVisibility() == View.GONE){
            Cursor cursor = db_helper.getWrongScansList();
            if (cursor.moveToFirst()){
                clickScanListLayout.setVisibility(View.VISIBLE);
                while(!cursor.isAfterLast()){
                    TextView wrongScanInfo = new TextView(this);
                    wrongScanInfo.setText(
                            (cursor.getString(cursor.getColumnIndex("_NAME")) != null ?
                                    cursor.getString(
                                            cursor.getColumnIndex("_NAME")
                                    ).concat(
                                            " : "
                                    ).concat(
                                            cursor.getString(
                                                    cursor.getColumnIndex("_STATE_NAME"))
                                    )
                                    :
                                    "Неизвестный товар \n".concat(
                                            cursor.getString(
                                                    cursor.getColumnIndex("_GTIN")
                                            )
                                    )
                            )
                    );
                    wrongScanInfo.setTextColor(getResources().getColor(R.color.mainTextColor));
                    wrongScanInfo.setTextSize(20);
                    clickScanListLayout.addView(wrongScanInfo);

                    wrongScanInfo = new TextView(this);
                    wrongScanInfo.setText(
                            cursor.getString(cursor.getColumnIndex("_QTYOUT")).concat(
                                    " : ").concat(
                                    cursor.getString(cursor.getColumnIndex("_QTYIN"))
                            ));
                    wrongScanInfo.setTextColor(getResources().getColor(R.color.errorsTextColor));
                    wrongScanInfo.setTextSize(20);
                    clickScanListLayout.addView(wrongScanInfo);

                    cursor.moveToNext();
                }
            }
        }
        else {
            clickScanListLayout.setVisibility(View.GONE);
            clickScanListLayout.removeAllViews();
        }}
        catch( Exception e) {Log.v("FF_TERMINAL_LOG", e.toString());}
    }

    public void onRemainingScansClick(View view){
        if (scanner.isScanning()) {
            scanner.stop();
        }

        LinearLayout clickScanListLayout = findViewById(R.id.clickRFIDScanListLayout);

        if( clickScanListLayout.getVisibility() == View.GONE){
            Cursor cursor = db_helper.getRemainingScansList();
            if (cursor.moveToFirst()){
                clickScanListLayout.setVisibility(View.VISIBLE);
                while(!cursor.isAfterLast()){
                    TextView wrongScanInfo = new TextView(this);
                    wrongScanInfo.setText(
                            cursor.getString(cursor.getColumnIndex("_NAME")).concat(" : ").concat(
                                    cursor.getString(cursor.getColumnIndex("_STATE_NAME")))
                    );
                    wrongScanInfo.setTextColor(getResources().getColor(R.color.mainTextColor));
                    wrongScanInfo.setTextSize(20);
                    clickScanListLayout.addView(wrongScanInfo);

                    wrongScanInfo = new TextView(this);
                    wrongScanInfo.setText(
                            cursor.getString(cursor.getColumnIndex("_QTYOUT")).concat(" : ").concat(
                                    cursor.getString(cursor.getColumnIndex("_QTYIN"))
                            ));
                    wrongScanInfo.setTextColor(getResources().getColor(R.color.qtyoutTextColor));
                    wrongScanInfo.setTextSize(20);
                    clickScanListLayout.addView(wrongScanInfo);

                    cursor.moveToNext();
                }
            }
        }
        else {
            clickScanListLayout.setVisibility(View.GONE);
            clickScanListLayout.removeAllViews();
        }
    }
    @Override
    protected void onDestroy() {
        Alien_RFID_Scanner_Utils.deinit();
        db_helper.closeDB();
        super.onDestroy();
    }
}
