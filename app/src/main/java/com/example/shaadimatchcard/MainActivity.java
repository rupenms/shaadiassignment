package com.example.shaadimatchcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase profileDb;
    RecyclerViewAdapter adapter;
    ArrayList<String> profileList;
    ArrayMap<String,String> profileListMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileDb = this.openOrCreateDatabase("Shaadi", MODE_PRIVATE, null);
        profileDb.execSQL("CREATE TABLE IF NOT EXISTS shaadi(id INTEGER PRIMARY KEY AUTOINCREMENT,gender VARCHAR, title VARCHAR, firstname VARCHAR, lastname VARCHAR, city VARCHAR, state VARCHAR, country VARCHAR, email VARCHAR, age INT(3), phone VARCHAR, picture VARCHAR, status VARCHAR)");
        profileDb.execSQL("DELETE FROM shaadi");

        profileList = new ArrayList<>();
        profileListMap = new ArrayMap<>();
        // set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.profileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, profileList);
        recyclerView.setAdapter(adapter);

        Downloadtask downloadtask = new Downloadtask();
        try {
            downloadtask.execute("https://randomuser.me/api/?results=10");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Downloadtask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String results = jsonObject.getString("results");
                JSONArray array = new JSONArray(results);

                for (int i=0; i < array.length(); i++) {
                    JSONObject jsonpart = array.getJSONObject(i);

                    String gender = jsonpart.getString("gender");
                    JSONObject nameObject = jsonpart.getJSONObject("name");
                    String title = nameObject.getString("title");
                    String firstName = nameObject.getString("first");
                    String lastName = nameObject.getString("last");
                    JSONObject locationObject = jsonpart.getJSONObject("location");
                    String city = locationObject.getString("city");
                    String state = locationObject.getString("state");
                    String country = locationObject.getString("country");
                    String email = jsonpart.getString("email");
                    JSONObject dobObject = jsonpart.getJSONObject("dob");
                    int age = dobObject.getInt("age");
                    String phone = jsonpart.getString("phone");
                    JSONObject picObject = jsonpart.getJSONObject("picture");
                    String picture = picObject.getString("large");
                    insertData(gender,title,firstName,lastName,city,state,country,email,age,phone,picture);
                }
                fetchData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insertData(String gender,String title,String firstName,String lastName,String city,String state,String country,String email,int age,String phone,String picture) {
        try {
            String sql = "INSERT INTO shaadi(gender, title, firstname, lastname, city, state, country, email, age, phone, picture) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            SQLiteStatement statement = profileDb.compileStatement(sql);
            statement.bindString(1, gender);
            statement.bindString(2, title);
            statement.bindString(3, firstName);
            statement.bindString(4, lastName);
            statement.bindString(5, city);
            statement.bindString(6, state);
            statement.bindString(7, country);
            statement.bindString(8, email);
            statement.bindString(9, Integer.toString(age));
            statement.bindString(10, phone);
            statement.bindString(11, picture);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchData() {
        try {
            Cursor c = profileDb.rawQuery("SELECT * FROM shaadi", null);

            int idIndex = c.getColumnIndex("id");
            int genderIndex = c.getColumnIndex("gender");
            int titleIndex = c.getColumnIndex("title");
            int firstNameIndex = c.getColumnIndex("firstname");
            int lastNameIndex = c.getColumnIndex("lastname");
            int cityIndex = c.getColumnIndex("city");
            int stateIndex = c.getColumnIndex("state");
            int countryIndex = c.getColumnIndex("country");
            int emailIndex = c.getColumnIndex("email");
            int ageIndex = c.getColumnIndex("age");
            int phoneIndex = c.getColumnIndex("phone");
            int pictureIndex = c.getColumnIndex("picture");

            c.moveToFirst();
            while (!c.isAfterLast()) {
                String id = Integer.toString(c.getInt(idIndex));
                String gender = c.getString(genderIndex);
                String title = c.getString(titleIndex);
                String firstName = c.getString(firstNameIndex);
                String lastName = c.getString(lastNameIndex);
                String city = c.getString(cityIndex);
                String state = c.getString(stateIndex);
                String country = c.getString(countryIndex);
                String email = c.getString(emailIndex);
                String age = Integer.toString(c.getInt(ageIndex));
                String phone = c.getString(phoneIndex);
                String picture = c.getString(pictureIndex);
                profileList.add(title+" "+firstName+" "+lastName+","+gender+" - "+age+" - "+city+" "+state+" "+country+","+email+","+phone+","+picture+","+id);
                c.moveToNext();
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}