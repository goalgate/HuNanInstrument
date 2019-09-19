package cn.cbdi.hunaninstrument.Tool.Update;

import android.os.Environment;

import java.io.File;

public class UpdateConstant {
    public static final String ORIGINAL_APK_MD5 = "89e8be1f9a917f753f76d130c6849541";

    public static final String New_APK_MD5 = "89e8be1f9a917f753f76d130c6849541";

    public static final String SIGN_MD5 = "89e8be1f9a917f753f76d130c6849541";
    //用于测试的packageName
    public static final String TEST_PACKAGENAME = "cn.cbdi.hunaninstrument";

    public static final String PATH = Environment.getExternalStorageDirectory() + File.separator + "ApkUpdate" + File.separator;

    public static final String ORIGINAL_APK_PATH = PATH + "original.apk";

    //合成得到的新版apk
    public static final String NEW_APK_PATH = PATH + "new.apk";

    //从服务器下载来的差分包
    public static final String PATCH_PATH = PATH + "patch.patch";

    public static final String MANUAL_PATH = PATH + "manual.patch";

}
