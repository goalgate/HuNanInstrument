package cn.cbdi.hunaninstrument.Bean.DataFlow;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zbsz on 2017/12/3.
 */
public class UpCheckRecordData {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int fp_head = 0;
    private static final int fp_type = 2;
    private static final int fp_time = 3;
    private static final int fp_perid1 = 10;
    private static final int fp_pername1 = 28;
    private static final int fp_perPic = 68;

    private static final byte btype = 2;
    private String pid_ = "";
    private String pname_ = "";
    private int checkType_ = 3;


    public final String getperID() {
        return pid_;
    }

    public final void setperID(String value) {
        pid_ = value;
    }

    public final String getperName() {
        return pname_;
    }

    public final void setperName(String value) {
        pname_ = value;
    }


    public final void setIdName(String id, String name) {
        pid_ = id;
        pname_ = name;
    }

    //取时间
    public final byte[] getTime() {
        String time = formatter.format(new Date(System.currentTimeMillis()));
        byte[] buf = new byte[7];
        buf[0] = (byte) (Integer.parseInt(time.substring(0, 2)));
        buf[1] = (byte) (Integer.parseInt(time.substring(2, 4)));
        buf[2] = (byte) (Integer.parseInt(time.substring(5, 7)));
        buf[3] = (byte) (Integer.parseInt(time.substring(8, 10)));
        buf[4] = (byte) (Integer.parseInt(time.substring(11, 13)));
        buf[5] = (byte) (Integer.parseInt(time.substring(14, 16)));
        buf[6] = (byte) (Integer.parseInt(time.substring(17, 19)));
        return buf;
    }

    //是否是身份证号
    public final boolean isCerid(String id) {
        if (id == null) {
            return false;
        } else if (id.trim().length() == 18) {
            return true;
        }
        return false;
    }

    public final boolean ThumbnailCallback() {

        return false;

    }


    //设置巡检类型 2企业巡检  3公安巡检
    public final int getcheckType() {
        return checkType_;
    }

    public final void setcheckType(int value) {
        checkType_ = value;
    }

    public final void clear() {
        pid_ = "";
        pname_ = "";
        checkType_ = 3;
    }


    //流格式 文件头2字节 0x23 0x24 1字节 类型 7字节时间 第一人身份18字节 名称40字节 第二人身份18字节 名称40字节 第1人照片长度  4字节  第2人照片
    public final ByteArrayOutputStream toCheckRecordData(String pid, Bitmap pimg, String name) {
        ByteArrayOutputStream ms = null;

        if (isCerid(pid)) {
            ms = new ByteArrayOutputStream();
            ByteArrayOutputStream msx = null;
            try {
                byte[] bx = new byte[40];
                ms.write(0x23);
                ms.write(0x24);
                ms.write(btype);
                ms.write(getTime(), 0, 7);
                //身份证号
                for (int i = 0; i < 40; i++) {
                    bx[i] = 0;
                }
                byte[] by = pid.getBytes();
                if (by != null) {
                    if (by.length == 18) {
                        ms.write(by, 0, 18);
                    } else {
                        ms.write(bx, 0, 18);
                    }

                } else {
                    ms.write(bx, 0, 18);
                }

                by = name.getBytes();
                for (int i = 0; i < 40; i++) {
                    bx[i] = 0;
                }
                if (by != null) {
                    int l = by.length;
                    if (l > 39) {
                        ms.write(bx, 0, 40);
                    } else {
                        bx[0] = (byte) l;
                        for (int i = 0; i < l; i++) {
                            bx[i + 1] = by[i];
                        }
                        ms.write(bx, 0, 40);
                    }
                } else {
                    ms.write(bx, 0, 40);
                }


                //照片
                for (int i = 0; i < 20; i++) {
                    bx[i] = 0;
                }
                if (pimg == null) {
                    ms.write(bx, 0, 4);
                } else {
                    msx = new ByteArrayOutputStream();
                    pimg.compress(Bitmap.CompressFormat.JPEG, 90, msx);
                    msx.flush();
                    if (msx != null) {
                        int ix = (int) msx.size();
                        bx[0] = (byte) (ix % 256);
                        bx[1] = (byte) ((ix / 256) % 256);
                        bx[2] = (byte) ((ix / 65536) % 256);
                        ms.write(bx, 0, 4);
                        ms.write(msx.toByteArray(), 0, ix);
                    } else {
                        ms.write(bx, 0, 4);
                    }
                }
                ms.flush();
                return ms;
            } catch (Exception e) {
                try {
                    ms.close();
                    ms = null;
                } catch (Exception e2) {
                }
            }

        }
        return null;
    }
}

