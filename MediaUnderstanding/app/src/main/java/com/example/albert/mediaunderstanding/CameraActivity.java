package com.example.albert.mediaunderstanding;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    @SuppressWarnings("deprecation")
    Camera camera; // camera class variable
    SurfaceView camView; // drawing camera preview using this variable
    SurfaceHolder surfaceHolder; // variable to hold surface for surfaceView which means display
    boolean camCondition = false;  // conditional variable for camera preview checking and set to false
    ImageView cap;    // image capturing button
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Bitmap bitmap;
    private StorageReference storageRef;
    private StorageReference imageRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private String fireImageUrl;
    public int baseUrlLength;
    LayoutInflater controlInflater = null;
    public String userAnswer;
    public String correctAnswer ;
    public boolean correct;
    private String synonyms;
    private String descr;
    public View cameraView = null;
    public View resultView = null;
    public View descriptionView = null;
    private File pictureFile;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera2);
        // getWindow() to get window and set it's pixel format which is UNKNOWN
        getWindow().setFormat(PixelFormat.UNKNOWN);
        // refering the id of surfaceView
        camView = (SurfaceView) findViewById(R.id.camera_preview);
        // getting access to the surface of surfaceView and return it to surfaceHolder
        surfaceHolder = camView.getHolder();
        // adding call back to this context means MainActivity
        surfaceHolder.addCallback(this);
        // to set surface type
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // click event on button

        getWindow().setFormat(PixelFormat.UNKNOWN);
        baseUrlLength = getString(R.string.base_url).length();
        controlInflater = LayoutInflater.from(getBaseContext());
        setMainScreen();
    }

    private void setMainScreen(){
        if(camCondition){
            camera.startPreview();
        }
        cameraView = controlInflater.inflate(R.layout.control, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup   .LayoutParams.FILL_PARENT);
        this.addContentView(cameraView, layoutParamsControl);
        showDescription(cameraView);
        removeLayout(resultView);
        resultView = null;
        removeLayout(descriptionView);
        descriptionView = null;
        cap = (ImageView) findViewById(R.id.take_a_pic);
        cap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // calling a method of camera class takepicture by passing one picture callback interface parameter
                removeLayout(resultView);
                removeLayout(descriptionView);
                camera.takePicture(null, null, null, mPictureCallback);
            }
        });
    }

    private void removeLayout(View view){
        if(view != null){
            ViewGroup vg = (ViewGroup)(view.getParent());
            if(vg != null){
                vg.removeView(view);
            }
        }
    }

    @SuppressWarnings("deprecation")
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            camera.stopPreview();
            String root = Environment.getExternalStorageDirectory().toString()
                    + "/Your_Folder";
            // Creating folders for Image
            String imageFolderPath = root + "/saved_images";
            File imagesFolder = new File(imageFolderPath);
            imagesFolder.mkdirs();

            // Generating file name
            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            String timeStamp = s.format(new Date());
            String imageName = timeStamp + ".png";

            // Creating image here
            pictureFile = new File(imageFolderPath, imageName);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
            uploadToFirebase(pictureFile);
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            askSpeechInput();
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        // stop the camera
        if(camCondition){
            camera.stopPreview(); // stop preview using stopPreview() method
            camCondition = false; // setting camera condition to false means stop
        }
        // condition to check whether your device have camera or not
        if (camera != null){
            try {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else {
                    //Choose another supported mode
                }
                camera.setParameters(parameters); // setting camera parameters
                camera.setPreviewDisplay(surfaceHolder); // setting preview of camera
                camera.startPreview();

                camCondition = true; // setting camera to true which means having camera
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();   // opening camera
        camera.setDisplayOrientation(90);   // setting camera preview orientation
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();  // stopping camera preview
        camera.release();       // releasing camera
        camera = null;          // setting camera to null when left
        camCondition = false;   // setting camera condition to false also when exit from application
    }

    public void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "What do you think that is?");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    public void uploadToFirebase(File file){
        storageRef = storage.getReference();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String imageName = sdf.format(new Date());
        imageRef = storageRef.child(imageName + ".png");

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


//        ByteBuffer bf = ByteBuffer.allocate(bytes);
//        bitmap.copyPixelsToBuffer(bf);
//        BitmapFactory.Options options=new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//        Log.d("bitmap_byte_site", Integer.toString(data.length));
        UploadTask uploadTask = imageRef.putStream(stream);
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

                fireImageUrl = downloadUrl.toString().substring(baseUrlLength);
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
                    Toast.makeText(this, result.get(0), Toast.LENGTH_LONG).show();
                    userAnswer = result.get(0);
                    userAnswer = userAnswer.replace(" ", "_");
                    AnswerAsyncTask answerTask = new AnswerAsyncTask(CameraActivity.this);
                    answerTask.execute(getString(R.string.server_url) + userAnswer + "/" + fireImageUrl);
                    fireImageUrl = "";
                }
                break;
            }

            default:
                Toast.makeText(this, "Something went wrong...",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void setAnswer(String answer, String syn, String description, boolean correctness){
        camera.startPreview();
        correctAnswer = answer;
        synonyms = syn;
        descr = description;
        correct = correctness;
        inflateResult();
    }

    private void inflateResult(){
        resultView = controlInflater.inflate(R.layout.result, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup   .LayoutParams.FILL_PARENT);
        this.addContentView(resultView, layoutParamsControl);
        TextView inputText = (TextView) resultView.findViewById(R.id.input_text);
        TextView outputText = (TextView) resultView.findViewById(R.id.output_text);
        TextView correctText = (TextView) resultView.findViewById(R.id.correct_text);

        inputText.setText("Your answer: " + userAnswer);
        outputText.setText("Correct answer: " + correctAnswer);
        if(correct){
            correctText.setText("Correct!");
        }else{
            correctText.setText("Incorrect!");
        }

        Log.d("desc", descr);
    }

    public void showDescription(View view){
        Log.d("HERE", "HERE!");
        removeLayout(resultView);

        descriptionView = controlInflater.inflate(R.layout.description, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup   .LayoutParams.FILL_PARENT);
        this.addContentView(descriptionView, layoutParamsControl);

        TextView description_text = (TextView) descriptionView.findViewById(R.id.descr_text);
        description_text.setText(descr);
    }

    public void setMain(View view){
        removeLayout(descriptionView);
    }

}
