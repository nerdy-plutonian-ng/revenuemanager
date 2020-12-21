package com.pluto.persolrevenuemanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.pluto.persolrevenuemanager.Constants.*;

public class RevenueManager {

    private DBActions dbActions;
    private Utils utils;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Print print;


    public RevenueManager(Context context) {
        this.context = context;
        dbActions = new DBActions(context);
        utils = new Utils(context);
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
    }

    public boolean shouldLoginOnline() {
        String result = dbActions.genericGetSingleItem(AGENT_TABLE, TOKENEXPIRY, ID, String.valueOf(AGENT_TABLE_ID));
        if (result == null || result.isEmpty()) {
            return true;
        } else return Utils.isTokenExpired(Long.parseLong(result));
    }

    public String getCurrentAgentId() {
        String result = dbActions.genericGetSingleItem(AGENT_TABLE, AGENTID, ID, String.valueOf(AGENT_TABLE_ID));
        return result == null ? "" : result;
    }

    public String getCurrentAgentName() {
        return dbActions.genericGetSingleItem(AGENT_TABLE, FIRSTNAME, ID, String.valueOf(AGENT_TABLE_ID))
                + " " + dbActions.genericGetSingleItem(AGENT_TABLE, LASTNAME, ID, String.valueOf(AGENT_TABLE_ID));
    }

    public boolean agentExists() {
        return !dbActions.isTableEmpty(AGENT_TABLE, AGENTID);
    }

    public boolean isSameUser(String newAgent) {
        return newAgent.equals(getCurrentAgentId());
    }

