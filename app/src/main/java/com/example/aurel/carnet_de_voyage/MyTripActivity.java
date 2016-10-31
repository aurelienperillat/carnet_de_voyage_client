package com.example.aurel.carnet_de_voyage;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.aurel.bdd.Trip;
import com.example.aurel.bdd.TripContentProvider;
import com.example.aurel.fragment.AddTripDialogFragment;
import com.example.aurel.fragment.DeleteTripDialogFragment;
import com.example.aurel.fragment.MyTripFragment;
import com.example.aurel.interfaces.OnTripDialog;

import java.util.ArrayList;

public class MyTripActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip);

        /*if(findViewById(R.id.activity_show_trip_fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }*/

            MyTripFragment myTripFragment = new MyTripFragment();
            myTripFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_my_trip_fragment, MyTripFragment.newInstance())
                    .commit()
            ;
        }
    }
