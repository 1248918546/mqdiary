package com.charming.mqdiary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    ImageView mFrontImage;
    ImageView mLeftImage;
    ImageView mRightImage;
    Context mContext;
    int mIndex = 0;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private static final int FRONT_PHOTO_REQUEST_CODE = 10001;
    private static final int LEFT_PHOTO_REQUEST_CODE = 10002;
    private static final int RIGHT_PHOTO_REQUEST_CODE = 10003;

    private int imageHeight;
    private int imageWidth;

    private static final String CAMERA_DIR = "/dcim/";
    private static final String albumName = "MQPhoto";

    File imageFile;
    Bitmap mPhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_show);
        mContext = this;
        initView();
        initWebview();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
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

    private void initView() {
        mWebView = (WebView) findViewById(R.id.forum_context);

        mFirstIcon = (ImageView) findViewById(R.id.first_icon);
        mPreviousIcon = (ImageView) findViewById(R.id.previous_icon);
        mPlayIcon = (ImageView) findViewById(R.id.play_icon);
        mNextIcon = (ImageView) findViewById(R.id.next_icon);
        mLastIcon = (ImageView) findViewById(R.id.final_icon);

        mFrontImage = (ImageView) findViewById(R.id.front_image);
        mLeftImage = (ImageView) findViewById(R.id.left_image);
        mRightImage = (ImageView) findViewById(R.id.right_image);

        imageHeight = mFrontImage.getHeight();
        imageWidth = mFrontImage.getWidth();

        mFirstIcon.setOnClickListener(this);
        mPreviousIcon.setOnClickListener(this);
        mPlayIcon.setOnClickListener(this);
        mNextIcon.setOnClickListener(this);
        mLastIcon.setOnClickListener(this);
        mFrontImage.setOnClickListener(this);
        mLeftImage.setOnClickListener(this);
        mRightImage.setOnClickListener(this);
    }

    private void initWebview() {
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
        Log.d(TAG, "onActivityResult, requestCode : " + requestCode + "resultCode : " + resultCode);
        switch (requestCode) {
            case FILE_CHOOSER_RESULT_CODE:
                if (null == uploadMessage && null == uploadMessageAboveL) return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                if (uploadMessageAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data);
                } else if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(result);
                    uploadMessage = null;
                }
                break;
            case FRONT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    decodeBitmap();
                    mFrontImage.setImageBitmap(mPhotoBitmap);
                }
                break;
            case LEFT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    decodeBitmap();
                    mLeftImage.setImageBitmap(mPhotoBitmap);
                }
                break;
            case RIGHT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    decodeBitmap();
                    mRightImage.setImageBitmap(mPhotoBitmap);
                }
                break;
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
            case R.id.front_image:
                Log.d(TAG, "front image is clicked");
                imageFile = new File(getPhotoDir() + "/round_" + mIndex + "_front.jpg");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, FRONT_PHOTO_REQUEST_CODE);
                break;
            case R.id.left_image:
                Log.d(TAG, "left image is clicked");
                imageFile = new File(getPhotoDir() + "/round_" + mIndex + "_left.jpg");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, LEFT_PHOTO_REQUEST_CODE);
                break;
            case R.id.right_image:
                Log.d(TAG, "right image is clicked");
                imageFile = new File(getPhotoDir() + "/round_" + mIndex + "_right.jpg");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, RIGHT_PHOTO_REQUEST_CODE);
                break;
        }
    }

    @JavascriptInterface
    public void pausePlaying() {
        Log.d(TAG, "pausePlaying is called");
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.playicon));
            }
        });
    }

    @JavascriptInterface
    public void startPlaying() {
        Log.d(TAG, "startPlaying is called");
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.stopicon));
            }
        });
    }

    @JavascriptInterface
    public void updateIndex(int index) {
        Log.d(TAG, "updateIndex is called, index is " + index);
        mIndex = index;
    }

    private void decodeBitmap() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((imageWidth > 0) && (imageHeight > 0)) {
            scaleFactor = Math.min(photoW / imageWidth, photoH / imageHeight);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        mPhotoBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
    }

    private String getPhotoDir() {
        File privateDir = null;
        File publicDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            privateDir = new File(Environment.getExternalStorageDirectory() + CAMERA_DIR + albumName);
            Log.d(TAG, "private dir : " + privateDir.getAbsolutePath());
            publicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
            Log.d(TAG, "public dir : " + publicDir.getAbsolutePath());
            if (publicDir != null) {
                if (!publicDir.mkdirs()) {
                    if (!publicDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
            return null;
        }

        return publicDir.getAbsolutePath();
    }


}





















