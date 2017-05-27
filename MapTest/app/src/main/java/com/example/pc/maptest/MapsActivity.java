package com.example.pc.maptest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    int markerCounter=0;
    private final int MY_PERMISSIONS_REQUEST_ACESS_LOCATION = 0;
    private EditText editText;
    SharedPreferences sharedPreferences = null;
    Marker marker = null;
    String markerIdentity = "";
    String markerCounterStr, strButton;
    private List<Marker> markerList;
    LatLng position1;
    Button btnStartPolygon;
    EditText markerText;
    Button btnClear;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Map loding.....
        btnStartPolygon = (Button) findViewById(R.id.btnPolygon);
        btnClear=(Button) findViewById(R.id.btnClear);
       //initialize Shared Preferences
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        //initialize the markerList
        if(markerList == null){
            markerList = new ArrayList<Marker>();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapLongClick(LatLng latitude_Longitude) {
        editText = (EditText) findViewById(R.id.editText);
        String text = String.valueOf(editText.getText());
        if (text.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Please Write a marker title First!" , Toast.LENGTH_SHORT)
                    .show();

        }
        else {

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latitude_Longitude)
                    .title(text)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //add the new marker to the list
            markerList.add(marker);

            //clear the edit text
            editText.setText(null);
        }

        /*EditText markerText = (EditText) findViewById(R.id.editText);
        markerIdentity = markerText.getText().toString();
        if (markerIdentity.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Please Write a marker title First!" , Toast.LENGTH_SHORT)
                    .show();

        }
        else{
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latitude_Longitude)
                    .title(markerIdentity)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            marker = mMap.addMarker(markerOptions);
            markerText.setText("");

            Toast.makeText(getApplicationContext(),
                    "New marker "+ markerIdentity + "added !" , Toast.LENGTH_SHORT)
                    .show();

            markerCounter++;
            markerCounterStr = Integer.toString(markerCounter);

            if (latitude_Longitude.latitude != 0){ //&& latitude_Longitude.longitude != 0) { + Integer.toString((markerCounter - 1))
                sharedPreferences.edit().putString("Lat" , Double.toString(latitude_Longitude.latitude)).apply();
                sharedPreferences.edit().putString("Lng" , Double.toString(latitude_Longitude.longitude)).apply();
                sharedPreferences.edit().putString("myLocation", markerIdentity).apply();
                sharedPreferences.edit().putInt("markerCounter", markerCounter).apply();
            }
        }*/
    }

    private void loadMarkers() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int size = sharedPreferences.getInt("listSize", 0);
        if(size > 0) {
            for (int i = 0; i < size; i++) {
                double lat = (double) sharedPreferences.getFloat("lat" + i, 0);
                double longit = (double) sharedPreferences.getFloat("long" + i, 0);
                String title = sharedPreferences.getString("title" + i, "NULL");
                markerList.add(mMap.addMarker(new MarkerOptions().position(new LatLng(lat, longit)).title(title)));
            }
        }
    }

     private LatLng getCentroid(List<LatLng> positions) {
        double centerX = 0;
        double centerY = 0;
        for (LatLng position : positions) {
            centerX += position.latitude;
            centerY += position.longitude;
        }
        LatLng center = new LatLng(centerX / positions.size(), centerY / positions.size());
        Toast.makeText(this, "Centroid Lat: "+center.latitude+" Long: "+center.longitude, Toast.LENGTH_LONG).show();
        return center;
    }

    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public void startPolygon(View v){
        if(markerList.size()>2) {
            if (btnStartPolygon.isEnabled()) {
                PolygonOptions polygonOptions = new PolygonOptions();
                for (Marker marker : markerList) {
                    polygonOptions.add(marker.getPosition());
                }
                LatLng centroid = getCentroid(polygonOptions.getPoints());
                Polygon polygon = mMap.addPolygon(polygonOptions.strokeWidth(10).fillColor(Color.argb(20, 80, 180, 255)).strokeColor(Color.GREEN));
                double area = SphericalUtil.computeArea(polygonOptions.getPoints());
                area = roundTwoDecimals(area);
                String unit = " m²";
                if (area > 1000) {
                    area = roundTwoDecimals(area / 1000);
                    unit = " km²";
                }
                mMap.addMarker(new MarkerOptions().position(centroid).title(String.valueOf(area) + unit));
                btnStartPolygon.setEnabled(false);
                SavePreferences();
               }
        }
       else {

           Toast.makeText(getApplicationContext(), "Please add more markers", Toast.LENGTH_SHORT).show();
       }

       }
    //http://stackoverflow.com/questions/25438043/store-google-maps-markers-in-sharedpreferences
    private void SavePreferences(){
        editor = sharedPreferences.edit();
        editor.putInt("listSize", markerList.size());
        for(int i = 0; i <markerList.size(); i++){
            editor.putFloat("lat"+i, (float) markerList.get(i).getPosition().latitude);
            editor.putFloat("long"+i, (float) markerList.get(i).getPosition().longitude);
            editor.putString("title"+i, markerList.get(i).getTitle());
        }
        editor.commit();
    }
    private LatLng computeCentroid(List<LatLng> positions) {
        double centerX = 0;
        double centerY = 0;
        for (LatLng position : positions) {
            centerX += position.latitude;
            centerY += position.longitude;
        }
        LatLng center = new LatLng(centerX / positions.size(), centerY / positions.size());
        Toast.makeText(this, "Centroid Lat: " + center.latitude + "\n Lng: " + center.longitude,
                Toast.LENGTH_SHORT).show();
        return center;
    }

    public void clearMap(View view){
           if(!btnStartPolygon.isEnabled()) {
            mMap.clear();
            markerList = new ArrayList<Marker>();
            sharedPreferences.edit().clear().apply();
            markerCounterStr = null;
            markerCounter = 0;
            btnStartPolygon.setEnabled(true);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadMarkers();
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (isGPSEnabled(getApplicationContext())) {
                    return false;
                } else Toast.makeText(getApplicationContext(),
                        "Please enable your GPS for detecting your current position!",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        mMap.setOnMapLongClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
             //public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                      //int[] grantResults)
            //to handle the case where the user grants the permission. See the documentation
           // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACESS_LOCATION);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

    public boolean isGPSEnabled(Context mContext) {   //http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}