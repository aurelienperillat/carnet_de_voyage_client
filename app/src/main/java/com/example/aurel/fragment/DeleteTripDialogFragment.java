package com.example.aurel.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.interfaces.OnTripDialog;

import java.util.ArrayList;
import java.util.List;


public class DeleteTripDialogFragment extends DialogFragment {
    private long id;

    private List<OnTripDialog> observers = new ArrayList<>();

    public DeleteTripDialogFragment() {}

    public static DeleteTripDialogFragment newInstance(long id) {
        DeleteTripDialogFragment fragment = new DeleteTripDialogFragment();
        Bundle args = new Bundle();
        args.putLong("DELETE_ID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments().getLong("DELETE_ID");
    }

    @Override
    public  void onResume() {
        super.onResume();
        int width = (int)getResources().getDimension(R.dimen.add_dialog_width);
        int height = (int)getResources().getDimension(R.dimen.add_dialog_height);
        getDialog().getWindow().setLayout(width, height);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_trip_dialog, container, false);

        Button delete = (Button)view.findViewById(R.id.valid_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTrip(id);
            }
        });

        return  view;
    }

    private void deleteTrip(long id) {
        for (OnTripDialog observer : observers) {
            observer.onDeleteTrip(id);
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
