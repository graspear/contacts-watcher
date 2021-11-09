package com.example.sqcontacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<ContactModel> contactModels = new ArrayList<ContactModel>();
    DBContacts dbContacts = new DBContacts(MainActivity.this);
    RecyclerView recyclerView;
    int statusOfContacts;
    ProgressDialog progressDialog;
    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        relativeLayout = findViewById(R.id.rel);
        getFromdb();

    }

    private void getFromdb() {
        if (dbContacts.getAll().size() > 0) {
            ContactsThread contactsThread = new ContactsThread(dbContacts.getAll());
            new Thread(contactsThread).start();
            createRecycle();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 100);
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("Fetching all contacts");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else getContacts();


    }

    private void getContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Uri uri1 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                String string_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cursor1 = getContentResolver().query(uri1, null, selection, new String[]{id}, null);
                if (cursor1.moveToNext()) {
                    String number = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    ContactModel model = new ContactModel();
                    model.setName(name);
                    model.setNumber(number);
                    model.setId(string_id);
                    dbContacts.addContacts(model);
                    contactModels.add(model);
                    cursor1.close();
                }
            } while (cursor.moveToNext());
            progressDialog.dismiss();
            createRecycle();
        }
    }

    private void createRecycle() {
        ContactsAdapter contactsAdapter = new ContactsAdapter(MainActivity.this, dbContacts.getAll());
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(contactsAdapter);
        Snackbar snackbar = Snackbar.make(relativeLayout, "All contacts fetched", Snackbar.LENGTH_LONG);
        snackbar.show();
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        } else
            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
    }

    public class ContactsThread implements Runnable {
        private List<ContactModel> all;

        public ContactsThread(List<ContactModel> all) {
            this.all = all;
        }

        @Override
        public void run() {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
            Cursor cursor = getContentResolver().query(uri, null, null, null, sort);
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Uri uri1 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                    String string_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor cursor1 = getContentResolver().query(uri1, null, selection, new String[]{id}, null);
                    if (cursor1.moveToNext()) {
                        String number = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        ContactModel model = new ContactModel();
                        model.setName(name);
                        model.setNumber(number);
                        model.setId(string_id);
                        contactModels.add(model);
                        cursor1.close();
                    }
                } while (cursor.moveToNext());
            }
            if (all.size() != contactModels.size()) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Detected few changes.Uploading new contacts", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        dbContacts.delete();
                        ContactsAdapter contactsAdapter = new ContactsAdapter(MainActivity.this, contactModels);
                        contactsAdapter.notifyDataSetChanged();
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
                        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
                        recyclerView.addItemDecoration(dividerItemDecoration);
                        recyclerView.setAdapter(contactsAdapter);
                        dbContacts.delete();
                        for (int i = 0; i < contactModels.size(); i++) {
                            dbContacts.addContacts(contactModels.get(i));
                        }
                    }
                });
            } else if (all.equals(contactModels)) {
                Handler handlerss = new Handler(Looper.getMainLooper());
                handlerss.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "No changes", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handlerrs = new Handler(Looper.getMainLooper());
                handlerrs.post(new Runnable() {
                                   @Override
                                   public void run() {
                                       Snackbar snackbar = Snackbar.make(relativeLayout, "Detected few changes.Uploading new contacts", Snackbar.LENGTH_LONG);
                                       snackbar.show();
                                       dbContacts.delete();
                                       ContactsAdapter contactsAdapter = new ContactsAdapter(MainActivity.this, contactModels);
                                       contactsAdapter.notifyDataSetChanged();
                                       DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
                                       dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
                                       recyclerView.addItemDecoration(dividerItemDecoration);
                                       recyclerView.setAdapter(contactsAdapter);
                                       dbContacts.delete();
                                       for (int i = 0; i < contactModels.size(); i++) {
                                           dbContacts.addContacts(contactModels.get(i));
                                       }
                                   }
                               }
                );
            }
        }
    }
}