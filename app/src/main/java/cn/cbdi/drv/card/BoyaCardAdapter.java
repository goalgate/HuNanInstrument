package cn.cbdi.drv.card;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.boya.funtechserial.FunTechSerial;


public class BoyaCardAdapter implements ICardInfo {

    private FunTechSerial SDK = new FunTechSerial();

    int speed;

    String devName;

    ICardState iCardState_;  //事件接口

    private int readType_ = 0;  //读卡类型

    private int readState_ = 0;  //返回状态

    private final int cardInfoget = 4;

    private final int uidget = 14;

    private final int samget = 20;

    boolean useIC = false;

    boolean useID = false;

    public String IC_uid;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 0) {
                return;
            }
            iCardState_.onCardState(readType_, readState_);
        }
    };

    public BoyaCardAdapter(int speed, String devName, ICardState iCardState_) {
        this.speed = speed;
        this.devName = devName;
        this.iCardState_ = iCardState_;
    }

    CardThread mCardThread;

    @Override
    public int open() {
        int i = SDK.StartSerial(devName, speed);
        if (mCardThread == null) {
            mCardThread = new CardThread();
            thread_continuous = true;
            mCardThread.start();
        }
        return i;
    }

    @Override
    public void close() {
        thread_continuous = false;
        SDK.StopSerial();
    }

    @Override
    public void clearIsReadOk() {

    }

    @Override
    public Bitmap getBmp() {
        return SDK.CardPhoto();
    }

    @Override
    public void readCard() {
        id_continuous = false;
        useID = true;
    }

    @Override
    public void stopReadCard() {
        useID = false;
    }

    @Override
    public void ReadIC() {
        useIC = true;
    }

    @Override
    public void stopReadIC() {
        useIC = false;
    }

    @Override
    public void setDevType(String sType) {

    }


    @Override
    public void readSam() {
        readType_ = samget;
        readState_ = 1;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public String getSam() {
        return SDK.GetSAM();
    }

    @Override
    public String cardId() {
        return SDK.CardID();
    }

    @Override
    public String name() {
        return SDK.CardName().replaceAll(" ","");
    }

    @Override
    public String getUid() {
        return IC_uid;
    }

    boolean thread_continuous = false;
    boolean id_continuous = false;
    boolean ic_continuous = false;


    private class CardThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (thread_continuous) {
                try {
                    int ret = SDK.CardReader();//首先读卡
                    if (ret == 1) {
                        if (useID&&!id_continuous) {
                            id_continuous = true;
                            readType_ = cardInfoget;
                            readState_ = 1;
                            mHandler.sendEmptyMessage(0);
                        }
                    } else if (ret == 2) { //IC卡
                        if (useIC&&!ic_continuous) {
                            ic_continuous = true;
                            IC_uid = SDK.IcCard().toUpperCase();
                            readType_ = uidget;
                            readState_ = 1;
                            mHandler.sendEmptyMessage(0);
                        }
                    }else if (ret == 0){
                        id_continuous = false;
                        ic_continuous = false;
                    }
                    Thread.sleep(2);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
