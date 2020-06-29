package cn.cbdi.hunaninstrument.Tool;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapTools {

    public static Bitmap scaleMatrix(Bitmap bitmap, int width) {

        int before_width = bitmap.getWidth();

        int before_height = bitmap.getHeight();

        int after_width = width;

        int after_height = (before_height * after_width)/before_width;

        float scaleW = (float) after_width / before_width;
        float scaleH = (float) after_height / before_height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例

        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, before_width, before_height, matrix, true);
        if (bitmap != null & !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }
        return newbm;
    }

}
