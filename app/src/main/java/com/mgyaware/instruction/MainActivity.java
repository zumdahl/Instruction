/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mgyaware.instruction;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.couchbase.lite.util.Log;

import java.util.ArrayList;


public class MainActivity extends ListActivity {
    public static final String TAG = "MainActivity";
    private Intent i;
    private Application application;
    private static ArrayList<Application.ListViewName> mCourses;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onResume() {
        super.onResume();
        setListView();
    }

    private void setListView() {
        application = (Application) getApplication();
        i = getIntent();

        String menuItem = i.getStringExtra("menu");
        if (!menuItem.equals("topMenu")) {
            setTitle(menuItem);
        }

        mCourses = application.ListViewMenus.get(menuItem);

        setListAdapter(new ArrayAdapter<Application.ListViewName>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mCourses));
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // Launch the sample associated with this list position.
        Intent i = new Intent(MainActivity.this, mCourses.get(position).activityClass);
        i.putExtra("menu", mCourses.get(position).toString());
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (!i.getStringExtra("menu").equals("topMenu")) {
            i.putExtra("menu", "topMenu");
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (i.getStringExtra("menu").equals("topMenu")) {
            getMenuInflater().inflate(R.menu.admin_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pull:
                application.pull();
                application.initData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
