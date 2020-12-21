package com.pluto.persolrevenuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.facebook.stetho.Stetho;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import static com.pluto.persolrevenuemanager.Constants.LOCATION_REQUEST;

public class LoginActivity extends AppCompatActivity {

    private static final int IDP_AUTH = 101;
    private static final String MY_CLIENT_ID = "TaxMobileCode";
    private static final Uri MY_REDIRECT_URI = Uri.parse("com.pluto.persolrevenuemanager:/oauth2callback");
    private Utils utils;
    private CoordinatorLayout coordinatorLayout;
    private int loginType = 0;
    private int showSnackType;
    private AuthorizationService authorizationService;
    private AuthorizationRequest authorizationRequest;
    private RevenueManager revenueManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST);
        }

        Utils.handleSSLHandshake();
        revenueManager = new RevenueManager(this);

//        if(!revenueManager.agentExists()){
//
//        }
        revenueManager.setAlarmForAllScheduledTasks();


        //setup stetho
        Stetho.initializeWithDefaults(this);

        coordinatorLayout = findViewById(R.id.loginCoor);

        utils = new Utils(this);

    }

    public void login(View view) {
        RevenueManager revenueManager = new RevenueManager(this);
        String mustHaves = utils.checkMustHaves(coordinatorLayout);
        if(!mustHaves.isEmpty()){
            Utils.showSnack(coordinatorLayout,mustHaves);
            return;
        }
        if(utils.isOnline()){
            if(revenueManager.shouldLoginOnline()){
                onlineLogin();
            } else {
                loginType = 0;
                goToPinActivity();
            }
        } else {
            if(revenueManager.agentExists()){
                loginType = 0;
                goToPinActivity();
            } else {
                Utils.showSnack(coordinatorLayout,getString(R.string.first_time_login));
            }

        }

    }

    private void goToPinActivity() {
        Intent intent = new Intent(this,PinActivity.class);
        intent.putExtra("loginType",loginType);
        startActivity(intent);
    }

    private void onlineLogin(){
        AuthorizationServiceConfiguration serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse("https://collect.localrevenue-gh.com/TaxRevenueIdp/connect/authorize"), // authorization endpoint
                        Uri.parse("https://collect.localrevenue-gh.com/TaxRevenueIdp/connect/token")); // token endpoint
        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        MY_CLIENT_ID, // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        MY_REDIRECT_URI); // the redirect URI to which the auth response is sent
        if(revenueManager.agentExists()){
            authorizationRequest = authRequestBuilder
                    .setScope("collectorapi email openid profile")
                    .build();
        } else {
            authorizationRequest = authRequestBuilder
                    .setScope("collectorapi email openid profile")
                    .setPrompt("login")
                    .build();
        }

        doAuthorization(authorizationRequest);
    }

    private void doAuthorization(AuthorizationRequest authRequest){
        authorizationService= new AuthorizationService(this);
        authorizationService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(this, 0, new Intent(this, PinActivity.class), 0),
                PendingIntent.getActivity(this, 0, new Intent(this, PinActivity.class), 0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LoginActivity.this, "Permission denied to access Location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

