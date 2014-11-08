package com.mgyaware.instruction;

import android.app.Activity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class Application extends android.app.Application {
    public static final String TAG = "CouchDB";
    private static final String DATABASE_NAME = "trainingtest";
    private static final String SYNC_URL = "";

    private Manager manager;
    private Database database;
    private URL syncUrl;

    public Database getDatabase() {
        return this.database;
    }


    public HashMap<String, ArrayList<ListViewName>> ListViewMenus;

    private static class ListViewNameComparator implements Comparator<ListViewName> {
        @Override
        public int compare(ListViewName lhs, ListViewName rhs) {
            return lhs.order - rhs.order;
        }
    }

    public class ListViewName {
        public String title;
        public int order;
        public Class<? extends Activity> activityClass;

        public ListViewName(String title, int order, Class<? extends Activity> activityClass) {
            this.title = title;
            this.order = order;
            this.activityClass = activityClass;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public void initData() {
        Log.e(TAG, "initData Start.");
        Query query = database.createAllDocumentsQuery();

        try {
            QueryEnumerator result = query.run();

            //something is up
            if (result.getCount() == 0) {
                Log.e(TAG, "no Query results.");
                return;
            }

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                Document value = row.getDocument();

                if (value.getProperty("title") != null) {
                    setTopMenu(value);
                } else if (value.getProperty("Module") != null) {
                    setModule(value);
                }
                //else nothing : error handle
            }

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot run query", e);
        }
        Log.e(TAG, "initData End.");
    }

    public void pull() {
        Log.v(TAG, "Start Pull...");
        Replication pullRep = database.createPullReplication(syncUrl);
        //pullRep.setFilter("training/pull");     //$host/trainingtest/_changes?filter=training/pull
        pullRep.setContinuous(false);
        pullRep.start();
        Log.v(TAG, "End Pull...");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ListViewMenus = new HashMap<String, ArrayList<ListViewName>>();

        initDatabase();
    }

    private void initDatabase() {
        Log.e(TAG, "initDatabase Start.");
        try {
            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.DEBUG);
            Manager.enableLogging(Log.TAG_QUERY, Log.DEBUG);
            Manager.enableLogging(Log.TAG_VIEW, Log.DEBUG);
            Manager.enableLogging(Log.TAG_DATABASE, Log.DEBUG);

            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create Manager object", e);
            return;
        }

        try {
            database = manager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get Database", e);
            return;
        }

        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Log.e(TAG, "initDatabase end.");

        initData();
    }

    private void setTopMenu(Document doc) {
        ArrayList modules = (ArrayList) doc.getProperty("Modules");
        setMenu(modules, "topMenu");
    }

    private void setMenu(ArrayList modules, String level) {
        if (modules != null) {
            int len = modules.size();
            if (len == 0) {
                Log.e(TAG, "menu length==0.");
                return;
            }
            ArrayList<ListViewName> menu = new ArrayList<ListViewName>();
            for (int i = 0; i < len; i++) {
                LinkedHashMap module = (LinkedHashMap) modules.get(i);
                menu.add(new ListViewName(module.get("title").toString(), Integer.parseInt(module.get("order").toString()),
                        (level.equals("topMenu")) ? MainActivity.class : ScreenSlideActivity.class
                ));
            }
            Collections.sort(menu, new ListViewNameComparator());
            ListViewMenus.put(level, menu);
        } else {
            Log.e(TAG, "Missing menu items");
        }
    }

    private void setModule(Document doc) {
        String level = doc.getProperty("Module").toString();
        ArrayList modules = (ArrayList) doc.getProperty("Slidesets");
        setMenu(modules, level);
        //set slides....

    }
}
