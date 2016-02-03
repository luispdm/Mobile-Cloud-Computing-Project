package com.example.luigidigirolamo.calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText username = null;
    private EditText password = null;
    private EditText ipAddress = null;
    public static JSONObject jsonObject = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void Login(View v) {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        ipAddress = (EditText) findViewById(R.id.ipAddress);

        try {
            new LoginOperation(this).execute(username.getText().toString(), password.getText().toString(), ipAddress.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Register(View v) {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        ipAddress = (EditText) findViewById(R.id.ipAddress);

        try {
            new RegisterOperation(this).execute(username.getText().toString(), password.getText().toString(), ipAddress.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
