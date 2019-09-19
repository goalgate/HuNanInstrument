package com.baidu.aip.callback;

import android.hardware.Camera;

import com.serenegiant.usb.UVCCamera;

/**
 * Time: 2019/1/25
 * Author: v_chaixiaogang
 * Description: camera1数据结果回调
 */
public interface CameraDataCallback {
    /**
     * @param data   预览数据
     * @param camera 相机设备
     * @param width  预览宽
     * @param height 预览高
     */
    void onGetCameraData(int[] data, Camera camera, int width, int height);

    void onGetCameraData(int[] data, UVCCamera camera, int width, int height);

}
