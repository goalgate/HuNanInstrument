package cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module;

import android.os.Handler;
import android.os.Message;

import java.util.Calendar;

import cn.cbdi.drv.card.SerialPortCom;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.log.Lg;

/**
 * Created by zbsz on 2017/8/23.
 */

public class SwitchImpl extends SerialPortCom implements ISwitching {


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
            0x23};  //D9断电器开命令
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

    //接收数据最后时间
    private long lastRevTime_;
    SerialPortCom ttyS3 = new SerialPortCom() {
        @Override
        public void onRead(int fd, int len, byte[] buf) {

        }
    };

    @Override
    public void onOpen(ISwitchingListener listener) {
        this.listener = listener;
        if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")) {
            ttyS3.setDevName("/dev/ttyS3");
            ttyS3.open(115200);
            setDevName("/dev/ttyS2");
            open(115200);
        } else if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3288")) {
            ttyS3.setDevName("/dev/ttyS2");
            ttyS3.open(115200);
            setDevName("/dev/ttyS0");
            open(115200);
        } else if (Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) >= 20180903
                && Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) < 20180918) {
            setDevName("/dev/ttyS2");
            open(115200);
        } else {
            setDevName("/dev/ttyS0");
            open(115200);
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
            ttyS3.write(dt_greenLightBlink);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onRedLightBlink() {
        onWhiteLighrOff();
        try {
            ttyS3.write(dt_redLightBlink);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
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

    @Override
    public void onBuzz(Hex hex) {
        if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")) {
            sendData(dt_buzz2);
        } else {
            sendData(adjust(dt_buzz_, hex));
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
            ttyS3.write(dt_whiteLightOn);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onWhiteLighrOff() {
        try {
            ttyS3.write(dt_whiteLightOff);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onRead(int fd, int len, byte[] buf) {
        if (buf == null) {
            return;
        }
        if (buf.length < len) {
            return;
        }

        int btr = len;
        byte[] by = new byte[btr];
        if (btr > 0) {
            System.arraycopy(buf, 0, by, 0, btr);        //依据串口数据长度BytesToRead来接收串口的数据并存放在by数组之中
            testStr = "";
            for (int i = 0; i < by.length; i++) {
                testStr += byteToHex(by[i]);
            }

            if (btr >= 9) {

                if (by[0] == (byte) 0xAA && by[1] == (byte) 0xAA && by[2] == (byte) 0xAA) {
                    for (int i = 0; i < 6; i++) {
                        switchingValue[i] = by[8 - i];
                    }
                    switchingValue[7] = 1;
                    switchingTime = Calendar.getInstance();
                    mhandler.sendEmptyMessage(0x123);
                } else if (by[0] == (byte) 0xBB && by[1] == (byte) 0xBB && by[2] == (byte) 0xBB) {
                    if (by[4] == 0x00 && by[7] == (byte) 0xC1 && by[8] == (byte) 0xEF) {
                        temperature = (int) by[5];
                        humidity = (int) by[3];
                        temHumTime = Calendar.getInstance();
                        mhandler.sendEmptyMessage(0x123);
                        mhandler.sendEmptyMessage(0x234);
                    } else if (by[3] == (byte) 0x96 && by[6] == 0x1F && by[7] == 0x44 && by[8] == (byte) 0xAD) {
                        //mhandler.sendMessage(getMsg(0x123));
                    }
                }
            }
        }
        lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        checkCount_ = 0;
    }

    private void sendData(byte[] bs) {
        try {
            write(bs);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        } catch (Exception ex) {
            Lg.e("M121_sendData", ex.toString());
        }
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
