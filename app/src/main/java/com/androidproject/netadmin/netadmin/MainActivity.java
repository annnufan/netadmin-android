package com.androidproject.netadmin.netadmin;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidproject.netadmin.netadmin.Utils.ConfigUtils;
import com.androidproject.netadmin.netadmin.Utils.State;
import com.androidproject.netadmin.netadmin.model.Color;
import com.androidproject.netadmin.netadmin.model.Computer;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static com.androidproject.netadmin.netadmin.Utils.NetworkUtils.ping;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String INTENT_FILTER = "NETWORK_STATE_CHANGED";

    public static final String FILENAME = "config.cfg";

    public ArrayList<Computer> devices;

    private SwipeRefreshLayout swipe;

    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    private ComputerAdapter adapter = null;

    private boolean onScanProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        devices = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipe.setOnRefreshListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComputerAdapter(this);
        adapter.setComputers(devices);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> names = new ArrayList<String>(devices.size());
        ArrayList<String> ip = new ArrayList<String>(devices.size());
        ArrayList<Integer> num = new ArrayList<Integer>(devices.size());
        ArrayList<String> state = new ArrayList<String>(devices.size());
        ArrayList<String> color = new ArrayList<String>(devices.size());
        int ind = 0;
        for (Computer device : devices) {
            names.add(ind, device.getName());
            ip.add(ind, device.getIP());
            num.add(ind, device.getId());
            state.add(ind, device.getState().toString());
            color.add(ind, device.getColor().toString());
            ind++;
        }
        outState.putStringArrayList("name", names);
        outState.putStringArrayList("ip", ip);
        outState.putStringArrayList("state", state);
        outState.putStringArrayList("color", color);
        outState.putIntegerArrayList("num", num);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        ArrayList<String> names = savedInstanceState.getStringArrayList("name");
        ArrayList<String> ip = savedInstanceState.getStringArrayList("ip");
        ArrayList<Integer> num = savedInstanceState.getIntegerArrayList("num");
        ArrayList<String> states = savedInstanceState.getStringArrayList("state");
        ArrayList<String> colors = savedInstanceState.getStringArrayList("color");
        int ind = 0;
        ArrayList<Computer> add = new ArrayList<Computer>();
        for (Integer id : num) {
            String nameState = states.get(ind);
            State state = (nameState == "ONLINE") ? State.ONLINE : State.OFFLINE;
            String nameColor = colors.get(ind);
            Color color = null;
            switch (nameColor) {
                case "GOOD":
                    color = Color.GOOD;
                    break;
                case "BAD":
                    color = Color.BAD;
                    break;
                case "FAIL":
                    color = Color.FAIL;
                    break;
                case "WAIT":
                    color = Color.WAIT;
                    break;
            }

            add.add(new Computer(id, ip.get(ind), names.get(ind), state, color));
            ind++;
        }
        devices = add;
        adapter = new ComputerAdapter(this);
        adapter.setComputers(devices);
        recyclerView.setAdapter(adapter);

    }

    public void onScanClick(View view) {
        class Scanner extends AsyncTask<Void, Void, ArrayList<Computer>> {
            final String TAG = "Scanner ";

            @Override
            protected void onPostExecute(ArrayList<Computer> result) {
                setDevices(result);
                onScanProcess = false;
            }

            @Override
            protected ArrayList<Computer> doInBackground(Void... params) {
                onScanProcess = true;
                String basicIP = "192.168.1.";
//                String basicIP = "127.0.0.";
                ArrayList<Computer> scannedDevices = new ArrayList<>();
                int num = 1;
                for (int i = 1; i < 255; i++) {
                    String ip = basicIP + Integer.toString(i);
                    if (ping(ip)) {
                        String name;
                        try {
                            InetAddress addr = InetAddress.getByName(ip);
                            name = addr.getCanonicalHostName();
                            if (name.equals(ip)) {
                                name = "--";
                            }
                        } catch (UnknownHostException e) {
                            name = "invalid name";
                        }
                        scannedDevices.add(new Computer(num++, basicIP + Integer.toString(i), name));
                    }
                }
                Log.d(TAG, Integer.toString(scannedDevices.size()));
                return scannedDevices;
            }
        }
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (onScanProcess) {
            String text = "Scanning in process";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else {
            new Scanner().execute();
        }
    }

    private void setDevices(ArrayList<Computer> devices) {
        progressBar.setVisibility(View.INVISIBLE);
        adapter.setComputers(devices);
        recyclerView.setVisibility(View.VISIBLE);
    }

    class Updater extends AsyncTask<ArrayList<Computer>, Void, ArrayList<Computer>> {

        @Override
        protected void onPostExecute(ArrayList<Computer> devices) {
            setDevices(devices);
        }

        @Override
        protected ArrayList<Computer> doInBackground(ArrayList<Computer>... params) {
            ArrayList<Computer> devices = params[0];
            for (Computer device : devices) {
                if (ping(device.getIP())) {
                    device.setState(State.ONLINE);
                } else {
                    device.setState(State.OFFLINE);
                }
            }
            return devices;
        }
    }


    public void onGetClick(View view) {

        final String TAG = "On get click ";
        File configFile = new File(getFilesDir(), FILENAME);
        if (configFile.exists()) {
            devices = ConfigUtils.getConfig(configFile);
            Log.d(TAG, "devices size " + Integer.toString(devices.size()));
            new Updater().execute(devices);
        } else {
            Log.d(TAG, "Config file not found");
            String text = "Config file not found";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
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
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else {
            ConfigUtils.setConfig(devices, configFile);
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
                    Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();
                } else {
                    new Updater().execute(devices);
                }
            }
        }, 3000);
        swipe.setRefreshing(false);
    }
}