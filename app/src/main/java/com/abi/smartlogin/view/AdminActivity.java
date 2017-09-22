package com.abi.smartlogin.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.abi.smartlogin.R;
import com.abi.smartlogin.db.DatabaseAccess;
import com.abi.smartlogin.entity.User;

import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private DatabaseAccess databaseAccess;
    private ListView lstUsers;
    private String username;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        this.lstUsers = (ListView) findViewById(R.id.lstUsers);
        this.databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        this.username = getIntent().getExtras().getString("username");

        this.lstUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("user", users.get(i));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.databaseAccess.open();
        this.users = this.databaseAccess.getUsers();
        this.databaseAccess.close();
        this.lstUsers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users));
    }
}
