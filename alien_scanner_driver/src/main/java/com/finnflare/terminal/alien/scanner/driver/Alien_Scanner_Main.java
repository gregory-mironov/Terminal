package com.finnflare.terminal.alien.scanner.driver;

import android.content.Context;
import android.content.Intent;

import com.finnflare.terminal.idevice.scanner.IDeviceScanner;

public final class Alien_Scanner_Main implements IDeviceScanner {
    @Override
    public void startBarcodeScan(Context context) {
        context.startActivity(new Intent(context,  Alien_Barcode_Scanner.class));
    }

    @Override
    public void startRFIDScan(Context context) {
        context.startActivity(new Intent(context,  Alien_RFID_Scanner.class));
    }
}
