package com.pluto.persolrevenuemanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.pluto.persolrevenuemanager.Constants.ACCOUNTCODE;
import static com.pluto.persolrevenuemanager.Constants.AGENTID;
import static com.pluto.persolrevenuemanager.Constants.AMOUNT;
import static com.pluto.persolrevenuemanager.Constants.BILL_DISTRIBUTION_TABLE;
import static com.pluto.persolrevenuemanager.Constants.DATE;
import static com.pluto.persolrevenuemanager.Constants.DOMAIN;
import static com.pluto.persolrevenuemanager.Constants.EMAIL;
import static com.pluto.persolrevenuemanager.Constants.FEEDBACK;
import static com.pluto.persolrevenuemanager.Constants.FEEDBACKS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.GCRNUMBER;
import static com.pluto.persolrevenuemanager.Constants.GPSSTATUS;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.ITEMID;
import static com.pluto.persolrevenuemanager.Constants.LOCATION;
import static com.pluto.persolrevenuemanager.Constants.LOCATIONLOGS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PAYERID;
import static com.pluto.persolrevenuemanager.Constants.PAYMENTMODE;
import static com.pluto.persolrevenuemanager.Constants.PHONE;
import static com.pluto.persolrevenuemanager.Constants.POST_BILLDISTRIBUTION;
import static com.pluto.persolrevenuemanager.Constants.POST_FEEDBACK;
import static com.pluto.persolrevenuemanager.Constants.POST_LOCATION_LOGS;
import static com.pluto.persolrevenuemanager.Constants.POST_TRANSACTIONS;
import static com.pluto.persolrevenuemanager.Constants.REASON;
import static com.pluto.persolrevenuemanager.Constants.SAMEASOWNER;
import static com.pluto.persolrevenuemanager.Constants.STATUS;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONDATE;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONS_TABLE;

public class SyncUp {

    private WeakReference<Context> weakReference;
    private Utils utils;
    private RevenueManager revenueManager;
    private ProgressBar progressBar;
    private String[] toDelete,billToDelete,feedbackToDelete,locationLogsToDelete;

    public SyncUp(Context context, ProgressBar progressBar) {
        this.weakReference = new WeakReference<>(context);
        this.progressBar = progressBar;
        utils = new Utils(weakReference.get());
        revenueManager = new RevenueManager(weakReference.get());
    }

