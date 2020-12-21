package com.pluto.persolrevenuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.pluto.persolrevenuemanager.Constants.AGENTID;
import static com.pluto.persolrevenuemanager.Constants.AGENTTOKEN;
import static com.pluto.persolrevenuemanager.Constants.AGENT_TABLE;
import static com.pluto.persolrevenuemanager.Constants.ASSEMBLYLOGO;
import static com.pluto.persolrevenuemanager.Constants.ASSEMBLYNAME;
import static com.pluto.persolrevenuemanager.Constants.CASHLIMIT;
import static com.pluto.persolrevenuemanager.Constants.COLLECTEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.DOMAIN;
import static com.pluto.persolrevenuemanager.Constants.FIRSTNAME;
import static com.pluto.persolrevenuemanager.Constants.GET_AGENT_SUMMARY;
import static com.pluto.persolrevenuemanager.Constants.LASTNAME;
import static com.pluto.persolrevenuemanager.Constants.LASTSYNC;
import static com.pluto.persolrevenuemanager.Constants.PIN;
import static com.pluto.persolrevenuemanager.Constants.RECEIPTNUMBER;
import static com.pluto.persolrevenuemanager.Constants.SETTLEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.TOKENEXPIRY;
import static com.pluto.persolrevenuemanager.Constants.ZONE;

public class PinActivity extends AppCompatActivity {

