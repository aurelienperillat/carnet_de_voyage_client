package com.example.aurel.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.interfaces.OnCardDialog;
import com.example.aurel.interfaces.OnTripDialog;

import java.util.ArrayList;
import java.util.List;

public class DeleteCardDialogFragment extends DialogFragment {
    private long id;

    private List<OnCardDialog> observers = new ArrayList<>();

    public DeleteCardDialogFragment() {}

    public static DeleteCardDialogFragment newInstance(long id) {
        DeleteCardDialogFragment fragment = new DeleteCardDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_delete_card_dialog, container, false);

        Button delete = (Button)view.findViewById(R.id.valid_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCard(id);
            }
        });

        return  view;
    }

    private void deleteCard(long id) {
        for (OnCardDialog observer : observers) {
            observer.onDeleteCard(id);
        }
        getDialog().dismiss();
    }

    public void setOnCardDialogListenet(OnCardDialog observer){
        observers.add(observer);
    }

    public void removeCardDialogListener(OnCardDialog observer){
        observers.remove(observer);
    }
}