package com.project.cosmofarmerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.project.cosmofarmerapp.services.APIClient;
import com.project.cosmofarmerapp.services.APIServices;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    APIServices apiServices;
    EditText usernameField, passwordField;
    Button loginButton;

    String username, password;
    SharedPreferences mPreferences;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_log_in);
        changeStatusBarColor();

        apiServices = APIClient.getClient().create(APIServices.class);

        // shared preferences
        mPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        checkSharedPreferences();
        
        Button changeLang = findViewById(R.id.changeLang);
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLangDialog();
            }
        });

        usernameField = findViewById(R.id.input_username);
        passwordField = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getValidData()) {
                    mDialog = new ProgressDialog(LogInActivity.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();
                    userLogin();
                }
            }
        });
    }

    private void showChangeLangDialog() {
        final String [] listiems = {"हिन्दी", "English"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LogInActivity.this);
        mBuilder.setTitle("Choose Language..");
        mBuilder.setSingleChoiceItems(listiems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    setLocale("hi");
                    recreate();
                }
                else if (which == 1) {
                    setLocale("en");
                    recreate();
                }

                dialog.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    private void userLogin() {
        final JsonObject user = new JsonObject();
        user.addProperty("username", username);

        Call<JsonObject> call = apiServices.loginUser(user);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject jsonResponse = response.body();
                    if (jsonResponse.get("response").getAsString().equals("success")) {
                        JsonObject data = jsonResponse.get("data").getAsJsonArray().get(0).getAsJsonObject();
                        String passCheck = data.get("password").getAsString();
                        if (password.equals(passCheck)) {
                            mDialog.dismiss();
                            savePreferences(data.toString());
                            startActivity(new Intent(LogInActivity.this, NavigationActivity.class)
                                    .putExtra("userData", data.toString()));
                            finish();
                        } else {
                            mDialog.dismiss();
                            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                    "Wrong Password.", Snackbar.LENGTH_INDEFINITE).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                jsonResponse.get("data").getAsString(), Snackbar.LENGTH_INDEFINITE).show();
                    }
                } else {
                    mDialog.dismiss();
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                            "Something is Wrong. Please, Try Again.", Snackbar.LENGTH_INDEFINITE).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mDialog.dismiss();
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                        "Something is Wrong. Please, Try Again.", Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    private boolean getValidData() {
        boolean valid = false;
        username = usernameField.getText().toString().trim();
        password = passwordField.getText().toString().trim();

        if (username.isEmpty()) {
            usernameField.setError("Enter Phone or Email");
            usernameField.requestFocus();
        } else if (password.isEmpty()) {
            passwordField.setError("Enter Password");
            passwordField.requestFocus();
        } else {
            valid = true;
        }
        return valid;
    }

    public void onClickRegister(View view) {
        startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
    }

    private void savePreferences(String data) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("userDataStringKey", data);
        editor.apply();
    }

    private void checkSharedPreferences() {

        if (mPreferences.contains("userDataStringKey")) {
            String userDataString = mPreferences.getString("userDataStringKey", "");
            Intent intent = new Intent(LogInActivity.this, NavigationActivity.class);
            intent.putExtra("userData", userDataString);
            startActivity(intent);
            finish();
        }
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
}
