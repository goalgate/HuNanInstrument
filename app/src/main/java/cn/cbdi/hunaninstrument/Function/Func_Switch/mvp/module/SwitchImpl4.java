package cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android_serialport_api.SerialPort;
import cn.cbdi.drv.card.ReadCard;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.log.Lg;

/**
 * 当前类注释:
 *
 * @author wzw
 * @date 2019/8/13 09:28
 */
public class SwitchImpl4 implements ISwitching {
    private byte[] buf_ = new byte[2048];
    private int bufCount = 0;
    private int checkCount_ = 0;
    private String testStr = "";
    private byte[] switchingValue = new byte[8]; //开关量状态
    private Calendar switchingTime = Calendar.getInstance(); //取开关时状态时间
    private Calendar temHumTime = Calendar.getInstance(); //取温湿度时间
    private int temperature = 0;  //温度
    private int humidity = 0;   //湿度
    ISwitchingListener listener;
    private byte[] dt_temHum_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x55, 0x63, 0x7E, 0x6B};
    //温湿度命令
    private byte[] dt_outD8off_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x40, 0x30, 0x58,
            (byte) 0xDD};  //D9断电器关命令
    private byte[] dt_outD8on_ =
            {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x41, 0x31, 0x08, 0x1D};  //D9断电器开命令
    private byte[] dt_outD9off_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x20, 0x10, (byte) 0x80,
            (byte) 0xF4};  //D9断电器关命令
    private byte[] dt_outD9on_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x21, 0x11, (byte) 0xD0,
            0x34};  //D9断电器开命令
    private byte[] dt_buzz_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x0B, 0x0B, 0x02, 0x33, (byte) 0x7B,
            0x23};
    private byte[] dt_buzzOff = {0x02, 0x02, 0x07, 0x00, 0x10, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xC8, 0x00,
            0x00, (byte) 0x99, (byte) 0x2E};  //D9断电器开命令
    private byte[] dt_doorOpen = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 0x04, 0x70, 0x03, 0x00, (byte) 0xC4,
            (byte) 0x3E};
    private byte[] dt_greenLightBlink = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00,
            (byte) 0x0B, 0x01, 0x01, (byte) 0xD1, (byte) 0x92, (byte) 0x93, 0x63};
    private byte[] dt_redLightBlink = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00,
            (byte) 0x0B, 0x02, 0x02, (byte) 0x91, (byte) 0x63, (byte) 0x93, 0x63};
    private byte[] dt_whiteLightOn = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00, 0x0A,
            0x01, 0x01, (byte) 0x80, 0x52, (byte) 0x93, 0x63};
    private byte[] dt_whiteLightOff = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00, 0x0A
            , 0x01, 0x00, (byte) 0x41, (byte) 0x92, (byte) 0x93, 0x63};

    private byte[] dt_buzz2 = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x22, 0x45, 0x35, (byte) 0xDF};


    //    新命令20190812

    //    12V继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_12Vrelay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x01,
            (byte) 0x00, (byte) 0x58, (byte) 0xF1};
    private byte[] dt_12Vrelay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x00,
            (byte) 0x11, (byte) 0x84, (byte) 0x66};
    private byte[] dt_12Vrelay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x00,
            (byte) 0x22, (byte) 0xC4, (byte) 0x73};


    //    继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x01,
            (byte) 0x00, (byte) 0x4C, (byte) 0xF0};
    private byte[] dt_relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x00,
            (byte) 0x11, (byte) 0x85, (byte) 0xF9};
    private byte[] dt_relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x00,
            (byte) 0x22, (byte) 0xC5, (byte) 0xEC};


    //    D10继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_D10relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x01,
            (byte) 0x00, (byte) 0xC3, (byte) 0x3E};
    private byte[] dt_D10relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x00,
            (byte) 0x11, (byte) 0xD1, (byte) 0xFF};
    private byte[] dt_D10relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x00,
            (byte) 0x22, (byte) 0x91, (byte) 0xEA};


    //    D5继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_D5relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x01,
            (byte) 0x00, (byte) 0x22, (byte) 0x7D};
    private byte[] dt_D5relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x00,
            (byte) 0x11, (byte) 0x61, (byte) 0x45};
    private byte[] dt_D5relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x00,
            (byte) 0x22, (byte) 0x21, (byte) 0x50};


    private int light_devfd = -1;

    private int switch_devfd = -1;

    private SerialPort light_port;

    private SerialPort switch_port;

    private InputStream light_InputStream;

    private OutputStream light_OutputStream;

    private InputStream switch_InputStream;

    private OutputStream switch_OutputStream;

    private ReadThread mReadThread;

    @Override
    public void onOpen(ISwitchingListener listener) {
        this.listener = listener;
        if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")) {
            light_devOpen(115200, "/dev/ttyS3");
            switch_devOpen(115200, "/dev/ttyS2");
        } else if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3288")) {
            light_devOpen(115200, "/dev/ttyS2");
            switch_devOpen(115200, "/dev/ttyS0");
        }


    }

    private int light_devOpen(int sp, String devName_) {
        try {
            light_port = new SerialPort(new File(devName_), sp, 0);
            light_InputStream = light_port.getInputStream();
            light_OutputStream = light_port.getOutputStream();
            Lg.e("switch_dev", "open  SerialPort ok");
            light_devfd = 1;
        } catch (Exception e) {
            Lg.e("switch_dev", e.toString());
        }
        //        Lg.e("SDs", CRC16.getCRC3(dt_Success2,((dt_Success2[1]<<8)+dt_Success2[0])+2));
        return light_devfd;
    }

    private int switch_devOpen(int sp, String devName_) {
        try {
            switch_port = new SerialPort(new File(devName_), sp, 0);
            switch_InputStream = switch_port.getInputStream();
            switch_OutputStream = switch_port.getOutputStream();
            Lg.e("switch_dev", "open  SerialPort ok");
            switch_devfd = 1;
        } catch (Exception e) {
            Lg.e("switch_dev", e.toString());
        }
        //        Lg.e("SDs", CRC16.getCRC3(dt_Success2,((dt_Success2[1]<<8)+dt_Success2[0])+2));
        if (mReadThread == null) {
            mReadThread = new ReadThread();
            thread_continuous = true;
            mReadThread.start();
        }
        return switch_devfd;
    }


    boolean thread_continuous = false;
    private byte[] readerbuffer = new byte[20];
    byte[] by_copy;
    String testStrTemp;

    class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (thread_continuous) {
                try {
                    //                    Thread.sleep(700);

                    int size = switch_InputStream.read(readerbuffer);


                    by_copy = new byte[size];

                    System.arraycopy(readerbuffer, 0, by_copy, 0, size);

                    testStrTemp = "";

                    for (int i = 0; i < size; i++) {
                        testStrTemp += byteToHex(by_copy[i]);
                    }

                    if ((bufCount += size) >= 9) {
                        bufCount = 0;
                        testStr += testStrTemp;
                        if (testStr.contains("AAAAAA")) {
                            testStr = testStr.substring(testStr.indexOf("AAAAAA"),
                                    testStr.indexOf("AAAAAA") + 18);
                            mhandler.sendEmptyMessage(0x123);
                        }
                        if (testStr.contains("BBBBBB")) {
                            testStr = testStr.substring(testStr.indexOf("BBBBBB"),
                                    testStrTemp.indexOf("BBBBBB") + 18);
                            temperature = (int) hexStr2Bytes(testStr.substring(10, 12))[0];
                            humidity = (int) hexStr2Bytes(testStr.substring(6, 8))[0];
                            mhandler.sendEmptyMessage(0x234);
                        }
                        Thread.sleep(300);
                        testStr = "";
                    } else {
                        testStr += testStrTemp;
                    }
                } catch (Exception e) {
                    Lg.e("switch_dev", e.toString());
                }
            }

        }
    }


    @Override
    public void onReadHum() {
        sendData(dt_temHum_);
    }

    @Override
    public void onOutD8(boolean status) {
        if (status) {
            sendData(dt_outD8on_);
        } else {
            sendData(dt_outD8off_);
        }
    }

    @Override
    public void onOutD9(boolean status) {
        if (status) {
            sendData(dt_outD9on_);
        } else {
            sendData(dt_outD9off_);
        }
    }

    @Override
    public void onGreenLightBlink() {
        onWhiteLighrOff();
        try {
            light_OutputStream.write(dt_greenLightBlink);
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onRedLightBlink() {
        onWhiteLighrOff();
        try {
            light_OutputStream.write(dt_redLightBlink);
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onBuzz(Hex hex) {
//        sendData(dt_buzz2);
        Log.e("sdsd",AppInit.getMyManager().getAndroidDisplay());
        if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")
                ||AppInit.getMyManager().getAndroidDisplay().startsWith("rk3288")) {
            sendData(dt_buzz2);
        } else {
            sendData(adjust(dt_buzz_, hex));
        }
    }

    @Override
    public void on12VRelay(Hex hex, boolean status) {
        if (!status) {
            sendData(dt_12Vrelay_close);
        } else {
            if (hex == Hex.H0) {
                sendData(dt_12Vrelay_open);
            } else {
                sendData(adjust(dt_12Vrelay, hex));
            }
        }
    }

    @Override
    public void onRelay(Hex hex, boolean status) {
        if (!status) {
            sendData(dt_relay_close);
        } else {
            if (hex == Hex.H0) {
                sendData(dt_relay_open);
            } else {
                sendData(adjust(dt_relay, hex));
            }
        }
    }

    @Override
    public void onD10Relay(Hex hex, boolean status) {
        if (!status) {
            sendData(dt_D10relay_close);
        } else {
            if (hex == Hex.H0) {
                sendData(dt_D10relay_open);
            } else {
                sendData(adjust(dt_D10relay, hex));
            }
        }
    }

    @Override
    public void onD5Relay(Hex hex, boolean status) {
        if (!status) {
            sendData(dt_D5relay_close);
        } else {
            if (hex == Hex.H0) {
                sendData(dt_D5relay_open);
            } else {
                sendData(adjust(dt_D5relay, hex));
            }
        }
    }

    @Override
    public void onBuzzOff() {
        sendData(dt_buzzOff);
    }

    @Override
    public void onDoorOpen() {
        sendData(dt_doorOpen);
    }

    @Override
    public void onWhiteLighrOn() {
        try {
            light_OutputStream.write(dt_whiteLightOn);
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onWhiteLighrOff() {
        try {
            light_OutputStream.write(dt_whiteLightOff);
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onClose() {
        thread_continuous = false;
    }

    private void sendData(byte[] bs) {
        try {
            switch_OutputStream.write(bs);
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }


    private byte[] adjust(byte[] order, Hex hex) {
        switch (hex) {
            case H1:
                order[5] = 0x01;
                break;
            case H2:
                order[5] = 0x02;
                break;
            case H3:
                order[5] = 0x03;
                break;
            case H4:
                order[5] = 0x04;
                break;
            case H5:
                order[5] = 0x05;
                break;
            case H6:
                order[5] = 0x06;
                break;
            case H7:
                order[5] = 0x07;
                break;
            case H8:
                order[5] = 0x08;
                break;
            case H9:
                order[5] = 0x09;
                break;
            case HA:
                order[5] = 0x0A;
                break;
            default:
                break;
        }
        return order;
    }

    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF & b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                listener.onSwitchingText(testStr);
            } else if (msg.what == 0x234) {
                listener.onTemHum(temperature, humidity);
            }
        }
    };
}
