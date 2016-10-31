package com.example.aurel.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aurel.bdd.Trip;
import com.example.aurel.carnet_de_voyage.AllTripActivity;
import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.carnet_de_voyage.ShowAllTripActivity;
import com.example.aurel.carnet_de_voyage.ShowTripActivity;
import com.example.aurel.interfaces.OnCardDialog;
import com.example.aurel.utility.MultipartUtility;

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

import static java.security.AccessController.getContext;

public class AllTripFragment extends Fragment {

    private RecyclerView rvTrip;
    private RecyclerView.Adapter rvTripAdapter;
    private LinearLayoutManager rvTripLayoutManager;
    private ArrayList<Trip> tripList;
    private ProgressDialog prgDialog;


    public AllTripFragment() {
        // Required empty public constructor
    }

    public static AllTripFragment newInstance() {
        AllTripFragment fragment = new AllTripFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_trip, container, false);

        rvTrip = (RecyclerView) v.findViewById(R.id.all_trip_list);

        rvTrip.setHasFixedSize(true);
        rvTripLayoutManager = new LinearLayoutManager(getContext());
        rvTripLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTrip.setLayoutManager(rvTripLayoutManager);

        Log.d("Web Service", "before update !");
        updateTrip();

        return v;
    }

    public void updateTrip() {
        Log.d("Web Service", "before task !");
        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                prgDialog = ProgressDialog.show(getActivity(), "Loadind trip", "please wait...", true, false);
                Log.d("Web Service", "before execute !");
            }

            @Override
            protected String doInBackground(String... params) {
                String result = "";

                try {
                    Log.d("Web Service", "before URL !");
                    URL url = new URL("http://"+ MultipartUtility.IP_SERVER+"/voyage_rest_service/load/trip");
                    Log.d("Web Service", "before connection !");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.d("Web Service", "after connection !");
                    conn.setRequestMethod("GET");
                    conn.connect();

                    if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                        result = convertStreamToString(conn.getInputStream());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String res) {
                tripList = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(res);
                    JSONArray trip = json.getJSONArray("trip");

                    for (int i = 0; i < trip.length(); i++) {
                        JSONObject obj = trip.getJSONObject(i);
                        tripList.add(new Trip(obj.getLong("id"), obj.getString("titre")));
                    }
                } catch (JSONException e) {
                }

                rvTripAdapter = new AllTripAdapter();
                rvTrip.setAdapter(rvTripAdapter);
                rvTrip.setItemAnimator(new DefaultItemAnimator());
                prgDialog.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        return sb.toString();
    }

    class AllTripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private long id;
        private TextView titre;

        public AllTripViewHolder(View itemView){
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            titre = (TextView)itemView.findViewById(R.id.rv_all_item_titre);
        }

        private long getId() { return id; }
        private void setId(long id) { this.id= id; }

        @Override
        public void onClick(View v) {
            /*if(getActivity().findViewById(R.id.activity_show_all_trip_fragment) != null){

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_show_all_trip_fragment, ShowAllTripFragment.newInstance())
                        //.addToBackStack(null)
                        .commit();
            }
            else{
                ShowAllTripFragment showAllTripFragment = (ShowAllTripFragment) getFragmentManager().findFragmentById(R.id.activity_all_trip_fragment);
            }*/
            Intent intent = new Intent(getActivity(), ShowAllTripActivity.class);
            intent.putExtra("ID", id);
            startActivity(intent);
        }
    }

    class AllTripAdapter extends RecyclerView.Adapter<AllTripViewHolder> {

        public AllTripAdapter() {}

        @Override
        public AllTripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_all_trip, parent, false);
            return new AllTripViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AllTripViewHolder holder, int position) {
            holder.setId(tripList.get(position).getId());
            holder.titre.setText(tripList.get(position).getTitre());
        }

        @Override
        public int getItemCount() {
            return tripList.size();
        }
    }
}
