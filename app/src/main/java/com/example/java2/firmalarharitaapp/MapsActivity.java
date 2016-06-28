package com.example.java2.firmalarharitaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<Marker, Integer> mHashMap = new HashMap<>();
    static int markerId;
    //HaspMap oluşturmanın mantıgı aşagıdadır. Marker'ları ekledigimiz yerde
    //Google servislerinden yararlanmak için
    //compile 'com.google.android.gms:play-services:9.0.1' eklendi
    //xml dosyasına map:cameraZoom="10" eklendi
    //map:cameraZoom="10" sayesinde ekledigimiz son noktaya 10 zoom yaparak yaklaşıyor.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //İnternet yoksa sürekli hata versin diye sınıf içerisine ekledik
        netKontrol();

    }

    public void netKontrol(){
        if(isNetworkAvailable()){
            //İnternet varsa
            String url = "http://jsonbulut.com/json/companyList.php?ref=7d638cf8daf402e8925b9651bbb68d09";
            new jsonOku(url, this).execute();
        }else{
            //İnternet yoksa
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Baglantı hatası");
            builder.setMessage("İnternet baglantısı bulanmadı!");
            //İnternet olmadıgında bu mesajın sürekli gösterilmesi için tekrar class'ı tetkiliyoruz
            builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    netKontrol();
                }
            });
            builder.show();
        }
    }

    class jsonOku extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pr;

        String url = "";
        String data = "";

        public jsonOku(String url, Activity ac) {
            this.url = url;
            pr = new ProgressDialog(ac);
            pr.setMessage("Yükleniyor, Lütfen Bekleyiniz...");
            pr.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute(); // bekleme durumu
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid); // biten nokta. Grafiksel işlemler burada yapılır.
            try {
                JSONObject obj = new JSONObject(data);
                JSONArray company = obj.getJSONArray("Company");
                JSONObject companySifir = company.getJSONObject(0);
                JSONArray bilgiler = companySifir.getJSONArray("bilgiler");
                LatLng aa;
                for (int i = 0; i < bilgiler.length(); i++) {
                    JSONObject bil = bilgiler.getJSONObject(i);
                    if (!bil.getString("enlem").equals("") && !bil.getString("enlem").equals("0")) {
                        String enlem = bil.getString("enlem");
                        String boylam = bil.getString("boylam");
                        int id = bil.getInt("id");
                        String adi = bil.getString("adi");
                        String adres = bil.getString("adres");

                        LatLng nokta = new LatLng(Double.valueOf(enlem), Double.valueOf(boylam));
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(nokta)
                                .title("Şirket Adı: " + adi)
                                .snippet("Adres: " + adres));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(nokta));
                        //HaspMap oluşturmanın mantıgı
                        /*
                            Markerlar yani noktaların her birinin kendine ait id numarası var. Örn m1,m2 gibi.
                            Fakat bunlara şirketlerinin id'lerini eklemek için hashMap'e ihtiyac var.
                            Sırayla 1.nokta 1.id, 2.nokta 2.id şeklinde devam ediyor.
                            Zaten her nokta id'siz olamayacagı için noktalar ile id'leri birleştirmiş olduk.
                         */
                        mHashMap.put(marker, id);
                    }
                }
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(MapsActivity.this, Bilgiler.class);
                        startActivity(intent);

                        //Burada da tıklanan markar'ın id'sini aldık.
                        markerId = mHashMap.get(marker);

                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            //burada toast gibi buton gibi grafiksel yapıları çalıştıramayız.
            try {
                data = Jsoup.connect(url).timeout(30000).ignoreContentType(true).execute().body();
                //Log.d("Gelen Data : ", data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pr.dismiss(); // yükleniyor yazısını durdurur.
            }
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //İnternet kontrol sınıfımız
    private boolean isNetworkAvailable() {
        //İnternet kontrol sınıfımız
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
