package com.example.aurel.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.aurel.carnet_de_voyage.AddCardActivity;
import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.interfaces.OnCardDialog;
import com.example.aurel.utility.MultipartUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ShowAllTripFragment extends Fragment implements View.OnClickListener {

    private RecyclerView.Adapter rvCardAdapter;
    private LinearLayoutManager rvCardLayoutManager;
    private ArrayList<Card> cardList;
    private TextView tvTitre;
    private long tripId;
    private ProgressDialog prgDialog;
    private RecyclerView rvCard;
    private ImageButton addCardButton;
    private OnCardDialog listener;
    public ArrayList<Bitmap> bitmaps;

    public ShowAllTripFragment() {
        // Required empty public constructor
    }

    public static ShowAllTripFragment newInstance() {
        ShowAllTripFragment fragment = new ShowAllTripFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_show_all_trip, container, false);
        tripId = getActivity().getIntent().getLongExtra("ID", 0);

        tvTitre = (TextView) v.findViewById(R.id.show_titre);
        tvTitre.setText(getTitre(tripId));

        rvCard = (RecyclerView) v.findViewById(R.id.card_list);
        rvCardLayoutManager = new LinearLayoutManager(getContext());
        rvCardLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCard.setLayoutManager(rvCardLayoutManager);

        updateCard();

        return v;
    }

    public void updateCard() {
        Log.d("Update Card", "before task !");
        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                prgDialog = ProgressDialog.show(getActivity(), "Loadind trip", "please wait...", true, false);
                Log.d("Update Card", "before execute !");
            }

            @Override
            protected String doInBackground(String... params) {
                String result = "";

                try {
                    Log.d("Update Card", "before URL !");
                    URL url = new URL("http://"+MultipartUtility.IP_SERVER+"/voyage_rest_service/load/card?tripId="+tripId);
                    Log.d("Update Card", "before connection !");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.d("Update Card", "after connection !");
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

                cardList = new ArrayList<>();
                bitmaps = new ArrayList<Bitmap>();

                try {
                    JSONObject json = new JSONObject(result);
                    JSONArray card = json.getJSONArray("card");

                    for (int i = 0; i < card.length(); i++) {
                        JSONObject obj = card.getJSONObject(i);
                        cardList.add(new Card(obj.getLong("id"), obj.getLong("tripId"), obj.getString("filename"), obj.getString("text")));
                        Bitmap bitmap = getBitmapFromURL("http://"+ MultipartUtility.IP_SERVER+"/voyage_rest_service/files/download?filename="+obj.getString("filename"));
                        bitmaps.add(bitmap);
                    }
                } catch (JSONException e) {
                }

                return result;
            }

            @Override
            protected void onPostExecute(String res) {
                rvCardAdapter = new CardAdapter();
                rvCard.setAdapter(rvCardAdapter);
                rvCard.setItemAnimator(new DefaultItemAnimator());
                prgDialog.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String getTitre(long id) {
        String URL = "content://com.example.aurel.carnet_de_voyage/trip";
        Uri uri = Uri.parse(URL);
        Cursor cursor = getActivity().managedQuery(uri, null, TripContentProvider.KEY_ID+"=?", new String[]{ String.valueOf(id) }, null);

        if(cursor.moveToFirst()) {
            String titre = cursor.getString(cursor.getColumnIndex(TripContentProvider.KEY_TITRE));
            return titre;
        }
        else { return "Not Found"; }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream input = connection.getInputStream();
            Log.d("Load bitmap", input.toString());

            int targetW = 600;
            int targetH = 400;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input,new Rect(),bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            Log.d("Camera", targetW + " " + targetH + " " + photoW + " " + photoH);
            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap myBitmap = BitmapFactory.decodeStream(input, new Rect(), bmOptions);
            Log.d("Load bitmap", myBitmap.toString());
            myBitmap = AddCardActivity.rotateBitmap(myBitmap, 90);

            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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


    @Override
    public void onClick(View view) {


    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private long id;
        private ImageView cardImage;
        private TextView cardText;


        public CardViewHolder(View itemView){
            super(itemView);
            cardImage = (ImageView)itemView.findViewById(R.id.card_picture);
            cardText = (TextView)itemView.findViewById(R.id.card_text);

            }

        private long getId() { return id; }
        private void setId(long id) { this.id= id; }
    }

    class CardAdapter extends RecyclerView.Adapter<CardViewHolder>{

        public CardAdapter() {}

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_all_holder_card, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            holder.cardText.setText(cardList.get(position).getText());

            holder.cardImage.setImageBitmap(bitmaps.get(position));
        }


        @Override
        public int getItemCount() {
            return cardList.size();
        }

        }


    }

