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
import java.util.Comparator;
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

    public ArrayList<ModuleName> topMenu;
    public static class ModuleNameComparator implements Comparator<ModuleName> {
        @Override
        public int compare(ModuleName lhs, ModuleName rhs) {
            return lhs.order-rhs.order;
        }
    }

    public class ModuleName{
        private String title;
        public int order;

        public ModuleName(String title, int order){
            this.title = title;
            this.order = order;
        }

        @Override
        public String toString() {
            return title.toString();
        }
    }

    public void initData(){
        Log.e(TAG,"setTopMenu Start.");
        Query query = database.createAllDocumentsQuery();

        try {
            QueryEnumerator result = query.run();

            if(result.getCount()==0){
                Log.e(TAG,"no Query results.");   //something is up
                return;
            }

            for(Iterator<QueryRow> it = result; it.hasNext();){
                QueryRow row = it.next();
                Document value = row.getDocument();

                if(value.getProperty("title") != null) {
                    setTopMenu(value);
                }
                else if(value.getProperty("Module") != null){
                    setModule(value);
                }
                //else nothing : error handle
            }

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot run query", e);
        }
    }

    public void pull(){
        Log.v(TAG, "Start Pull...");

        Replication pullRep = database.createPullReplication(syncUrl);
        pullRep.setFilter("training/pull");     //$host/trainingtest/_changes?filter=training/pull
        pullRep.setContinuous(false);
        pullRep.start();

        Log.v(TAG, "End Pull...");

    }

    @Override
    public void onCreate() {
        super.onCreate();

        topMenu = new ArrayList<ModuleName>();
        initDatabase();
    }

    private void initDatabase() {
        Log.e(TAG,"initDatabase Start.");
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
        }catch (MalformedURLException e){
            throw new RuntimeException(e);
        }
        Log.e(TAG,"initDatabase end.");

        initData();
    }

    private void setTopMenu(Document doc){
        ArrayList modules = (ArrayList)doc.getProperty("Modules");
        if(modules != null){
            int len = modules.size();
            if(len==0){Log.e(TAG,"zero length menu.");}
            for(int i=0;i<len;i++) {
                LinkedHashMap module= (LinkedHashMap) modules.get(i);
                topMenu.add(new ModuleName(module.get("title").toString(),Integer.parseInt(module.get("order").toString())));
            }
            Collections.sort(topMenu, new ModuleNameComparator());
        }
        else {
            Log.e(TAG, "Missing toplevel");
        }
    }

    private void setModule(Document doc){
        
    }
}
