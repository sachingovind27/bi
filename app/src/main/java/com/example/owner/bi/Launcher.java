package com.example.owner.bi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.net.Uri.EMPTY;
import static android.support.v4.content.FileProvider.getUriForFile;
import static com.example.owner.bi.WiFiDirectActivity.TAG;
import static java.security.AccessController.getContext;

/**
 * Created by Sachin Govind on 21-Mar-17.
 */

public class Launcher extends Activity {
    public String sendFileURI;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    public String SEND_FILE = "com.example.owner.bi";
    public String mCurrentPhotoPath;
    public static int findImage=1;
static Uri sendURI = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcheri);

        Button clickButton = (Button) findViewById(R.id.launchButton);
        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



                dispatchTakePictureIntent();

            }
        });
        Button clickButton1 = (Button) findViewById(R.id.launchButton1);
        clickButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Intent intent =new Intent(Launcher.this,Gall.class) ;
                Launcher.this.startActivity(intent);
            }
        });

        Button button1=(Button) findViewById(R.id.receiverActivity);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               findImage*=-1;
                if(findImage==-1) {
                    Toast.makeText(v.getContext(), "Image filtering on", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(v.getContext(), "Image filtering turned off", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sendURI!=null) {
            if (sendURI == null) {
                sendURI = Uri.EMPTY;
            }
            sendFileURI = sendURI.toString();
            Intent intent = new Intent(Launcher.this, WiFiDirectActivity.class);
            intent.putExtra(SEND_FILE, mCurrentPhotoPath);
            startActivity(intent);
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();//the photo is unique named
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.owner.bi", photoFile);
                sendURI = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }
}