    public boolean clearCurrentUser() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookie();
        } else {
            CookieManager.getInstance().removeAllCookies(null);
        }
        return dbActions.clearTable(AGENT_TABLE);
    }

    public boolean login(int enteredPIN) {
        String result = dbActions.genericGetSingleItem(AGENT_TABLE, PIN, ID, String.valueOf(AGENT_TABLE_ID));
        int currentPIN;
        if (result == null || result.isEmpty()) {
            currentPIN = 0;
        } else {
            currentPIN = Integer.parseInt(dbActions.genericGetSingleItem(AGENT_TABLE, PIN, ID, String.valueOf(AGENT_TABLE_ID)));
        }
        return enteredPIN == currentPIN;
    }

    public boolean saveAgent(Agent agent, int firstTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, AGENT_TABLE_ID);
        contentValues.put(AGENTID, agent.getId());
        contentValues.put(FIRSTNAME, agent.getFirstName());
        contentValues.put(LASTNAME, agent.getLastName());
        contentValues.put(CASHLIMIT, agent.getCashLimit());
        contentValues.put(COLLECTEDTODATE, agent.getCollectedToDate());
        contentValues.put(SETTLEDTODATE, agent.getSettledToDate());
        contentValues.put(PIN, agent.getPin());
        contentValues.put(AGENTTOKEN, agent.getToken());
        contentValues.put(ASSEMBLYNAME, agent.getAssemblyName());
        contentValues.put(ASSEMBLYLOGO, agent.getAssemblyLogo());
        contentValues.put(TOKENEXPIRY, agent.getTokenExpiry());
        if (firstTime == 0) {
            contentValues.put(LASTSYNC, "");
        }
        return dbActions.genericSingleInsert(AGENT_TABLE, contentValues);
    }

    public boolean replaceAgent(Agent agent) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AGENTID, agent.getId());
        contentValues.put(FIRSTNAME, agent.getFirstName());
        contentValues.put(LASTNAME, agent.getLastName());
        contentValues.put(CASHLIMIT, agent.getCashLimit());
        contentValues.put(COLLECTEDTODATE, agent.getCollectedToDate());
        contentValues.put(SETTLEDTODATE, agent.getSettledToDate());
        contentValues.put(PIN, agent.getPin());
        contentValues.put(AGENTTOKEN, agent.getToken());
        contentValues.put(ASSEMBLYNAME, agent.getAssemblyName());
        contentValues.put(ASSEMBLYLOGO, agent.getAssemblyLogo());
        contentValues.put(TOKENEXPIRY, agent.getTokenExpiry());
        return dbActions.genericSingleUpdate(AGENT_TABLE, ID, String.valueOf(AGENT_TABLE_ID), contentValues);
    }

    public boolean replaceAgent(Agent agent, String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AGENTID, agent.getId());
        contentValues.put(FIRSTNAME, agent.getFirstName());
        contentValues.put(LASTNAME, agent.getLastName());
        contentValues.put(CASHLIMIT, agent.getCashLimit());
        contentValues.put(COLLECTEDTODATE, agent.getCollectedToDate());
        contentValues.put(SETTLEDTODATE, agent.getSettledToDate());
        contentValues.put(AGENTTOKEN, agent.getToken());
        contentValues.put(ASSEMBLYNAME, agent.getAssemblyName());
        contentValues.put(ASSEMBLYLOGO, agent.getAssemblyLogo());
        contentValues.put(TOKENEXPIRY, agent.getTokenExpiry());
        return dbActions.genericSingleUpdate(AGENT_TABLE, ID, String.valueOf(AGENT_TABLE_ID), contentValues);
    }

    public String getAgentToken() {
        return dbActions.genericGetSingleItem(AGENT_TABLE, AGENTTOKEN, ID, String.valueOf(AGENT_TABLE_ID));
    }

    public void syncDown(ProgressBar progressBar,ListView listView) {
        SyncDown syncDown = new SyncDown(context, progressBar,listView);
        syncDown.masterSync();
    }

    public void syncUp(ProgressBar progressBar) {
        SyncUp syncUp = new SyncUp(context, progressBar);
        syncUp.sync();
    }

    public String getAssemblyName() {
        return dbActions.genericGetSingleItem(AGENT_TABLE, ASSEMBLYNAME, ID, String.valueOf(AGENT_TABLE_ID));
    }

    public String getAssemblyLogo() {
        return dbActions.genericGetSingleItem(AGENT_TABLE, ASSEMBLYLOGO, ID, String.valueOf(AGENT_TABLE_ID));
    }

    public boolean saveTransaction(int itemId, double amount, String agentId, String payeeID,
                                   String receiptNo, String gcrNumber, String transDate,
                                   int paymentMode, String location) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, receiptNo);
        contentValues.put(ITEMID, itemId);
        contentValues.put(AMOUNT, amount);
        contentValues.put(AGENTID, agentId);
        contentValues.put(PAYERID, payeeID);
        contentValues.put(GCRNUMBER, gcrNumber);
        contentValues.put(LOCATION, location);
        contentValues.put(PAYMENTMODE, paymentMode);
        contentValues.put(TRANSACTIONDATE, transDate);
        return dbActions.genericSingleInsert(TRANSACTIONS_TABLE, contentValues);
    }

    public Item getItem(int id) {
        Cursor cursor = dbActions.genericGetSpecificCursor(ITEMS_TABLE, new String[]{ID, NAME, BASEAMOUNT}, ID, String.valueOf(id));
        try {
            if (cursor.moveToFirst()) {
                Item item = new Item(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2));
                cursor.close();
                return item;
            }
            cursor.close();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Business getBusiness(String id) {
        Cursor cursor = dbActions.genericGetSpecificCursor(BUSINESSES_TABLE,
                new String[]{ACCOUNTCODE, TIN, BILLID, NAME, TYPE, CURRENTCHARGE, BALANCEBROUGHTFORWARD, PAIDTHISYEAR},
                ID, id);
        if (cursor.moveToFirst()) {
            Business business = new Business(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6),
                    cursor.getDouble(7));
            cursor.close();
            return business;
        }
        return null;
    }

    public Property getProperty(String id) {
        Cursor cursor = dbActions.genericGetSpecificCursor(PROPERTIES_TABLE,
                new String[]{ACCOUNTCODE, BILLID, NAME, TYPE, CURRENTCHARGE, BALANCEBROUGHTFORWARD, PAIDTHISYEAR},
                ID, id);
        if (cursor.moveToFirst()) {
            Log.e("test", "getProperty: data");
            Property property = new Property(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getDouble(4), cursor.getDouble(5), cursor.getDouble(6));
            cursor.close();
            return property;
        }
        Log.e("test", "getProperty: null");
        return null;
    }

    public String generateReceipt() {
        return String.valueOf(new Date().getTime());
    }

    public boolean ratesTableEmpty() {
        return dbActions.isTableEmpty(RATES_TABLE, ID);
    }



    public void loadItems(ListView itemsListview) {

        try {
            Cursor cursor = dbActions.genericGetCursor(ITEMS_TABLE, new String[]{Constants.ID, Constants.NAME});
            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(context,
                    //android.R.layout.simple_list_item_1,
                    R.layout.listview_item,
                    cursor,
                    new String[]{Constants.NAME, Constants.ID},
                    //new int[]{android.R.id.text1},
                    new int[]{R.id.itemTv},
                    0);
            itemsListview.setAdapter(listAdapter);

        } catch (SQLiteException e) {
            Toast.makeText(context, "Failed to open database. Make sure you have good internet connection", Toast.LENGTH_SHORT).show();
        }

        //Create a listener to listen for clicks in the list view
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listItems, View view, int position, long id) {
                //revert
                if(false){// (!utils.isDataEnabled() || !utils.isWifiEnabled()) {
                    utils.toastMessage("Please switch on both data and wifi", Toast.LENGTH_LONG);
                    return;
                } else {
                    String name = dbActions.genericGetSingleItem(ITEMS_TABLE, NAME, ID, String.valueOf(id));
                    if (name.toLowerCase().contains("business") || name.toLowerCase().contains("property")) {
                        showBusPropDialog((int) id);
                    } else {
                        showSimpleRevenueDialog((int) id);
                    }

//                    if(checkSettleStatus()){
//                        if(UTILITIES.isConnected(context)){
//                            showMessage("Cash limit reached, initiate settlement.");
//                        }else {
//                            showMessage("Cash limit reached, but you have no internet to initiate settlement");
//                        }
//                    }
//                    else {
//                        if(printing){
//                            showMessage("Please waiting till printing is done.");
//                        }else {
//                            loadTransactionDetails(String.valueOf(id));
//                        }
//                    }
                }

            }
        };
        itemsListview.setOnItemClickListener(itemClickListener);
    }

    public void showSimpleRevenueDialog(int itemId) {
        DialogFragment newFragment = new SimpleRevenueDialog(itemId);
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "simplerevenue");
    }

    public void showBusPropDialog(int itemId) {
        DialogFragment newFragment = new BusPropDialog(itemId);
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "busProp");
    }

    public void showBillDistro() {
        DialogFragment newFragment = new BillDistoDialog();
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "billDistro");
    }

    public void showFeedbackDialog() {
        DialogFragment newFragment = new FeedbackDialog();
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "feedback");
    }

    public Cursor getBusPropCursor(String table, String query) {
        String sql = "SELECT " + NAME + " , " + ID + " FROM " + table + " WHERE " + ACCOUNTCODE + " LIKE '%" + query + "%' OR " + NAME + " LIKE '%" + query + "%' OR " + BILLID + " LIKE '%" + query + "%'";
        Log.e("test", sql);
        return dbActions.getGenericCursorSql(sql);
    }

    public Cursor getItemsCursor() {
        String sql = "SELECT " + NAME + " , " + ID + " FROM " + ITEMS_TABLE;
        return dbActions.getGenericCursorSql(sql);
    }

    public Cursor getGenericCursor(String table, String[] columns) {
        return dbActions.genericGetCursor(table, columns);
    }

    public void deleteSentTrans(String[] ids){
        dbActions.multipleGenericDelete(ids,TRANSACTIONS_TABLE,ID);
    }

    public void deleteBills(String[] ids){
        dbActions.multipleGenericDelete(ids,BILL_DISTRIBUTION_TABLE,ID);
    }

    public void deleteFeedback(String[] ids){
        dbActions.multipleGenericDelete(ids,FEEDBACKS_TABLE,ID);
    }

    public void deleteLocationLogs(String[] ids){
        dbActions.multipleGenericDelete(ids,LOCATIONLOGS_TABLE,ID);
    }

    public void exit(){
        Intent intent = new Intent(context,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void logout() {
        dbActions.clearTable(AGENT_TABLE);
        dbActions.clearTable(BUSINESSES_TABLE);
        dbActions.clearTable(PROPERTIES_TABLE);
        dbActions.clearTable(ITEMS_TABLE);
        dbActions.clearTable(RATES_TABLE);
        exit();
    }

    public String getName(String table, String valueColumn, String whereColumn, String whereValue) {
        return dbActions.genericGetSingleItem(table, valueColumn, whereColumn, whereValue);
    }

    public boolean saveBillDistroTransaction(String accountCode, String recName, String recPhone,
                                             String recEmail, String location, String feedback, String remarks,
                                             String reason, String transDate, String agentId, int itemId, int deliveryStatus, int sameAsOwner) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, UUID.randomUUID().toString());
        contentValues.put(ACCOUNTCODE, accountCode);
        contentValues.put(NAME, recName);
        contentValues.put(PHONE, recPhone);
        contentValues.put(EMAIL, recEmail);
        contentValues.put(LOCATION, location);
        contentValues.put(FEEDBACK, feedback);
        contentValues.put(REMARKS, remarks);
        contentValues.put(REASON, reason);
        contentValues.put(DATE, transDate);
        contentValues.put(AGENTID, agentId);
        contentValues.put(ITEMID, itemId);
        contentValues.put(STATUS, deliveryStatus);
        contentValues.put(SAMEASOWNER, sameAsOwner);
        return dbActions.genericSingleInsert(BILL_DISTRIBUTION_TABLE, contentValues);
    }

    public boolean saveFeedback(String agentId, int itemId, String feedback, String date, String location, String accountCode) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, UUID.randomUUID().toString());
        contentValues.put(AGENTID, agentId);
        contentValues.put(ITEMID, itemId);
        contentValues.put(FEEDBACK, feedback);
        contentValues.put(DATE, date);
        contentValues.put(LOCATION, location);
        contentValues.put(ACCOUNTCODE, accountCode);
        return dbActions.genericSingleInsert(FEEDBACKS_TABLE, contentValues);
    }

    public void generateSettlement() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = DOMAIN + GET_AGENT_SUMMARY + getCurrentAgentId();
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject data = response.getJSONObject(0);
                            double collected = data.getDouble("TotalAmountCollected");
                            double settled = data.getDouble("TotalAmountSettled");
                            double amountToSettle = collected - settled;
                            Log.e("amountToSettle",amountToSettle+"");
                            getSettlement(getSettlementID(),1,getCurrentAgentId(),amountToSettle,Utils.getISOdate());
                            return;
                        } catch (JSONException e) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PRA", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                Log.e("token",getAgentToken());
                headers.put("Authorization", "Bearer " + getAgentToken());
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public ArrayList<Rate> getRates(int itemId){
        Cursor cursor  = dbActions.genericGetSpecificCursor(RATES_TABLE,new String[]{ID,NAME,ITEMID,FACTOR},ITEMID, String.valueOf(itemId));
        ArrayList<Rate> rateArrayList = new ArrayList<>();
        while (cursor.moveToNext()){
            Rate rate = new Rate(cursor.getInt(0),cursor.getString(1),cursor.getDouble(3),cursor.getInt(2));
            rateArrayList.add(rate);
        }
        return rateArrayList;
    }

    private void getSettlement(final String settlementId, final int itemId, final String agentId, final double amount, final String date){
        String url = DOMAIN + GET_SETTLEMENT_ID;
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject data = new JSONObject();
        try {
            data.put("iSettlementID",settlementId);
            data.put("iItemID",itemId);
            data.put("iAgentID",agentId);
            data.put("nAmountSettled",amount);
            data.put("dSettlementDate",date);

            Log.e("PRA",data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("settlementSuccess",response.toString());
                        printSettlement(settlementId,amount);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("settlementFailure",error.toString());
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Put access token in HTTP request.
                headers.put("Authorization", "Bearer " + getAgentToken());
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private String getSettlementID(){
        Random rnd = new Random();
        return String.valueOf(100000 + rnd.nextInt(900000));
    }

    public void saveGPSCoor(double latitude, double longitude){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(LATITUDE, (float) latitude);
        editor.putFloat(LONGITUDE, (float) longitude);
        editor.apply();
    }

    public double[] getGPSCoor(){
        double[] gpsCoor = new double[2];
        gpsCoor[0] = sharedPreferences.getFloat(LATITUDE,0.0f);
        gpsCoor[1] = sharedPreferences.getFloat(LONGITUDE,0.0F);
        return  gpsCoor;
    }

    public void printReceipt(String receiptNo,String item,double amount,String payer){
        print = new Print(context);
        print.printAssembly(receiptNo,item,amount,payer);
    }

    public void printSettlement(String settlementID, double amount) {
        print = new Print(context);
        print.printSettlement(settlementID,amount);
    }


    public void showReprintDialog() {
        DialogFragment newFragment = new ReprintDialog();
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "reprint");
    }

    public void getReprintTransaction(String gcrNo){
        Cursor cursor = dbActions.genericGetSpecificCursor(TRANSACTIONS_TABLE,new String[]{ID,ITEMID,AMOUNT,PAYERID,TRANSACTIONDATE},GCRNUMBER,gcrNo);
        if(cursor.moveToFirst()){
            reprintTransaction(new Transaction(cursor.getString(0),cursor.getInt(1),cursor.getDouble(2),cursor.getString(3),new Date(cursor.getString(4))));
            return;
        }
        Toast.makeText(context, "No transaction found", Toast.LENGTH_SHORT).show();
    }

    public void reprintTransaction(Transaction transaction){
        if(transaction != null){
            Item item = getItem(transaction.getItemID());
            printReceipt(transaction.getId(),item.getName(),transaction.getAmount(),getName(item.getName().toLowerCase().contains("business") ? BUSINESSES_TABLE : PROPERTIES_TABLE,NAME,ACCOUNTCODE,transaction.getPayerID()));
        }
    }
    public void dialNumber(String agentId,String accountCode, int itemId, String momoNumber, double amount) {

        Log.e("PRA","amount: GHS "+amount);
        String ussdCode = "*789*1*" + agentId + "*" +  50000 + "*" + itemId + "*" + momoNumber + "*" + (int)amount + Uri.encode("#");
        Log.e("PRA","THE STRING : "+ussdCode);
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
    }

    public void setAlarmForAllScheduledTasks(){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);;
        Intent intent = new Intent(context, AppReceiver.class);
        intent.setAction("android.intent.action.SCHEDULED_TASKS");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5000,//AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }

    public void saveLocationLog(String location){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID,UUID.randomUUID().toString());
        contentValues.put(AGENTID,getCurrentAgentId());
        contentValues.put(LOCATION,location);
        contentValues.put(DATE,Utils.getISOdate());
        contentValues.put(GPSSTATUS,utils.isGPSenabled()  ? 1 : 0);
        dbActions.genericSingleInsert(LOCATIONLOGS_TABLE,contentValues);
    }

}
