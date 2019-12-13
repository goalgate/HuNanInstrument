package cn.cbdi.hunaninstrument.Tool;

import android.content.Context;
import android.content.Intent;

import com.ys.myapi.MyManager;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Config.HuNanConfig;
import cn.cbdi.hunaninstrument.Config.WYYConfig;

public class WZWManager {

    private static WZWManager wzwManager;

    //由于亿晟的jar包rkapi和rkapi1的包名一模一样，编译apk先在build.gradle选择注释掉不用的那个apk包
    MyManager manager;

    public static final String STATICIP = "StaticIp";

    public static final String DHCP = "DHCP";

    public static final String ethernet = "ethernet";

    public void unBindAIDLService(Context context) {
//        manager.unBindAIDLService(context);
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.unBindAIDLService(context);
        //        } else {
        //            manager1.unBindAIDLService(context);
        //        }
    }

    public void setTime(int year, int month, int day, int hour, int minute, int second) {
        manager.setTime(year, month, day, hour, minute);
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.setTime(year, month, day, hour, minute, second);
        //        } else {
        //            manager1.setTime(year, month, day, hour, minute);
        //        }
    }


    public void reboot() {
        if (AppInit.getInstrumentConfig().getClass().getName().equals(WYYConfig.class.getName())) {
            manager.reboot();
        } else {
            Intent intent = new Intent("com.xs.reboot");
            AppInit.getContext().sendBroadcast(intent);
        }

        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.reboot();
        //        } else {
        //            manager1.reboot();
        //        }
    }

    public void setStaticEthIPAddress(String IPaddr, String gateWay, String mask, String dns1, String dns2) {
        if (getAndroidDisplay().startsWith("rk3288")) {
            manager.setStaticEthIPAddress(IPaddr, mask, gateWay, dns1, dns2);
        } else {
            manager.setStaticEthIPAddress(IPaddr, mask, gateWay, dns1, dns2);
        }
        FileUtils.writeFileSdcard(ethernet, STATICIP);
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.setStaticEthIPAddress(IPaddr, gateWay, mask, dns1, dns2);
        //        } else {
        //            manager1.setStaticEthIPAddress(IPaddr, gateWay, mask, dns1, dns2);
        //        }
    }

    public String getAndroidDisplay() {
        return manager.getAndroidDisplay();
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            return manager2.getAndroidDisplay();
        //        } else {
        //            return manager1.getAndroidDisplay();
        //        }
    }


    public void setDhcpIpAddress(Context context) {
        manager.setDhcpIpAddress(context);
        FileUtils.writeFileSdcard(ethernet, DHCP);

        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.setDhcpIpAddress(context);
        //        } else {
        //            manager1.setDhcpIpAddress(context);
        //        }
    }

    public String getEthMode() {
        try {
            return FileUtils.readFileSdcard(ethernet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
        //        return manager.getEthMode();
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            return manager2.getEthMode();
        //        } else {
        //            return manager1.getEthMode();
        //        }
    }


    public void bindAIDLService(Context context) {
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.bindAIDLService(context);
        //        } else {
        //            manager1.bindAIDLService(context);
        //        }
        manager.bindAIDLService(context);
    }

    public static WZWManager getInstance(Context context) {
        if (wzwManager == null) {
            wzwManager = new WZWManager(context);
        }
        return wzwManager;
    }

    private WZWManager(Context context) {
        manager = MyManager.getInstance(context);
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") +
        // 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2 = com.ys.rkapi.MyManager.getInstance(context);
        //        } else {
        //            manager1 = com.ys.myapi.MyManager.getInstance(context);
        //        }
    }

    public void ethEnabled(boolean status) {
        manager.ethEnabled(status);
        //        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
        //            manager2.ethEnabled(status);
        //        } else {
        //            manager1.ethEnabled(status);
        //        }
    }
}
