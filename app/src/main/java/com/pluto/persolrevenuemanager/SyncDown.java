package com.pluto.persolrevenuemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.inspector.protocol.module.DOM;
import com.facebook.stetho.inspector.protocol.module.Database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.pluto.persolrevenuemanager.Constants.ACCOUNTCODE;
import static com.pluto.persolrevenuemanager.Constants.AGENT_TABLE;
import static com.pluto.persolrevenuemanager.Constants.BALANCEBROUGHTFORWARD;
import static com.pluto.persolrevenuemanager.Constants.BILLID;
import static com.pluto.persolrevenuemanager.Constants.BUSINESSES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.CASHLIMIT;
import static com.pluto.persolrevenuemanager.Constants.COLLECTEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.CURRENTCHARGE;
import static com.pluto.persolrevenuemanager.Constants.DOMAIN;
import static com.pluto.persolrevenuemanager.Constants.FIRSTNAME;
import static com.pluto.persolrevenuemanager.Constants.GET_AGENT_BY_USERNAME;
import static com.pluto.persolrevenuemanager.Constants.GET_AGENT_SUMMARY;
import static com.pluto.persolrevenuemanager.Constants.GET_ALL_ITEMS;
import static com.pluto.persolrevenuemanager.Constants.GET_ALL_RATES;
import static com.pluto.persolrevenuemanager.Constants.GET_ALL_REASONS;
import static com.pluto.persolrevenuemanager.Constants.GET_BUSINESSES;
import static com.pluto.persolrevenuemanager.Constants.GET_PROPERTIES;
import static com.pluto.persolrevenuemanager.Constants.GET_RELATIONSHIPS;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.ITEMS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.LASTNAME;
import static com.pluto.persolrevenuemanager.Constants.LASTPAYMENTDATE;
import static com.pluto.persolrevenuemanager.Constants.LONG;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PAIDTHISYEAR;
import static com.pluto.persolrevenuemanager.Constants.PASSWORD;
import static com.pluto.persolrevenuemanager.Constants.PROPERTIES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.SETTLEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.TIN;
import static com.pluto.persolrevenuemanager.Constants.TOTALAMOUNTOWED;
import static com.pluto.persolrevenuemanager.Constants.TYPE;
import static com.pluto.persolrevenuemanager.Constants.USERNAME;

public class SyncDown {

    private WeakReference<Context> weakReference;
    private RequestQueue queue;
    private Utils utils;
    private SQLiteDatabase database;
    private RevenueManager revenueManager;
    private ProgressBar progressBar;
    private ListView itemListView;

    public SyncDown(Context context, ProgressBar progressBar, ListView itemListView){
        this.weakReference = new WeakReference<>(context);
        this.queue = Volley.newRequestQueue(weakReference.get());
        this.utils = new Utils(weakReference.get());
        database = new DatabaseHelper(weakReference.get()).getWritableDatabase();
        revenueManager = new RevenueManager(weakReference.get());
        this.progressBar = progressBar;
        if(this.progressBar != null){
            this.progressBar.setProgress(0);
            this.progressBar.setVisibility(View.VISIBLE);
            this.itemListView = itemListView;
        }
    }

    public void masterSync(){
        if(utils.isOnline()){
            getItems();
        }else {
            utils.toastMessage("Device is Offline",Toast.LENGTH_LONG);
        }

    }

    private void getItems(){
        Log.e("test","in items func");
        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String url = DOMAIN + GET_ALL_ITEMS;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("test","in items func response");
                        new GetItems(response).execute();
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
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(jsonArrayRequest);
    }

