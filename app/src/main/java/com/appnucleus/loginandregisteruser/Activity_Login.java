package com.appnucleus.loginandregisteruser;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import android.os.Vibrator;

public class Activity_Login extends AbsRuntimePermission {
    // LogCat tag
    private static final String TAG = Activity_Register.class.getSimpleName();
    private static final int REQUEST_PERMISSION = 10;
    private Button btnLogin;
    private Button btnLinkToRegister, btnLinkToForgotPassword;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    RequestQueue rq;
    ProgressDialog dialog;
    String p_id, name3, name4;
    int a, a1;
    String email, password, url;
    private Session session;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        session = new Session(this);

        rq = Volley.newRequestQueue(Activity_Login.this);
        super.onCreate(savedInstanceState);
        setContentView(com.appnucleus.loginandregisteruser.R.layout.activity_login);


        requestAppPermissions(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS},
                        R.string.msg,REQUEST_PERMISSION);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnLinkToForgotPassword = (Button) findViewById(R.id.forgot_password);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //mysesion
       if (session.loggedin()){
          startActivity(new Intent(Activity_Login.this, NevigationDrawer.class));
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                try {
                    if(email.equals("") || password.equals(""))
                    {
                        Toast.makeText(getApplicationContext(), R.string.ep_blank, Toast.LENGTH_LONG).show();
                        Animation shake = AnimationUtils.loadAnimation(Activity_Login.this, R.anim.shake);
                        inputPassword.startAnimation(shake);
                        inputEmail.startAnimation(shake);
                        vibrate(btnLogin);
                    }
                    else{
                        name3 = URLEncoder.encode(email, "UTF-8");
                        name4 = URLEncoder.encode(password, "UTF-8");
                        sendr();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        Activity_Register.class);
                startActivity(i);

                overridePendingTransition(com.appnucleus.loginandregisteruser.R.anim.push_left_in, com.appnucleus.loginandregisteruser.R.anim.push_left_out);
            }
        });

        btnLinkToForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Login.this, ForgotPassword.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    public void sendr() {

        dialog = new ProgressDialog(Activity_Login.this);
        dialog.setMessage(Activity_Login.this.getString(R.string.logging));
        dialog.show();

        url = "https://sens-agriculture.herokuapp.com/login?uname="+name3+"&pwd="+name4;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray obj = response.getJSONArray("rows");
                    a1 = obj.length();
                    if(a1>0) {
                        for (int i = 0; i < obj.length(); i++) {
                            JSONObject jsonObject1 = obj.getJSONObject(i);
                            p_id = jsonObject1.getString("product_id");
                            check();
                        }
                    } else{
                        dialog.dismiss();
                        Animation shake = AnimationUtils.loadAnimation(Activity_Login.this, R.anim.shake);
                        inputPassword.startAnimation(shake);
                        inputEmail.startAnimation(shake);
                        vibrate(btnLogin);
                        Toast.makeText(getApplicationContext(), R.string.ep_wrong, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });

        rq.add(jsonObjectRequest);
    }

    public void check() {

        dialog.dismiss();

        if (p_id.equalsIgnoreCase("null")) {
            Toast.makeText(getApplicationContext(), R.string.no_prod_id, Toast.LENGTH_LONG).show();
        }
        else
            {

            session.setLoggedin(true);
            Intent i1 = new Intent(Activity_Login.this, NevigationDrawer.class);
            i1.putExtra("p_id1", p_id);
            i1.putExtra("username1", email);
            startActivity(i1);
            finish();

            inputEmail.setText("");
            inputPassword.setText("");

        }
    }

    public void vibrate(View view) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}