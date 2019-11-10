package com.finnflare.terminal.idevice.scanner;

import android.content.Context;

public interface IDeviceScanner {
    void startBarcodeScan(Context context);

    void startRFIDScan(Context context);
}