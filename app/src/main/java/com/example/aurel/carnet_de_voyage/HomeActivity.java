package com.example.aurel.carnet_de_voyage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void manageTrip(View v) {
        Intent intent = new Intent(this, MyTripActivity.class);
        startActivity(intent);
    }

    public void allTrip(View v) {
        Intent intent = new Intent(this, AllTripActivity.class);
        startActivity(intent);
    }
}
