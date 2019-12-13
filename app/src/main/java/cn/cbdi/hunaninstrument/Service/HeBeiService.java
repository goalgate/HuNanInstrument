package cn.cbdi.hunaninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.User;
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
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.ReUploadWithBsBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.CloseDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.Door;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.Tool.Update.ApkUtils;
import cn.cbdi.hunaninstrument.Tool.Update.SignUtils;
import cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import cn.cbdi.hunaninstrument.greendao.ReUploadWithBsBeanDao;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.SIGN_MD5;

public class HeBeiService extends Service implements ISwitchView {

    private String TAG = HuNanService.class.getSimpleName();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat url_timeformatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");


    HashMap<String, String> paramsMap = new HashMap<String, String>();

    SwitchPresenter sp = SwitchPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();


    String Last_Value;

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    String THSwitchValue;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Md5", SignUtils.getSignMd5Str(AppInit.getInstance()));
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        mapInit();
        autoUpdate();
        CopySourceFile();
        syncData();
        reUpload();
        Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> reboot());
        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());
        Observable.interval(0, AppInit.getInstrumentConfig().getCheckOnlineTime(), TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .subscribe((l) -> checkOnline());
        if (AppInit.getInstrumentConfig().isTemHum()) {
            Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> sp.readHum());
            Observable.interval(10, 3600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                    .subscribe((l) -> StateRecord());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
    public void onTemHum(int temperature, int humidity) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
        if ((Math.abs(temperature - last_mTemperature) > 3 || Math.abs(temperature - last_mTemperature) > 10)) {
            last_mTemperature = temperature;
            last_mHumidity = humidity;
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
    }

    @Override
    public void onSwitchingText(String value) {
//        if (value.startsWith("AAAAAA")) {
//            if ((Last_Value == null || Last_Value.equals(""))) {
//                Last_Value = value;
//            }
//            if (!value.equals(Last_Value)) {
//                Last_Value = value;
//                if (Last_Value.equals("AAAAAA000000000000")) {
//                    if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
//                        Lock.getInstance().doNext();
//                        alarmRecord();
//                    }
//                }
//            }
//        }
        Lg.e("switchValue", value);
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
            if (value.startsWith("AAAAAA") && value.endsWith("000000")) {
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
                            final String closeDoorTime = formatter.format(new Date(System.currentTimeMillis()));
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

    private Handler handler = new Handler();

    private void reUpload() {
        ReUploadWithBsBeanDao reUploadWithBsBeanDao = mdaoSession.getReUploadWithBsBeanDao();
        List<ReUploadWithBsBean> list = reUploadWithBsBeanDao.queryBuilder().list();
        for (final ReUploadWithBsBean bean : list) {
            if (bean.getContent() != null) {
                if (bean.getType_patrol() != 0) {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid") + "&checkType=" + bean.getType_patrol(),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadWithBsBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                } else {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid"),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadWithBsBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                }
            } else {
                connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid"),
                        config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    if (response.startsWith("true")) {
                                        Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                        reUploadWithBsBeanDao.delete(bean);
                                    }
                                }
                            }
                        });
            }
        }
        ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list1 = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list1) {
            RetrofitGenerator.getHebeiApi().withDataRs(bean.getMethod(), config.getString("key"), bean.getContent())
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

    private void CopySourceFile() {
        if (config.getBoolean("CopySourceFileVer1", true)) {
            Observable.create((emitter) -> {
                emitter.onNext(ApkUtils.copyfile(
                        new File(ApkUtils.getSourceApkPath(HeBeiService.this, UpdateConstant.TEST_PACKAGENAME)),
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

    private void syncData() {
        syncFace();
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "updatePersion");
        map.put("persionType", String.valueOf(3));
        RetrofitGenerator.getHebeiApi().GeneralPersionInfo(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            Log.e("persionType = 3",s);
                            mdaoSession.getEmployerDao().deleteAll();
                            String[] idList = s.split("\\|");
                            if (idList.length > 0) {
                                for (String id : idList) {
                                    mdaoSession.insertOrReplace(new Employer(id, 3));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        map.put("persionType", String.valueOf(2));
                        RetrofitGenerator.getHebeiApi().GeneralPersionInfo(map)
                                .subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<String>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(String s) {
                                        try {
                                            Log.e("persionType = 2",s);
                                            String[] idList = s.split("\\|");
                                            if (idList.length > 0) {
                                                for (String id : idList) {
                                                    mdaoSession.insertOrReplace(new Employer(id, 2));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        map.put("persionType", String.valueOf(1));
                                        RetrofitGenerator.getHebeiApi().GeneralPersionInfo(map)
                                                .subscribeOn(Schedulers.io())
                                                .unsubscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<String>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onNext(String s) {
                                                        try {
                                                            Log.e("persionType = 1",s);
                                                            String[] idList = s.split("\\|");
                                                            if (idList.length > 0) {
                                                                for (String id : idList) {
                                                                    mdaoSession.insertOrReplace(new Employer(id, 1));
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        try {
                                                            List<Keeper> keeperList = mdaoSession.getKeeperDao().loadAll();
                                                            for (Keeper keeper : keeperList) {
                                                                try {
                                                                    mdaoSession.queryRaw(Employer.class, "where CARD_ID = '" + keeper.getCardID() + "'").get(0);
                                                                } catch (IndexOutOfBoundsException e) {
                                                                    mdaoSession.delete(keeper);
                                                                    FaceApi.getInstance().userDelete(keeper.getFaceUserId(), "1");
                                                                }
                                                            }
                                                        } catch (SQLiteException e) {
                                                            Lg.e(TAG, e.toString());
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void syncFace() {
        if (config.getBoolean("syncFace", true)) {
            RetrofitGenerator.getHebeiApi().getAllFace("getAllFace", paramsMap.get("daid"), paramsMap.get("pass"))
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String s) {
                            try {
                                JSONObject data = new JSONObject(s);
                                if (("true").equals(data.getString("result"))) {
                                    JSONArray jsonArray = data.getJSONArray("returnData");
                                    if (null != jsonArray && jsonArray.length() != 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject item = jsonArray.getJSONObject(i);
                                            Keeper keeper = new Keeper();
                                            keeper.setCardID(item.getString("cardID"));
                                            keeper.setName(item.getString("name"));
                                            byte[] faceFeature = Base64.decode(item.getString("feature"), Base64.DEFAULT);
                                            keeper.setFeature(faceFeature);
                                            mdaoSession.insertOrReplace(keeper);
                                            User user = new User();
                                            user.setUserId(keeper.getCardID());
                                            user.setUserInfo(keeper.getName());
                                            user.setGroupId("1");
                                            Feature feature = new Feature();
                                            feature.setGroupId("1");
                                            feature.setUserId(keeper.getCardID());
                                            feature.setFeature(keeper.getFeature());
                                            user.getFeatureList().add(feature);
                                            FaceApi.getInstance().userAdd(user);
                                        }
                                    }

                                }
                            } catch (Exception e) {
                                Lg.e(TAG, e.toString());
                            }

                        }


                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            FacePresenter.getInstance().FaceIdentifyReady();
                            config.put("syncFace", false);
                        }
                    });
        }
    }


    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
    }

    private void testNet() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "test");
        RetrofitGenerator.getHebeiApi().GeneralUpdata(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.startsWith("true")) {
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

    private void checkOnline() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "checkOnline");
        RetrofitGenerator.getHebeiApi().GeneralUpdata(map)
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

    private void CloseDoorRecord(String time) {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "closeDoor");
        map.put("time", time);
        RetrofitGenerator.getHebeiApi().GeneralUpdata(map)
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
                        mdaoSession.insert(new ReUploadWithBsBean(null, "dataType=closeDoor" + "&time=" + url_timeformatter.format(new Date(System.currentTimeMillis())), null, 0));

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "alarm");
        map.put("alarmType", String.valueOf(1));
        map.put("time", formatter.format(new Date(System.currentTimeMillis())));
        RetrofitGenerator.getHebeiApi().GeneralUpdata(map)
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
                        mdaoSession.insert(new ReUploadWithBsBean(null, "dataType=alarm&alarmType=1" + "&time=" + url_timeformatter.format(new Date(System.currentTimeMillis())), null, 0));
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void StateRecord() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "temHum");
        map.put("tem", String.valueOf(last_mTemperature));
        map.put("hum", String.valueOf(last_mHumidity));
        map.put("time", formatter.format(new Date(System.currentTimeMillis())));
        RetrofitGenerator.getHebeiApi().GeneralUpdata(map)
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
                    AppInit.getMyManager().reboot();
                    Log.e("信息提示", "关机了");
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
