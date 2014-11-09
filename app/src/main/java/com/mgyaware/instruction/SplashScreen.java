package com.mgyaware.instruction;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.couchbase.lite.util.Log;

public class SplashScreen extends Activity {
    public static final String TAG = "SplashScreen";
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Log.e(TAG,"initdata should be called again at some point");
            Application application = (Application) getApplication();
            if (application.ListViewMenus.get("topMenu") != null) {
                handler.removeCallbacks(runnable);
                launchMain();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();
        handler.postDelayed(runnable, 1000);

    }

    private void launchMain() {
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        i.putExtra("menu", "topMenu");
        startActivity(i);
        finish();
    }

}