    public void sync(){

        try {
            JSONArray transactions = new JSONArray();
            Cursor cursor = revenueManager.getGenericCursor(TRANSACTIONS_TABLE,new String[]{ITEMID,AMOUNT,AGENTID,PAYERID,GCRNUMBER,LOCATION,PAYMENTMODE,TRANSACTIONDATE,ID});
            toDelete = new String[cursor.getCount()];
            int count = 0;
            if(cursor.getCount() == 0){
                cursor.close();
                syncBillDistribution();
                return;
            }
            while (cursor.moveToNext()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("iItemId",cursor.getInt(0));
                jsonObject.put("nRate",1);
                jsonObject.put("nQty",1);
                jsonObject.put("nAmount",cursor.getDouble(1));
                jsonObject.put("dExpirydate",Utils.getISOdate(cursor.getString(7)));
                jsonObject.put("iUserId",cursor.getString(2));
                jsonObject.put("szDescription","");
                jsonObject.put("iMember",1);
                jsonObject.put("nPrice",cursor.getDouble(1));
                jsonObject.put("payeeid",cursor.getString(3));
                jsonObject.put("payeename","");
                jsonObject.put("receiptNumber",cursor.getString(8));
                jsonObject.put("chequeNumber","");
                jsonObject.put("chequeDate",Utils.getISOdate(cursor.getString(7)));
                jsonObject.put("GCRNumber",cursor.getString(4));
                jsonObject.put("dTime",Utils.getISOdate(cursor.getString(7)));
                jsonObject.put("iPaymentMode",cursor.getInt(6));
                jsonObject.put("Feedback","");
                jsonObject.put("Remarks","");
                String[] location = cursor.getString(5).split(",");
                jsonObject.put("szImeiNumber",utils.getDeviceId());
                jsonObject.put("szLatitude",location[0]);
                jsonObject.put("szLongitude",location[1]);
                transactions.put(jsonObject);
                toDelete[count] = cursor.getString(8);
                count++;
            }
            cursor.close();
            Log.e("data",transactions.toString());
            RequestQueue queue = Volley.newRequestQueue(weakReference.get());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                    DOMAIN + POST_TRANSACTIONS,
                    transactions,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("PRM", "onResponse: Success");
                            revenueManager.deleteSentTrans(toDelete);
                            syncBillDistribution();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PRM", "ERRPR: "+error.toString());
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Put access token in HTTP request.
                    Log.e("token",revenueManager.getAgentToken());
                    headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                    return headers;
                }
            };
            queue.add(jsonArrayRequest);
        } catch (Exception e){
            Log.e("Transactions", "sync: "+e.toString() );
        }
    }

    private void syncBillDistribution(){
        try {
            JSONArray transactions = new JSONArray();
            Cursor cursor = revenueManager.getGenericCursor(BILL_DISTRIBUTION_TABLE,
                    new String[]{ACCOUNTCODE, NAME,PHONE,EMAIL,LOCATION,FEEDBACK,REASON,DATE,AGENTID,
                            ITEMID,STATUS,SAMEASOWNER,ID});
            billToDelete = new String[cursor.getCount()];
            int count = 0;
            if(cursor.getCount() == 0){
                cursor.close();
                syncFeedBack();
                return;
            }
            while (cursor.moveToNext()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("szAccountId",cursor.getString(0));
                jsonObject.put("szRecipientName",cursor.getString(1));
                jsonObject.put("szRecipientPhone",cursor.getString(2));
                jsonObject.put("szRecipientEmail",cursor.getString(3));
                String[] locationArr = cursor.getString(4).split(",");
                jsonObject.put("szLatitude",locationArr[0]);
                jsonObject.put("szLongitude",locationArr[1]);
                jsonObject.put("szFeedback",cursor.getString(5));
                jsonObject.put("szReason",cursor.getString(6));
                jsonObject.put("dDistributionDate",cursor.getString(7));
                jsonObject.put("iAgentId",cursor.getString(8));
                jsonObject.put("iItemId",cursor.getInt(9));
                jsonObject.put("iStatus",cursor.getInt(10));
                jsonObject.put("iSameAsOwner",cursor.getInt(11));
                transactions.put(jsonObject);
                billToDelete[count] = cursor.getString(12);
                count++;
            }
            cursor.close();
            Log.e("data",transactions.toString());
            if(transactions.length() == 0){
                return;
            }
            RequestQueue queue = Volley.newRequestQueue(weakReference.get());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                    DOMAIN + POST_BILLDISTRIBUTION,
                    transactions,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("PRM", "onResponse: Success");
                            revenueManager.deleteBills(billToDelete);
                            syncFeedBack();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PRM", "ERRPR: "+error.toString());
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Put access token in HTTP request.
                    Log.e("token",revenueManager.getAgentToken());
                    headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                    return headers;
                }
            };
            queue.add(jsonArrayRequest);
        } catch (Exception e){
            Log.e("PRM", "sync: "+e.toString() );
        }
    }
        private void syncFeedBack(){
        try {
            JSONArray transactions = new JSONArray();
            Cursor cursor = revenueManager.getGenericCursor(FEEDBACKS_TABLE,
                    new String[]{AGENTID, ITEMID,FEEDBACK,DATE,LOCATION,ACCOUNTCODE,ID});
            feedbackToDelete = new String[cursor.getCount()];
            int count = 0;
            if(cursor.getCount() == 0){
                cursor.close();
                sendLocationLogs();
                return;
            }
            while (cursor.moveToNext()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("AgentId",cursor.getString(0));
                jsonObject.put("ItemId",cursor.getInt(1));
                jsonObject.put("Feedback",cursor.getString(2));
                jsonObject.put("FeedbackType","OTR");
                jsonObject.put("FeedbackTime",cursor.getString(3));
                String[] locationArr = cursor.getString(4).split(",");
                jsonObject.put("Latitude",locationArr[0]);
                jsonObject.put("Longitude",locationArr[1]);
                jsonObject.put("ImeiNumber",utils.getDeviceId());
                jsonObject.put("AccountCode",cursor.getString(5));
                transactions.put(jsonObject);
                feedbackToDelete[count] = cursor.getString(6);
                count++;
            }
            cursor.close();
            Log.e("data",transactions.toString());
            RequestQueue queue = Volley.newRequestQueue(weakReference.get());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                    DOMAIN + POST_FEEDBACK,
                    transactions,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("PRM", "onResponse: Success");
                            revenueManager.deleteFeedback(feedbackToDelete);
                            sendLocationLogs();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PRM", "ERRPR: "+error.toString());
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Put access token in HTTP request.
                    Log.e("token",revenueManager.getAgentToken());
                    headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                    return headers;
                }
            };
            queue.add(jsonArrayRequest);
        } catch (Exception e){
            Log.e("PRM", "sync: "+e.toString() );
        }
    }

    private void sendLocationLogs(){
        try {
            JSONArray transactions = new JSONArray();
            Cursor cursor = revenueManager.getGenericCursor(LOCATIONLOGS_TABLE,
                    new String[]{AGENTID,LOCATION,DATE,GPSSTATUS,ID});
            locationLogsToDelete = new String[cursor.getCount()];
            int count = 0;
            if(cursor.getCount() == 0){
                cursor.close();
                return;
            }
            while (cursor.moveToNext()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("iAgentId",cursor.getString(0));
                jsonObject.put("dLogDate",cursor.getString(2));
                String[] locationArr = cursor.getString(1).split(",");
                jsonObject.put("Latitude",locationArr[0]);
                jsonObject.put("Longitude",locationArr[1]);
                jsonObject.put("LocationServiceStatus",cursor.getInt(3));
                jsonObject.put("ImeiNumber",utils.getDeviceId());
                transactions.put(jsonObject);
                locationLogsToDelete[count] = cursor.getString(4);
                count++;
            }
            cursor.close();
            Log.e("data",transactions.toString());
            RequestQueue queue = Volley.newRequestQueue(weakReference.get());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                    DOMAIN + POST_LOCATION_LOGS,
                    transactions,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("PRM", "onResponse: Success");
                            revenueManager.deleteLocationLogs(locationLogsToDelete);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PRM", "ERRPR: "+error.toString());
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Put access token in HTTP request.
                    Log.e("token",revenueManager.getAgentToken());
                    headers.put("Authorization", "Bearer " + revenueManager.getAgentToken());
                    return headers;
                }
            };
            queue.add(jsonArrayRequest);
        } catch (Exception e){
            Log.e("PRM", "sync: "+e.toString() );
        }
    }
}