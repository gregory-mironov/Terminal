package com.finnflare.terminal.alien.scanner.driver;

import android.util.Log;

import com.alien.rfid.RFID;
import com.alien.rfid.RFIDCallback;
import com.alien.rfid.RFIDReader;
import com.alien.rfid.ReaderException;

public class Alien_RFID_Scanner_Utils {
    private static final String TAG = "FF_TERMINAL_LOG";
    private static RFIDReader reader;
    private RFIDCallback listener;

    static synchronized void init() throws ReaderException {
        synchronized (Alien_RFID_Scanner_Utils.class) {
            if (reader == null) {
                reader = RFID.open();
                Log.i(TAG, "Reader initialized successfully");
            }
        }
    }

    static synchronized void deinit() {
        synchronized (Alien_RFID_Scanner_Utils.class) {
            if (reader != null) {
                reader.close();
                reader = null;
                Log.v(TAG, "Reader closed successfully");
            }
        }
    }

    Alien_RFID_Scanner_Utils(RFIDCallback listener2){
        try {
            this.listener = listener2;
            init();
        } catch (Exception e) {
            Log.e(TAG, "Error init RFID scanner: " + e);
            e.printStackTrace();
        }
    }

    void setListener(RFIDCallback listener2) {
        this.listener = listener2;
    }

    void scan() {
        try {
            if (this.listener != null) {
                reader.inventory(this.listener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when scanning tags: " + e);
        }
    }

    boolean stop() {
        try {
            if (reader != null) {
                reader.stop();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error when stop RFID scanner: " + e);
            return false;
        }
    }

    boolean isScanning() {
        return reader.isRunning();
    }
}
