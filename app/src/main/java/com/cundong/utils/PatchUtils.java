package com.cundong.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PatchUtils {

    /**
     * native方法 使用路径为oldApkPath的apk与路径为patchPath的补丁包，合成新的apk，并存储于newApkPath
     * <p>
     * 返回：0，说明操作成功
     *
     * @param oldApkPath 示例:/sdcard/old.apk
     * @param newApkPath 示例:/sdcard/new.apk
     * @param patchPath  示例:/sdcard/xx.patch
     * @return
     */

    static {
        try {
            System.loadLibrary("ApkPatchLibrary");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static native int patch(String oldApkPath, String newApkPath, String patchPath);
}