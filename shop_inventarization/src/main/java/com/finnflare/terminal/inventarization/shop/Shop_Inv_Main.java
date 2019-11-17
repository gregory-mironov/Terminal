package com.finnflare.terminal.inventarization.shop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;

import com.finnflare.terminal.db_helper.DB_Files_Manager;
import com.finnflare.terminal.scanner_ui.Scanner_Api;

public class Shop_Inv_Main extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_inv_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void startScan(View view){
        startActivity(new Intent(this, Scanner_Api.class));
    }

    public void onFileUtilsClick(View view){
        startActivity(new Intent(this, DB_Files_Manager.class));
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }
}
