package cn.cbdi.drv.card;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cvr.device.CVRApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import cn.cbdi.log.Lg;

/**
 * 当前类注释:
 *
 * @author wzw
 * @date 2019/8/6 16:56
 */
public class ReadCard2 implements ICardInfo {

    //检测是否有设备
    private byte[] dt_check = {0x02, 0x00, 0x11, 0x03, (byte) 0xAA, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, (byte) 0xB9, 0x03};
    private byte[] dt_check_ = {0x02, 0x00, 0x11, 0x03, 0x00, 0x01, 0x03, 0x01, 0x07, 0x01, 0x03, 0x01, 0x0F, 0x01,
            0x03, 0x18, 0x03};

    //读取SAM编号
    private byte[] dt_sam = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF,
            (byte) 0xEE};
    private byte[] dt_sam_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00};

    //检测是否有身份证
    private byte[] dt_isCer = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};
    private byte[] dt_isCer_no = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x04, 0x00, 0x00,
            (byte) 0x80, (byte) 0x84}; //11
    private byte[] dt_isCer_yes = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x08, 0x00, 0x00,
            (byte) 0x9F, 0x00, 0x00, 0x00, 0x00, (byte) 0x97};  //15
    //选卡
    private byte[] dt_selectCer = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02,
            0x21};
    private byte[] dt_selectCer_no = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x04, 0x00,
            0x00, (byte) 0x81, (byte) 0x85}; //11
    private byte[] dt_selectCer_yes = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x0C, 0x00,
            0x00, (byte) 0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x9C}; //19

    //读身份证
    private byte[] dt_readCer = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01,
            0x32};
    private byte[] dt_readCer_no = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x04, 0x00, 0x00,
            0x41, 0x45}; //11
    private byte[] dt_readCer_yes = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x05, 0x08, 0x00, 0x00
            , (byte) 0x90}; //10


    //开天线
    private byte[] dt_antenna_open = new byte[]{0x02, 0x02, 0x03, 0x00, 0x0c, 0x01, 0x01, 0x60, 0x53, 0x55};
    //关天线
    private byte[] dt_antenna_close = new byte[]{0x02, 0x02, 0x03, 0x00, 0x0c, 0x01, 0x00, (byte) 0xa1, (byte) 0x93,
            0x55};
    //设置TypeA协议
    private byte[] dt_ica_set = new byte[]{0x02, 0x02, 0x03, 0x00, 0x08, 0x01, 0x41, 0x20, 0x62, 0x55};
    private byte[] dt_ica_setB = new byte[]{0x02, 0x02, 0x03, 0x00, 0x08, 0x01, 0x42, 0x60, 0x63, 0x55};


    //返回成功命令
    private byte[] dt_Success = new byte[]{0x02, 0x02, 0x01, 0x00, 0x00, 0x20, 0x00};

    private byte[] dt_ica_read = new byte[]{0x02, 0x02, 0x03, 0x00, 0x12, 0x02, 0x52, 0x40, (byte) 0x98};
    private byte[] dt_ica_ok = new byte[]{0x02, 0x02, 0x01, 0x00, 0x00, 0x20, 0x00};
    private byte[] dt_ica_false = new byte[]{0x02, 0x02, 0x01, 0x00, 0x15, (byte) 0xE1, (byte) 0xCF};
    private byte[] dt_ica_readOK = new byte[]{0x02, 0x02, 0x08, 0x00, 0x00, 0x07, 0x04};


    //readType_的返回类型
    private final int cardInfoget = 4;

    private final int uidget = 14;

    private final int samget = 20;

    private SerialPort rkSerial_;

    private int devfd = -1;

    private String filepath_ = "";

    protected InputStream mInputStream;

    protected OutputStream mOutputStream;

    boolean useIC = false;

    boolean useID = false;

    CVRApi picUnpack;

    Handler cvrHandler = new Handler();

    int speed;

    String devName;

    ICardState iCardState_;  //事件接口

    private int readType_ = 0;  //读卡类型

    private int readState_ = 0;  //返回状态

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

    public ReadCard2(int sp, String devName, ICardState cardState) {
        this.speed = sp;
        this.devName = devName;
        this.iCardState_ = cardState;
        filepath_ = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            String ph = filepath_ + "/assets/wltlib";
            File file = new File(ph);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception ex) {
            Lg.e("CardInfo_CardInfo", ex.toString());
        }
    }

    @Override
    public int open() {
        int ix = devOpen(115200, devName);
        if (ix >= 0) {
            picUnpack = new CVRApi(cvrHandler);
        }
        return ix;
    }

    @Override
    public void close() {
        thread_continuous =false;
        if (devfd != -1) {
            rkSerial_.close();
            devfd = -1;
        }
    }

    @Override
    public void readCard() {
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
    public void clearIsReadOk() {

    }


    CardThread mCardThread;
    public int devOpen(int sp, String devName_) {
        try {
            rkSerial_ = new SerialPort(new File(devName_), sp, 0);
            mInputStream = rkSerial_.getInputStream();
            mOutputStream = rkSerial_.getOutputStream();
            Lg.e("card_dev", "open  SerialPort ok");
            devfd = 1;
        } catch (Exception e) {
            Lg.e("card_dev", e.toString());
        }
        //        Lg.e("SDs", CRC16.getCRC3(dt_Success2,((dt_Success2[1]<<8)+dt_Success2[0])+2));
        if(mCardThread==null){
            mCardThread = new CardThread();
            thread_continuous = true;
            mCardThread.start();
        }
        return devfd;
    }


    boolean ic_continuous = false;
    boolean id_continuous = false;
    boolean thread_continuous =false;
    byte[] readBuffer = new byte[2048];



    private class CardThread extends Thread {
        @Override
        public void run() {
            super.run();
            if(useID) {
                try {
                    sendandread(dt_sam, readBuffer, () -> {
                        System.arraycopy(readBuffer, 0, buf_, 0, readBuffer.length);
                        getSam_();
                    }, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while (thread_continuous) {
                if (mInputStream != null && mOutputStream != null) {
                    try {
                        sendandread(dt_antenna_close, readBuffer, () -> {
                            System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                            if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                                    + readBuffer[0]) + 2).equals("2000")) {
                                Lg.e("card天线关闭", "天线关闭成功");
                            } else {
                                Lg.e("card天线关闭", "天线关闭失败");
                            }
                        }, 50);
                        sendandread(dt_antenna_open, readBuffer, () -> {
                            System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                            if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                                    + readBuffer[0]) + 2).equals("2000")) {
                                Lg.e("card天线开启", "天线开启成功");
                            } else {
                                Lg.e("card天线开启", "天线开启失败");
                            }
                        }, 50);
                        sendandread(dt_ica_set, readBuffer, () -> {
                            System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                            if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                                    + readBuffer[0]) + 2).equals("2000")) {
                                Lg.e("cardTYBEA设置", "TYBEA设置成功");
                            } else {
                                Lg.e("cardTYBEA设置", "TYBEA设置失败");
                            }
                        }, 50);
                        if(useIC){
                            sendandread(dt_ica_read, readBuffer, () -> {
                                System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                                if (readBuffer[0] >= 0x08 && !ic_continuous) {
                                    ic_continuous = true;
                                    System.arraycopy(readBuffer, 5, readBuffer, 0, 4);
                                    uid_ = byteToStr(readBuffer, 4).toUpperCase();
                                    readType_ = uidget; readState_ = 1;
                                    mHandler.sendEmptyMessage(0);
                                    Lg.e("cardIC读卡", "IC卡已读");
                                } else if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                                        + readBuffer[0]) + 2).equals("E1CF")) {
                                    ic_continuous = false;
                                    Lg.e("cardIC读卡", "IC无读卡数据，转入ID");
                                    IDCardOperation();
                                }
                            }, 100);
                        }else{
                            IDCardOperation();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void IDCardOperation() {
        if(useID){
            try {
                sendandread(dt_antenna_close, readBuffer, () -> {
                    System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                    if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                            + readBuffer[0]) + 2).equals("2000")) {
                        Lg.e("card天线关闭", "天线关闭成功");
                    } else {
                        Lg.e("card天线关闭", "天线关闭失败");
                    }
                }, 50);
                sendandread(dt_antenna_open, readBuffer, () -> {
                    System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                    if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                            + readBuffer[0]) + 2).equals("2000")) {
                        Lg.e("card天线开启", "天线开启成功");
                    } else {
                        Lg.e("card天线开启", "天线开启失败");
                    }
                }, 50);
                sendandread(dt_ica_setB, readBuffer, () -> {
                    System.arraycopy(readBuffer, 2, readBuffer, 0, readBuffer.length - 4);
                    if (CRC16.getCRC3(readBuffer, ((readBuffer[1] << 8)
                            + readBuffer[0]) + 2).equals("2000")) {
                        Lg.e("cardTYBEB设置", "TYBEB设置成功");
                    } else {
                        Lg.e("cardTYBEB设置", "TYBEB设置失败");
                    }
                }, 50);
                sendandread(dt_isCer, readBuffer, () -> {
                    System.arraycopy(readBuffer, 5, readBuffer, 0, readBuffer.length - 5);
                    if (readBuffer[((readBuffer[0] << 8) + readBuffer[1]) + 1] == CRC16.Xor(readBuffer,
                            ((readBuffer[0] << 8) + readBuffer[1]) + 1)) {
                        if (readBuffer[1] == 0x08) {
                            Lg.e("cardID寻卡", "寻卡成功");
                        } else if (readBuffer[1] == 0x04) {
                            Lg.e("cardID寻卡", "寻卡失败");
                            id_continuous = false;
                        }
                    } else {
                        Lg.e("cardID寻卡", "寻卡：错误数据包");

                    }
                }, 200);
                sendandread(dt_selectCer, readBuffer, () -> {
                    System.arraycopy(readBuffer, 5, readBuffer, 0, readBuffer.length - 5);
                    if (readBuffer[((readBuffer[0] << 8) + readBuffer[1]) + 1] == CRC16.Xor(readBuffer,
                            ((readBuffer[0] << 8) + readBuffer[1]) + 1)) {
                        if (readBuffer[1] == 0x0C) {
                            Lg.e("cardID选卡", "选卡成功");
                        } else if (readBuffer[1] == 0x04) {
                            Lg.e("cardID选卡", "选卡失败");
                        }
                    } else {
                        Lg.e("cardID选卡", "选卡：错误数据包");
                    }
                }, 100);
                if (id_continuous) {
                    return;
                }
                sendandread(dt_readCer, readBuffer, () -> {
                    if (readBuffer[6] == 0x04) {
                        Lg.e("cardID读卡", "读卡失败");
                    } else {
                        if ((readBuffer[5] << 8) + readBuffer[6] == 1288) {
                            Lg.e("cardID读卡", "读卡成功");
                            System.arraycopy(readBuffer,0,buf_,0,readBuffer.length);
                            readCerd();
                            id_continuous = true;
                        } else {
                            Lg.e("cardID读卡", "读卡返回数据不完整");
                        }
                    }
                }, 1100);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void sendandread(byte[] order, byte[] bytes, feadback feadback, int delay) throws Exception {
        mOutputStream.write(order);
        Thread.sleep(delay);
        mInputStream.read(bytes);
        feadback.back();
    }

    interface feadback {
        void back();
    }


    //姓名
    private String name_ = "";
    //姓别
    private String sex_ = "";
    //民族
    private String nation_ = "";
    //出生
    private String birthday_ = "";
    //住址
    private String address_ = "";
    //公民身份号码
    private String cardId_ = "";
    //签发机关
    private String dept_ = "";
    //有效期起始日期
    private String validDateBegin_ = "";
    //有效期截止日期
    private String validDate_ = "";
    //身份证出场模块日期
    private String sam_ = "";
    //IC卡Uid
    private String uid_ = "";
    //照片数据
    private byte[] buf_ = new byte[2048];

    private byte[] wltBuf = new byte[1024];

    private final int t_name = 1;
    private final int t_sex = 2;
    private final int t_nation = 3;
    private final int t_birthday = 4;
    private final int t_address = 5;
    private final int t_cerdId = 6;
    private final int t_dept = 7;
    private final int t_validDateBegin = 8;
    private final int t_validDate = 9;

    private void readCerd() {
        name_ = "";
        sex_ = "";
        nation_ = "";
        birthday_ = "";
        address_ = "";
        cardId_ = "";
        dept_ = "";
        validDateBegin_ = "";
        validDate_ = "";
        name_ = getCerInfo(buf_, t_name);
        sex_ = getCerInfo(buf_, t_sex);
        nation_ = getCerInfo(buf_, t_nation);
        birthday_ = getCerInfo(buf_, t_birthday);
        address_ = getCerInfo(buf_, t_address);
        cardId_ = getCerInfo(buf_, t_cerdId);
        dept_ = getCerInfo(buf_, t_dept);
        validDateBegin_ = getCerInfo(buf_, t_validDateBegin);
        validDate_ = getCerInfo(buf_, t_validDate);
        sex_ = getSex(sex_);
        nation_ = getNation(nation_);
        try {
            System.arraycopy(buf_, 270, wltBuf, 0, 1024);
        } catch (Exception ex) {
            Lg.e("CardInfo_readCerd", ex.toString());
        }
        readType_ = cardInfoget; readState_ = 1;
        mHandler.sendEmptyMessage(0);

    }


    @Override
    public Bitmap getBmp() {
        return getBmp(wltBuf);
    }

    @Override
    public void setDevType(String sType) {

    }

    @Override
    public void readSam() {
        readType_ = samget; readState_ = 1;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public String getSam() {
        return sam_;
    }

    @Override
    public String getUid() {
        return uid_;
    }

    @Override
    public String name() {
        return name_;
    }

    @Override
    public String cardId() {
        return cardId_;
    }

    public Bitmap getBmp(byte[] wlt) {
        if (picUnpack != null) {
            try {
                byte[] bmp_data = new byte[38862];
                String fp = Environment.getExternalStorageDirectory() + "/wltlib";
                //String fp="/storage/sdcard0/wltlib";
                File fn = new File(fp + "/zp.bmp");
                if (fn.exists()) {
                    fn.delete();
                }
                int len = picUnpack.Unpack(fp, wlt, bmp_data);
                if (fn.exists()) {
                    FileInputStream fis = new FileInputStream(fp + "/zp.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                    return bmp;
                }
            } catch (Exception ex) {
                Lg.e("CardInfo_getBmp", ex.toString());
            }
            ;
        }
        return null;
    }

    public String getCerInfo(byte[] bs, int itype) {
        if (bs == null) {
            return "";
        }
        ;
        String s = "";
        if (bs.length >= 256) {
            if (itype == 1) {
                byte[] bx = new byte[30];
                for (int i = 0; i < 30; i++) {
                    bx[i] = bs[i + 14];
                }
                s = toUCS2(bx);
            } else if (itype == 2) {
                byte[] bx = new byte[2];
                for (int i = 0; i < 2; i++) {
                    bx[i] = bs[i + 44];
                }
                s = toUCS2(bx);
            } else if (itype == 3) {
                byte[] bx = new byte[4];
                for (int i = 0; i < 4; i++) {
                    bx[i] = bs[i + 46];
                }
                s = toUCS2(bx);
            } else if (itype == 4) {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++) {
                    bx[i] = bs[i + 50];
                }
                s = toUCS2(bx);
            } else if (itype == 5) {
                byte[] bx = new byte[70];
                for (int i = 0; i < 70; i++) {
                    bx[i] = bs[i + 66];
                }
                s = toUCS2(bx);
            } else if (itype == 6) {
                byte[] bx = new byte[36];
                for (int i = 0; i < 36; i++) {
                    bx[i] = bs[i + 136];
                }
                s = toUCS2(bx);
            } else if (itype == 7) {
                byte[] bx = new byte[30];
                for (int i = 0; i < 30; i++) {
                    bx[i] = bs[i + 172];
                }
                s = toUCS2(bx);
            } else if (itype == 8) {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++) {
                    bx[i] = bs[i + 202];
                }
                s = toUCS2(bx);
            } else if (itype == 9) {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++) {
                    bx[i] = bs[i + 218];
                }
                s = toUCS2(bx);
            }
                /*
            else if (itype ==10)
            {
                byte[] bx = new byte[70];
                for (int i = 0; i <70; i++)
                {
                    bx[i] = bs[i + 234];
                }
                s = System.Text.Encoding.GetEncoding("UCS-2").GetString(bx).ToString().Trim();
            }
            */
        }
        return s;
    }

    //取性别
    private String getSex(String code) {
        if (code.trim().equals("2")) {
            return ("女");
        } else if (code.trim().equals("1")) {
            return ("男");
        } else {
            return "";
        }
    }

    //取民族
    private String getNation(String scode) {
        int code = 0;
        try {

            code = Integer.parseInt(scode);
        } catch (Exception ex) {
            code = 0;
        }
        ;

        switch (code) {
            case 01:
                return ("汉");
            case 02:
                return ("蒙古");
            case 03:
                return ("回");
            case 04:
                return ("藏");
            case 05:
                return ("维吾尔");
            case 06:
                return ("苗");
            case 07:
                return ("彝");
            case 8:
                return ("壮");
            case 9:
                return ("布依");
            case 10:
                return ("朝鲜");
            case 11:
                return ("满");
            case 12:
                return ("侗");
            case 13:
                return ("瑶");
            case 14:
                return ("白");
            case 15:
                return ("土家");
            case 16:
                return ("哈尼");
            case 17:
                return ("哈萨克");
            case 18:
                return ("傣");
            case 19:
                return ("黎");
            case 20:
                return ("傈僳");
            case 21:
                return ("佤");
            case 22:
                return ("畲");
            case 23:
                return ("高山");
            case 24:
                return ("拉祜");
            case 25:
                return ("水");
            case 26:
                return ("东乡");
            case 27:
                return ("纳西");
            case 28:
                return ("景颇");
            case 29:
                return ("柯尔克孜");
            case 30:
                return ("土");
            case 31:
                return ("达斡尔");
            case 32:
                return ("仫佬");
            case 33:
                return ("羌");
            case 34:
                return ("布朗");
            case 35:
                return ("撒拉");
            case 36:
                return ("毛南");
            case 37:
                return ("仡佬");
            case 38:
                return ("锡伯");
            case 39:
                return ("阿昌");
            case 40:
                return ("普米");
            case 41:
                return ("塔吉克");
            case 42:
                return ("怒");
            case 43:
                return ("乌孜别克");
            case 44:
                return ("俄罗斯");
            case 45:
                return ("鄂温克");
            case 46:
                return ("德昂");
            case 47:
                return ("保安");
            case 48:
                return ("裕固");
            case 49:
                return ("京");
            case 50:
                return ("塔塔尔");
            case 51:
                return ("独龙");
            case 52:
                return ("鄂伦春");
            case 53:
                return ("赫哲");
            case 54:
                return ("门巴");
            case 55:
                return ("珞巴");
            case 56:
                return ("基诺");
            case 97:
                return ("其他");
            case 98:
                return ("外国血统中国籍人士");
            default:
                return ("");
        }
    }

    public String toUCS2(byte[] bx) {
        if (bx == null) {
            return null;
        }
        ;
        byte b = 0;
        for (int i = 0; i < (bx.length / 2); i++) {
            b = bx[2 * i];
            bx[2 * i] = bx[2 * i + 1];
            bx[2 * i + 1] = b;
        }
        String s = "";
        try {
            s = new String(bx, "ISO-10646-UCS-2");
            s = s.trim();
        } catch (Exception ex) {
            s = "";
        }
        ;
        return s;
    }

    public String byteToStr(byte[] bs, int len) {
        if (bs.length >= len) {
            String s = "";
            for (int i = 0; i < len; i++) {
                s += byteToHex(bs[i]);
            }
            return s;

        } else {
            return "";
        }
    }

    public String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    private void getSam_() {

        sam_ = formatStr(intToStr(buf_, 10, 1), 2) + formatStr(intToStr(buf_, 12, 1), 2) + "-" + intToStr(buf_, 14, 4) +
                "-" + formatStr(intToStr(buf_, 18, 4), 10) + "-" + formatStr(intToStr(buf_, 22, 4), 10);
    }

    private String intToStr(byte[] bs, int pos, int len) {
        String s = "";
        if (bs.length < (pos + len)) {
            return "";
        } else {
            long ii = 0;
            long ix = 1;
            try {
                for (int i = pos; i < pos + len; i++) {
                    ii += (bs[i] & 0xff) * ix;
                    ix *= 256;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            s = "" + ii;
        }
        return s;
    }

    public String formatStr(String str, int len) {
        String s = "";
        if (str.length() == len) {
            s = str;
        } else if (str.length() < len) {
            for (int i = str.length(); i < len; i++) {
                s = '0' + s;
            }
            s = s + str;
        } else if (str.length() > len) {
            s = str.substring(str.length() - len);

        }
        return s;
    }

}

