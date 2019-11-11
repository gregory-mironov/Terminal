package com.finnflare.terminal.alien.scanner.driver;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alien.common.KeyCode;
import com.alien.rfid.RFIDCallback;
import com.alien.rfid.Tag;
import com.finnflare.terminal.db_helper.DB_Helper;

import java.util.HashMap;

public class Alien_RFID_Scanner extends AppCompatActivity implements RFIDCallback {

    private String TAG = "FF_TERMINAL_LOG";
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

    private Alien_RFID_Scanner_Utils scanner;
    private DB_Helper db_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien_rfid_scanner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db_helper = new DB_Helper(this);

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
            scanner.scan();
        }
    }

    public synchronized void stopScan() {
        if (scanner.isScanning()) {
            scanner.stop();
        }
    }


    public void onTagRead(Tag tag) {
        HashMap<String, String> scanData = db_helper.decodeScanResult(tag.getEPC());
        if(scanData != null){

            HashMap<String, String> good = db_helper.getGoodInfo(scanData);

            Bundle bundle = new Bundle();

            int incr = db_helper.increaseGoodLeftoversCount(good);
            switch(incr){
                case 1:{
                    bundle.putString("increment", "correct");
                    break;
                }
                case 2:{
                    bundle.putString("increment", "wrong");
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

    @Override
    protected void onDestroy() {
        Alien_RFID_Scanner_Utils.deinit();
        db_helper.closeDB();
        super.onDestroy();
    }
}
