package com.example.java2.firmalarharitaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class Bilgiler extends AppCompatActivity {

    int gelenMarkerId;
    TextView sirketAdi, adres, telefon, yetkiliAdiSoyadi, yetkiliMail;

    /*
        <uses-permission android:name="android.permission.CALL_PHONE" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        Arama ve internet kontrol için bunları unutmuyoruz
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilgiler);

        //Gelen id üzerinden firma bililerine ulaşırız
        gelenMarkerId = MapsActivity.markerId;
        //Toast.makeText(Bilgiler.this, "Gelen id" + gelenMarkerId, Toast.LENGTH_SHORT).show();

        sirketAdi = (TextView) findViewById(R.id.sirketAdi);
        adres = (TextView) findViewById(R.id.adres);
        telefon = (TextView) findViewById(R.id.telefon);
        yetkiliAdiSoyadi = (TextView) findViewById(R.id.yetkiliAdiSoyadi);
        yetkiliMail = (TextView) findViewById(R.id.yetkiliMail);

        String url = "http://jsonbulut.com/json/companyList.php?ref=7d638cf8daf402e8925b9651bbb68d09";
        new jsonOku(url, this).execute();

        //android:theme="@style/Theme.AppCompat.NoActionBar
        this.setTitle("Firma Bilgileri");



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
                for (int i = 0; i < bilgiler.length(); i++) {
                    JSONObject bil = bilgiler.getJSONObject(i);
                    if (bil.getInt("id") == gelenMarkerId) {
                        sirketAdi.setText(bil.getString("adi"));
                        adres.setText(bil.getString("adres"));
                        telefon.setText(bil.getString("tel"));
                        yetkiliAdiSoyadi.setText(bil.getString("yetkili_adi") + " " + bil.getString("yetkili_soyadi"));
                        yetkiliMail.setText(bil.getString("yetkili_mail"));
                    }
                }

                //Numaranın üstüne tıklayınca arasın.
                telefon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Önce uyarı mesajı versin. Belki yanlışlıkla dokunur. Hemen aramasın.
                        AlertDialog.Builder uyari = new AlertDialog.Builder(Bilgiler.this);
                        uyari.setTitle("Arama Uyarısı");
                        uyari.setMessage(telefon.getText()+" nolu numarayı aramak istermisiniz?");
                        uyari.setPositiveButton("Evet - Ara", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefon.getText()));
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(intent);
                            }
                        });
                        uyari.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        uyari.create();
                        uyari.show();

                    }
                });

                //Email gönderme işlemi
                yetkiliMail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{(String) yetkiliMail.getText()});
                        try {
                            startActivity(Intent.createChooser(i, "Email gönderiliyor..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(Bilgiler.this, "Hata! E-mail gönderilemedi.", Toast.LENGTH_SHORT).show();
                        }
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

}
