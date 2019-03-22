package com.example.bestrestaurant;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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

public class GetNearbyPlace extends AsyncTask<Object, String, String> {

    GoogleMap mMap;
    String url;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuilder stringBuilder;
    String data;
    private Context context;

    public GetNearbyPlace(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Object... params) {
        mMap = (GoogleMap) params[0];
        url = (String) params[1];

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

//            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject jsonObject = resultsArray.getJSONObject(0);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");
                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(0);

                String name_restaurant = nameObject.getString("name");
                String vicinity = nameObject.getString("vicinity");

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name_restaurant);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                markerOptions.position(latLng);
                mMap.addMarker(markerOptions);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
