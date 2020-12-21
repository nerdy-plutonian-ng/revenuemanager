package com.pluto.persolrevenuemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import static com.pluto.persolrevenuemanager.Constants.ACCOUNTCODE;
import static com.pluto.persolrevenuemanager.Constants.AGENTID;
import static com.pluto.persolrevenuemanager.Constants.AGENTTOKEN;
import static com.pluto.persolrevenuemanager.Constants.AGENT_TABLE;
import static com.pluto.persolrevenuemanager.Constants.AMOUNT;
import static com.pluto.persolrevenuemanager.Constants.ASSEMBLYLOGO;
import static com.pluto.persolrevenuemanager.Constants.ASSEMBLYNAME;
import static com.pluto.persolrevenuemanager.Constants.BALANCEBROUGHTFORWARD;
import static com.pluto.persolrevenuemanager.Constants.BASEAMOUNT;
import static com.pluto.persolrevenuemanager.Constants.BILLID;
import static com.pluto.persolrevenuemanager.Constants.BUSINESSES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.CASHLIMIT;
import static com.pluto.persolrevenuemanager.Constants.COLLECTEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.CURRENTCHARGE;
import static com.pluto.persolrevenuemanager.Constants.DATE;
import static com.pluto.persolrevenuemanager.Constants.DB_NAME;
import static com.pluto.persolrevenuemanager.Constants.DB_VERSION;
import static com.pluto.persolrevenuemanager.Constants.DEVICEID;
import static com.pluto.persolrevenuemanager.Constants.EMAIL;
import static com.pluto.persolrevenuemanager.Constants.FACTOR;
import static com.pluto.persolrevenuemanager.Constants.FEEDBACK;
import static com.pluto.persolrevenuemanager.Constants.FEEDBACKS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.FIRSTNAME;
import static com.pluto.persolrevenuemanager.Constants.GCRNUMBER;
import static com.pluto.persolrevenuemanager.Constants.GPSSTATUS;
import static com.pluto.persolrevenuemanager.Constants.IN_USE;
import static com.pluto.persolrevenuemanager.Constants.ITEMID;
import static com.pluto.persolrevenuemanager.Constants.ITEMS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.LASTNAME;
import static com.pluto.persolrevenuemanager.Constants.LASTPAYMENTDATE;
import static com.pluto.persolrevenuemanager.Constants.LASTSYNC;
import static com.pluto.persolrevenuemanager.Constants.LATITUDE;
import static com.pluto.persolrevenuemanager.Constants.LOCATION;
import static com.pluto.persolrevenuemanager.Constants.LOCATIONLOGS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.LONGITUDE;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PAIDTHISYEAR;
import static com.pluto.persolrevenuemanager.Constants.PASSWORD;
import static com.pluto.persolrevenuemanager.Constants.PAYERID;
import static com.pluto.persolrevenuemanager.Constants.PAYMENTMODE;
import static com.pluto.persolrevenuemanager.Constants.PHONE;
import static com.pluto.persolrevenuemanager.Constants.PIN;
import static com.pluto.persolrevenuemanager.Constants.PROPERTIES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.RATES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.REASON;
import static com.pluto.persolrevenuemanager.Constants.REASONS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.RECEIPTNUMBER;
import static com.pluto.persolrevenuemanager.Constants.RELATIONSHIP;
import static com.pluto.persolrevenuemanager.Constants.RELATIONSHIPS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.REMARKS;
import static com.pluto.persolrevenuemanager.Constants.SAMEASOWNER;
import static com.pluto.persolrevenuemanager.Constants.SETTLEDTODATE;
import static com.pluto.persolrevenuemanager.Constants.STATUS;
import static com.pluto.persolrevenuemanager.Constants.TIN;
import static com.pluto.persolrevenuemanager.Constants.TOKENEXPIRY;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONDATE;
import static com.pluto.persolrevenuemanager.Constants.TRANSACTIONS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.TYPE;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE "+ AGENT_TABLE + " (_id INTEGER PRIMARY KEY," +
                AGENTID + " TEXT," +
                CASHLIMIT + " REAL," +
                FIRSTNAME + " TEXT," +
                LASTNAME + " TEXT," +
                SETTLEDTODATE + " REAL," +
                COLLECTEDTODATE + " REAL,"+
                PIN + " INTEGER," +
                ASSEMBLYNAME + " TEXT," +
                ASSEMBLYLOGO + " TEXT," +
                AGENTTOKEN + " TEXT," +
                TOKENEXPIRY + " TEXT," +
                LASTSYNC + " TEXT)");

        db.execSQL("CREATE TABLE "+ ITEMS_TABLE + "(_id INTEGER PRIMARY KEY," +
                NAME + " TEXT," +
                BASEAMOUNT + " REAL)");

        db.execSQL("CREATE TABLE "+ RATES_TABLE + "(_id INTEGER PRIMARY KEY," +
                ITEMID +" INTEGER," +
                FACTOR + " INTEGER," +
                NAME + " TEXT)");

        db.execSQL("CREATE TABLE "+ REASONS_TABLE + "(_id INTEGER PRIMARY KEY," +
                REASON + " TEXT)");

        db.execSQL("CREATE TABLE "+ RELATIONSHIPS_TABLE + "(_id INTEGER PRIMARY KEY," +
                RELATIONSHIP + " TEXT)");

        db.execSQL("CREATE TABLE " + BUSINESSES_TABLE + "(_id TEXT PRIMARY KEY," +
                ACCOUNTCODE + " TEXT," +
                TIN + " TEXT," +
                BILLID + " TEXT," +
                NAME + " TEXT," +
                TYPE + " TEXT," +
                CURRENTCHARGE + " REAL," +
                BALANCEBROUGHTFORWARD + " REAL," +
                PAIDTHISYEAR + " REAL)");

        db.execSQL("CREATE TABLE " + PROPERTIES_TABLE + "(_id TEXT PRIMARY KEY," +
                ACCOUNTCODE + " TEXT," +
                BILLID + " TEXT," +
                NAME + " TEXT," +
                TYPE + " TEXT," +
                CURRENTCHARGE + " REAL," +
                BALANCEBROUGHTFORWARD + " REAL," +
                PAIDTHISYEAR + " REAL)");

        db.execSQL("CREATE TABLE " + TRANSACTIONS_TABLE + "(_id TEXT PRIMARY KEY," +
                ITEMID + " INTEGER," +
                AMOUNT + " REAL," +
                AGENTID + " INTEGER," +
                PAYERID + " TEXT," +
                GCRNUMBER + " TEXT UNIQUE," +
                LOCATION + " TEXT," +
                PAYMENTMODE + " INTEGER," +
                TRANSACTIONDATE + " TEXT)");

        db.execSQL("CREATE TABLE " + LOCATIONLOGS_TABLE + "(_id TEXT PRIMARY KEY, "+
                AGENTID+ " TEXT, " +
                LOCATION+ " TEXT, " +
                DATE +" TEXT, " +
                GPSSTATUS+ " INTEGER);");

        db.execSQL("CREATE TABLE " + FEEDBACKS_TABLE +"(_id TEXT PRIMARY KEY, "+
                AGENTID+ " INTEGER, "+
                ITEMID+ " INTEGER,"+
                FEEDBACK+ " TEXT,"+
                REMARKS+ " TEXT,"+
                DATE+ " TEXT, "+
                LOCATION+ " TEXT, "+
                ACCOUNTCODE+" TEXT);");

        db.execSQL("CREATE TABLE BILLDISTRIBUTION (_id TEXT PRIMARY KEY, "+
                ACCOUNTCODE+ " TEXT, "+
                NAME+ " TEXT,"+
                PHONE+ " TEXT,"+
                EMAIL+ " TEXT, "+
                LOCATION+ " TEXT, "+
                FEEDBACK+ " TEXT, "+
                REMARKS+ " TEXT, "+
                REASON+ " TEXT,"+
                DATE+ " TEXT,"+
                AGENTID+ " INTEGER,"+
                ITEMID+ " INTEGER,"+
                STATUS+ " INTEGER,"+
                SAMEASOWNER +" INTEGER);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
