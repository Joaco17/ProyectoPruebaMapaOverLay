package com.joaquin.proyectopruebamapaoverlay;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends android.support.v4.app.Fragment implements LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final float UPDATE_DISTANCE = 5;

    MapView mapView;
    GoogleMap map;
    double latitude;
    double longitude;
    LocationManager mLocationManager;
    private ArrayList<Marker> markers = new ArrayList<>();
    int numMarcador = 0;
    private ArrayList<Polyline> polylines = new ArrayList<>();


    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        Button b = (Button)v.findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia&avoid=tf");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12);
        map.animateCamera(cameraUpdate);
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {


                if (markers.size() == 0) {
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng).draggable(true));
                    Log.d("JOACO", marker.getId());
                    markers.add(marker);
                    numMarcador++;
                    LatLng latLng1 = marker.getPosition();
                    FetchUserDataTask fetchUserDataTask = new FetchUserDataTask();
                    fetchUserDataTask.execute(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(latLng1.latitude), String.valueOf(latLng1.longitude));
                } else {
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng).draggable(true));
                    Log.d("JOACO",marker.getId());
                    Marker markerAnterior = markers.get(numMarcador-1);
                    LatLng latLngAnterior = markerAnterior.getPosition();
                    markers.add(marker);
                    numMarcador++;
                    LatLng latLng1 = marker.getPosition();
                    FetchUserDataTask fetchUserDataTask = new FetchUserDataTask();
                    fetchUserDataTask.execute(String.valueOf(latLngAnterior.latitude), String.valueOf(latLngAnterior.longitude), String.valueOf(latLng1.latitude), String.valueOf(latLng1.longitude));
                }
            }
        });
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d("JOACO", marker.getId());
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                Marker markerAnterior = null;
                LatLng latLngAnterior = null;

                LatLng latLng = marker.getPosition();
                marker.setPosition(latLng);

                Marker markerNuevo;
                LatLng latLngNueva = null;

                for (int i = 0; i < markers.size(); i++) {
                    markerAnterior = markers.get(i);

                    if(marker.getId().equals(markerAnterior.getId())){
                        Log.d("JOACO", markerAnterior.getId());
                        markers.remove(i);
                        markerAnterior.remove();

                        Polyline polylineAnterior = polylines.get(i);
                        polylineAnterior.remove();
                        Log.d("JOACO", polylineAnterior.getId());
                        Log.d("JOACO", "POLY BORRADO");
                        numMarcador--;

                        markerAnterior = markers.get(numMarcador - 1);
                        latLngAnterior = markerAnterior.getPosition();

                        markerNuevo = map.addMarker(new MarkerOptions().position(latLng).draggable(true));

                        latLngNueva = markerNuevo.getPosition();
                        markers.add(markerNuevo);
                        numMarcador++;


                        FetchUserDataTask fetchUserDataTask = new FetchUserDataTask();
                        fetchUserDataTask.execute(String.valueOf(latLngAnterior.latitude), String.valueOf(latLngAnterior.longitude), String.valueOf(latLngNueva.latitude), String.valueOf(latLngNueva.longitude));

                        /*FetchUserDataTask fetchUserDataTask2 = new FetchUserDataTask();
                        fetchUserDataTask.execute(String.valueOf(latLngmovido.latitude), String.valueOf(latLngmovido.longitude), String.valueOf(latLngSiquiente.latitude), String.valueOf(latLngSiquiente.longitude));*/
                    }
                }
            }
        });
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class FetchUserDataTask extends AsyncTask<String, Void, List<LatLng>> {

        private final String LOG_TAG = FetchUserDataTask.class.getSimpleName();

        @Override
        protected List<LatLng> doInBackground(String... params) {

            StringBuilder userDataJsonStr = new StringBuilder();

            String http = "https://maps.googleapis.com/maps/api/directions/json?origin="+params[0]+","+params[1]+"&destination="+params[2]+","+params[3]+"&key=AIzaSyCyXS8izwgnQRBZ3XmEASh1b8wo-wY9H3Q&mode=walking";

            HttpURLConnection urlConnection=null;
            try {
                URL url = new URL(http);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                int HttpResult = urlConnection.getResponseCode();
                if(HttpResult == HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        userDataJsonStr.append(line);
                    }
                    br.close();

                }else{
                    System.out.println(urlConnection.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }

            try{
                return getUserDataFromJson(userDataJsonStr.toString());
            }catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private List<LatLng> getUserDataFromJson(String userDataJsonStr) throws JSONException{

            //Tranform the string into a json object
            final JSONObject json = new JSONObject(userDataJsonStr);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");

            return decodePoly(encodedString);
        }


        @Override
        protected void onPostExecute(List<LatLng> result) {

            if(result!=null){

                Polyline line = map.addPolyline(new PolylineOptions()
                                .addAll(result)
                                .width(12)
                                .color(Color.parseColor("#009688"))//Allways color
                                .geodesic(true)
                );

                polylines.add(line);
            }
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

}
