package com.mgyaware.instruction;

import android.app.Activity;

public class ListViewName extends BaseOrder {
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
