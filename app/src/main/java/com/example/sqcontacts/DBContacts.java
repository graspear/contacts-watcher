package com.example.sqcontacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class DBContacts extends SQLiteOpenHelper {
    public static final String TABLE_NAME="CONTACTS_TABLE";
    public static final String COLUMN_NAME="USER_NAME";
    public static final String COLUMN_NUMBER="USER_NUMBER";
    public static final String COLUMN_CONTACT_ID="USER_CONTACT_ID";
    public static final String COLUMN_ID="USER_ID";


    public DBContacts(@Nullable Context context) {
        super(context, "contacts.db", null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable="CREATE TABLE "+TABLE_NAME+" ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_NAME+" TEXT, "+COLUMN_NUMBER+" TEXT, "+COLUMN_CONTACT_ID+" TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public boolean addContacts(ContactModel contactModel){
        SQLiteDatabase sqLiteDatabase=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COLUMN_NAME,contactModel.getName());
        contentValues.put(COLUMN_NUMBER,contactModel.getNumber());
        contentValues.put(COLUMN_CONTACT_ID,contactModel.getId());
        long insert=sqLiteDatabase.insert(TABLE_NAME,null,contentValues);
        if (insert==-1) return false;
        return true;
    }
    public List<ContactModel> getAll(){
        SQLiteDatabase database=this.getReadableDatabase();
        String queryString="SELECT * FROM "+TABLE_NAME;
        List<ContactModel> contactModels=new ArrayList<>();
        Cursor cursor=database.rawQuery(queryString,null,null);
        if (cursor.moveToFirst()){
            do {
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String user_id=cursor.getString(3);
                ContactModel contactModel = new ContactModel(name, number,user_id);
                contactModels.add(contactModel);
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return contactModels;
    }
    public void delete(){
        SQLiteDatabase database=this.getWritableDatabase();
        database.execSQL("DELETE FROM "+TABLE_NAME);
    }
}
