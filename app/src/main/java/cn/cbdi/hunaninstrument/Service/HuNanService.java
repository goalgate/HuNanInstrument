package cn.cbdi.hunaninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.User;
import com.baidu.liantian.ac.F;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
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
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbdi.hunaninstrument.Tool.Update.UpdateConstant.SIGN_MD5;

public class HuNanService extends Service implements ISwitchView {

    private String TAG = HuNanService.class.getSimpleName();

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
        Log.e("Md5", SignUtils.getSignMd5Str(AppInit.getInstance()));
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        autoUpdate();
        CopySourceFile();
        syncData();
        reUpload();
        Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> reboot());
        Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> sp.readHum());
        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());
        Observable.interval(10, 600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> StateRecord());
    }


    int count = 0;
    StringBuffer logMen;

    private void getPic() {
        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                try {
                    User user = FaceApi.getInstance().getUserInfo("1", employer.getCardID());
                    logMen.append(user.getUserInfo() + "、");
                    count++;
                } catch (NullPointerException e) {
                    RetrofitGenerator.getHnmbyApi()
                            .recentPic("recentPic", config.getString("key"), employer.getCardID())
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
                                        JSONObject jsonObject = new JSONObject(responseBody.string());
                                        String ps = jsonObject.getString("result");
                                        String name = jsonObject.getString("name");
//                                        String name = "我问问";
                                        if (!TextUtils.isEmpty(ps)) {
                                            Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                            FacePresenter.getInstance().FaceRegInBackGround(new CardInfoBean(employer.getCardID(), name),
                                                    bitmap, null);
                                        }
                                    } catch (Exception e) {
                                        Lg.e(TAG, e.toString());
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    count++;


                                }

                                @Override
                                public void onComplete() {
                                    count++;
                                    if (count == employers.size()) {
                                        FacePresenter.getInstance().FaceIdentifyReady();
                                    }

                                }
                            });
                }
            }
            if (count == employers.size()) {
                if (logMen.length() > 0) {
                    logMen.deleteCharAt(logMen.length() - 1);
                    ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕");
                }

            }
        }

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
//                            CloseDoorRecord(TimeUtils.getNowString());
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
        if ((Math.abs(temperature - last_mTemperature) > 3 || Math.abs(temperature - last_mTemperature) > 10)) {
            last_mTemperature = temperature;
            last_mHumidity = humidity;
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
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

    private void CopySourceFile() {
        if (config.getBoolean("CopySourceFileVer1", true)) {
            Observable.create((emitter) -> {
                emitter.onNext(ApkUtils.copyfile(
                        new File(ApkUtils.getSourceApkPath(HuNanService.this, UpdateConstant.TEST_PACKAGENAME)),
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
            RetrofitGenerator.getHnmbyApi().withDataRs(bean.getMethod(), config.getString("key"), bean.getContent())
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


    private void syncData() {
//        mdaoSession.insertOrReplace(new Employer("441302199308100538", 1));
//        mdaoSession.insertOrReplace(new Employer("440982199104204312", 1));
//        mdaoSession.insertOrReplace(new Employer("440923198104247334", 1));
//        mdaoSession.insertOrReplace(new Employer("412325197011264532", 1));
//        mdaoSession.insertOrReplace(new Employer("450211197801011312", 1));

//        syncFace();
        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 3)
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
                            mdaoSession.getEmployerDao().deleteAll();
                            if (s.equals("no")) {

                            } else {
                                String[] idList = s.split("\\|");
                                if (idList.length > 0) {
                                    for (String id : idList) {
                                        if (!id.equals("")) {
                                            mdaoSession.insertOrReplace(new Employer(id, 3));
                                        }
                                    }
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
                        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 2)
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
                                            if (s.equals("no")) {

                                            } else {
                                                String[] idList = s.split("\\|");
                                                if (idList.length > 0) {
                                                    for (String id : idList) {
                                                        if (!id.equals("")) {
                                                            mdaoSession.insertOrReplace(new Employer(id, 2));
                                                        }
                                                    }
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
                                        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 1)
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
                                                            if (s.equals("no")) {

                                                            } else {
                                                                String[] idList = s.split("\\|");
                                                                if (idList.length > 0) {
                                                                    for (String id : idList) {
                                                                        if (!id.equals("")) {
                                                                            mdaoSession.insertOrReplace(new Employer(id, 1));
                                                                        }
                                                                    }
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
                                                        getPic();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void syncFace() {
        if (config.getBoolean("syncFace", true)) {
            RetrofitGenerator.getHnmbyApi().withDataRr("getAllFace", config.getString("key"), null)
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
                                JSONObject data = new JSONObject(responseBody.string());
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

    private void testNet() {
        RetrofitGenerator.getHnmbyApi().withDataRs("testNet", config.getString("key"), null)
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

    private void CloseDoorRecord(String time) {
        final JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
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
        RetrofitGenerator.getHnmbyApi().withDataRs("stateRecord", config.getString("key"), jsonObject.toString())
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

        RetrofitGenerator.getHnmbyApi().withDataRs("alarmRecord", config.getString("key"), alarmRecordJson.toString())
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
