package com.ahn.remoteantivirus;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Util Class
 */
public class Util {

    public static String getLocalIpAddress(int type) {

        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        switch (type) {
                            case Constants.TYPE_INET6ADDRESS:
                                if (inetAddress instanceof Inet6Address) {
                                    return inetAddress.getHostAddress();
                                }
                                break;
                            case Constants.TYPE_INET4ADDRESS:
                                if (inetAddress instanceof Inet4Address) {
                                    return inetAddress.getHostAddress();
                                }
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
