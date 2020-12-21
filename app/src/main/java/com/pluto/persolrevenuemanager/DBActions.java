package com.pluto.persolrevenuemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.pluto.persolrevenuemanager.Constants.*;

import java.lang.ref.WeakReference;


public class DBActions {

    private SQLiteDatabase database;
    private WeakReference<Context> weakReference;

    public DBActions(Context context){
        weakReference = new WeakReference<>(context);
        database = new DatabaseHelper(weakReference.get()).getWritableDatabase();
    }

    public class GenericInsert extends AsyncTask<Void,Void,Boolean>{

        private JSONArray jsonArray;
        private SQLiteDatabase database;

        public GenericInsert(JSONArray jsonArray){
            this.jsonArray = jsonArray;
            database = new DatabaseHelper(weakReference.get()).getWritableDatabase();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String table = jsonObject.getString("name");
                    JSONArray nameData = jsonObject.getJSONArray("nameData");
                    JSONArray valuesData = jsonObject.getJSONArray("valueData");
                    ContentValues contentValues = new ContentValues();
                    for(int j = 0; j < nameData.length(); j++){
                        contentValues.put(nameData.getJSONObject(j).getString("name"),valuesData.getJSONObject(j).getString("value"));
                    }
                    database.insert(table,null,contentValues);
                }
                return true;
            } catch (Exception e){
                return false;
            }
        }
    }

    public String genericGetSingleItem(String table,String valueColumn, String whereColumn, String whereValue){
        try{
            Cursor cursor = database.query(table,
                    new String[]{valueColumn},
                    whereColumn + " = ?",
                    new String[]{whereValue},
                    null,null,null);
            if(cursor.moveToFirst()){
                String result = cursor.getString(0);
                cursor.close();
                return result;
            }
            return "";
        } catch (Exception e){
            return null;
        }
    }

    public static class CLearTables extends AsyncTask<Void,Void,Boolean>{

        private String[] tables;
        private SQLiteDatabase database;

        public CLearTables(Context context,String[] tables){
            this.tables = tables;
            WeakReference<Context> weakReference = new WeakReference<>(context);
            database = new DatabaseHelper(weakReference.get()).getWritableDatabase();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(int i = 0; i < tables.length; i++){
                clearTable(tables[i]);
            }
            return true;
        }

        private void clearTable(String table){
            try{
                database.execSQL("DELETE FROM "+table);
            }catch (Exception e){
                Log.e("PRM", "Error truncating " + table + " table");
                Log.e("PRM", e.toString());
            }
        }
    }

    public boolean clearTable(String table){
        try{
            database.execSQL("DELETE FROM "+table);
            return true;
        }catch (Exception e){
            Log.e("PRM", "Error truncating " + table + " table");
            Log.e("PRM", e.toString());
            return false;
        }
    }

    public boolean genericSingleInsert(String table, ContentValues contentValues){
        try{
            database.insert(table,null,contentValues);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean genericSingleUpdate(String table, String whereColumn, String whereValue, ContentValues contentValues){
        try{
            database.update(table,contentValues,whereColumn + " = ?",new String[]{whereValue});
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean isTableEmpty(String table, String whereColumn){
        try{
            Cursor cursor = database.query(table,
                    new String[]{whereColumn},
                    null,null,null,null,null);
            if(cursor.moveToFirst()){
                cursor.close();
                return false;
            }
            return true;
        } catch (Exception e){
            return true;
        }
    }

    public Cursor genericGetCursor(String table,String[] columns){
        return database.query(table,
                columns,
                null,null,null,null,null);
    }

    public Cursor genericGetSpecificCursor(String table,String[] columns, String whereColumn, String whereValue){
        return database.query(table,
                columns,
                whereColumn+" = ?",
                new String[]{whereValue}
                ,null,null,null);
    }

    public Cursor getGenericCursorSql(String sql){
        return database.rawQuery(sql,null);
    }

    public boolean genericDelete(String table, String whereColumn, String whereValue){
        try {
            database.delete(table, whereColumn + " = ?", new String[]{whereValue});
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public void multipleGenericDelete(String[] ids, String table,String whereColumn){
        for (String id : ids){
            Log.e("debug","about to delete "+id);
            genericDelete(table,whereColumn,id);
        }
    }
}
