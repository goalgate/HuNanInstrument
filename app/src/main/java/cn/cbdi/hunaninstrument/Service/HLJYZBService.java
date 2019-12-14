package cn.cbdi.hunaninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.baidu.aip.api.FaceApi;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cundong.utils.PatchUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;

import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.Door;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.Tool.Update.ApkUtils;
import cn.cbdi.hunaninstrument.Tool.Update.SignUtils;
import cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.SIGN_MD5;

public class HLJYZBService extends Service implements ISwitchView {
    SwitchPresenter sp = SwitchPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    String Last_Value;

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    String THSwitchValue;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        sp.SwitchPresenterSetView(this);
        autoUpdate();
        CopySourceFile();
        reUpload();
        Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> reboot());
        Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> sp.readHum());
        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());
        Observable.interval(10, 3600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> StateRecord());
    }

    private void CopySourceFile() {
        if (config.getBoolean("CopySourceFileVer1", true)) {
            Observable.create((emitter) -> {
                emitter.onNext(ApkUtils.copyfile(
                        new File(ApkUtils.getSourceApkPath(HLJYZBService.this, UpdateConstant.TEST_PACKAGENAME)),
                        new File(UpdateConstant.ORIGINAL_APK_PATH),
                        true));
            })
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> {
                        Boolean status = (boolean) l;
                        if (status) {
                            ToastUtils.showLong("源文件复制成功");
                            config.put("CopySourceFileVer1", false);
                            autoUpdate();
                        } else {
                            ToastUtils.showLong("源文件复制失败");
                        }

                    });
        }
    }


    private Handler handler = new Handler();

    private void autoUpdate() {
        File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
        if (config.getBoolean("CopySourceFileVer1", true)) {

        } else {
            File manual_patch = new File(MANUAL_PATH);
            if (manual_patch.exists()) {
                if (SignUtils.getSignMd5Str(AppInit.getInstance()).equals(SIGN_MD5)) {
                    ToastUtils.showLong("正在合成APK，请稍候");
                    new Thread(() -> {
                        int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, UpdateConstant.NEW_APK_PATH, UpdateConstant.MANUAL_PATH);
                        manual_patch.delete();
                        if (patchResult == 0) {
                            handler.post(() -> ApkUtils.installApk(AppInit.getContext(), UpdateConstant.NEW_APK_PATH));
                        } else {
                            handler.post(() -> ToastUtils.showLong("apk合成失败"));
                        }

                    }).start();
                } else {
                    ToastUtils.showLong("旧有的MD5值与设定MD5值不一");
                }
            } else {
                new ServerConnectionUtil().download("http://sbgl.wxhxp.cn:8050/daServer/updateRLCJQ.do?ver=" + AppUtils.getAppVersionName() + "&url=" + config.getString("ServerId") + "&daid=" + config.getString("daid") + "&updateType=apk&faceid=" + FileIOUtils.readFile2String(key),
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
                                                            int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, UpdateConstant.NEW_APK_PATH, UpdateConstant.PATCH_PATH);
                                                            if (patchResult == 0) {
                                                                handler.post(() -> ApkUtils.installApk(AppInit.getContext(), UpdateConstant.NEW_APK_PATH));
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
    }


    private void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list) {
            RetrofitGenerator.getWyyConnectApi().withDataRs(bean.getMethod(), config.getString("key"), bean.getContent())
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(Schedulers.single())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull String s) {
                            Log.e("信息提示", bean.getMethod());
                            reUploadBeanDao.delete(bean);


                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("信息提示error", bean.getMethod());

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        Lock.getInstance().setState(Lock.LockState.STATE_Unlock);
        Lock.getInstance().doNext();
        Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        unlock_noOpen = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                        sp.buzz(SwitchImpl.Hex.H2);
                        EventBus.getDefault().post(new LockUpEvent());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSwitchingText(String value) {
        if ((Last_Value == null || Last_Value.equals(""))) {
            if (value.startsWith("AAAAAA")) {
                Last_Value = value;
                if (value.equals("AAAAAA000000000000")) {
                    Door.getInstance().setMdoorState(Door.DoorState.State_Open);
                    Door.getInstance().doNext();
                    alarmRecord();
                }
            }
        } else {
            if (value.startsWith("AAAAAA")) {
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        if (Door.getInstance().getMdoorState().equals(Door.DoorState.State_Close)) {
                            Door.getInstance().setMdoorState(Door.DoorState.State_Open);
                            Door.getInstance().doNext();
                            if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                                alarmRecord();
                            }
                        }
                        if (unlock_noOpen != null) {
                            unlock_noOpen.dispose();
                        }
                        if (rx_delay != null) {
                            rx_delay.dispose();
                        }
                    } else if (Last_Value.equals("AAAAAA000001000000")) {
                        if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Unlock)) {
                            final String closeDoorTime = TimeUtils.getNowString();
                            Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            rx_delay = d;
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                                            Door.getInstance().setMdoorState(Door.DoorState.State_Close);
                                            sp.buzz(SwitchImpl.Hex.H2);
                                            if (unlock_noOpen != null) {
                                                unlock_noOpen.dispose();
                                            }
                                            CloseDoorRecord(closeDoorTime);
                                            EventBus.getDefault().post(new LockUpEvent());
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {

                                        }
                                    });
                        } else {
                            CloseDoorRecord(TimeUtils.getNowString());
                            Door.getInstance().setMdoorState(Door.DoorState.State_Close);
                        }
                    }
                }
            } else {
                if (value.startsWith("BBBBBB") && value.endsWith("C1EF")) {
                    THSwitchValue = value;
                }
            }
        }
    }

    @Override
    public void onTemHum(int temperature, int humidity) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
        if ((Math.abs(temperature - last_mTemperature) > 5 || Math.abs(temperature - last_mTemperature) > 10)) {
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
    }


    private void CloseDoorRecord(String time) {
        final JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRs("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "closeDoorRecord", CloseDoorRecordJson.toString()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        final JSONObject alarmRecordJson = new JSONObject();
        try {
            alarmRecordJson.put("datetime", TimeUtils.getNowString());
            alarmRecordJson.put("alarmType", String.valueOf(1));
            alarmRecordJson.put("alarmValue", String.valueOf(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RetrofitGenerator.getWyyConnectApi().withDataRs("alarmRecord", config.getString("key"), alarmRecordJson.toString())
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull String s) {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                mdaoSession.insert(new ReUploadBean(null, "alarmRecord", alarmRecordJson.toString()));
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void StateRecord() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("datetime", TimeUtils.getNowString());
            jsonObject.put("switching", THSwitchValue);
            jsonObject.put("temperature", last_mTemperature);
            jsonObject.put("humidity", last_mHumidity);
            if (Door.getInstance().getMdoorState().equals(Door.DoorState.State_Open)) {
                jsonObject.put("state", "0");
            } else {
                jsonObject.put("state", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRs("stateRecord", config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void testNet() {
        RetrofitGenerator.getWyyConnectApi().withDataRs("testNet", config.getString("key"), null)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            EventBus.getDefault().post(new NetworkEvent(true));
                        } else {
                            EventBus.getDefault().post(new NetworkEvent(false));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new NetworkEvent(false));

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void reboot() {
        long daySpan = 24 * 60 * 60 * 1000 * 1;
        // 规定的每天时间，某时刻运行
        int randomTime = new Random().nextInt(50) + 10;
        String pattern = "yyyy-MM-dd '03:" + randomTime + ":00'";
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Log.e("rebootTime", pattern);
        // 首次运行时间
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime()) {
                startTime = new Date(startTime.getTime() + daySpan);
            } else if (startTime.getHours() == new Date().getHours()) {
                startTime = new Date(startTime.getTime() + daySpan);
            }
            Log.e("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));
            Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 要执行的代码
                    Log.e("reboot", "reboot");
                    AppInit.getMyManager().reboot();
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}