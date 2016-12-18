package com.androidproject.netadmin.netadmin.Utils;

import android.util.Log;

import com.androidproject.netadmin.netadmin.model.Computer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by roman on 14.12.16.
 *
 * Changed by Anna Kopeliovich on 18.12.16
 */

public final class NetworkUtils {
    public static boolean ping(String ip) {
        try {

            if (InetAddress.getByName(ip).isReachable(100)) {
                System.out.println(ip + " Successful ping");
                return true;
            } else {
                System.out.println(ip + " Failed ping");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private NetworkUtils() {
    }

    private static final String TAG = "Check network state: ";
}
