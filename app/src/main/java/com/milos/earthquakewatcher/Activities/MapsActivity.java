package com.milos.earthquakewatcher.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.milos.earthquakewatcher.Model.EarthQuake;
import com.milos.earthquakewatcher.QuakesListActivity;
import com.milos.earthquakewatcher.R;
import com.milos.earthquakewatcher.UI.CustomInfoWindow;
import com.milos.earthquakewatcher.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "Tag";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
    private BitmapDescriptor[] iconColors;
    private Button showList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showList = findViewById(R.id.showListBtn);

        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, QuakesListActivity.class));
            }
        });

        iconColors = new BitmapDescriptor[] {

                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)


        };

        queue = Volley.newRequestQueue(this);

        getEarthQuakes();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .title("Hello"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
            }

        }

        // Add a marker in Sydney and move the camera
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public void getEarthQuakes() {

        final EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0; i<Constants.LIMIT; i++) {
                                JSONObject properties  = features.getJSONObject(i).getJSONObject("properties");

                                //get geom object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");

                                //get coordinates array

                                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                                    double lon = coordinates.getDouble(0);
                                    double lat = coordinates.getDouble(1);

                                    earthQuake.setPlace(properties.getString("place"));
                                    earthQuake.setType(properties.getString("type"));
                                    earthQuake.setLat(lat);
                                    earthQuake.setLon(lon);
                                    earthQuake.setTime(properties.getLong("time"));
                                    earthQuake.setMagnitude(properties.getDouble("mag"));
                                    earthQuake.setDetailLink(properties.getString("detail"));

                                    java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();

                                    String formattedDate = dateFormat
                                            .format(new Date(properties.getLong("time")).getTime());

                                    MarkerOptions markerOptions = new MarkerOptions();

                                    markerOptions.icon(iconColors[Constants.randomInt(iconColors.length, 0)]);
                                    markerOptions.title(earthQuake.getPlace());
                                    markerOptions.position(new LatLng(earthQuake.getLat(), earthQuake.getLon()));
                                    markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude()
                                            + "\n" + "Date: " + formattedDate);

                                    //Add a circle to markers that have mag > x

                                if (earthQuake.getMagnitude()>=4.0) {

                                    CircleOptions circleOptions = new CircleOptions();

                                    circleOptions.center(new LatLng(earthQuake.getLat(), earthQuake.getLon()));
                                    circleOptions.radius(90000);
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.fillColor(Color.BLACK);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                    mMap.addCircle(circleOptions);

                                }

                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.setTag(earthQuake.getDetailLink());
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),5));


                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);


    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        getQuakeDetails(marker.getTag().toString());

    }

    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for ( int i=0; i<geoserve.length(); i++) {

                        JSONObject geoserveObj = geoserve.getJSONObject(i);
                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                        detailsUrl = geoJsonObj.getString("url");
                    }

                    getMoreDetails(detailsUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }

    public void getMoreDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                dialogBuilder = new AlertDialog.Builder(MapsActivity.this);

                View view = getLayoutInflater().inflate(R.layout.popup, null);

                Button dismissButton = view.findViewById(R.id.dismissPop);
                Button dismissButtonTop = view.findViewById(R.id.dismissPopup);
                TextView popList = view.findViewById(R.id.popList);
                WebView htmlPop = view.findViewById(R.id.htmlWebView);

                try {

                    if (response.has("tectonicSummary") && response.getString("tectonicSummary") != null) {

                        JSONObject tectonic = response.getJSONObject("tectonicSummary");

                        if (tectonic.has("text") && tectonic.getString("text") != null) {

                            String text  = tectonic.getString("text");

                            htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);

                        }


                    }
                    JSONArray cities = response.getJSONArray("cities");

                    StringBuilder stringBuilder = new StringBuilder();


                    for (int i = 0; i < cities.length(); i++) {

                        JSONObject citiesObj = cities.getJSONObject(i);



                        stringBuilder.append("City: " + citiesObj.getString("name") + "\n" +
                                "Distance: " + citiesObj.getString("distance") + "\n" +
                                "Population: " + citiesObj.get("population"));

                        stringBuilder.append("\n\n");


                    }
                    popList.setText(stringBuilder);

                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            alertDialog.dismiss();

                        }
                    });

                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            alertDialog.dismiss();
                        }
                    });

                    dialogBuilder.setView(view);
                    alertDialog = dialogBuilder.create();
                    alertDialog.show();


                } catch (JSONException e) {

                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==1) {

            if (resultCode == RESULT_OK) {

                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom);
            }
        }

    }
}

