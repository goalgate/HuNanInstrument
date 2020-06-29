package cn.cbdi.hunaninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cundong.utils.PatchUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.EventBus.RebootEvent;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;

import cn.cbdi.hunaninstrument.Tool.Update.ApkUtils;
import cn.cbdi.hunaninstrument.Tool.Update.SignUtils;
import cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.NEW_APK_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.SIGN_MD5;

public class UpdateService extends Service {

    private SPUtils config = SPUtils.getInstance("config");

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    private Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        Log.e("UpdateService", "自动更新已启动");
        autoUpdate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }


    private void autoUpdate() {
        File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
        File file = new File(NEW_APK_PATH);
        if (file.exists()) {
            file.delete();
        }
        File manual_patch = new File(MANUAL_PATH);
        if (manual_patch.exists()) {
            if (SignUtils.getSignMd5Str(AppInit.getInstance()).equals(SIGN_MD5)) {
                ToastUtils.showLong("正在合成APK，请稍候");
                new Thread(() -> {
                    int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, NEW_APK_PATH, MANUAL_PATH);
                    manual_patch.delete();
                    if (patchResult == 0) {
                        handler.post(() -> ApkUtils.installApk(AppInit.getContext(), NEW_APK_PATH));
                    } else {
                        handler.post(() -> ToastUtils.showLong("apk合成失败"));
                    }

                }).start();
            } else {
                ToastUtils.showLong("旧有的MD5值与设定MD5值不一");
            }

        } else {
            connectionUtil.download("http://sbgl.wxhxp.cn:8050/daServer/updateRLCJQ.do?ver=" + AppUtils.getAppVersionName() + "&url=" + config.getString("ServerId") + "&daid=" + config.getString("daid") + "&updateType=apk&faceid=" + FileIOUtils.readFile2String(key),
                    config.getString("ServerId"),
                    (s) -> {
                        if (s != null) {
                            if (s.equals("true")) {
                                if (SignUtils.getSignMd5Str(AppInit.getInstance()).equals(SIGN_MD5)) {
                                    ToastUtils.showLong("正在安装APK，请稍候");
                                    AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
                                } else {
                                    ToastUtils.showLong("旧有的MD5值与设定MD5值不一");
                                }
                            }
                        } else {
                            new ApkUtils().download("http://sbgl.wxhxp.cn:8050/daServer/updateRLCJQ.do?ver=" + AppUtils.getAppVersionName() + "&url=" + config.getString("ServerId") + "&daid=" + config.getString("daid") + "&updateType=patch&faceid=" + FileIOUtils.readFile2String(key),
                                    (result) -> {
                                        if (result != null) {
                                            if (result.equals("true")) {
                                                if (SignUtils.getSignMd5Str(AppInit.getInstance()).equals(SIGN_MD5)) {
                                                    ToastUtils.showLong("正在合成APK，请稍候");
                                                    new Thread(() -> {
                                                        int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, NEW_APK_PATH, UpdateConstant.PATCH_PATH);
                                                        if (patchResult == 0) {
                                                            handler.post(() -> ApkUtils.installApk(AppInit.getContext(), NEW_APK_PATH));
                                                        } else {
                                                            handler.post(() -> ToastUtils.showLong("apk合成失败"));
                                                        }
                                                    }).start();
                                                } else {
                                                    ToastUtils.showLong("旧有的MD5值与设定MD5值不一");
                                                }
                                            }
                                        }
                                    });
                        }
                    });
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRebootEvent(RebootEvent event) {
        Log.e("信息提示", "自动升级");
        autoUpdate();
    }
}
