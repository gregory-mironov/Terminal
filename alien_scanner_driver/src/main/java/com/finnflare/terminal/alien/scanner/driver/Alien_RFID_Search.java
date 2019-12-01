package com.finnflare.terminal.alien.scanner.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.alien.rfid.RFIDCallback;
import com.alien.rfid.Tag;
import com.finnflare.terminal.db_helper.DB_RFID_Helper;

public class Alien_RFID_Search extends AppCompatActivity implements RFIDCallback {
    private long LastScanTime = 0;
    private Alien_RFID_Scanner_Utils scanner;
    private DB_RFID_Helper db_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien_rfid_search);

        if (scanner == null)
            scanner = new Alien_RFID_Scanner_Utils(this);
        else
            scanner.setListener(this);
    }

    @Override
    public void onTagRead(Tag tag) {
        double rssi = tag.getRSSI();

    }
}
