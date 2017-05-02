package com.charming.mqdiary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;

public class ModelShowActivity extends Activity implements View.OnClickListener {
    WebView mWebView;
    OkHttpClient okHttpClient = new OkHttpClient();
    private String htmlContent;
    public static final String TAG = "MainActivity";
    private Handler mMainThreadHandler;
    private Button mButton;
    ImageView mFirstIcon;
    ImageView mPreviousIcon;
    ImageView mPlayIcon;
    ImageView mNextIcon;
    ImageView mLastIcon;
    Context mContext;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_show);
        mContext = this;
        mWebView = (WebView) findViewById(R.id.forum_context);
        mFirstIcon = (ImageView) findViewById(R.id.first_icon);
        mPreviousIcon = (ImageView) findViewById(R.id.previous_icon);
        mPlayIcon = (ImageView) findViewById(R.id.play_icon);
        mNextIcon = (ImageView) findViewById(R.id.next_icon);
        mLastIcon = (ImageView) findViewById(R.id.final_icon);

        mFirstIcon.setOnClickListener(this);
        mPreviousIcon.setOnClickListener(this);
        mPlayIcon.setOnClickListener(this);
        mNextIcon.setOnClickListener(this);
        mLastIcon.setOnClickListener(this);
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        init();
//        mWebView.loadUrl(url);
//        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(mContext.getString(R.string.oss_url)).build();
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        htmlContent = response.body().string();
                        Log.d(TAG, htmlContent);
                        mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
//                                mWebView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "utf-8", null);
                                mWebView.loadDataWithBaseURL("file:///", htmlContent, "text/html", "utf-8", null);
//                                mWebView.loadUrl(mContext.getString(R.string.oss_url));
                            }
                        });
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    private void init() {
        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);

        webSetting.setLoadWithOverviewMode(true);
        webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        Log.d("appcache path", this.getDir("appcache", 0).getPath());
        Log.d("databases path", this.getDir("databases", 0).getPath());
        Log.d("geolocation PATH", this.getDir("geolocation", 0).getPath());
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        mWebView.addJavascriptInterface(this, "AndroidClient");

        mWebView.setWebChromeClient(new WebChromeClient() {

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.first_icon:
                Log.d(TAG, "first icon is clicked");
                mWebView.loadUrl("javascript:sayHello()");
                mWebView.loadUrl("javascript:remoteControlFirst()");
                break;
            case R.id.previous_icon:
                Log.d(TAG, "pre icon is clicked");
                mWebView.loadUrl("javascript:remoteControlPre()");
                break;
            case R.id.play_icon:
                Log.d(TAG, "play icon is clicked");
                mWebView.loadUrl("javascript:remoteControlPlay()");
                break;
            case R.id.next_icon:
                Log.d(TAG, "next icon is clicked");
                mWebView.loadUrl("javascript:sayHello()");
                mWebView.loadUrl("javascript:remoteControlNext()");
                break;
            case R.id.final_icon:
                Log.d(TAG, "final icon is clicked");
                mWebView.loadUrl("javascript:remoteControlFinal()");
                break;
        }
    }

    @JavascriptInterface
    public void pausePlaying() {
        Log.d(TAG,"pausePlaying is called");
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.playicon));
            }
        });
    }

    @JavascriptInterface
    public void startPlaying() {
        Log.d(TAG,"startPlaying is called");
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.stopicon));
            }
        });
    }
}





















