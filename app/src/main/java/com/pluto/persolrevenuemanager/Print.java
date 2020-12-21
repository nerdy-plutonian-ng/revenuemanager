package com.pluto.persolrevenuemanager;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.basewin.aidl.OnPrinterListener;
import com.basewin.services.ServiceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.RegEx;

public class Print {

    private Context context;
    private byte[] mContent;
    private RevenueManager revenueManager;
    private String newLine = "\n";
    private String mun_ass = "Municipal Assembly";

    public Print(Context context) {
        this.context = context;
        ServiceManager.getInstence().init(context);
        revenueManager = new RevenueManager(this.context);
    }

    public void printReceipt(){
        //printLogo();
        //printAssembly();
    }

    private boolean printLogo(){
        if(!canPrint()){
            Toast.makeText(context, "Check your Printer. Do you have paper?", Toast.LENGTH_SHORT).show();
            return false;
        }
        Uri originalUri = Uri.parse("android.resource://com.pluto.persolrevenuemanager/drawable/coabaw");
        ContentResolver resolver = context.getContentResolver();

        try {
            mContent = PicUtils.getBytesFromInputStream(resolver.openInputStream(Uri.parse(originalUri.toString())), 3500000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Converts a byte array to a bitmap object that can be called by a ImageView (将字节数组转换为ImageView可调用的Bitmap对象)
        Bitmap bm = PicUtils.getPicFromBytes(mContent, null);
        if (bm != null) {
            //he bitmap object is cut, the width of 240, height according to the geometric proportion zoom (将bitmap对象进行裁剪，宽度为240,高度按等比比例缩放)
            bm = PicUtils.zoomImage(bm, 384);
            bm = PicUtils.switchColor(bm);//将彩色图片转换成黑白图片
        } else {
            Toast.makeText(context, "Failed getting logo. ", Toast.LENGTH_SHORT).show();
        }

        try {

            // add picture
            JSONObject printJson = new JSONObject();
            JSONArray printTest = new JSONArray();

            JSONObject config = new JSONObject();
            config.put("content-type", "jpg");
            config.put("position", "center");

            ServiceManager.getInstence().getPrinter().setPrintGray(Integer.valueOf(1000));//set Gray

            printTest.put(config);
            printJson.put("spos", printTest);
            // 设置底部空3行
            // Set at the bottom of the empty 3 rows
            Bitmap qr = BitmapFactory.decodeResource(context.getResources(), R.drawable.coa);
            Bitmap[] bitmaps = null;
            if (bm == null) {// if  you has not choosen  a picture ,we will print the default picture.
                bitmaps = new Bitmap[]{qr.createScaledBitmap(qr, 80, 80, true)};
            } else {
                bitmaps = new Bitmap[]{qr.createScaledBitmap(bm, 80, 80, true)};
            }
            ServiceManager.getInstence().getPrinter().printNoFeed(printJson.toString(), bitmaps, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {
                    //printAssembly();
                }

                @Override
                public void onStart() {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean canPrint(){
        try {
            return ServiceManager.getInstence().getPrinter().queryIfHavePaper();
        } catch (Exception e) {
            return false;
        }
    }

    public void printAssembly(final String receiptNo, final String item, final double amount,final String payer){
        if(!canPrint()){
            Toast.makeText(context, "Check your Printer. Do you have paper?", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        String assemblyName = revenueManager.getAssemblyName().replace(mun_ass,"").replace(mun_ass.toLowerCase(),"").replace(mun_ass.toUpperCase(),"");
        builder.append(assemblyName)
                .append(newLine)
                .append("Municipal Assembly")
                .append(newLine)
                .append(newLine);
        JSONArray printData = new JSONArray();

        JSONObject config = new JSONObject();
        try {
            // Add text printing
            config.put("content-type", "txt");
            config.put("content", builder);
            config.put("size", 30);
            config.put("position", "center");
            config.put("offset", "0");
            config.put("bold", 1);
            //config.put("italic", "italic");
            //config.put("height", "-1");
            ServiceManager.getInstence().getPrinter().setPrintGray(2000);
            //ServiceManager.getInstence().getPrinter().setLineSpace(2);
            //ServiceManager.getInstence().getPrinter().printBottomFeedLine(2);

            printData.put(config);
            JSONObject printJson = new JSONObject();
            printJson.put("spos", printData);

            ServiceManager.getInstence().getPrinter().printNoFeed(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {
                    printBody(receiptNo,item,amount,payer);
                }

                @Override
                public void onStart() {
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void printBody(String receiptNo,String item,double amount,String payer){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
        StringBuilder builder = new StringBuilder();
        builder.append("Date: ").append(simpleDateFormat.format(new Date()))
                .append(newLine)
                .append("Receipt No: ").append(receiptNo)
                .append(newLine)
                .append("Item: ").append(item)
                .append(newLine)
                .append("Amount Paid(GHS): ").append(Utils.formatMoney(amount))
                .append(newLine)
                .append("Payer: ").append(payer)
                .append(newLine)
                .append("Collector: ").append(revenueManager.getCurrentAgentName())
                .append(newLine);
        final JSONArray printData = new JSONArray();

        JSONObject config = new JSONObject();
        try {
            config.put("content-type", "txt");
            config.put("content", builder);
            config.put("size", 25);
            config.put("position", "left");
            config.put("offset", "0");
            ServiceManager.getInstence().getPrinter().setPrintGray(2000);

            printData.put(config);
            JSONObject printJson = new JSONObject();
            printJson.put("spos", printData);

            ServiceManager.getInstence().getPrinter().printNoFeed(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {
                    printFooter();
                }

                @Override
                public void onStart() {
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void printFooter(){
        StringBuilder builder = new StringBuilder();
        builder.append("Keep the city clean!!!");
        final JSONArray printData = new JSONArray();

        JSONObject config = new JSONObject();
        try {
            // Add text printing
            config.put("content-type", "txt");
            config.put("content", builder);
            config.put("size", 25);
            config.put("position", "center");
            config.put("offset", "0");
            config.put("bold", 1);
            config.put("italic", 1);
            //config.put("height", "-1");
            ServiceManager.getInstence().getPrinter().setPrintGray(2000);
            //ServiceManager.getInstence().getPrinter().setLineSpace(2);
            ServiceManager.getInstence().getPrinter().printBottomFeedLine(2);

            printData.put(config);
            JSONObject printJson = new JSONObject();
            printJson.put("spos", printData);

            ServiceManager.getInstence().getPrinter().print(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onStart() {
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public void printSettlement(String settlementId,double amount){
        printSettlementLabel(settlementId,amount);
    }

    private void printSettlementLabel(final String settlementId, final double amount) {
        if(!canPrint()){
            Toast.makeText(context, "Check your Printer. Do you have paper?", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Settlement Slip")
                .append(newLine)
                .append(newLine);
        JSONArray printData = new JSONArray();

        JSONObject config = new JSONObject();
        try {
            // Add text printing
            config.put("content-type", "txt");
            config.put("content", builder);
            config.put("size", 30);
            config.put("position", "center");
            config.put("offset", "0");
            config.put("bold", 1);
            //config.put("italic", "italic");
            //config.put("height", "-1");
            ServiceManager.getInstence().getPrinter().setPrintGray(2000);
            //ServiceManager.getInstence().getPrinter().setLineSpace(2);
            //ServiceManager.getInstence().getPrinter().printBottomFeedLine(2);

            printData.put(config);
            JSONObject printJson = new JSONObject();
            printJson.put("spos", printData);

            ServiceManager.getInstence().getPrinter().printNoFeed(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {
                    printSettlementBody(settlementId,amount);
                }

                @Override
                public void onStart() {
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void printSettlementBody(String settlementID,double amount){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
        StringBuilder builder = new StringBuilder();
        builder.append("Date: ").append(simpleDateFormat.format(new Date()))
                .append(newLine)
                .append("Settlement ID: ").append(settlementID)
                .append(newLine)
                .append("Amount to settle(GHS): ").append(Utils.formatMoney(amount))
                .append(newLine)
                .append("Collector: ").append(revenueManager.getCurrentAgentName())
                .append(newLine);
        final JSONArray printData = new JSONArray();

        JSONObject config = new JSONObject();
        try {
            config.put("content-type", "txt");
            config.put("content", builder);
            config.put("size", 25);
            config.put("position", "left");
            config.put("offset", "0");
            ServiceManager.getInstence().getPrinter().setPrintGray(2000);

            printData.put(config);
            JSONObject printJson = new JSONObject();
            printJson.put("spos", printData);

            ServiceManager.getInstence().getPrinter().printNoFeed(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onFinish() {
                    printFooter();
                }

                @Override
                public void onStart() {
                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


}
