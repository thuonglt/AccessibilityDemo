package com.example.android.globalactionbarservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class TransactionBank {
    String bankName;
    String accountNumber = "00";
    HashMap<String, String> detail = new HashMap<>();

    public String convertToJson() throws JSONException {
        String jsonData = "";
        String transactionId = "NULL";
        JSONObject object = new JSONObject();
        object.put("bankName", bankName);
        object.put("accountNumber", accountNumber);
        JSONObject detailObject = new JSONObject();
        Set<String> keys = detail.keySet();
        for (String key : keys) {
            detailObject.put(key, detail.get(key));
            if (key.equalsIgnoreCase("Số lệnh giao dịch")) {
                transactionId = detail.get(key);
            }
        }
        object.put("transaction", detailObject);
        object.put("transactionId", transactionId);
        jsonData = object.toString();
        return jsonData;
    }
}
