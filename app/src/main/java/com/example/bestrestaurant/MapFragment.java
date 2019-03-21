package com.example.bestrestaurant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    GoogleApiClient googleApiClient = null;

    public static final int REQUEST_LOCATION = 1;

    private GoogleMap mMap;

    private LocationRequest locationRequest;

    private Location lastLocation;

    private Marker currentUserLocationMarker;

    private static final int REQUEST_USER_LOCATION_CODE = 99;

    SharedPreferences sharedPreferences;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btnFindRestaurants = (Button) root.findViewById(R.id.btnFindRestaurants);
        btnFindRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRestaurants(v);
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkUserLocationPermission(){
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
            return false;
        }else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (googleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(this.getActivity(), "Vous n'avez pas les droits", Toast.LENGTH_SHORT).show();
                }
        }
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(1100);
            locationRequest.setFastestInterval(1100);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void findRestaurants (View v){
        Log.d("test", "Je suis dans findRestaurant");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("&location="+lastLocation.getLatitude()+","+lastLocation.getLongitude());
        stringBuilder.append("&radius="+2000);
        stringBuilder.append("&keyword="+"restaurant");
        stringBuilder.append("&key="+getResources().getString(R.string.google_api_key));

        String url = stringBuilder.toString();

        Log.d("url", url);

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces(this.getActivity());
        getNearbyPlaces.execute(dataTransfer);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (currentUserLocationMarker != null){
            currentUserLocationMarker.remove();
        }

        Boolean isAnotherLocActive = sharedPreferences.getBoolean("key_another_location", false);

        double lat;
        double lon;
        if (isAnotherLocActive){
             lat = Double.parseDouble(sharedPreferences.getString("latitude", "47.6437109"));
             lon = Double.parseDouble(sharedPreferences.getString("longitude", "6.8408862"));
        }else {
             lat = location.getLatitude();
             lon = location.getLongitude();
        }

        LatLng latLng = new LatLng(lat, lon);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Vous êtes ici !");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        currentUserLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

        if (googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
