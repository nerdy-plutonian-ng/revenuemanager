package com.pluto.persolrevenuemanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.provider.Contacts.SettingsColumns.KEY;
import static com.pluto.persolrevenuemanager.Constants.AGENTID;
import static com.pluto.persolrevenuemanager.Constants.AMOUNT;
import static com.pluto.persolrevenuemanager.Constants.GCRNUMBER;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.ITEMID;
import static com.pluto.persolrevenuemanager.Constants.LOCATION;
import static com.pluto.persolrevenuemanager.Constants.PAYERID;
import static com.pluto.persolrevenuemanager.Constants.PAYMENTMODE;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONDATE;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONS_TABLE;

public class Utils {

    private Context context;
    private SQLiteDatabase database;

    public Utils(Context context) {
        this.context = context;
        this.database = new DatabaseHelper(context).getWritableDatabase();
    }


    public void toastMessage(String message,int duration){
        Toast.makeText(context, message, duration).show();
    }

    //show snackbar
    public static void showSnack(CoordinatorLayout coordinatorLayout, final String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public boolean isWifiEnabled(){
        boolean enabled = false;
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()){
            //wifi is enabled
            enabled = true;
        }
        return enabled;
    }

    public boolean isDataEnabled(){
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean beingAWeek(Long oldate){
        Long currentDate = new Date().getTime();
        Long diff = currentDate - oldate;
        int days = (int) (diff / (1000*60*60*24));
        if(days >= 7){
            return true;
        } else {
            return false;
        }
    }

    public static Long getTimeSeconds(){
        return new Date().getTime();
    }

    public static String getISOdate(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return simpleDateFormat.format(date);
    }

    public static String getISOdate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return simpleDateFormat.format(date);
    }

    public static String getISOdate(String time){
        Date date = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return simpleDateFormat.format(date);
    }



    public boolean isGPSenabled() {
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
        //assert manager != null;
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }
        return false;
    }

    public String getDeviceId(){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return telephonyManager.getDeviceId();
        }else {
            return "";
        }
    }

    public static boolean isValidEmail(String email){
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static String formatMoney(double amount){
        return new DecimalFormat("#,###.00").format(amount);
    }

    public static double removeFormatting(String amount){
        try{
            return Double.parseDouble(amount.replace(",",""));
        } catch (Exception e){
            return 0.00;
        }

    }

    public static boolean isTokenExpired(long tokenExpirationDate){
        long currentDate = new Date().getTime();
        Log.e("currentDate",currentDate+"");
        Log.e("expirytDate",tokenExpirationDate+"");
        if(currentDate >= tokenExpirationDate){
            return true;
        };
        return false;
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public String checkMustHaves(CoordinatorLayout coordinatorLayout){
//        if(!isDataEnabled()){
//            Utils.showSnack(coordinatorLayout,"Switch on Mobile Data");
//            return "Switch on Mobile Data";
//        }
//        if(!isWifiEnabled()){
//            Utils.showSnack(coordinatorLayout,"Switch on Wifi");
//            return "Switch on WIFI";
//        }
//        if(!isGPSenabled()){
//            Utils.showSnack(coordinatorLayout,"Switch on GPS");
//            return "Switch on GPS";
//        }
        return "";
    }

    public static void sleepAndDisplay(View view, int delay){
        try{
            Thread.sleep(delay);
            view.setVisibility(View.GONE);
        } catch (Exception e){

        }
    }
}