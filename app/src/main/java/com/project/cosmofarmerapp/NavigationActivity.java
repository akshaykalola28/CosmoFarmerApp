package com.project.cosmofarmerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView displayName, displayEmail;

    JSONObject userDataJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        displayName = headerView.findViewById(R.id.user_name);
        displayEmail = headerView.findViewById(R.id.user_email);

        String userDataString = getIntent().getStringExtra("userData");
        try {
            userDataJson = new JSONObject(userDataString);

            displayName.setText(String.format("%s %s", userDataJson.getString("fname"), userDataJson.getString("lname")));
            displayEmail.setText(userDataJson.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new HomeFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        SharedPreferences prefs = getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(NavigationActivity.this, LogInActivity.class));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_crop) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddCropFragment()).commit();
        } else if (id == R.id.nav_my_crop) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new MyCropFragment()).commit();
        } else if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new HomeFragment()).commit();
        } else if (id == R.id.nav_add_land) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new AddLandFragment()).commit();
        } else if (id == R.id.nav_my_land) {
            getSupportFragmentManager().beginTransaction().replace(R.id.navigation_frame, new MyLandFragment()).commit();
        } else if (id == R.id.nav_logout) {
            logOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public JSONObject getUser() {
        return userDataJson;
    }


}