    private Agent agent;
    private double cashLimit, collectedToDate, settledToDate;
    String firstName, lastName, assemblyName, assemblyLogo, agentToken, tokenExpiry, lastsync;
    int agentID;
    private AuthorizationService authService;
    private JWT jwt;
    private RevenueManager revenueManager;
    private Button saveButton;
    private CoordinatorLayout coordinatorLayout;
    private Utils utils;
    private TextView titleTV;
    private TextInputEditText pinBoxEt;
    private TextInputLayout pinBoxTIL;
    private int count = 0;
    private boolean newPin = false;
    private int pin;
    private boolean agentUpdated = false;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);


        saveButton = findViewById(R.id.saveBtn);
        coordinatorLayout = findViewById(R.id.pinCoor);
        titleTV = findViewById(R.id.titleTV);
        pinBoxEt = findViewById(R.id.pinBoxEt);
        pinBoxTIL = findViewById(R.id.pinBoxTIL);

        utils = new Utils(this);
        revenueManager = new RevenueManager(this);
        agent = new Agent();

        Log.e("Token", revenueManager.getAgentToken());

        Intent intent = getIntent();
        if(intent != null){
            switch (intent.getIntExtra("loginType",-1)){
                case 0:
                    showRelevantData(0);
                    newPin = false;
                    break;
                case -1:
                    exchangeToken();
            }
        }

    }

    public void resetPassword(View view) {

    }

    public void goToLogin(int type){
        authService = null;
        Intent intent = new Intent(this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.getIntExtra("type",type);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(authService != null){
            authService.dispose();
            authService = null;
        }

    }

    public void savePIN(View view) {
        if(pinBoxEt.getText().toString().isEmpty()){
            pinBoxTIL.setError("This field cannot be empty");
            Utils.showSnack(coordinatorLayout,"Please enter your PIN");
            return;
        }
        if(newPin){
            if(count == 0){
                pin = Integer.parseInt(pinBoxEt.getText().toString().trim());
                count++;
                pinBoxEt.getText().clear();
                Utils.showSnack(coordinatorLayout,"Enter your PIN again");
            } else {
                if(pin != Integer.parseInt(pinBoxEt.getText().toString().trim())){
                    pinBoxTIL.setError("PINs do not match");
                    Utils.showSnack(coordinatorLayout,"Enter the same PIN twice");
                } else {
                    agent.setPin(Integer.parseInt(pinBoxEt.getText().toString().trim()));
                    if(revenueManager.agentExists()){
                        if(revenueManager.replaceAgent(agent)){
                            goToHome();
                        } else {
                            Utils.showSnack(coordinatorLayout,"Failed saving agent");
                        }
                    } else {
                        if(revenueManager.saveAgent(agent,1)){
                            goToHome();
                        } else {
                            Utils.showSnack(coordinatorLayout,"Failed saving agent");
                        }
                    }
                }
            }

        } else {
            if(revenueManager.login(Integer.parseInt(pinBoxEt.getText().toString()))){
                if(agentUpdated){
                    revenueManager.replaceAgent(agent,"");
                }
                goToHome();
            } else {
                pinBoxTIL.setError("Wrong PIN");
                Utils.showSnack(coordinatorLayout,"You entered a wrong PIN");
            }
        }
    }

    private void exchangeToken(){
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
        if (resp != null) {
            // authorization completed
            authService = new AuthorizationService(this);
            authService.performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override public void onTokenRequestCompleted(
                                final TokenResponse resp, AuthorizationException ex) {
                            if (resp != null) {
                                // exchange succeeded
                                JWT jwt = new JWT(resp.accessToken);
                                Log.e("PRMtoken",resp.accessToken);
                                idToken = resp.idToken;
                                agent.setFirstName(jwt.getClaim("FirstName").asString());
                                agent.setLastName(jwt.getClaim("LastName").asString());
                                agent.setId(jwt.getClaim("pkId").asString());
                                agent.setCashLimit(Double.parseDouble(jwt.getClaim("CashLimit").asString()));
                                agent.setAssemblyName(jwt.getClaim("AssemblyName").asString());
                                agent.setAssemblyLogo(jwt.getClaim("AssemblyLogo").asString());
                                agent.setToken(resp.accessToken);
                                agent.setTokenExpiry(String.valueOf(jwt.getExpiresAt().getTime()));
                                RequestQueue queue = Volley.newRequestQueue(PinActivity.this);
                                Log.e("url",DOMAIN + GET_AGENT_SUMMARY + agent.getId());
                                Log.e("agentId",agent.getId()+"");
                                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                                        DOMAIN + GET_AGENT_SUMMARY + agent.getId(),
                                        null,
                                        new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                try {
                                                    JSONObject jsonObject = response.getJSONObject(0);
                                                    agent.setCollectedToDate(jsonObject.getDouble("TotalAmountCollected"));
                                                    agent.setSettledToDate(jsonObject.getDouble("TotalAmountSettled"));
                                                    agentUpdated = true;
                                                    if(revenueManager.isSameUser(agent.getId())){
                                                        newPin = false;
                                                        showRelevantData(0);
                                                    } else {
                                                        newPin = true;
                                                        showRelevantData(1);
                                                    }
                                                } catch (JSONException e) {
                                                    Log.e("PRM",e.toString());
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("PRM",error.toString());
                                            }
                                        }){
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> headers = new HashMap<>();
                                        // Put access token in HTTP request.
                                        headers.put("Authorization", "Bearer " + resp.accessToken);
                                        return headers;
                                    }
                                };
                                saveButton.setText("Please wait...");
                                queue.add(jsonArrayRequest);

//                                titleTV.setText(R.string.setup_pin);
//                                saveButton.setVisibility(View.VISIBLE);
//                                Utils.showSnack(coordinatorLayout,"Set up your 4 digit PIN");
                            } else {
                                // authorization failed, check ex for more details
                                //goToLogin();
                                Log.e("PRM","Auth failed on second step");
                            }
                        }
                    });
        } else {
            // authorization failed, check ex for more details
            Log.e("PRM","Auth failed");
            goToLogin(0);
        }
    }

    private void showRelevantData(int type){
        if(type == -1 || type == 1){
            titleTV.setText(R.string.setup_pin);
            Utils.showSnack(coordinatorLayout,"Set up your PIN");
            saveButton.setText(R.string.save);
        } else if(type == 0) {
            titleTV.setText(R.string.enter_pin);
            Utils.showSnack(coordinatorLayout,"Enter your PIN");
            saveButton.setText(R.string.login);
        }
        pinBoxTIL.setHint("Enter PIN");
        saveButton.setEnabled(true);
    }

    private void goToHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(authService != null){
            authService.dispose();
            authService = null;
        }
        intent.putExtra("idToken",idToken);
        startActivity(intent);
    }


}