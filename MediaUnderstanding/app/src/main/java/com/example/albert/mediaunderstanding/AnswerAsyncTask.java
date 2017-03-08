package com.example.albert.mediaunderstanding;

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

    Context context;
    MainActivity activity;


    public AnswerAsyncTask(MainActivity activity){
        this.activity = activity;
        this.context = this.activity.getApplicationContext();
    }

    @Override
    protected String doInBackground(String... params) {
        return HttpRequestHelper.downloadFromServer(params[0]);
    }

    protected void onPreExecute() {

    }

    // onPostExecute()
    protected void onPostExecute(String result) {
        Log.d("result", result);

        super.onPostExecute(result);
        if (result.length() == 0) {
            Toast.makeText(context, "No results found!", Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                JSONObject respObj = new JSONObject(result);
                String label = respObj.getString("label");
                JSONArray synonyms = respObj.getJSONArray("synomnyms");
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private float[] stringToFloatArray(String coordinates){
        coordinates = coordinates.replace("[", "");
        coordinates = coordinates.replace("]", "");
        coordinates = coordinates.replace(",", " ");
        String[] split_coords = coordinates.split("\\s+");
        float[] finalCoords = new float[2];
        finalCoords[0] = Float.parseFloat(split_coords[1]);
        finalCoords[1] = Float.parseFloat(split_coords[0]);
        return finalCoords;
    }
}
