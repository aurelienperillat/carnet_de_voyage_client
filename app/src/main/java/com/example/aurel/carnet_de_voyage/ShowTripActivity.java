package com.example.aurel.carnet_de_voyage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aurel.bdd.Card;
import com.example.aurel.bdd.Trip;
import com.example.aurel.bdd.TripContentProvider;
import com.example.aurel.fragment.DeleteCardDialogFragment;
import com.example.aurel.fragment.ShareTripDialogFragment;
import com.example.aurel.fragment.ShowTripFragment;
import com.example.aurel.interfaces.OnCardDialog;
import com.example.aurel.interfaces.OnTripDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ShowTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_trip);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_show_trip_fragment, ShowTripFragment.newInstance())
                .commit();
    }
}