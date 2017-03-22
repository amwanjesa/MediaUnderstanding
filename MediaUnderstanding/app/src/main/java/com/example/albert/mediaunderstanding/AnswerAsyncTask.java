package com.example.albert.mediaunderstanding;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.albert.mediaunderstanding.HttpRequestHelper;
import com.example.albert.mediaunderstanding.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Albert Mwanjesa 16/12/2016
 * This class takes care of getting all the data for parking garages in Amsterdam.
 * using the HttpRequestHelper. The JSONObject is altered to get garages after
 * execution.
 */

class AnswerAsyncTask extends AsyncTask<String, Integer, String> {

    private ProgressDialog dialog;
    Context context;
    CameraActivity activity;


    public AnswerAsyncTask(CameraActivity activity){
        dialog = new ProgressDialog(activity);
        this.activity = activity;
        this.context = this.activity.getApplicationContext();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return HttpRequestHelper.downloadFromServer(params[0]);
    }

    protected void onPreExecute() {
        dialog.setMessage("Checking your answer.");
        dialog.show();
    }

    // onPostExecute()
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d("result", result);

        super.onPostExecute(result);
        if (result.length() == 0) {
            Toast.makeText(context, "No results found!", Toast.LENGTH_SHORT).show();
            this.activity.camera.startPreview();
        }
        else {
            try {
                JSONObject respObj = new JSONObject(result);
                String label = respObj.getString("label");
                JSONArray synonyms = respObj.getJSONArray("label_synonyms");
                String descr = respObj.getString("label_desc");
                boolean correct = respObj.getBoolean("similarity");
                String syn = "";
                for(int i=0; i< synonyms.length(); i++){
                    if(i == 0){
                        syn += synonyms.getString(i);
                    }else{
                        syn += ", "  + synonyms.getString(i);
                    }
                }

                this.activity.setAnswer(label, syn, descr, correct);

            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
