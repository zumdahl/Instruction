package com.mgyaware.instruction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.couchbase.lite.util.Log;

public class SplashScreen extends Activity {
    public static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Application application = (Application) getApplication();
        if(application.topMenu.size()==0)
        {
            Log.e(TAG, "topMenu size == 0. ");
            application.pull();
            application.initData();
            if(application.topMenu.size()==0)
            {
                Log.e(TAG, "last attempt, topMenu size == 0. ");
                //error handling...

            }
        }

        //goto mainActivity
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        //i.putExtra("",);
        startActivity(i);
        finish();
    }

}
