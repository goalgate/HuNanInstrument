package cn.cbdi.hunaninstrument.Bean.DataFlow;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zbsz on 2017/12/3.
 */

public class UpOpenDoorData {

    private static final int fp_head = 0;
    private static final int fp_type = 2;
    private static final int fp_time = 3;
    private static final int fp_perid1 = 10;
    private static final int fp_pername1 = 28;
    private static final int fp_perid2 = 68;
    private static final int fp_pername2 = 86;
    private static final int fp_perPic = 126;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private byte[] faceResult_ = {95, 95, 95}; //返回结果

    public final void setFaceResult(int i, int v) {
        int ix = v;
        if (ix > 100) {
            ix = 200;
        } else if (v < 0) {
            ix = 95;
        } else {
            ix = v + 100;
        }

        if (i == 1) {
            faceResult_[0] = (byte) ix;

        } else if (i == 2) {
            faceResult_[1] = (byte) ix;

        } else if (i == 3) {
            faceResult_[2] = (byte) ix;
        }

    }

    public final int getFaceResult(int i) {
        if (i == 1) {
            return faceResult_[0];
        } else if (i == 2) {
            return faceResult_[1];

        } else if (i == 3) {
            return faceResult_[2];
        }
        return -5;

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

//        /
//转图大小
//        public Image GetReducedImage(Image ResourceImage, int Width, int Height)
//        {
//            try
//            {
//                Image ReducedImage;
//                Image.GetThumbnailImageAbort callb = new Image.GetThumbnailImageAbort(ThumbnailCallback);
//                ReducedImage = ResourceImage.GetThumbnailImage(Width, Height, callb, IntPtr.Zero);
//                return ReducedImage;
//            }
//            catch (Exception e)
//            {
//                return null;
//            }
//        }
//


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


    //流格式 文件头2字节 0x23 0x24 1字节 类型 7字节时间 第一人身份18字节 名称40字节 第二人身份18字节 名称40字节 第1人照片长度  4字节  第2人照片
    public final ByteArrayOutputStream toOpenDoorData(byte btype, String pid1, Bitmap p1, String pid2, Bitmap p2) {
        return toOpenDoorData(btype, pid1, "", p1, pid2, "", p2);
    }

    //流格式 文件头2字节 0x23 0x24 1字节 类型 7字节时间 第一人身份18字节 名称40字节 第二人身份18字节 名称40字节 第1人照片长度  4字节  第2人照片
    public final ByteArrayOutputStream toOpenDoorData(byte btype, String pid1, String name1, Bitmap p1, String pid2, String name2, Bitmap p2) {
        ByteArrayOutputStream ms = null;
        if (btype == 1) {
            if (isCerid(pid1) && isCerid(pid2)) {
                ms = new ByteArrayOutputStream();
                ByteArrayOutputStream msx1 = null;
                ByteArrayOutputStream msx2 = null;
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
                    byte[] by = pid1.getBytes();
                    if (by != null) {
                        if (by.length == 18) {
                            ms.write(by, 0, 18);
                        } else {
                            ms.write(bx, 0, 18);
                        }

                    } else {
                        ms.write(bx, 0, 18);
                    }

                    by = name1.getBytes();
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

                    //for (int i = 0; i < 40; i++) { bx[i] = 0; };
                    //ms.Write(bx, 0, 40);

                    for (int i = 0; i < 40; i++) {
                        bx[i] = 0;
                    }
                    by = pid2.getBytes();
                    if (by != null) {
                        if (by.length == 18) {
                            ms.write(by, 0, 18);
                        } else {
                            ms.write(bx, 0, 18);
                        }

                    } else {
                        ms.write(bx, 0, 18);
                    }

                    by = name2.getBytes();
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
                    // for (int i = 0; i < 40; i++) { bx[i] = 0; };
                    // ms.Write(bx, 0, 40);
                    //照片
                    for (int i = 0; i < 20; i++) {
                        bx[i] = 0;
                    }

                    if (p1 == null) {
                        ms.write(bx, 0, 8);
                    } else {
                        msx1 = new ByteArrayOutputStream();
                        p1.compress(Bitmap.CompressFormat.JPEG, 90, msx1);
                        int ix=0,iy=0;
                        if (msx1 != null) {
                            ix = (int) msx1.size();
                            bx[0] = (byte) (ix % 256);
                            bx[1] = (byte) ((ix / 256) % 256);
                            bx[2] = (byte) ((ix / 65536) % 256);
                        } else {
                            ms.write(bx, 0, 4);
                        }
                        if (p2 != null) {
                            msx2 = new ByteArrayOutputStream();
                            p2.compress(Bitmap.CompressFormat.JPEG, 90, msx2);
                            iy = (int) msx2.size();
                            bx[4] = (byte) (iy % 256);
                            bx[5] = (byte) ((iy / 256) % 256);
                            bx[6] = (byte) ((iy / 65536) % 256);
                        }
                        ms.write(bx, 0, 8);
                        if(msx1!= null){
                            ms.write(msx1.toByteArray(), 0, ix);
                        }
                        if(msx2!= null){
                            ms.write(msx2.toByteArray(), 0, iy);
                        }
                    }
                    ms.flush();
                    return ms;
                } catch (Exception e2) {
                    try {
                        ms.close();
                        ms = null;
                    } catch (Exception e3) {
                    }
                }

            }
        }
        return null;
    }

}
