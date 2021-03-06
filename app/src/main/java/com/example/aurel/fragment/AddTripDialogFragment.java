package com.example.aurel.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.interfaces.OnTripDialog;

import java.util.ArrayList;
import java.util.List;

public class AddTripDialogFragment extends DialogFragment {

    private List<OnTripDialog> observers = new ArrayList<>();

    public AddTripDialogFragment() {}

    public static AddTripDialogFragment newInstance() {
        AddTripDialogFragment fragment = new AddTripDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public  void onResume() {
        super.onResume();
        int width = (int)getResources().getDimension(R.dimen.add_dialog_width);
        int height = (int)getResources().getDimension(R.dimen.dialog_height);
        getDialog().getWindow().setLayout(width, height);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_trip_dialog, container, false);

        final EditText titre = (EditText)view.findViewById(R.id.trip_titre);
        Button valid = (Button)view.findViewById(R.id.valid_add);

        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validTripp(titre.getText().toString());
            }
        });

        return  view;
    }

    public void validTripp(String titre) {
        for (OnTripDialog observer : observers) {
            observer.onAddTrip(titre);
        }
        getDialog().dismiss();
    }

    public void setOnTripDialogListenet(OnTripDialog observer){
        observers.add(observer);
    }

    public void removeTripDialogListener(OnTripDialog observer){
        observers.remove(observer);
    }
}
