package com.pluto.persolrevenuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basewin.services.ServiceManager;

public class HomeActivity extends AppCompatActivity {

    private ListView listView;
    private Utils utils;
    private ProgressBar progressBar;
    private RevenueManager revenueManager;
    private String idToken;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        revenueManager = new RevenueManager(this);
        utils = new Utils(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    revenueManager.saveGPSCoor(location.getLatitude(),location.getLongitude());
                    Log.e("location",location.getLatitude()+","+location.getLongitude());
                    Toast.makeText(HomeActivity.this, location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
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
            });
        } else{
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
        }

        idToken = getIntent().getStringExtra(idToken);

        TextView assemblyTv = findViewById(R.id.assemblyTV);
        assemblyTv.setText(revenueManager.getAssemblyName());

        listView = findViewById(R.id.itemsListView);
        progressBar = findViewById(R.id.progressBar);

        revenueManager.loadItems(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RevenueManager revenueManager = new RevenueManager(HomeActivity.this);
        switch (item.getItemId()) {
            case R.id.exit:
                revenueManager.exit();
                return true;

            case R.id.settlement:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                revenueManager.generateSettlement();
                return true;

            case R.id.billDistribution:
                revenueManager.showBillDistro();
                return true;

            case R.id.feedback:
                revenueManager.showFeedbackDialog();
                return true;

            case R.id.syncDown:
                revenueManager.syncDown(progressBar,listView);
                return true;

            case R.id.syncUp:
                revenueManager.syncUp(progressBar);
                return true;

            case R.id.reprint:
                revenueManager.showReprintDialog();
                return true;

            case R.id.logout:
                Log.e("debug","log out clicked");
                revenueManager.logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}