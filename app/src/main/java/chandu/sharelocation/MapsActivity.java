package chandu.sharelocation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import chandu.sharelocation.api.ServiceApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Boolean mLocationPermission = false;
    private Boolean mLocationOn = false;
    private Context context = MapsActivity.this;
    private LocationManager locationManager;
    public Button bottomSheetBtn;
    private Boolean onOff=true;
    private Boolean isTicking=false;
    private int id=0;
    private CountDownTimer countDownTimer;
    private MarkerOptions marker=null;
    private  Button share,send;
    BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bottomSheetBtn=(Button)findViewById(R.id.btn);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkPermissions();


    }

    protected void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 1000);

            } else {
                Toast.makeText(getApplicationContext(), "SDK Not Supported..!", Toast.LENGTH_SHORT).show();

            }

        } else {
            mLocationPermission = true;
            mMap.setMyLocationEnabled(true);
            //checkLocationOn();
            allSet();

        }
    }

//    protected void checkLocationOn() {
//        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("GPS NOT FOUND");  // GPS not found
//            builder.setMessage("Do you want to enable GPS..?"); // Want to enable?
//            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1000);
//
//                }
//            });
//            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Toast.makeText(MapsActivity.this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
//                    checkLocationOn();
//                }
//            });
//            builder.create().show();
//
//        } else {
//            mLocationOn = true;
//            Toast.makeText(MapsActivity.this, "Fetching your location..!", Toast.LENGTH_SHORT).show();
//            allSet();
//
//        }
//    }

    protected void allSet() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        final ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Fetching your location..!");
        progressDialog.show();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude,longitude);
                Retrofit retrofit =new Retrofit.Builder()
                        .baseUrl("http://testbed2.riktamtech.com:3000")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                final ServiceApi serviceApi=retrofit.create(ServiceApi.class);
                Retrofit retrofit2 =new Retrofit.Builder()
                        .baseUrl("http://testbed2.riktamtech.com:3000")
                        .build();
                final ServiceApi serviceApi2=retrofit2.create(ServiceApi.class);
                if(isTicking) {
                    Log.v("insideticking:",String.valueOf(id));

                    if(id==0){
                        Log.v("if id 0:",String.valueOf(id));

                        Call<ResponseBody> call = serviceApi2.insertLocation(location.getLatitude(),location.getLongitude());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    JSONArray jsonArray = new JSONArray(response.body().string());
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    id=jsonObject.getInt("MAX");
                                    share.setText("Click to stop sharing");
                                    send.setVisibility(View.VISIBLE);
                                    Log.v("create:",String.valueOf(id));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(MapsActivity.this,"Network N.A, Trying again please wait..!",Toast.LENGTH_SHORT).show();
                                share.setText("Click to stop Ticking");
                                send.setVisibility(View.VISIBLE);
                            }
                        });
                    }else {
                    Call<ResponseBody> call = serviceApi.updateLocation(id, location.getLatitude(), location.getLongitude());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            share.setText("Click to stop sharing");
                            send.setVisibility(View.VISIBLE);

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                            Toast.makeText(MapsActivity.this,"Network N.A, Trying again please wait..!",Toast.LENGTH_SHORT).show();
                            share.setText("Click to stop Ticking");
                            send.setVisibility(View.VISIBLE);
                        }
                    });
                    }
                }
                if(marker==null){
                    marker= new MarkerOptions().position(latLng).title("marker");

                    mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f ));


                }
                else {
                    mMap.clear();
                    marker= new MarkerOptions().position(latLng).title("marker");

                    mMap.addMarker(marker);

                }
                bottomSheetBtn.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
                bottomSheetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapsActivity.this);
                        View parentView = getLayoutInflater().inflate(R.layout.bottomsheet,null);
                        bottomSheetDialog.setContentView(parentView);
                        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                        bottomSheetBehavior.setPeekHeight(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,100,getResources().getDisplayMetrics())
                        );
                         share = (Button) parentView.findViewById(R.id.share);
                        send =(Button) parentView.findViewById(R.id.send);

                        if(isTicking){
                            share.setText("Click to stop sharing");
                            send.setVisibility(View.VISIBLE);
                        }
                        bottomSheetDialog.show();

                        final Spinner timeSpinner = (Spinner) parentView.findViewById(R.id.timespinner);
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MapsActivity.this,
                                R.array.time,android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        timeSpinner.setAdapter(adapter);


                        send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi ! Link for my live location... " +
                                        "http://testbed2.riktamtech.com:3000/googleMap/"+id);
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                            }
                        });



                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(onOff){
                                    onOff=false;
                                    Intent i =new Intent(MapsActivity.this,GPS_Service.class);
                                    startService(i);
                                    countDownTimer = new CountDownTimer(Integer.parseInt(timeSpinner.getSelectedItem().toString())*60*1000, 15000) {

                                        public void onTick(final long millisUntilFinished) {

                                            Toast.makeText(MapsActivity.this,millisUntilFinished / 1000 +" sec remaining..",Toast.LENGTH_SHORT).show();

                                            isTicking=true;

                                        }

                                        public void onFinish() {
                                            share.setText("share");
                                            isTicking=false;
                                            onOff=true;
                                            send.setVisibility(View.GONE);
                                            Call<ResponseBody> call = serviceApi.updateLocation(id,0.0,0.0);
                                            call.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    Toast.makeText(MapsActivity.this,"Done sharing..!",Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent(MapsActivity.this,GPS_Service.class);
                                                    stopService(i);
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    Toast.makeText(MapsActivity.this,"Done sharing but couldn't reset location..!",Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent(MapsActivity.this,GPS_Service.class);
                                                    stopService(i);

                                                }
                                            });

                                        }
                                    }.start();

                                }
                                else{
                                    onOff = true;
                                    isTicking=false;
                                    countDownTimer.cancel();
                                    share.setText("share");
                                    send.setVisibility(View.GONE);
                                    Intent i = new Intent(MapsActivity.this,GPS_Service.class);
                                    stopService(i);
                                    if(id !=0) {
                                        Call<ResponseBody> call = serviceApi.updateLocation(id, 0.0, 0.0);
                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toast.makeText(MapsActivity.this, "Network failure couldn't reset location..!", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(MapsActivity.this, GPS_Service.class);
                                                stopService(i);
                                            }
                                        });
                                    }
                                    Toast.makeText(MapsActivity.this,"Stopped",Toast.LENGTH_SHORT).show();

                                }


                            }
                        });

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
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            // Make sure the request was successful
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationOn = true;
                Toast.makeText(MapsActivity.this, "Fetching your location..!", Toast.LENGTH_SHORT).show();
                allSet();


            }
            else{
                Toast.makeText(MapsActivity.this,"Please turn on GPS",Toast.LENGTH_SHORT).show();
               // checkLocationOn();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.v("location:",intent.getExtras().get("coordinates").toString());
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000){
            for (int i = 0; i < grantResults.length; i++) {
                mLocationPermission = false;


                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermission = true;

                }
            }
                if (mLocationPermission) {

                    Toast.makeText(getApplicationContext(),"Permission Granted...!",Toast.LENGTH_SHORT).show();
                    //checkLocationOn();
                    allSet();


                }else{
                    Toast.makeText(getApplicationContext(),"Permission Rejected...!",Toast.LENGTH_SHORT).show();

                }



            }
    }
}
