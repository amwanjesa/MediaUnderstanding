package com.example.albert.mediaunderstanding;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView voiceInput;
    private static String root = null;
    private static String imageFolderPath = null;
    private String imageName = null;
    private static Uri fileUri = null;
    private static final int CAMERA_IMAGE_REQUEST = 1;
    public String imagePath;
    private Bitmap bitmap;
    private StorageReference storageRef;
    private StorageReference imageRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private String fireImageUrl;
    private int baseUrlLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storagePermitted(MainActivity.this);
        baseUrlLength = getString(R.string.base_url).length();
        setContentView(R.layout.activity_main);
        voiceInput = (TextView) findViewById(R.id.speechText);
    }

    public void openCamera(View view) {

        ImageView imageView = (ImageView) findViewById(R.id.photo);

        // fetching the root directory
        root = Environment.getExternalStorageDirectory().toString()
                + "/Your_Folder";

        // Creating folders for Image
        imageFolderPath = root + "/saved_images";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        // Generating file name
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String timeStamp = s.format(new Date());
        imageName = timeStamp + ".png";

        // Creating image here
        File image = new File(imageFolderPath, imageName);

        fileUri = Uri.fromFile(image);
        imagePath = imageFolderPath + File.separator + imageName;
        imageView.setTag(imageFolderPath + File.separator + imageName);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(takePictureIntent,
                CAMERA_IMAGE_REQUEST);
    }

    public void askSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    public void uploadToFirebase(){
        storageRef = storage.getReference();
        imageRef = storageRef.child(imageName + ".jpg");
        StorageReference imagesChildRef = storageRef.child("images/" + imageName + "mountains.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                fireImageUrl = downloadUrl.toString();
                fireImageUrl = fireImageUrl.substring(baseUrlLength);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput.setText(result.get(0));
                    AnswerAsyncTask answerTask = new AnswerAsyncTask(this);
                    Log.d("url", fireImageUrl);
                    answerTask.execute("http://145.109.44.162:9999/retrieve/" + fireImageUrl);
                }
                break;
            }

            case CAMERA_IMAGE_REQUEST:

                bitmap = null;
                try {
                    GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
                    bitmap = getImageThumbnail.getThumbnail(fileUri, this);
                    uploadToFirebase();
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // Setting image image icon on the imageview
                ImageView imageView = (ImageView) this
                        .findViewById(R.id.photo);
                imageView.setImageBitmap(bitmap);
                break;

            default:
                Toast.makeText(this, "Something went wrong...",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private static boolean storagePermitted(Activity activity) {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        return false;

    }
}