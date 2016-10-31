package com.example.aurel.carnet_de_voyage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.aurel.fragment.ShowAllTripFragment;
import com.example.aurel.fragment.ShowTripFragment;


public class ShowAllTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_trip);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_show__all_trip_fragment, ShowAllTripFragment.newInstance())
                .commit()
        ;

    }

}
