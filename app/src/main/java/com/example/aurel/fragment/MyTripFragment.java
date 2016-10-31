package com.example.aurel.fragment;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.example.aurel.carnet_de_voyage.MyTripActivity;
import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.carnet_de_voyage.ShowTripActivity;
import com.example.aurel.interfaces.OnTripDialog;

import java.util.ArrayList;


public class MyTripFragment extends Fragment implements OnTripDialog, View.OnClickListener {

    private RecyclerView rvTrip;
    private RecyclerView.Adapter rvTripAdapter;
    private LinearLayoutManager rvTripLayoutManager;
    private ArrayList<Trip> tripList;


    public MyTripFragment() {
        // Required empty public constructor
    }


    public static MyTripFragment newInstance() {
        MyTripFragment fragment = new MyTripFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_trip, container, false);

        rvTrip = (RecyclerView) v.findViewById(R.id.trip_list);

        rvTrip.setHasFixedSize(true);
        rvTripLayoutManager = new LinearLayoutManager(getContext());
        rvTripLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTrip.setLayoutManager(rvTripLayoutManager);

        tripList =  updateTripList();

        rvTripAdapter = new TripAdapter(this);
        rvTrip.setAdapter(rvTripAdapter);
        rvTrip.setItemAnimator(new DefaultItemAnimator());

        ImageButton addTrip = (ImageButton) v.findViewById(R.id.add_trip_button);

        addTrip.setOnClickListener(this);

        return v;
    }

    public ArrayList<Trip> updateTripList() {
        ArrayList<Trip> list = new ArrayList<>();
        String URL = "content://com.example.aurel.carnet_de_voyage/trip";
        Uri uri = Uri.parse(URL);
        Cursor cursor = getActivity().managedQuery(uri, null, null, null, null);

        if(cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(TripContentProvider.KEY_ID));
                String titre = cursor.getString(cursor.getColumnIndex(TripContentProvider.KEY_TITRE));
                list.add(new Trip(id, titre));
            }while (cursor.moveToNext());
        }

        return list;
    }

    @Override
    public void onAddTrip(String titre) {
        ContentValues values = new ContentValues();
        values.put(TripContentProvider.KEY_TITRE, titre);
        Uri uri = getActivity().getContentResolver().insert(TripContentProvider.CONTENT_URI_TRIP, values);

        tripList = updateTripList();
        rvTripAdapter = new TripAdapter(this);
        rvTrip.setAdapter(rvTripAdapter);


    }

    @Override
    public void onDeleteTrip(long id) {

        int delete = getActivity().getContentResolver().delete(TripContentProvider.CONTENT_URI_TRIP, TripContentProvider.KEY_ID+"=?",
                new String[]{ String.valueOf(id) });

        tripList = updateTripList();
        rvTripAdapter = new TripAdapter(this);
        rvTrip.setAdapter(rvTripAdapter);

    }

    @Override
    public void onClick(View view) {
        FragmentTransaction addTransaction = getChildFragmentManager().beginTransaction();

        Fragment prev = getChildFragmentManager().findFragmentByTag("addDialog");
        if (prev != null) {
            addTransaction.remove(prev);
        }
        addTransaction.addToBackStack(null);

        AddTripDialogFragment addDialog = AddTripDialogFragment.newInstance();
        addDialog.setOnTripDialogListenet(this);
        addDialog.show(addTransaction, "addDialog");

    }

    class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private long id;
        private TextView titre;
        private ImageButton delete;

        public TripViewHolder(View itemView){
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            titre = (TextView)itemView.findViewById(R.id.rv_item_titre);
            delete = (ImageButton)itemView.findViewById(R.id.rv_item_trash);
        }

        private long getId() { return id; }
        private void setId(long id) { this.id= id; }

        @Override
        public void onClick(View v) {

            /*if(getActivity().findViewById(R.id.activity_show_trip_fragment) != null){

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_show_trip_fragment, ShowTripFragment.newInstance())
                        .commit();

            }
            else{
                ShowTripFragment ShowTripFragment = (ShowTripFragment) getFragmentManager().findFragmentById(R.id.activity_my_trip_fragment);

            }*/
            Intent intent = new Intent(getActivity(), ShowTripActivity.class);
            intent.putExtra("ID", id);
            startActivity(intent);
        }
    }

    class TripAdapter extends RecyclerView.Adapter<TripViewHolder> {
        private OnTripDialog parent;

        public TripAdapter(OnTripDialog parent){
            this.parent = parent;
        }

        @Override
        public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_trip, parent, false);
            return new TripViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final TripViewHolder holder, final int position) {
            holder.setId(tripList.get(position).getId());
            holder.titre.setText(tripList.get(position).getTitre());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction deleteTransaction = getChildFragmentManager().beginTransaction();

                    Fragment prev = getChildFragmentManager().findFragmentByTag("deleteDialog");
                    if (prev != null) {
                        deleteTransaction.remove(prev);
                    }
                    deleteTransaction.addToBackStack(null);

                    DeleteTripDialogFragment deleteDialog = DeleteTripDialogFragment.newInstance(tripList.get(position).getId());
                    deleteDialog.setOnTripDialogListenet(parent);
                    deleteDialog.show(deleteTransaction, "deleteDialog");
                }
            });
        }

        @Override
        public int getItemCount() {
            return tripList.size();
        }
    }

}
