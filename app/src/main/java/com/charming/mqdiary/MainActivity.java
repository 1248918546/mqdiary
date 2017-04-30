package com.charming.mqdiary;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by cm on 2017/4/27.
 */

public class MainActivity extends Activity implements View.OnClickListener {
    Button mButton;
    String modelDownloadUrl = "model.bin";
    String gumDownloadUrl = "2_10_1.gum";
    String planDownloadUrl = "2_10_1.plan";
    String[] list = new String[]{modelDownloadUrl, gumDownloadUrl, planDownloadUrl};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Log.d("path", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    void initView() {
        mButton = (Button) findViewById(R.id.download_button);
        mButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        for (String url : list) {
            Log.d("url", url);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.getString(R.string.server_address) + url));
            request.setDestinationInExternalPublicDir("/download/", url);
            DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        }

    }
}
