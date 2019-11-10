package com.finnflare.terminal.scanner_ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.finnflare.terminal.alien.scanner.driver.Alien_Scanner_Main;
import com.finnflare.terminal.idevice.scanner.IDeviceScanner;


public class Scanner_Api extends AppCompatActivity {
    IDeviceScanner Scanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_api);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onBarcodeScannerStart(View view){
        Scanner = new Alien_Scanner_Main();
        Scanner.startBarcodeScan(Scanner_Api.this);
    }

    public void onRFIDScannerStart(View view){
        Scanner = new Alien_Scanner_Main();
        Scanner.startRFIDScan(Scanner_Api.this);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }
}
