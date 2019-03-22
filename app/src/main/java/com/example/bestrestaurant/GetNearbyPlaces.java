package com.example.bestrestaurant;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.bestrestaurant.dummy.DummyContent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetNearbyPlaces extends AsyncTask<Object, String, String> {


    String url;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuilder stringBuilder;
    String data;
    private Context context;
    DummyContent dummyContent;

    public GetNearbyPlaces(Context context) {
        this.context = context;
        dummyContent = new DummyContent();
    }

    @Override
    protected String doInBackground(Object... params) {
        url = (String) params[0];

        try{
            URL myUrl = new URL(url);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) myUrl.openConnection();
            httpsURLConnection.connect();
            inputStream = httpsURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            stringBuilder = new StringBuilder();

            while((line=bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            data = stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        try{
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultsArray = parentObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject jsonObject = resultsArray.getJSONObject(i);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");
                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(i);

                String name_restaurant = nameObject.getString("name");
                String vicinity = nameObject.getString("vicinity");

                DummyContent.DummyItem dummyItem = new DummyContent.DummyItem(""+i, name_restaurant, vicinity);
                dummyContent.ITEMS.add(dummyItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
