package com.androidproject.netadmin.netadmin;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidproject.netadmin.netadmin.Utils.ConfigUtils;
import com.androidproject.netadmin.netadmin.Utils.State;
import com.androidproject.netadmin.netadmin.model.Color;
import com.androidproject.netadmin.netadmin.model.Computer;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

import static com.androidproject.netadmin.netadmin.Utils.NetworkUtils.ping;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String INTENT_FILTER = "NETWORK_STATE_CHANGED";

    public static final String FILENAME = "config.cfg";

    public ArrayList<Computer> devices;

    private SwipeRefreshLayout swipe;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        devices = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipe.setOnRefreshListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ComputerAdapter adapter = new ComputerAdapter(this);
        adapter.setComputers(devices);
        recyclerView.setAdapter(adapter);
    }

    public void onScanClick(View view) {
        final String TAG = "On scan click ";
        class Scan implements Runnable {

            private Scan(){};

            @Override
            public void run() {
                String basicIP = "192.168.1.";
                ArrayList<Computer> scannedDevices = new ArrayList<>();
                int num = 1;
                for (int i = 1; i < 255; i++) {
                    if (ping(basicIP + Integer.toString(i))) {
                        scannedDevices.add(new Computer(num, basicIP + Integer.toString(i), "basic name", Color.GOOD));
                    }
                }
                Log.d(TAG, Integer.toString(scannedDevices.size()));
                if (!scannedDevices.isEmpty()) {
                    devices = scannedDevices;
                }
            }
        }

        Thread thread = new Thread(new Scan());
        thread.start();

        while (thread.isAlive());
        if (devices.isEmpty()) {
            Log.d(TAG, "Computers in network not found");
            String text = "Not find computers in network";
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            ComputerAdapter adapter = new ComputerAdapter(recyclerView.getContext());
            adapter.setComputers(devices);
            recyclerView.setAdapter(adapter);
        }

    }


    public void onGetClick(View view) {
        final String TAG = "On get click ";
        File configFile = new File(getFilesDir(), FILENAME);
        if (configFile.exists()) {
            devices = ConfigUtils.getConfig(configFile);
            ComputerAdapter adapter = new ComputerAdapter(recyclerView.getContext());
            adapter.setComputers(devices);
            recyclerView.setAdapter(adapter);
        } else {
            Log.d(TAG, "Config file not found");
            String text = "Config file not found";
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void onSaveClick(View view) {
        final String TAG = "On save click ";
        File configFile = new File(getFilesDir(), FILENAME);
        if (devices.isEmpty()) {
            Log.d(TAG, "Nothing to save");
            String text = "Nothing to save";
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onRefresh() {
        final String TAG = "On swipe ";
        swipe.setRefreshing(true);

        swipe.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (devices.isEmpty()) {
                    Log.d(TAG, "Computers in network not found");
                    String text = "Computers in network not found";
                    int duration = Toast.LENGTH_SHORT;
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {

                    class Scan implements Runnable {

                        private Scan(){};

                        @Override
                        public void run() {
                            for (Computer device : devices) {
                                if (ping(device.getIP())) {
                                    device.setState(State.ONLINE);
                                } else {
                                    device.setState(State.OFFLINE);
                                }
                            }
                            ComputerAdapter adapter = new ComputerAdapter(recyclerView.getContext());
                            adapter.setComputers(devices);
                            recyclerView.setAdapter(adapter);
                            swipe.setRefreshing(false);
                        }
                    }

                    new Thread(new Scan()).start();
                }
            }
        }, 3000);

    }
}