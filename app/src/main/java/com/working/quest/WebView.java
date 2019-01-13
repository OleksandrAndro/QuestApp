package com.working.quest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.chromium.base.Log;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkGetBitmapCallback;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.util.TimerTask;

public class WebView extends XWalkActivity {

    XWalkView mXWalkView;                                    //initialization XWalkView
    public static final String TAG = "XWalkViewCallbacks";
    ProgressBar progressBar;
    int loadUrlCount = 0;
    Bundle msavedInstanceState;
    //Timer timer;                                            //uncomment when need to used page loading timeout
    String URL;
    int sevnBackToExitPressedOnce = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //window = getWindow();
        //window.requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        URL = intent.getStringExtra("url");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //WebView mWebView = (WebView) findViewById(R.id.webview);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    if (sevnBackToExitPressedOnce == 0) {

                        finish();
                    }

                    if (mXWalkView.getNavigationHistory().canGoBack()){
                        mXWalkView.getNavigationHistory().clear();
                    }

                    sevnBackToExitPressedOnce--;
                    if (sevnBackToExitPressedOnce == 1)
                        //Toast.makeText(this, "click one else to exit", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            sevnBackToExitPressedOnce=7;
                        }
                    }, 4000);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void initXWalkView() {

            //mXWalkView.load("http://statistics.carrot.network/carrot-miner-58-dashboard.html", null);
                       //getting URL from MainActivity via intent
            //Toast toast = Toast.makeText(getApplicationContext(), URL, Toast.LENGTH_LONG);  //it was for debuging
            //toast.show();
            mXWalkView.load(URL, null);
    }

    @Override
    protected void onXWalkReady() {
        mXWalkView = (XWalkView) findViewById(R.id.xwalkview);


        mXWalkView.setInitialScale(1);
        MyUIClient myUIClient = new MyUIClient(mXWalkView);    //connecting UIClient to view;
        mXWalkView.setUIClient(myUIClient);



        /*if (msavedInstanceState != null)
            mXWalkView.restoreState(msavedInstanceState);
        else*/
        initXWalkView();
    }

    class MyUIClient extends XWalkUIClient {             //client used for control start/stop/success/failed page loading

        MyUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, java.lang.String url) {
            Log.w(TAG, "onPageLoadStarted: " + url);
//            progressBar.setVisibility(View.VISIBLE);     //show progress Bar
            //timer = new Timer();                       //uncomment when nessesary to used page loading timeout
            //timer.schedule(new TimeoutLoad(), 40000);  //uncomment when nessesary to used page loading timeout
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, final LoadStatus status) {
            Log.w(TAG, "onPageLoadStopped: " + url + ", status: " + status);

            if (status == LoadStatus.FINISHED) {
                view.captureBitmapAsync(new XWalkGetBitmapCallback() {
                    @Override
                    public void onFinishGetBitmap(Bitmap bitmap, int i) {
                        Log.w(TAG, "onFinishGetBitmap: " + bitmap);
                        loadUrlCount = 0;                         //skip to zero reload count
                        //timer.cancel();                         //uncomment when nessesary to used page loading tim
                    }
                });
            }

            if (status == LoadStatus.FAILED) {
                if (loadUrlCount < 3) {                 //if page can't loading for 3 times - skip loading and closing activity
                    mXWalkView.reload(XWalkView.RELOAD_NORMAL);
                    loadUrlCount++;                     //increment reload count if failed
                } else {
                    Toast toast = Toast.makeText(WebView.this, "someTrouble", Toast.LENGTH_LONG);
                    toast.show();
                    WebView.this.finish();
                }
            }
        }
    }

   /* public void onClick (View v) {                       //handler for onClick

        switch (v.getId()) {

            case R.id.refreshButton:                     // handler for refresh button
                mXWalkView.reload(XWalkView.RELOAD_NORMAL);
                break;

            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;

        }

    }*/

    private void ifFailure() {                           // it can used for add page loading timeout too
        if (loadUrlCount == 0) {
            mXWalkView.stopLoading();
            mXWalkView.reload(XWalkView.RELOAD_NORMAL);
            loadUrlCount++;
        } else {
            Toast toast = Toast.makeText(WebView.this, "someTrouble", Toast.LENGTH_LONG);
            toast.show();
            WebView.this.finish();
        }
    }


}
