// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

public class GlobalActionBarService extends AccessibilityService {
    public static String TAG = "GlobalActionBarService";
    private static String VCB_PKG = "com.VCB";
    public static String PASS_VCB = "";
    private static String mAccountNumber = "00";
    public static long DELAY_TIME_LONG = 10000;
    public static long DELAY_TIME_MEDIUM = 5000;
    public static long DELAY_TIME_SHORT = 3000;
    public static long DELAY_TIME_VEY_SHORT = 1000;
    FrameLayout mLayout;
    Context mContext;

    @Override
    protected void onServiceConnected() {
        // Create an overlay and display the action bar
        Log.d(TAG, "onServiceConnected");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);

        mContext = getApplicationContext();
        configureStartButton();
        configureScrollButton();
        configureSwipeButton();
        configureStopButton();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    private void configureStopButton() {
        Button stopButton = (Button) mLayout.findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "configureStopButton call doLoginVCB");
                showAllNodes(getRootInActiveWindow());

                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void configureScrollButton() {
        Button scrollButton = (Button) mLayout.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }

    private void configureSwipeButton() {
        Button swipeButton = (Button) mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSwipe();
            }
        });
    }

    private void doSwipe() {
        Path swipePath = new Path();
        swipePath.moveTo(600, 1600);
        swipePath.lineTo(600, 300);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    private void configureStartButton() {
        Button startButton = (Button) mLayout.findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                Log.d(TAG, "configureStartButton onClick");
                Intent intent = getPackageManager().getLaunchIntentForPackage(VCB_PKG);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                waitIdle(DELAY_TIME_SHORT);
                doLoginVCB();
                waitIdle(DELAY_TIME_MEDIUM);
                getTransactionHistory();
            }
        });
    }

    private void getTransactionHistory() {
        // get account number
        AccessibilityNodeInfo accountNo = getNodeByID("com.VCB:id/tvAccountNo");
        if (accountNo != null) {
            mAccountNumber = accountNo.getText().toString().trim();
        }
        //-------------------
        List<AccessibilityNodeInfo> nodes = findNodesByText(getRootInActiveWindow(), "Cài đặt");
        for (AccessibilityNodeInfo nodeInfo : nodes) {
            clickParentIfAble(nodeInfo);
            Log.d(TAG, "getTransactionHistory setting: " + nodeInfo);
            break;
        }
        waitIdle(DELAY_TIME_SHORT);
        nodes = findNodesByText(getRootInActiveWindow(), "Báo cáo giao dịch");
        for (AccessibilityNodeInfo nodeInfo : nodes) {
            clickParentIfAble(nodeInfo);
            Log.d(TAG, "getTransactionHistory transaction: " + nodeInfo);
            break;
        }
        waitIdle(DELAY_TIME_SHORT);
        AccessibilityNodeInfo nodeSearch = getNodeByID("com.VCB:id/btSearch");
        if (nodeSearch != null) {
            clickParentIfAble(nodeSearch);
        }
        waitIdle(DELAY_TIME_MEDIUM);
        int prev_count = 0;
        List<AccessibilityNodeInfo> nodeMoneys = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.VCB:id/tvMoney");
        while (nodeMoneys.size() != prev_count) {
            prev_count = nodeMoneys.size();
            doSwipe();
            waitIdle(DELAY_TIME_SHORT);
            nodeMoneys = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.VCB:id/tvMoney");
        }
        Log.d(TAG, "getTransactionHistory tvMoney: " + nodeMoneys.size());
        for (AccessibilityNodeInfo nodeInfo : nodeMoneys) {
            TransactionBank transaction = new TransactionBank();
            transaction.bankName = "VCB";
            transaction.accountNumber = mAccountNumber;
            clickParentIfAble(nodeInfo);
            waitIdle(DELAY_TIME_VEY_SHORT);
            getTransactionDetail(transaction);
            performGlobalAction(GLOBAL_ACTION_BACK);
            waitIdle(DELAY_TIME_VEY_SHORT);
        }
    }

    private void getTransactionDetail(TransactionBank transaction) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> llRoot = rootNode.findAccessibilityNodeInfosByViewId("com.VCB:id/llRoot");
        for (AccessibilityNodeInfo nodeInfo : llRoot) {
            AccessibilityNodeInfo labelNode = getNodeByID(nodeInfo, "com.VCB:id/tvLabel");
            AccessibilityNodeInfo contentNode = getNodeByID(nodeInfo, "com.VCB:id/tvContent");
            if (labelNode != null && contentNode != null) {
                String label = labelNode.getText().toString().trim();
                String content = contentNode.getText().toString().trim();
                transaction.detail.put(label, content);
                //Log.d(TAG, "getTransactionHistory tvLabel: " + label);
                //Log.d(TAG, "getTransactionHistory tvContent: " + content);
            }
        }
        try {
            String jsonData = transaction.convertToJson();
            Log.d(TAG, jsonData);
            Utils.uploadTransaction(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getTransactionHistory ------------------------- DONE!");
    }

    private void doLoginVCB() {
        Log.d(TAG, "doLoginVCB start!");
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.d(TAG, "doLoginVCB rootNode is null!");
            return;
        }
        fillPassword(rootNode, PASS_VCB);
        Utils.loginAdmin();
    }

    private void fillPassword(AccessibilityNodeInfo rootNode, String password) {
        Log.d(TAG, "fillPassword start!");
        AccessibilityNodeInfo passwordNode = getNodeByID("com.VCB:id/edtInput");
        if (passwordNode != null) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, password);
            passwordNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            Log.d(TAG, "fillPassword editText: performAction ACTION_SET_TEXT");
        }

        List<AccessibilityNodeInfo> nodeInfoList = rootNode.findAccessibilityNodeInfosByViewId("com.VCB:id/btnNext");
        for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.d(TAG, "fillPassword btnNext: " + nodeInfo.toString());
        }
    }

    private AccessibilityNodeInfo getNodeByID(AccessibilityNodeInfo rootNode, String id) {
        if (rootNode == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(id);
        return nodes.size() > 0 ? nodes.get(0) : null;
    }

    private AccessibilityNodeInfo getNodeByID(String id) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(id);
        return nodes.size() > 0 ? nodes.get(0) : null;
    }

    private List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo root, String text) {
        List<AccessibilityNodeInfo> results = new ArrayList<>();
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if ((node.getText() != null && node.getText().toString().contains(text)) ||
                    (node.getContentDescription() != null && node.getContentDescription().toString().contains(text))) {
                results.add(node);
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return results;
    }

    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    private void showAllNodes(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            Log.d(TAG, "Node:" + node);
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
    }

    private void showAllWindow() {
        List<AccessibilityWindowInfo> root = getWindows();
        Deque<AccessibilityWindowInfo> deque = new ArrayDeque<>();
        for (AccessibilityWindowInfo node : root) {
            deque.add(node);
        }

        while (!deque.isEmpty()) {
            AccessibilityWindowInfo node = deque.removeFirst();
            Log.d(TAG, "Node:" + node);
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
    }

    void clickParentIfAble(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.isClickable()) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return;
        }
        clickParentIfAble(node.getParent());
    }

    public void waitIdle(long delay_time) {
        try {
            Thread.sleep(delay_time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
