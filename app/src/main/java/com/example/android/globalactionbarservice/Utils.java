package com.example.android.globalactionbarservice;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static String TAG = "Utils";
    public static String accessToken = "";
    public static RequestQueue volleyQueue = null;
    public static String username = "admin1";
    public static String password = "abc13579";
    public static String loginUrl = "https://api.citysports.vn/auth/login/";

    public static String uploadTransactionUrl = "https://api.citysports.vn/bank";


    public static void loginAdmin() {
        Log.d(TAG, "loginAdmin START!");
        StringRequest loginRequest = new StringRequest(Request.Method.POST, Utils.loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        JSONObject tokens = jsonObject.getJSONObject("tokens");
                        Utils.accessToken = tokens.getString("access").toString().trim();
                        Log.e(TAG, "onResponse access:" + Utils.accessToken);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        Utils.volleyQueue.add(loginRequest);
        Log.d(TAG, "loginAdmin DONE!");
    }

    public static void uploadTransaction(String jsonData) {
        if (accessToken.isEmpty() || volleyQueue == null) {
            Log.d(TAG, "uploadTransaction accessToken not init!");
            return;
        }
        if (jsonData == null || jsonData.isEmpty()) {
            Log.d(TAG, "uploadTransaction rawBodyJson is null!");
            return;
        }
        Log.d(TAG, "uploadTransaction start!");
        StringRequest uploadRequest = new StringRequest(Request.Method.POST, Utils.uploadTransactionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject jsonObject = null;
                    Log.e(TAG, "onResponse: " + response);
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error is " + error);
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessToken);
                params.put("Content-Type", "application/json");
                return params;
            }

            //Pass Your Body here
            @Override
            public byte[] getBody() throws AuthFailureError {
                byte[] body = {};
                try {
                    body = jsonData.replace("\\/", "/").getBytes("utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return body;
            }

        };
        Utils.volleyQueue.add(uploadRequest);
    }
}
