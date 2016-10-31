package com.example.aurel.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.aurel.carnet_de_voyage.ModifyCardActivity;
import com.example.aurel.carnet_de_voyage.R;
import com.example.aurel.carnet_de_voyage.ShowTripActivity;
import com.example.aurel.interfaces.OnCardDialog;
import com.example.aurel.utility.MultipartUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShowTripFragment extends Fragment implements OnCardDialog, View.OnClickListener {

    private RecyclerView.Adapter rvCardAdapter;
    private LinearLayoutManager rvCardLayoutManager;
    private ArrayList<Card> cardList;
    private TextView tvTitre;
    private long tripId;
    private ProgressDialog prgDialog;
    private RecyclerView rvCard;
    private ImageButton shareButton;
    private ImageButton addCardButton;
    private ImageButton mapModeButton;

    public ShowTripFragment() {
        // Required empty public constructor
    }

    public static ShowTripFragment newInstance() {
        ShowTripFragment fragment = new ShowTripFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_show_trip, container, false);

        tripId = getActivity().getIntent().getLongExtra("ID", 0);

        tvTitre = (TextView) v.findViewById(R.id.show_titre);
        tvTitre.setText(getTitre(tripId));

        rvCard = (RecyclerView) v.findViewById(R.id.card_list);
        rvCardLayoutManager = new LinearLayoutManager(getContext());
        rvCardLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCard.setLayoutManager(rvCardLayoutManager);

        cardList = updateCardList();

        rvCardAdapter = new CardAdapter(this);
        rvCard.setAdapter(rvCardAdapter);
        rvCard.setItemAnimator(new DefaultItemAnimator());

        shareButton = (ImageButton) v.findViewById(R.id.share_trip);
        addCardButton = (ImageButton) v.findViewById(R.id.add_card_button);

        shareButton.setOnClickListener(this);
        addCardButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        cardList = updateCardList();
        rvCardAdapter = new ShowTripFragment.CardAdapter(this);
        rvCard.setAdapter(rvCardAdapter);
        rvCard.setItemAnimator(new DefaultItemAnimator());
    }

    public ArrayList<Card> updateCardList() {
        ArrayList<Card> list = new ArrayList<>();
        String URL = "content://com.example.aurel.carnet_de_voyage/card";
        Uri uri = Uri.parse(URL);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, TripContentProvider.KEY_TRIP_ID+"=?", new String[]{String.valueOf(tripId)}, null);

        if(cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(TripContentProvider.KEY_CARD_ID));
                long tripId = cursor.getLong(cursor.getColumnIndex(TripContentProvider.KEY_TRIP_ID));
                String  imgUri = cursor.getString(cursor.getColumnIndex(TripContentProvider.KEY_IMG));
                String text  = cursor.getString(cursor.getColumnIndex(TripContentProvider.KEY_TEXT));
                list.add(new Card(id, tripId, imgUri, text));
            }while (cursor.moveToNext());
        }
        return list;
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

    @Override
    public void onDeleteCard(long id) {
        int delete = getActivity().getContentResolver().delete(TripContentProvider.CONTENT_URI_CARD, TripContentProvider.KEY_CARD_ID+"=?",
                new String[] {String.valueOf(id)});
        cardList = updateCardList();
        rvCardAdapter = new ShowTripFragment.CardAdapter(this);
        rvCard.setAdapter(rvCardAdapter);
    }

    @Override
    public void onShareTrip() {
        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                prgDialog = ProgressDialog.show(getActivity(), "Publishing trip", "please wait...", true, false);
            }

            @Override
            protected String doInBackground(String... params) {
                String result = "";
                String titre = getTitre(tripId);

                try{
                    URL url = new URL("http://"+MultipartUtility.IP_SERVER+"/voyage_rest_service/publish/trip?titre=" + titre);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();

                    long newId;
                    if(HttpURLConnection.HTTP_OK == conn.getResponseCode()){
                        String responseBody = convertStreamToString(conn.getInputStream());
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            newId = json.getLong("newId");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            newId = -1;
                        }
                        result = "Publish succeed"; }
                    else {
                        result = "Publish failed";
                        newId = -1;
                    }

                    for(Card card : cardList){
                        Card tempCard = new Card(card.getId(), newId, card.getImgUri(), card.getText());
                        uploadCard(tempCard);
                        uploadFile(tempCard.getImgUri());
                    }
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String res) {
                prgDialog.dismiss();
                Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String uploadCard(Card card) {
        String result = "";
        String uri = card.getImgUri();
        String filename = uri.substring(uri.lastIndexOf('/')+1);
        Log.i("UploadCard", card.getTripId() + " " + filename + " " + card.getText());


        try{
            URL url = new URL("http://"+ MultipartUtility.IP_SERVER+"/voyage_rest_service/publish/card");

            JSONObject obj = new JSONObject();
            try {
                obj.put("tripId", card.getTripId());
                obj.put("filename", filename);
                obj.put("text", card.getText());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
            }

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(obj.toString().length()));
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(obj.toString());
            wr.flush();
            conn.connect();

            if(HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                result = "Publish succeed";
                Log.i("UploadCard", result);
            }
            else {
                result = "Publish failed";
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        Log.i("uploadcard", result);

        return result;
    }

    public int uploadFile(final String sourceFileUri) {

        int serverResponseCode = 0;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        String fileName = sourceFile.getName();

        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :" + sourceFileUri);
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://"+ MultipartUtility.IP_SERVER+"/voyage_rest_service/files/upload");
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                //  conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file", fileName);


                dos = new DataOutputStream(conn.getOutputStream());


                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //Adding Parameter media file(audio,video and image)

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    Log.i("uploadFile", "Upload succeed !");
                }

                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (final Exception e) {
                e.printStackTrace();
            }

            return serverResponseCode;
        }
    }

    @Override
    public void onClick(View view) {

        if (view.equals(shareButton)) {
            FragmentTransaction shareTransaction = getChildFragmentManager().beginTransaction();

            Fragment prev = getChildFragmentManager().findFragmentByTag("shareDialog");
            if (prev != null) {
                shareTransaction.remove(prev);
            }
            shareTransaction.addToBackStack(null);

            ShareTripDialogFragment shareDialog = ShareTripDialogFragment.newInstance();
            shareDialog.setOnCardDialogListenet(this);
            shareDialog.show(shareTransaction, "shareDialog");
        }
        if (view.equals(addCardButton)) {
            Intent intent = new Intent(getActivity(), AddCardActivity.class);
            intent.putExtra("TripId", tripId);
            startActivity(intent);

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

    class CardViewHolder extends RecyclerView.ViewHolder {
        private long id;
        private ImageView cardImage;
        private TextView cardText;
        private ImageButton modify;
        private ImageButton delete;

        public CardViewHolder(View itemView){
            super(itemView);
            cardImage = (ImageView)itemView.findViewById(R.id.card_picture);
            cardText = (TextView)itemView.findViewById(R.id.card_text);
            modify = (ImageButton)itemView.findViewById(R.id.card_modify);
            delete = (ImageButton)itemView.findViewById(R.id.card_delete);
        }

        private long getId() { return id; }
        private void setId(long id) { this.id= id; }
    }

    class CardAdapter extends RecyclerView.Adapter<ShowTripFragment.CardViewHolder>{
        private OnCardDialog parent;

        public CardAdapter(OnCardDialog parent) {
            this.parent = parent;
        }

        @Override
        public ShowTripFragment.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_card, parent, false);
            return new ShowTripFragment.CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShowTripFragment.CardViewHolder holder, int position) {
            final Card card = cardList.get(position);
            holder.cardText.setText(card.getText());

            try {
                int targetW = 600;
                int targetH = 400;

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(card.getImgUri(), bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                Log.d("Camera", targetW + " " + targetH + " " + photoW + " " + photoH);
                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeFile(card.getImgUri(), bmOptions);
                bitmap = AddCardActivity.rotateBitmap(bitmap, 90);
                holder.cardImage.setImageBitmap(bitmap);

            }catch (Exception e){
                e.printStackTrace();
            }

            holder.modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ModifyCardActivity.class);
                    intent.putExtra("CardId", card.getId());
                    intent.putExtra("TripId", card.getTripId());
                    startActivity(intent);
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction deleteTransaction = getChildFragmentManager().beginTransaction();

                    Fragment prev = getChildFragmentManager().findFragmentByTag("deleteDialog");
                    if (prev != null) {
                        deleteTransaction.remove(prev);
                    }
                    deleteTransaction.addToBackStack(null);

                    DeleteCardDialogFragment deleteDialog = DeleteCardDialogFragment.newInstance(card.getId());
                    deleteDialog.setOnCardDialogListenet(parent);
                    deleteDialog.show(deleteTransaction, "deleteDialog");
                }
            });
        }

        @Override
        public int getItemCount() {
            return cardList.size();
        }
    }
}
