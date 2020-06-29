package com.baidu.aip.manager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.aip.callback.FaceCallback;
import com.baidu.aip.ui.Activation;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.callback.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Time: 2019/1/15
 * @Author: v_chaixiaogang
 * @Description: FaceSDK管理类
 */
public class FaceSDKManager {

    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_FAIL = 5;
    public static final int SDK_SUCCESS = 6;
    public static final String licenseFileName = "idl-license.face-android";
    private FaceDetector faceDetector;
    private FaceFeatures faceFeature;
    private FaceAttribute faceAttribute;
    private FaceLiveness faceLiveness;
    private FacefeaturesImage mFacefeaturesImage;
    private Context context;
    public static volatile int initStatus = SDK_UNACTIVATION;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Activation activation;
    private FaceAuth mFaceAuth;

    private FaceEnvironment faceEnvironment;

    private FaceSDKManager() {
        mFaceAuth = new FaceAuth();
//        mFaceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE); // 是否开启Log
        mFaceAuth.setAnakinThreadsConfigure(2, 0); // 推荐3288板子上设置为2个大核0个小核3399上设为4个大核0个小核
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeatures();
        faceAttribute = new FaceAttribute();
        faceLiveness = new FaceLiveness();
        mFacefeaturesImage = new FacefeaturesImage();
        faceEnvironment = new FaceEnvironment();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public void release() {
        context = null;
    }


    public int initStatus() {
        return initStatus;
    }

    public FaceAuth getFaceAuth() {
        return mFaceAuth;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeatures getFaceFeature() {
        return faceFeature;
    }

    public FaceAttribute getFaceAttribute() {
        return faceAttribute;
    }

    public FaceLiveness getFaceLiveness() {
        return faceLiveness;
    }

    public FacefeaturesImage getFacefeaturesImage() {
        return mFacefeaturesImage;
    }

    /**
     * FaceSDK 初始化
     *
     * @param context
     */
    public void init(final Context context, final SdkInitListener sdkInitListener) {
        this.context = context;
        PreferencesUtil.initPrefs(context.getApplicationContext());
        final String licenseKey = PreferencesUtil.getString("activate_key", "");
        if (licenseKey.equals("") || TextUtils.isEmpty(licenseKey)) {
            showActivation(sdkInitListener);
            return;
        } else {
            if (licenseKey.equals("LMPW-TXGV-9C28-AFTR") ||
                    licenseKey.equals("ULME-2JXX-EAQY-LEDV") ||
                    licenseKey.equals("GB78-TWDK-MVMC-KGBX") ||
                    licenseKey.equals("GHXX-CDLX-SVNV-MCUX") ||
                    licenseKey.equals("XNW7-HGCU-DDRK-HHCJ") ||
                    licenseKey.equals("JD4B-2CGD-TKNW-XTAN")) {
                String real_licenseKey = "";
                if (licenseKey.equals("LMPW-TXGV-9C28-AFTR")) {
                    real_licenseKey = "NBGA-KYHY-ZTD9-8XAV";
                } else if (licenseKey.equals("ULME-2JXX-EAQY-LEDV")) {
                    real_licenseKey = "GHXX-CDLX-SVNV-MCUX";
                } else if (licenseKey.equals("GB78-TWDK-MVMC-KGBX")) {
                    real_licenseKey = "YUPX-YSNZ-NYE8-LBPX";
                } else if (licenseKey.equals("GHXX-CDLX-SVNV-MCUX")) {
                    real_licenseKey = "TZJF-CQQQ-WXSK-JNXU";
                } else if (licenseKey.equals("XNW7-HGCU-DDRK-HHCJ")) {
                    real_licenseKey = "WCFF-X5HB-VXMX-2XNX";
                } else if (licenseKey.equals("JD4B-2CGD-TKNW-XTAN")) {
                    real_licenseKey = "ALSF-XKXC-VXKY-VVNY";
                }
                toast("人脸激活号正在转换，请稍候...");
                activation = new Activation(context);
                activation.show();
                activation.setActivationCallback(new Activation.ActivationCallback() {
                    @Override
                    public void callback(int code, String response, String licenseKey) {
                        if (code == 0) {
                            Log.e("FaceSDK", "授权成功");
                            if (sdkInitListener != null) {
                                PreferencesUtil.putString("activate_key", licenseKey);
                                initStatus = SDK_SUCCESS;
                                sdkInitListener.initSuccess();
                                initModel();
                            }
                            return;
                        } else {
                            Log.e("FaceSDK", "授权失败:" + response);
                            if (sdkInitListener != null) {
                                dissDialog();
//                                sdkInitListener.initFail(code, "授权失败:" + response);
                                toast("人脸号更换失败，请确定设备已联网");
                            }
                        }
                    }
                });
                activation.keyEt.setText(real_licenseKey);
                activation.activateBtn.performClick();
            } else {
                if (sdkInitListener != null) {
                    sdkInitListener.initStart();
                }
                Log.e("FaceSDK", "初始化授权");
                check(context, licenseKey, new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code == 0) {
                            Log.e("FaceSDK", "授权成功");
                            if (sdkInitListener != null) {
                                SaveTxt(licenseKey);
                                initStatus = SDK_SUCCESS;
                                sdkInitListener.initSuccess();
                                initModel();
                            }
                            return;
                        } else {
                            Log.e("FaceSDK", "授权失败:" + response);
                            if (sdkInitListener != null) {
                                sdkInitListener.initFail(code, "授权失败:" + response);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showActivation(sdkInitListener);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void initModel() {
        faceDetector.initModel(context, "detect_rgb_anakin_2.0.2.bin",
                "detect_nir_2.0.2.model",
                "align_anakin_2.0.2.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
        faceDetector.initQuality(context, "blur_2.0.2.binary",
                "occlusion_anakin_2.0.2.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
        faceDetector.loadConfig(getFaceEnvironmentConfig());
        faceFeature.initModel(context, "recognize_rgb_idcard_pytorch_anakin_2.0.2.bin",
                "recognize_rgb_live_pytorch_anakin_2.0.2.bin",
                "", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
        faceLiveness.initModel(context, "liveness_rgb_anakin_2.0.2.bin",
                "liveness_nir_anakin_2.0.2.bin",
                "liveness_depth_anakin_2.0.2.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
        faceAttribute.initModel(context, "attribute_anakin_2.0.2.bin",
                "emotion_anakin_2.0.2.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
        mFacefeaturesImage.initMdoel(context, "detect_rgb_anakin_2.0.2.bin",
                "align_anakin_2.0.2.bin",
                "recognize_rgb_idcard_pytorch_anakin_2.0.2.bin",
                "recognize_rgb_live_pytorch_anakin_2.0.2.bin", new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        Log.e(String.valueOf(code), response);
                    }
                });
    }

    public FaceEnvironment getFaceEnvironmentConfig() {
        faceEnvironment.setMinFaceSize(160);
        faceEnvironment.setMaxFaceSize(-1);
        faceEnvironment.setDetectInterval(1000);
        faceEnvironment.setTrackInterval(500);
        faceEnvironment.setNoFaceSize(0.5f);
        faceEnvironment.setPitch(30);
        faceEnvironment.setYaw(30);
        faceEnvironment.setRoll(30);
        faceEnvironment.setCheckBlur(false);
        faceEnvironment.setOcclusion(false);
        faceEnvironment.setIllumination(false);
        faceEnvironment.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);
        return faceEnvironment;
    }

    public void check(Context context, String licenseKey, final FaceCallback faceCallback) {
        mFaceAuth.initLicense(context, licenseKey, licenseFileName, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                faceCallback.onResponse(code, response);
            }
        });
    }

    public void showActivation(final SdkInitListener sdkInitListener) {
        activation = new Activation(context);
        activation.show();
        activation.setActivationCallback(new Activation.ActivationCallback() {
            @Override
            public void callback(int code, String response, String licenseKey) {
                if (code == 0) {
                    Log.e("FaceSDK", "授权成功");
                    if (sdkInitListener != null) {
                        PreferencesUtil.putString("activate_key", licenseKey);
                        initStatus = SDK_SUCCESS;
                        dissDialog();
                        sdkInitListener.initSuccess();
                        initModel();
                    }
                    return;
                } else {
                    Log.e("FaceSDK", "授权失败:" + response);
                    if (sdkInitListener != null) {
                        dissDialog();
                        sdkInitListener.initFail(code, "授权失败:" + response);
                    }
                }
            }
        });

    }

    private void dissDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                activation.dismissActivationDialog();
            }
        });
    }

    private void toast(final int code, final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        code + "  " + text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public interface SdkInitListener {

        public void initStart();

        public void initSuccess();

        public void initFail(int errorCode, String msg);
    }

    public static void SaveTxt(String str) {
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "key.txt");//SD卡中的路径
            fw.flush();
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}