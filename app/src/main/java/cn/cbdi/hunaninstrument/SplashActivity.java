package cn.cbdi.hunaninstrument;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.baidu.aip.manager.FaceSDKManager;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.hunaninstrument.Service.UpdateService;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.AssetsUtils;
import cn.cbdi.hunaninstrument.Tool.DESX;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.NetInfo;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SplashActivity extends RxActivity {

    String TAG = SplashActivity.class.getSimpleName();

    private SPUtils config = SPUtils.getInstance("config");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_splash);
        ActivityCollector.addActivity(this);

        Intent intent = new Intent(SplashActivity.this, UpdateService.class);
        startService(intent);

        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            copyToClipboard(AppInit.getContext(), FileIOUtils.readFile2String(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaHelper.mediaOpen();

        FacePresenter.getInstance().FaceInit(this, new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.e(TAG, "sdk init start");
            }

            @Override
            public void initSuccess() {
                Log.e(TAG, "sdk init success");
                Observable.timer(3, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(SplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe((l) -> {
                            if (AppInit.getInstrumentConfig().getDev_prefix().startsWith("800")) {
                                if (config.getBoolean("firstStart", true)) {
                                    ActivityUtils.startActivity(getPackageName(), getPackageName() + ".StartActivity");
                                    return;
                                } else {
                                    ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                                    return;
                                }
                            }
                            if (AppInit.getInstrumentConfig().fingerprint()) {
                                FingerPrintPresenter.getInstance().fpInit();
                                FingerPrintPresenter.getInstance().fpOpen();
                            }

                            if (config.getBoolean("firstStart", true)) {
                                JSONObject jsonKey = new JSONObject();
                                try {
                                    jsonKey.put("daid", new NetInfo().getMacId());
                                    jsonKey.put("check", DESX.encrypt(new NetInfo().getMacId()));
//                                    jsonKey.put("daid", "024147-127051-090034");
//                                    jsonKey.put("check", DESX.encrypt("024147-127051-090034"));
//                                    jsonKey.put("daid", "024147-127050-205074");
//                                    jsonKey.put("check", DESX.encrypt("024147-127050-205074"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                config.put("firstStart", false);
                                config.put("daid", new NetInfo().getMacId());
//                                config.put("daid", "024147-127051-090034");
//                                config.put("daid", "024147-127050-205074");
                                config.put("key", DESX.encrypt(jsonKey.toString()));
                                config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
                                AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib", "wltlib");
                            }

                            if (AppInit.getInstrumentConfig()
                                    .DoorMonitorChosen() && config.getBoolean("SetDoorMonitor", true)) {
                                new AlertView("选择门感应方式", null, null, new String[]{"门磁", "红外对射"}, null, SplashActivity.this, AlertView.Style.Alert, (o, position) -> {
                                    if (position == 0) {
                                        config.put("isHongWai", false);
                                        AppInit.getInstrumentConfig().setHongWai(false);
                                    } else if (position == 1) {
                                        config.put("isHongWai", true);
                                        AppInit.getInstrumentConfig().setHongWai(true);
                                    }
                                    config.put("SetDoorMonitor", false);
                                    ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                                }).show();
                            } else {
                                if (AppInit.getInstrumentConfig().DoorMonitorChosen()) {
                                    if (config.getBoolean("isHongWai", true)) {
                                        AppInit.getInstrumentConfig().setHongWai(true);
                                    } else {
                                        AppInit.getInstrumentConfig().setHongWai(false);
                                    }
                                }
                                Observable.timer(3, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .compose(SplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                        .subscribe(new Observer<Long>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onNext(@NonNull Long aLong) {
                                                Log.e("key", config.getString("key"));
                                                ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {

                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        });
                            }
                        });
            }

            @Override
            public void initFail(int errorCode, String msg) {
                runOnUiThread(() -> ToastUtils.showLong("加载人脸算法失败,请联网重试"));
            }
        });
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}