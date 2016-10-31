package com.example.aurel.carnet_de_voyage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.aurel.bdd.CardContentProvider;
import com.example.aurel.bdd.TripContentProvider;
import com.example.aurel.utility.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class AddCardActivity extends AppCompatActivity {
    private long tripId;
    private Camera mCamera;
    private File pictureFile;
    private EditText editDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        tripId = getIntent().getLongExtra("TripId", -1);
        editDescription = (EditText)findViewById(R.id.edit_description);
    }

    public void openPicture(View view) {
        LinearLayout tohide = (LinearLayout) findViewById(R.id.layout_formulaire);
        FrameLayout toshow = (FrameLayout) findViewById(R.id.layout_camera);

        tohide.setVisibility(View.GONE);
        toshow.setVisibility(View.VISIBLE);

        checkCameraHardware(this);
    }

    public void valideCard(View view) {
        ContentValues values = new ContentValues();
        values.put(CardContentProvider.KEY_TRIP_ID, tripId);
        values.put(CardContentProvider.KEY_IMG, pictureFile.getPath());
        values.put(CardContentProvider.KEY_TEXT, editDescription.getText().toString());
        getContentResolver().insert(TripContentProvider.CONTENT_URI_CARD, values);

        finish();
    }

    public void snapshot(View view) {
        ImageButton toshow1 = (ImageButton) findViewById(R.id.save_picture);
        ImageButton toshow2= (ImageButton) findViewById(R.id.delete_picture);

        toshow1.setVisibility(View.VISIBLE);
        toshow2.setVisibility(View.VISIBLE);

        Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d("snapshot", "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("snapshot", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("snapshot", "Error accessing file: " + e.getMessage());
                }
            }
        };

        mCamera.takePicture(null, null, mPicture);

    }

    public void savePicture(View view) {
        ImageButton tohide1 = (ImageButton) findViewById(R.id.save_picture);
        ImageButton tohide2= (ImageButton) findViewById(R.id.delete_picture);
        FrameLayout tohide3 = (FrameLayout) findViewById(R.id.layout_camera);
        LinearLayout toshow = (LinearLayout) findViewById(R.id.layout_formulaire);
        ImageButton buttonTohide = (ImageButton)findViewById(R.id.take_picture);
        ImageView imageToShow = (ImageView)findViewById(R.id.card_picture);

        tohide1.setVisibility(View.GONE);
        tohide2.setVisibility(View.GONE);

        tohide3.setVisibility(View.GONE);
        toshow.setVisibility(View.VISIBLE);

        buttonTohide.setVisibility(View.GONE);

        int targetW = imageToShow.getWidth();
        int targetH = imageToShow.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pictureFile.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.d("Camera", targetW + " " + targetH + " " + photoW + " " + photoH);
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath(), bmOptions);
        bitmap = rotateBitmap(bitmap, 90);
        imageToShow.setImageBitmap(bitmap);

        mCamera.release();
    }

    public void deletePicture(View GONE) {
        ImageButton tohide1 = (ImageButton) findViewById(R.id.save_picture);
        ImageButton tohide2= (ImageButton) findViewById(R.id.delete_picture);

        tohide1.setVisibility(View.GONE);
        tohide2.setVisibility(View.GONE);

        mCamera.startPreview();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // Create an instance of Camera

            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            CameraPreview mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            return true;
        } else {

            Toast.makeText(this, "Sorry Camera can't open", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed()
    {
        if(mCamera != null)
            mCamera.release();
        super.onBackPressed();
    }
}


