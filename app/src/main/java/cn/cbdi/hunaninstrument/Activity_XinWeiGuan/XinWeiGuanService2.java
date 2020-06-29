package cn.cbdi.hunaninstrument.Activity_XinWeiGuan;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Config.WenZhouConfig;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.FingerPrintIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.RebootEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.view.ISwitchView;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.Door;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.Tool.Update.ApkUtils;
import cn.cbdi.hunaninstrument.Tool.Update.SignUtils;
import cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.State.DoorState.Door.DoorState.State_Close;
import static cn.cbdi.hunaninstrument.State.DoorState.Door.DoorState.State_Open;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.NEW_APK_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.SIGN_MD5;

public class XinWeiGuanService2 extends Service implements ISwitchView {
    private String TAG = XinWeiGuanService2.class.getSimpleName();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat url_timeformatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    SwitchPresenter sp = SwitchPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    private SPUtils fingerprintBooksRevert = SPUtils.getInstance("fingerprintBooksRevert");

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
        CopySourceFile();
//        autoUpdate();
        Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> syncData());
        reUpload();
        Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> reboot());
        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());

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
        if (!AppInit.getInstrumentConfig().isHongWai()) {

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
        if (AppInit.getInstrumentConfig().isHongWai()) {
            if (value.startsWith("AAAAAA")) {
                if ((Last_Value == null || Last_Value.equals(""))) {
                    Last_Value = value;
                }
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                            Lock.getInstance().doNext();
                            alarmRecord();
                        }
                    }
                }
            }
        } else {
            Lg.e("switchValue", value);
            if ((Last_Value == null || Last_Value.equals(""))) {
                if (value.startsWith("AAAAAA")) {
                    Last_Value = value;
                    if (value.equals("AAAAAA000000000000")) {
                        Door.getInstance().setMdoorState(State_Open);
                        Door.getInstance().doNext();
                        alarmRecord();
                    }
                }
            } else {
                if (value.startsWith("AAAAAA") && value.endsWith("000000")) {
                    if (!value.equals(Last_Value)) {
                        Last_Value = value;
                        if (Last_Value.equals("AAAAAA000000000000")) {
                            if (Door.getInstance().getMdoorState().equals(State_Close)) {
                                Door.getInstance().setMdoorState(State_Open);
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
                                                Door.getInstance().setMdoorState(State_Close);
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
                                Door.getInstance().setMdoorState(State_Close);
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

    }

    private Handler handler = new Handler();

    private void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list) {
            RetrofitGenerator.getXinWeiGuanApi().withDataRr(bean.getMethod(), config.getString("key"), bean.getContent())
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(Schedulers.single())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull ResponseBody responseBody) {
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
            try {
                File file = new File(NEW_APK_PATH);
                if (file.exists()) {
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            File manual_patch = new File(MANUAL_PATH);
            if (manual_patch.exists()) {
                if (SignUtils.getSignMd5Str(AppInit.getInstance()).equals(SIGN_MD5)) {
                    ToastUtils.showLong("正在合成APK，请稍候");
                    new Thread(() -> {
                        int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, NEW_APK_PATH, UpdateConstant.MANUAL_PATH);
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
    }

    private void CopySourceFile() {
        if (AppUtils.getAppVersionName().equals("1.1") ||
                (AppUtils.getAppVersionName().equals("1.4") &&
                        AppInit.getInstrumentConfig().getClass().getName().equals(WenZhouConfig.class.getName()))) {
            Observable.create((emitter) -> {
                emitter.onNext(ApkUtils.copyfile(
                        new File(ApkUtils.getSourceApkPath(XinWeiGuanService2.this, UpdateConstant.TEST_PACKAGENAME)),
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
        } else {
            config.put("CopySourceFileVer1", false);
        }
    }

    private void syncData() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("personType", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("updatePerson", config.getString("key"), obj.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String s = ParsingTool.extractMainContent(responseBody);

                            mdaoSession.getEmployerDao().deleteAll();
                            String[] idList = s.split("\\|");
                            if (idList.length > 0) {
                                for (String id : idList) {
                                    mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 2));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new FingerPrintIdentityEvent());
                    }

                    @Override
                    public void onComplete() {
                        try {
                            obj.put("personType", "1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RetrofitGenerator.getXinWeiGuanApi().withDataRr("updatePerson", config.getString("key"), obj.toString())
                                .subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<ResponseBody>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(ResponseBody responseBody) {
                                        try {
                                            String s = ParsingTool.extractMainContent(responseBody);

                                            String[] idList = s.split("\\|");
                                            if (idList.length > 0) {
                                                for (String id : idList) {
                                                    mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 1));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        EventBus.getDefault().post(new FingerPrintIdentityEvent());
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
                                                    FingerPrintPresenter.getInstance().fpRemoveTmpl(fingerprintBooksRevert.getString(keeper.getFaceUserId()));
                                                }
                                            }
                                        } catch (SQLiteException e) {
                                            Lg.e(TAG, e.toString());
                                        }
                                        getPic();
                                        EventBus.getDefault().post(new FingerPrintIdentityEvent());

                                    }
                                });
                    }
                });
    }

    int count = 0;

    StringBuffer logMen;



    private void getPic() {
        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getXinWeiGuanApi().queryPersonInfo("recentPic", config.getString("key"), employer.getCardID())
                        .subscribeOn(Schedulers.single())
                        .unsubscribeOn(Schedulers.single())
                        .observeOn(Schedulers.single())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    count++;
                                    String s = ParsingTool.extractMainContent(responseBody);
                                    JSONObject jsonObject = new JSONObject(s);
                                    String result = jsonObject.getString("result");
                                    if (result.equals("true")) {
                                        String ps = jsonObject.getString("returnPic");
                                        String name = jsonObject.getString("personName");
                                        try {
                                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + employer.getCardID().toUpperCase() + "'").get(0);
                                            if (!TextUtils.isEmpty(ps) && keeper.getHeadphoto().length() != ps.length()) {
                                                Log.e("ps_len", String.valueOf(ps.length()));
                                                Log.e("keeper_len", String.valueOf(keeper.getHeadphoto().replaceAll("\r|\n", "").length()));
                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                                FacePresenter.getInstance().FaceRegInBackGround(new CardInfoBean(employer.getCardID(), name), bitmap, ps);

                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            if (!TextUtils.isEmpty(ps)) {
                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                                FacePresenter.getInstance().FaceRegInBackGround(new CardInfoBean(employer.getCardID(), name), bitmap, ps);

                                            }
                                        }
                                    }
                                    if (count == employers.size()) {
                                        FacePresenter.getInstance().FaceIdentifyReady();
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);

                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG,logMen.toString());

                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG,logMen.toString());

                                        }
                                    }
                                } catch (Exception e) {
                                    Lg.e(TAG, e.toString());
                                    if (count == employers.size()) {
                                        FacePresenter.getInstance().FaceIdentifyReady();
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);
                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG,logMen.toString());
                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG,logMen.toString());

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                count++;
                                if (count == employers.size()) {
                                    FacePresenter.getInstance().FaceIdentifyReady();
                                    List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                    if (keeperList.size() > 0) {
                                        Set<String> list = new HashSet<>();
                                        for (Keeper keeper : keeperList) {
                                            list.add(keeper.getName());
                                        }
                                        for (String name : list) {
                                            logMen.append(name + "、");
                                        }
                                        logMen.deleteCharAt(logMen.length() - 1);
                                        handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                        Log.e(TAG,logMen.toString());

                                    } else {
                                        handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                        Log.e(TAG,logMen.toString());

                                    }
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        } else {
            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
        }
    }

    private void testNet() {
        RetrofitGenerator.getXinWeiGuanApi().noData("testNet", config.getString("key"))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String s = ParsingTool.extractMainContent(responseBody);
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


    private void CloseDoorRecord(String time) {
        JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
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
            alarmRecordJson.put("datetime", TimeUtils.getNowString());// 报警时间
            alarmRecordJson.put("alarmType", String.valueOf(1));  //报警类型
            alarmRecordJson.put("alarmValue", String.valueOf(0));  //报警值
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("alarmRecord", config.getString("key"), alarmRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
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
            if (Door.getInstance().getMdoorState().equals(State_Open)) {
                jsonObject.put("state", "0");
            } else {
                jsonObject.put("state", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("stateRecord", config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

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
                    autoUpdate();
                    syncData();
                    reUpload();
//                    EventBus.getDefault().post(new RebootEvent());
                    Log.e("信息提示", "关机了");
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
