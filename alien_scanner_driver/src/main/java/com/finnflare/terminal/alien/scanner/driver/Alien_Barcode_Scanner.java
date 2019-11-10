package com.finnflare.terminal.alien.scanner.driver;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alien.barcode.BarcodeCallback;
import com.alien.barcode.BarcodeReader;
import com.alien.common.KeyCode.ALR_H450;

import java.util.HashMap;

import com.finnflare.terminal.db_helper.DB_Helper;

public class Alien_Barcode_Scanner extends AppCompatActivity {
    private BarcodeReader barcodeReader;
    private DB_Helper db_helper;
    private HashMap<String, String> good;
    boolean isAbleToDecrease = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien_barcode_scanner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        db_helper = new DB_Helper(this);
        barcodeReader = new BarcodeReader(this);

        HashMap<String, Long> Counts = db_helper.getLeftoversCount();

        TextView txtview = findViewById(R.id.planScanNum);
        txtview.setText(String.valueOf(Counts.get("total")));

        txtview = findViewById(R.id.correctScanNum);
        txtview.setText(String.valueOf(Counts.get("correct")));

        txtview = findViewById(R.id.wrongScanNum);
        txtview.setText(String.valueOf(Counts.get("wrong")));
    }

    private void startScan() {
        if (!barcodeReader.isRunning()) {
            barcodeReader.start(new BarcodeCallback() {
                public void onBarcodeRead(String scanRes) {
                    HashMap<String, String> scanData = db_helper.decodeScanResult(scanRes);
                    if(scanData != null){
                        good = db_helper.getGoodInfo(scanData);

                        TextView txtview = findViewById(R.id.goodName);
                        txtview.setText(
                                good.get("_NAME").equals("") ?
                                        "Неизвестный товар" : good.get("_NAME")
                        );
                        txtview = findViewById(R.id.goodColor);
                        txtview.setText(good.get("_COLOR"));
                        txtview = findViewById(R.id.goodSize);
                        txtview.setText(good.get("_SIZE"));

                        if(!db_helper.increaseGoodLeftoversCount(
                                good.get("_GUID"),
                                good.get("_SN"),
                                good.get("_RFID"))
                        ) {
                            if(db_helper.increaseGoodCountOverPlan(
                                        good.get("_GUID"),
                                        good.get("_SN"),
                                        good.get("_RFID")) ||
                                    db_helper.addGoodLeftoversCount(good)){
                                txtview = (TextView) findViewById(R.id.wrongScanNum);
                                txtview.setText(
                                        String.valueOf(
                                                Integer.parseInt(
                                                        txtview.getText().toString()
                                                ) + 1
                                        ));
                                isAbleToDecrease = true;
                            }
                            else{
                                isAbleToDecrease = false;
                            }
                        }
                        else{
                            txtview = findViewById(R.id.correctScanNum);
                            txtview.setText(
                                    String.valueOf(
                                            Integer.parseInt(
                                                    txtview.getText().toString()
                                            ) + 1
                                    ));
                            isAbleToDecrease = true;
                        }

                        HashMap<String, Long> goodCount = db_helper.getGoodLeftoverCount(
                                good.get("_GUID"),
                                good.get("_SN")
                        );
                        txtview = findViewById(R.id.goodScanTextView);

                        txtview.setText(String.valueOf(goodCount.get("_QTYOUT")));

                        if(goodCount.get("_QTYOUT") > goodCount.get("_QTYIN"))
                            txtview.setTextColor(getResources().getColor(R.color.errorsTextColor));
                        else
                            txtview.setTextColor(getResources().getColor(R.color.qtyoutTextColor));

                        Button revBut = findViewById(R.id.revertBut);
                        revBut.setEnabled(isAbleToDecrease);

                        txtview = findViewById(R.id.goodQTYINTextView);
                        txtview.setText(String.valueOf(goodCount.get("_QTYIN")));
                    }
                }
            });
        }
    }

    public synchronized void stopScan() {
        if (this.barcodeReader.isRunning()) {
            this.barcodeReader.stop();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != ALR_H450.SCAN || event.getRepeatCount() != 0) {
            return super.onKeyDown(keyCode, event);
        }
        startScan();
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != ALR_H450.SCAN) {
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

    public void onRevertScanClick(View view){
        TextView txtview = findViewById(R.id.goodName);
        txtview.setText(R.string.fullGoodName);
        txtview = findViewById(R.id.goodColor);
        txtview.setText(R.string.goodColorText);
        txtview = findViewById(R.id.goodSize);
        txtview.setText(R.string.goodSizeText);

        txtview = findViewById(R.id.goodScanTextView);
        txtview.setTextColor(getResources().getColor(R.color.qtyoutTextColor));
        txtview.setText(String.valueOf(0));

        txtview = findViewById(R.id.goodQTYINTextView);
        txtview.setText(String.valueOf(0));

        HashMap<String, Long> goodCount = db_helper.getGoodLeftoverCount(
                good.get("_GUID"),
                good.get("_SN")
        );

        if(goodCount.get("_QTYOUT") > goodCount.get("_QTYIN") + 1){
            txtview = findViewById(R.id.wrongScanNum);
            txtview.setText(String.valueOf(
                   Long.parseLong(txtview.getText().toString()) - 1)
            );
        }
        else {
            txtview = findViewById(R.id.correctScanNum);
            txtview.setText(String.valueOf(
                    Long.parseLong(txtview.getText().toString()) - 1)
            );
        }
        isAbleToDecrease = false;
        db_helper.decreaseGoodLeftoverCount(good);
    }

    @Override
    protected void onDestroy() {
        db_helper.closeDB();
        super.onDestroy();
    }
}
