package com.androidproject.netadmin.netadmin;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;

/**
 * Created by appolinariya on 20.12.16.
 */


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mainActivity;
    private View scan, save, get, refresh, items, number, name, ip, state;
    private EditText my;
    public void testCreatedMenu() throws Exception {
        assertNotNull(mainActivity);
        assertNotNull(scan);
        assertNotNull(save);
        assertNotNull(get);
        assertNotNull(refresh);
//        assertNotNull(items);
//        assertNotNull(number);
//        assertNotNull(name);
//        assertNotNull(ip);
//        assertNotNull(state);
    }
/*
    public void testScan() throws Exception {

    }

    public void testSave() throws Exception {

    }

    public void testGet() throws Exception {

    }
*/
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mainActivity = getActivity();
        scan = mainActivity.findViewById(R.id.scanButton);
        save = mainActivity.findViewById(R.id.saveButton);
        get = mainActivity.findViewById(R.id.getButton);
        refresh = mainActivity.findViewById(R.id.refresh);
        items = mainActivity.findViewById(R.id.Items);
        number = mainActivity.findViewById(R.id.Number);
        name = mainActivity.findViewById(R.id.Name);
        ip = mainActivity.findViewById(R.id.IP);
        state = mainActivity.findViewById(R.id.State);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
