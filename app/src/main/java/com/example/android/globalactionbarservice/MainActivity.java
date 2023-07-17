package com.example.android.globalactionbarservice;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    EditText edtPass;
    Button btnUpdatePass;
    SharedPreferences sharedPref;
    public static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        Button btnStart = findViewById(R.id.btnStart);
        btnUpdatePass = findViewById(R.id.btnUpdatePass);
        edtPass = findViewById(R.id.edtPass);

        btnUpdatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = edtPass.getText().toString().trim();
                GlobalActionBarService.PASS_VCB = pass;
                edtPass.clearFocus();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                Log.d(TAG, "Update password DONE!");
                Toast.makeText(getApplicationContext(), "Update password DONE", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pass_bank", GlobalActionBarService.PASS_VCB);
                editor.apply();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.loginAdmin();
                if (!isAccessibilityServiceEnabled(mContext, GlobalActionBarService.class)) {
                    Toast.makeText(mContext, "GlobalActionBarService is not Enable", Toast.LENGTH_LONG);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            }
        });

        if (Utils.volleyQueue == null) {
            Utils.volleyQueue = Volley.newRequestQueue(this);
            Log.d(TAG,"onCreate init Volley");
        } else {
            Log.d(TAG,"onCreate Volley not null!");
        }

        sharedPref = getApplicationContext().getSharedPreferences("BANK_DEMO", Context.MODE_PRIVATE);
        String pass = sharedPref.getString("pass_bank", "000");
        edtPass.setText(pass);
        GlobalActionBarService.PASS_VCB = pass;

    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }

}