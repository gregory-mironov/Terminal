package com.finnflare.terminal.idevice.scaner;

import android.content.Context;

public interface IDeviceScanner {
    void startBarcodeScan(Context context);

    void startRFIDScan(Context context);
}
