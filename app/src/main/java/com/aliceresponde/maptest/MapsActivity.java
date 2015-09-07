package com.aliceresponde.maptest;

import android.app.ActionBar.LayoutParams;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText et_address;
    private ImageButton imageButton_find;
    private EditText et_address_pop;
    CircularProgressView progressView ;

    private  LatLng latLng;
    private  LatLng selected_latLng;
    private  MarkerOptions markerOptions;

    private ListView lv_addresses ;
    private List<String> list_resut_address= new ArrayList<String>();
    private List<Address> list_rest_from_gc;
    private String str_user="";

    private  PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        et_address = (EditText) findViewById(R.id.et_address);


        imageButton_find =(ImageButton) findViewById(R.id.img_b_search);
        imageButton_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_user = et_address.getText().toString();
                callPopUp();
            }
        });


    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * Creo la ventana donde estara el resultado de la busqueda
     *
     */
    public  void  callPopUp(){

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);

        popupWindow=new PopupWindow(popupView,LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Closes the popup window when touch outside of it - when looses focus
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        // Removes default black background
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Dismiss Listener
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Toast.makeText(getApplicationContext(), "out"+ str_user + selected_latLng, Toast.LENGTH_LONG).show();

            }
        });

        progressView= (CircularProgressView) popupView.findViewById(R.id.progress_view);
        progressView.setVisibility(View.INVISIBLE);

        et_address_pop = (EditText) popupView.findViewById(R.id.et_address_pop);
        et_address_pop.setText(str_user);
        et_address_pop.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Toast.makeText(getApplicationContext(), "editing Text", Toast.LENGTH_LONG).show();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        lv_addresses = (ListView) popupView.findViewById(R.id.lv_addresses);
        lv_addresses.setAdapter(
                new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.my_row,
                    new ArrayList<String>()
                ));

        //Accion para buscar  una vez se ha seleccionado una opcion
        lv_addresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                str_user= list_resut_address.get(position);
                Address aux_address= list_rest_from_gc.get(position);
                selected_latLng = new LatLng(aux_address.getLatitude(), aux_address.getLongitude());
                popupWindow.dismiss();

            }
        });

        Log.i("user1", str_user);

        if(str_user.length()!=0) {
            GeocoderTask geocoderTask = new GeocoderTask();
            geocoderTask.execute(str_user + ", bogota, co");
        }



    }
    // An AsyncTask class for accessing the GeoCoding Web Service


    private class GeocoderTask extends AsyncTask<String, Void, Void> {
        private ArrayAdapter<String> adapter;
        private Geocoder gc ;

        @Override
        protected void onPreExecute() {
            Log.i("onPreExecute","");
            adapter = (ArrayAdapter<String>)  lv_addresses.getAdapter();
            gc= new Geocoder(getApplicationContext());
            progressView.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.i("doInBackGround","----------------------------------------");

            try {
                list_rest_from_gc= gc.getFromLocationName(params[0],10);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("onPostExecute","");

            if(list_rest_from_gc.size()==0){
                Log.i("found", list_rest_from_gc.size() + "");
                Toast.makeText(getApplicationContext(), "Sin resultados de busqueda", Toast.LENGTH_LONG).show();
            }
            else {
                Log.i("found", list_rest_from_gc.size() + "");
                for (Address i : list_rest_from_gc) {
//                    Log.i("addresL[0]", i.getAddressLine(0));
//                    Log.i("addresL[1]", i.getAddressLine(1));
//                    Log.i("addresL[2]", i.getAddressLine(2));
                    String addressText = String.format("%s, %s",
                            i.getMaxAddressLineIndex() > 0 ? i.getAddressLine(0) : "",
                            i.getAddressLine(1).split(",")[0]);
                    Log.i("" + i, addressText);
                    String s  ="";
                    s=i.getAddressLine(1).length()>0? i.getAddressLine(1):"";
                    list_resut_address.add(s);

                    adapter.add(addressText);
                    lv_addresses.setAdapter(adapter);
                }
            }

            progressView.setVisibility(View.GONE);
        }
    }

}
