package com.mgyaware.instruction;


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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class Application extends android.app.Application {
    public static final String TAG = "Application";

    private static final String DATABASE_NAME = Config.database;
    private static final String SYNC_URL = Config.url;

    public HashMap<String, ArrayList<Slide>> SlideSets;
    public HashMap<String, ArrayList<ListViewName>> ListViewMenus;

    private Manager manager;
    private Database database;
    private URL syncUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        ListViewMenus = new HashMap<String, ArrayList<ListViewName>>();
        SlideSets = new HashMap<String, ArrayList<Slide>>();

        initDatabase();
    }

    public Database getDatabase() {
        return this.database;
    }

    public void initData() {
        Log.e(TAG, "initData Start.");
        Query query = database.createAllDocumentsQuery();

        try {
            QueryEnumerator result = query.run();

            //something is up
            if (result.getCount() == 0) {
                Log.e(TAG, "no Query results.");
                pull();
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

        //add handler to initData after complete

        Log.v(TAG, "End Pull...");
    }

    public void initDatabase() {
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
            Collections.sort(menu, new BaseOrderComparator());
            ListViewMenus.put(level, menu);
        } else {
            Log.e(TAG, "Missing menu items");
        }
    }

    private void setSlides(ArrayList modules, String level) {
        if (modules != null) {
            int len = modules.size();
            if (len == 0) {
                Log.e(TAG, "slides length==0.");
                return;
            }

            ArrayList<Slide> slides = new ArrayList<Slide>();
            for (int i = 0; i < len; i++) {
                LinkedHashMap module = (LinkedHashMap) modules.get(i);
                slides.add(new Slide(module.get("title").toString(), Integer.parseInt(module.get("order").toString()),
                        module.get("content").toString(), module.get("picture").toString(), module.get("video").toString()));
            }
            Collections.sort(slides, new BaseOrderComparator());
            SlideSets.put(level, slides);
        } else {
            Log.e(TAG, "Missing slide items");
        }
    }

    private void setModule(Document doc) {
        String level = doc.getProperty("Module").toString();
        ArrayList modules;
        for (Map.Entry<String, Object> p : doc.getProperties().entrySet()) {
            if (p.getValue().getClass() == ArrayList.class) {
                modules = (ArrayList) p.getValue();
                if (p.getKey().equals("Slidesets")) {
                    setMenu(modules, level);
                } else {
                    setSlides(modules, level + "-" + p.getKey());
                }
            }
        }
    }


//    public void getSlides(String name) {
//
//    }
}
