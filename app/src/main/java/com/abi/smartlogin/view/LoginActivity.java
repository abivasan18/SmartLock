package com.abi.smartlogin.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.abi.smartlogin.R;
import com.abi.smartlogin.db.DatabaseAccess;
import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.entity.UserRole;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.etUsername = (EditText) findViewById(R.id.etUsername);
        this.etPassword = (EditText) findViewById(R.id.etPassword);
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
    }

    public void login(View view) {

        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        databaseAccess.open();
        User user = databaseAccess.getUser(username);
        databaseAccess.close();
        if (user != null && password.equals(user.getPassword())) {

            Class clazz;
            if (UserRole.ADMIN == user.getRole()) {
                clazz = AdminActivity.class;
            } else {
                clazz = UserActivity.class;
            }
            Intent intent = new Intent(this, clazz);
            intent.putExtra("username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            etUsername.setText("");
            etPassword.setText("");
        }
    }
}
