package cn.cbdi.hunaninstrument.Bean.DataFlow;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zbsz on 2017/11/28.
 */

public class UpPersonRecordData {

    private static final int fp_head = 0;
    private static final int fp_type = 2;
    private static final int fp_time = 3;
    private static final int fp_perid1 = 10;
    private static final int fp_pername1 = 28;
    private static final int fp_perPic = 68;
    private static final byte btype = 3;
    private byte[] pic_ = null;
    private String perID_ = "";
    private String perName_ = "";
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final String getperID() {
        return perID_;
    }

    public final void setperID(String value) {
        perID_ = value;
    }

    public final byte[] getPic() {
        return pic_;
    }

    //设置图像数据
    public final boolean setPic(byte[] px) {
        if (px != null) {

            int ix = px.length;
            if (ix < 100000) {
                try {
                    pic_ = new byte[ix];
                    System.arraycopy(px, 0, pic_, 0, ix);
                    return true;
                } catch (Exception e) {
                }

            }
        } else {
            pic_ = null;
        }
        return false;
    }

    public final String getperName() {
        return perName_;
    }

    public final void setperName(String value) {
        perName_ = value;
    }

    public final void clear() {
        perID_ = "";
        perName_ = "";
    }

    //取时间
    public final byte[] getTime() {
        String time = formatter.format(new Date(System.currentTimeMillis()));
        byte[] buf = new byte[7];
        buf[0] = (byte) (Integer.parseInt(time.substring(0,2)));
        buf[1] = (byte) (Integer.parseInt(time.substring(2,4)));
        buf[2] = (byte) (Integer.parseInt(time.substring(5,7)));
        buf[3] = (byte) (Integer.parseInt(time.substring(8,10)));
        buf[4] = (byte) (Integer.parseInt(time.substring(11,13)));
        buf[5] = (byte) (Integer.parseInt(time.substring(14,16)));
        buf[6] = (byte) (Integer.parseInt(time.substring(17,19)));
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

/*    //转图大小
    public final Image GetReducedImage(Image ResourceImage, int toWidth, int toHeight)
    {
        Image originalImage = ResourceImage;
        if (toWidth <= 0 && toHeight <= 0)
        {
            return originalImage;
        }
        else if (toWidth > 0 && toHeight > 0)
        {
            if (originalImage.getWidth() < toWidth && originalImage.getHeight() < toHeight)
            {
                return originalImage;
            }
        }
        else if (toWidth <= 0 && toHeight > 0)
        {
            if (originalImage.getHeight() < toHeight)
            {
                return originalImage;
            }
            toWidth = originalImage.getWidth() * toHeight / originalImage.getHeight();
        }
        else if (toHeight <= 0 && toWidth > 0)
        {
            if (originalImage.getWidth() < toWidth)
            {
                return originalImage;
            }
            toHeight = originalImage.getHeight() * toWidth / originalImage.getWidth();
        }
        Image toBitmap = new Bitmap(toWidth, toHeight);
//C# TO JAVA CONVERTER NOTE: The following 'using' block is replaced by its Java equivalent:
//		using (Graphics g = Graphics.FromImage(toBitmap))
        Graphics g = Graphics.FromImage(toBitmap);
        try
        {
            //g.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.High;
            //g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.HighQuality;
            g.Clear(Color.Transparent);
            g.DrawImage(originalImage, new Rectangle(0, 0, toWidth, toHeight), new Rectangle(0, 0, originalImage.getWidth(), originalImage.getHeight()), GraphicsUnit.Pixel);
            //originalImage.Dispose();
            return toBitmap;
        }
        finally
        {
            g.dispose();
        }
    }*/


    public ByteArrayOutputStream toPersonRecordData(String pid, Bitmap pimg, String name) {

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
                    ms.write(bx, 0, 8);
                } else {
                    Bitmap img = pimg; // GetReducedImage(pimg, 320, 240);
                    msx = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.JPEG, 90, msx);
                    int ix=0,iy=0;
                    if (msx != null) {
                        ix = (int) msx.size();
                        bx[0] = (byte) (ix % 256);
                        bx[1] = (byte) ((ix / 256) % 256);
                        bx[2] = (byte) ((ix / 65536) % 256);
                    }
                    if (pic_ != null) {
                        iy = (int) pic_.length;
                        bx[4] = (byte) (iy % 256);
                        bx[5] = (byte) ((iy / 256) % 256);
                        bx[6] = (byte) ((iy / 65536) % 256);
                    }
                    ms.write(bx, 0, 8);
                    if (msx != null) {
                        ms.write(msx.toByteArray(), 0, ix);
                    }
                    if (pic_ != null) {
                        ms.write(pic_, 0, iy);
                    }
                    img = null;

                }
                ms.flush();
                return ms;
            } catch (IOException e) {
                try {

                    ms.close();
                    ms = null;
                } catch (Exception e2) {

                }
            }finally {
                try {
                    msx.close();
                    ms.close();
                    ms = null;
                } catch (Exception e2) {

                }
            }

        }

        return null;
    }


 /*   //流格式 文件头2字节 0x23 0x24 1字节 类型 7字节时间 第一人身份18字节 名称40字节 第二人身份18字节 名称40字节 第1人照片长度  4字节  第2人照片
    public final MemoryStream toPersonRecordData(String pid, Image pimg, String name) {
        MemoryStream ms = null;

        if (isCerid(pid)) {
            ms = new MemoryStream();
            MemoryStream msx = null;
            try {
                byte[] bx = new byte[40];
                ms.WriteByte(0x23);
                ms.WriteByte(0x24);
                ms.WriteByte(btype);
                ms.Write(getTime(), 0, 7);
                //身份证号
                for (int i = 0; i < 40; i++) {
                    bx[i] = 0;
                }
                byte[] by = new ASCIIEncoding().GetBytes(pid);
                if (by != null) {
                    if (by.length == 18) {
                        ms.Write(by, 0, 18);
                    } else {
                        ms.Write(bx, 0, 18);
                    }

                } else {
                    ms.Write(bx, 0, 18);
                }

                by = UTF8Encoding.UTF8.GetBytes(name);
                for (int i = 0; i < 40; i++) {
                    bx[i] = 0;
                }
                if (by != null) {
                    int l = by.length;
                    if (l > 39) {
                        ms.Write(bx, 0, 40);
                    } else {
                        bx[0] = (byte) l;
                        for (int i = 0; i < l; i++) {
                            bx[i + 1] = by[i];
                        }
                        ms.Write(bx, 0, 40);
                    }
                } else {
                    ms.Write(bx, 0, 40);
                }


                //照片
                for (int i = 0; i < 20; i++) {
                    bx[i] = 0;
                }
                if (pimg == null) {
                    ms.Write(bx, 0, 8);
                } else {
                    Image img = pimg; // GetReducedImage(pimg, 320, 240);
                    msx = new MemoryStream();
                    img.Save(msx, System.Drawing.Imaging.ImageFormat.Jpeg);
                    if (msx != null) {
                        int ix = (int) msx.getLength();
                        bx[0] = (byte) (ix % 256);
                        bx[1] = (byte) ((ix / 256) % 256);
                        bx[2] = (byte) ((ix / 65536) % 256);
                        ms.Write(bx, 0, 8);
                        ms.Write(msx.GetBuffer(), 0, ix);
                    } else {
                        ms.Write(bx, 0, 8);
                    }
                    img = null;

                }
                //头像
                for (int i = 0; i < 20; i++) {
                    bx[i] = 0;
                }
                if (pic_ != null) {
                    int ix = (int) pic_.length;
                    bx[0] = (byte) (ix % 256);
                    bx[1] = (byte) ((ix / 256) % 256);
                    bx[2] = (byte) ((ix / 65536) % 256);
                    ms.Write(pic_, 0, ix);
                }
                ms.Position = fp_perPic + 4;
                ms.Write(bx, 0, 4);
                ms.Flush();
                return ms;
            } catch (java.lang.Exception e) {
                try {
                    ms.Close();
                    ms = null;
                } catch (java.lang.Exception e2) {
                }
            }

        }
        return null;
    }*/
}