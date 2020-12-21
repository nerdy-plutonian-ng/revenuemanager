package com.pluto.persolrevenuemanager;

import android.widget.Toast;

public class Constants {

    //DB CONSTANTS
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "PERSOLREVENUEDB";
    public static final int AGENT_TABLE_ID = 1001;
    //////////////////////////////////////////DB COLUMNS
    //COMMON COLUMNS
    public static final String ID = "_id";
    public static final String NAME = "NAME";
    public static final String TOTALAMOUNTOWED = "TOTALAMOUNTOWED";
    public static final String BALANCEBROUGHTFORWARD = "BALANCEBROUGHTFORWARD";
    public static final String PAIDTHISYEAR = "PAIDTHISYEAR";
    public static final String LASTPAYMENTDATE = "LASTPAYMENTDATE";
    public static final String TYPE = "TYPE";
    public static final String IN_USE = "INUSE";
    public static final String BILLID = "BILLID";
    public static final String CURRENTCHARGE = "CURRENTCHARGED";
    public static final String REASON = "REASON";
    public static final String RELATIONSHIP = "RELATIONSHIP";
    public static final String LOCATION = "LOCATION";
    public static final String ACCOUNTCODE = "ACCOUNTCODE";
    public static final String DATE = "DATE";
    public static final String GPSSTATUS = "GPSSTATUS";
    public static final String FEEDBACK = "FEEDBACK";
    public static final String FEEDBACKTYPES = "FEEDBACKTYPES";
    public static final String PHONE = "PHONE";
    public static final String EMAIL = "EMAIL";
    public static final String STATUS = "STATUS";
    public static final String SAMEASOWNER = "SAMEASOWNER";
    public static final String REMARKS = "REMARKS";
    //AGENTS TABLE
    public static final String LASTSYNC = "LASTSYNC";
    public static final String AGENTID = "AGENTID";
    public static final String CASHLIMIT = "CASHLIMIT";
    public static final String FIRSTNAME = "FIRSTNAME";
    public static final String LASTNAME = "LASTNAME";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String SETTLEDTODATE = "SETTLEDTODATE";
    public static final String COLLECTEDTODATE = "COLLECTEDTODATE";
    public static final String ZONE = "ZONE";
    public static final String PIN = "PIN";
    public static final String ASSEMBLYNAME = "ASSEMBLYNAME";
    public static final String ASSEMBLYCODE = "ASSEMBLYCODE";
    public static final String ASSEMBLYLOGO = "ASSEMBLYLOGO";
    public static final String AGENTTOKEN = "AGENTTOKEN";
    public static final String TOKENEXPIRY = "TOKENEXPIRY";
    public static final String SHARED_PREFS = "SHARED_PREFERENCES";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

    //ITEMS TABLE
    public static final String BASEAMOUNT = "BASEAMOUNT";
    //RATES TABLE
    public static final String ITEMID = "ITEMID";
    public static final String FACTOR = "FACTOR";
    //BUSINESSES TABLE
    public static final String TIN = "TIN";
    //TRANSACTIONS
    public static final String AMOUNT = "AMOUNT";
    public static final String PAYERID = "PAYERID";
    public static final String RECEIPTNUMBER = "RECEIPTNUMBER";
    public static final String GCRNUMBER = "GCRNUMBER";
    public static final String PAYMENTMODE = "PAYMENTMODE";
    public static final String TRANSACTIONDATE = "TRANSACTIONDATE";
    public static final String DEVICEID = "DEVICEID";
    public static final int LOCATION_REQUEST = 345;
    //TABLES
    public static final String APP_INFO_TABLE = "APPINFO";
    public static final String AGENT_TABLE = "AGENTINFO";
    public static final String ITEMS_TABLE = "ITEMS";
    public static final String RATES_TABLE = "RATES";
    public static final String REASONS_TABLE = "REASONS";
    public static final String RELATIONSHIPS_TABLE = "RELATIONSHIPS";
    public static final String BUSINESSES_TABLE = "BUSINESSES";
    public static final String PROPERTIES_TABLE = "PROPERTIES";
    public static final String TRANSACTIONS_TABLE = "TRANSACTIONS";
    public static final String LOCATIONLOGS_TABLE = "LOCATIONLOGS";
    public static final String FEEDBACKS_TABLE = "FEEDBACKS";
    public static final String BILL_DISTRIBUTION_TABLE = "BILLDISTRIBUTION";

    //NETWORK
    //public static final String DOMAIN = "http://192.168.0.68/AndroidPortalService/api/";
    public static final String DOMAIN = "https://collect.localrevenue-gh.com/TaxRevenueService/api/";
    public static final String GET_AGENT_BY_USERNAME = "GetUserByUsername?szUserName=";
    //public static final String GET_AGENT_SUMMARY = "Transactions/GetAgentTransactionSummaryByAgentId?agentId=";
    public static final String GET_AGENT_SUMMARY = "Transactions/GetAgentTransactionSummaryByUniAgentId?agentId=";
    public static final String GET_ALL_ITEMS = "GetAllItems";
    public static final String GET_ALL_RATES = "GetRateClass";
    public static final String GET_ALL_REASONS = "Localization/GetPaymentDefaultedReasons";
    public static final String GET_RELATIONSHIPS = "Bills/GetRelationships";
    public static final String GET_BUSINESSES = "Balances/GetBusinessBillSummary/!";
    public static final String GET_PROPERTIES = "Balances/GetPropertyBillSummary/!";
    public static final String POST_TRANSACTIONS = "Transaction/PostTransactions";
    public static final String GET_SETTLEMENT_ID = "Settlement/PostSettlementSlipDetails";
    public static final String POST_BILLDISTRIBUTION = "Distribution/PostBillDistributionInfo";
    public static final String POST_FEEDBACK = "Localization/PostFeedbackLog";
    public static final String POST_LOCATION_LOGS = "Localization/PostPeriodicLog";

    //OTHERS
    public static final int SHORT = Toast.LENGTH_SHORT;
    public static final int LONG = Toast.LENGTH_LONG;
    public static final int NEW_SYNC = 1;
    public static final int OLD_SYNC = 0;
    public static final String BILL_DISTRIBUTION = "Bill Distribution";
    public static final String[] MONTHS = {"Jan","Feb","Mar"};

}