    private void getRates(){
        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String rates_items = DOMAIN + GET_ALL_RATES;
        JsonArrayRequest ItemsjsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                rates_items,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("pra","about to save rates");
                        new GetRates(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(ItemsjsonArrayRequest);
    }

    private void getReasons(){
        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String url_items = DOMAIN + GET_ALL_REASONS;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url_items,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        new GetReasons(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(jsonArrayRequest);
    }

    private void getRelationships(){
        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String url_properties = DOMAIN + GET_RELATIONSHIPS;
        JsonArrayRequest businessesjsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url_properties,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("pra","about to save relationships");
                        new GetRelationships(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Log.e("error_gettingBusinesses",error.getMessage());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(businessesjsonArrayRequest);
    }

    private void getBusinesses(){

        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String url = DOMAIN + GET_BUSINESSES;
        JsonArrayRequest businessesjsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("pra","about to save "+response.length()+" businesses");
                        new GetBusinesses(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error_gettingBusinesses",error.getMessage());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(businessesjsonArrayRequest);
    }

    private void getProperties(){
        RequestQueue queue = Volley.newRequestQueue(weakReference.get());
        String url_properties = DOMAIN + GET_PROPERTIES;
        JsonArrayRequest propertiesjsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url_properties,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("pra","about to save "+response.length()+" properties");
                        new GetProperties(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error_gettingProps",error.getMessage());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("PRM",revenueManager.getAgentToken());
                headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                return headers;
            }
        };
        queue.add(propertiesjsonArrayRequest);
    }

    private class GetItems extends AsyncTask<Void,Void,Boolean>{

        private JSONArray items;
        private boolean error = false;

        public GetItems(JSONArray items) {
            this.items = items;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject jsonObject = items.getJSONObject(i);
                    saveItem(jsonObject.getInt("pkId"),jsonObject.getString("szItemName"),
                            jsonObject.getDouble("nPrice"),jsonObject.getInt("iStatus"));
                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(error){

            } else {
                if(progressBar != null){
                    progressBar.setProgress(10);
                }
                getRates();
            }
        }

        private void saveItem(int id, String itemName, Double price, int status) {
            ContentValues itemValues = new ContentValues();
            itemValues.put("_id", id);
            itemValues.put("NAME", itemName);
            itemValues.put("BASEAMOUNT", price);
            try {
                Cursor cursor = database.query(ITEMS_TABLE,
                        new String[]{"_id"},
                        "_id = ?",
                        new String[]{String.valueOf(id)},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    if (status == 0) {
                        database.update(ITEMS_TABLE, itemValues, "_id = ?", new String[]{String.valueOf(id)});
                    } else {
                        database.delete(ITEMS_TABLE, "_id = ?", new String[]{String.valueOf(id)});
                    }

                } else {
                    if (status == 0) {
                        database.insert(ITEMS_TABLE, null, itemValues);
                    }
                }
                cursor.close();
            } catch (Exception e) {
            }
        }
    }

    private class GetRates extends AsyncTask<Void,Void,Boolean>{

        private JSONArray rates;
        private boolean error = false;

        public GetRates(JSONArray rates) {
            this.rates = rates;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                for (int i = 0; i < rates.length(); i++) {
                        JSONObject jsonObject = rates.getJSONObject(i);
                        int id = Integer.parseInt(jsonObject.getString("pkId"));
                        int itemId = Integer.parseInt(jsonObject.getString("iItemId"));
                        String rateName = jsonObject.getString("szRateName");
                        double factor = jsonObject.getDouble("nFactor");
                        saveRates( id, itemId, rateName, factor);
                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(error){

            } else {
                if(progressBar != null){
                    progressBar.setProgress(20);
                }
                getReasons();
            }
        }

        private void saveRates(int id, int itemId, String rateName, double factor) {

            ContentValues contentValues = new ContentValues();
            contentValues.put("_id",id);
            contentValues.put("ITEMID",itemId);
            contentValues.put("NAME",rateName);
            contentValues.put("FACTOR",factor);
            try{
                Cursor cursor = database.query("RATES",
                        new String[]{"_id"},
                        "_id = ?",
                        new String[]{String.valueOf(id)},
                        null,null,null);
                if(cursor.moveToFirst()){
                    database.update("RATES",contentValues,"_id = ?",new String[]{String.valueOf(id)});
                } else {
                    database.insert("RATES",null,contentValues);
                }
            } catch (Exception e){

            }
        }
    }

    private class GetReasons extends AsyncTask<Void,Void,Boolean>{

        private JSONArray reasons;
        private boolean error = false;

        public GetReasons(JSONArray reasons) {
            this.reasons = reasons;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                for (int i = 0; i < reasons.length(); i++) {
                    JSONObject jsonObject = reasons.getJSONObject(i);
                    String id = jsonObject.getString("pkId");
                    String reason = jsonObject.getString("szReason");
                    saveReason( id, reason);
                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean error) {
            super.onPostExecute(error);
            if(error){

            } else {
                if(progressBar != null){
                    progressBar.setProgress(30);
                }
                getRelationships();
            }
        }

        private void saveReason(String id, String reason) {
            ContentValues itemValues = new ContentValues();
            itemValues.put("_id", id);
            itemValues.put("REASON", reason);
            try {
                Cursor cursor = database.query("REASONS",
                        new String[]{"_id"},
                        "_id = ?",
                        new String[]{String.valueOf(id)},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    database.update("REASONS", itemValues, "_id = ?", new String[]{String.valueOf(id)});
                } else {
                    database.insert("REASONS", null, itemValues);
                }
                cursor.close();
                //db.close();
            } catch (Exception e) {
            }
        }
    }

    private class GetRelationships extends AsyncTask<Void,Void,Boolean>{

        private JSONArray relationships;
        private boolean error = false;

        public GetRelationships(JSONArray relationships) {
            this.relationships = relationships;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                for (int i = 0; i < relationships.length(); i++) {
                    JSONObject jsonObject = relationships.getJSONObject(i);
                    int id = Integer.parseInt(jsonObject.getString("pkId"));
                    String relationship = jsonObject.getString("szName");
                    saveRelationships(id, relationship);
                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(error){

            } else {
                if(progressBar != null){
                    progressBar.setProgress(40);
                }
                getBusinesses();
            }
        }

        private int saveRelationships(int id, String rateName) {
            int result;
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id",id);
            contentValues.put("RELATIONSHIP",rateName);
            try{
                Cursor cursor = database.query("RELATIONSHIPS",
                        new String[]{"_id"},
                        "_id = ?",
                        new String[]{String.valueOf(id)},
                        null,null,null);
                if(cursor.moveToFirst()){
                    database.update("RELATIONSHIPS",contentValues,"_id = ?",new String[]{String.valueOf(id)});
                    result = 1;
                } else {
                    database.insert("RELATIONSHIPS",null,contentValues);
                    result = 1;
                }
            } catch (Exception e){
                result = 0;
            }
            return result;
        }
    }

    private class GetBusinesses extends AsyncTask<Void,Void,Boolean>{

        private JSONArray businesses;
        private boolean error = false;

        public GetBusinesses(JSONArray businesses) {
            this.businesses = businesses;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                for(int i = 0;i < businesses.length();i++) {
                    JSONObject jsonObject = businesses.getJSONObject(i);
                    String id = jsonObject.getString("Id");
                    String accountCode = jsonObject.getString("AccountNumber");
                    String tin = "";
                    String billId = jsonObject.getString("BillNumber");
                    String name = jsonObject.getString("BusinessName");
                    String type = jsonObject.getString("BusinessType");
                    double curCharge = jsonObject.getDouble("CurCharge");
                    double outBalance = jsonObject.getDouble("OutBalance");
                    double curPAid = jsonObject.getDouble("CurPaidAmt");
                    saveBusiness(id,accountCode,tin,billId,name,type,curCharge,outBalance,curPAid);
                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean error) {
            super.onPostExecute(error);
            if(error){

            } else {
                getProperties();
                if(progressBar != null){
                    progressBar.setProgress(60);
                }
            }

        }

        private void saveBusiness(String id, String accountCode, String tin, String billId, String name, String type,
                                  double curCharge, double outBalance, double curPAid) {
            ContentValues businessValues = new ContentValues();
            businessValues.put(ID, id);
            businessValues.put(ACCOUNTCODE, accountCode);
            businessValues.put(TIN, tin);
            businessValues.put(BILLID, billId);
            businessValues.put(NAME, name);
            businessValues.put(TYPE, type);
            businessValues.put(CURRENTCHARGE, curCharge);
            businessValues.put(BALANCEBROUGHTFORWARD, outBalance);
            businessValues.put(PAIDTHISYEAR, curPAid);
            try {
                Cursor cursor = database.query(BUSINESSES_TABLE,
                        new String[]{ID},
                        ID+" = ?",
                        new String[]{id},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    database.update(BUSINESSES_TABLE, businessValues, ID+" = ?", new String[]{String.valueOf(id)});

                } else {
                    database.insert(BUSINESSES_TABLE, null, businessValues);
                }
                cursor.close();
                //db.close();
            }catch (Exception e){

            }
        }
    }

    private class GetProperties extends AsyncTask<Void,Void,Boolean>{

        private JSONArray properties;
        private boolean error = false;

        public GetProperties(JSONArray properties) {
            this.properties = properties;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                for(int i = 0;i < properties.length();i++){
                    JSONObject jsonObject = properties.getJSONObject(i);
                    String id = jsonObject.getString("Id");
                    String accountCode = jsonObject.getString("AccountNumber");
                    String billId  = jsonObject.getString("BillNumber");
                    String ownerName = jsonObject.getString("OwnerName");
                    String type = "";
                    double curCharge = jsonObject.getDouble("CurCharge");
                    double outBalance = jsonObject.getDouble("OutBalance");
                    double curPAid = jsonObject.getDouble("CurPaidAmt");

                    saveProperties(id,accountCode,billId,ownerName,type,curCharge,outBalance,curPAid);

                }
            } catch (Exception e){
                error = true;
            }
            return error;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean error) {
            super.onPostExecute(error);
            if(error){

            } else {
                if(progressBar != null){
                    progressBar.setProgress(100);
                    Utils.sleepAndDisplay(progressBar,2000);
                    revenueManager.loadItems(itemListView);
                    Toast.makeText(weakReference.get(), "Records updated", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void saveProperties(String id, String accountCode, String billId, String name, String type,
                                    double curCharge, double outBalance, double curPAid) {
            ContentValues propertyValues = new ContentValues();
            propertyValues.put(ID, id);
            propertyValues.put(ACCOUNTCODE, accountCode);
            propertyValues.put(BILLID, billId);
            propertyValues.put(NAME, name);
            propertyValues.put(TYPE, type);
            propertyValues.put(CURRENTCHARGE, curCharge);
            propertyValues.put(BALANCEBROUGHTFORWARD, outBalance);
            propertyValues.put(PAIDTHISYEAR, curPAid);
            try {
                Cursor cursor = database.query(PROPERTIES_TABLE,
                        new String[]{ID},
                        ID+" = ?",
                        new String[]{id},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    database.update(PROPERTIES_TABLE, propertyValues, ID+" = ?", new String[]{String.valueOf(id)});

                } else {
                    database.insert(PROPERTIES_TABLE, null, propertyValues);
                }
                cursor.close();
                //db.close();
            } catch (Exception e){
                Log.e("test",e.toString());
            }
        }
    }
}