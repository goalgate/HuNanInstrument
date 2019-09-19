package cn.cbdi.hunaninstrument.Activity_WYY;


import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.Alert.Alert_IP;
import cn.cbdi.hunaninstrument.Alert.Alert_Message;
import cn.cbdi.hunaninstrument.Alert.Alert_Password;
import cn.cbdi.hunaninstrument.Alert.Alert_Server;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.SceneFingerprintUser;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.DESX;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.PersonType;
import cn.cbdi.hunaninstrument.UI.SuperWindow;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify_non;

public class WYYMainActivity extends BaseActivity implements SuperWindow.OptionTypeListener {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Intent intent;

    Disposable checkChange;

    Disposable disposableTips;

    SceneFingerprintUser cg_User1 = new SceneFingerprintUser();

    SceneFingerprintUser cg_User2 = new SceneFingerprintUser();

    SceneFingerprintUser unknownUser = new SceneFingerprintUser();

    private SuperWindow superWindow;

    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);

    Alert_Password alert_password = new Alert_Password(this);

    @OnClick(R.id.lay_setting)
    void option() {
        alert_password.show();
    }

    @OnClick(R.id.lay_network)
    void showMessage() {
        alert_message.showMessage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        UIReady();
        openService();
        Lg.setIsOut(false);
        network_state = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);

    }

    private void UIReady() {
        setGestures();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(60, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi)));
        alert_password.PasswordViewInit(() -> {
            superWindow = new SuperWindow(WYYMainActivity.this);
            superWindow.setOptionTypeListener(WYYMainActivity.this);
            superWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        });
        alert_message.messageInit();
        syncTime();
    }

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;
    GestureLibrary mGestureLib;

    private void setGestures() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener((overlayView, gesture) -> {
            ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
            if (predictions.size() > 0) {
                Prediction prediction = (Prediction) predictions.get(0);
                // 匹配的手势
                if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                    if (prediction.name.equals("setting")) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    void openService() {
        intent = new Intent(WYYMainActivity.this, AppInit.getInstrumentConfig().getServiceName());
        startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }

    boolean network_state;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi));
            network_state = true;
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi1));
            network_state = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        Alarm.getInstance(this).messageAlarm("门磁打开报警，请检查门磁情况");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        OpenDoorRecord(event.getLegal());
        if (checkChange != null) {
            checkChange.dispose();
        }
        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetLockUpEvent(LockUpEvent event) {
        Alarm.getInstance(this).setKnown(false);
        tv_info.setText("仓库已重新上锁");
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj));
        cg_User1 = new SceneFingerprintUser();
        cg_User2 = new SceneFingerprintUser();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
    }

    @Override
    public void onResume() {
        super.onResume();
        fp.FaceIdentify_model();
        cg_User1 = new SceneFingerprintUser();
        cg_User2 = new SceneFingerprintUser();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        tv_info.setText("等待用户操作...");
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe((l) -> tv_time.setText(formatter.format(new Date(System.currentTimeMillis()))));

    }

    @Override
    public void onPause() {
        super.onPause();
        Alarm.getInstance(this).release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(intent);
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        disposableTips.dispose();
    }

    @Override
    public void onSuperOptionType(Button view, int type) {
        superWindow.dismiss();
        if (type == 1) {
            fp.PreviewCease(() -> ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getAddActivity()));
        } else if (type == 2) {
            alert_server.show();
        } else if (type == 3) {
            ViewGroup extView2 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.inputdevid_form, null);
            final EditText et_devid = (EditText) extView2.findViewById(R.id.devid_input);
            new AlertView("设备信息同步", null, "取消", new String[]{"确定"}, null, WYYMainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        if (TextUtils.isEmpty(et_devid.getText().toString())) {
                            ToastUtils.showLong("您的输入为空请重试");
                        } else {
                            fpp.fpCancel(true);
                            equipment_sync(et_devid.getText().toString());
                        }
                    }
                }
            }).addExtView(extView2).show();
        } else if (type == 4) {
            alert_ip.show();
        } else if (type == 5) {
            ViewGroup deleteView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.delete_person_form, null);
            final EditText et_idcard = (EditText) deleteView.findViewById(R.id.idcard_input);
            final EditText et_finger = (EditText) deleteView.findViewById(R.id.et_finger);
            new AlertView("删除人员指纹信息", null, "取消", new String[]{"确定"}, null, WYYMainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        if (TextUtils.isEmpty(et_idcard.getText().toString()) || TextUtils.isEmpty(et_finger.getText().toString())) {
                            ToastUtils.showLong("您的输入为空请重试");
                        } else {
                            deletePerson(et_idcard.getText().toString().toUpperCase(), et_finger.getText().toString());
                        }
                    }
                }
            }).addExtView(deleteView).show();
        }
    }

    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {

    }

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.getUid());
        }
    }

    @Override
    public void onSetText(String Msg) {

    }

    @Override
    public void onSetImg(Bitmap bmp) {

    }

    @Override
    public void onText(String msg) {
        if ("请确认指纹是否已登记".equals(msg)) {
            tv_info.setText("请确认指纹是否已登记,再重试");
        } else if ("松开手指".equals(msg)) {
            tv_info.setText(msg);
        }
    }

    @Override
    public void onFpSucc(final String msg) {
        Alarm.getInstance(this).doorAlarm(new Alarm.doorCallback() {
            @Override
            public void onTextBack(String msg) {
                tv_info.setText(msg);
            }

            @Override
            public void onSucc() {
                Alarm.getInstance(WYYMainActivity.this).networkAlarm(network_state, new Alarm.networkCallback() {
                    @Override
                    public void onIsKnown() {
                        loadMessage(msg.substring(3, msg.length()));
                    }

                    @Override
                    public void onTextBack(String msg) {
                        Alarm.getInstance(WYYMainActivity.this).setKnown(true);
                        tv_info.setText(msg);
                    }
                });
            }
        });
    }

    private void loadMessage(String sp) {
        try {
            FingerprintUser fingerprintUser = mdaoSession.queryRaw(FingerprintUser.class,
                    "where FINGERPRINT_ID = '" + sp + "'").get(0);
            if (fingerprintUser.getCourType().equals(PersonType.KuGuan)) {
                if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                    cg_User1.setUser(fingerprintUser);
                    fp.FaceGetAllView();
                } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                    if (!fingerprintUser.getCardId().equals(cg_User1.getUser().getCardId())) {
                        cg_User2.setUser(fingerprintUser);
                        fp.FaceGetAllView();
                        EventBus.getDefault().post(new PassEvent());
                        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
                    } else {
                        tv_info.setText("请不要连续输入相同的管理员信息");
                    }
                } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                    tv_info.setText("仓库门已解锁");
                }
            } else if (fingerprintUser.getCourType().equals(PersonType.XunJian)) {
                if (checkChange != null) {
                    checkChange.dispose();
                }
                if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                    Alarm.getInstance(this).messageAlarm("请注意，该人员为巡检员，无法正常解锁\n如需解锁还请两名仓管员到现场重新操作\n此次巡检记录已保存");
                    SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.HA);
                }
                cg_User1.setUser(fingerprintUser);
                checkRecord(String.valueOf(2));
            } else {
                unknownUser.setUser(fingerprintUser);
                fp.FaceGetAllView();
            }
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }


    }

    Bitmap Scene_Bitmap;

    Bitmap Scene_headphoto;

    String faceScore;

    String CompareScore;

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_non)) {
            tv_info.setText(text);
            sp.redLight();
//            SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.H2);
        } else if (resultType.equals(Identify)) {
            faceScore = text;
        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
            CompareScore = text;
            tv_info.setText("仓管员" + cg_User2.getUser().getName() + "操作成功,仓库门已解锁");
            DoorOpenOperation.getInstance().doNext();
            EventBus.getDefault().post(new PassEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, com.baidu.aip.entity.User user) {
        if (resultType.equals(Identify)) {
            try {
                FingerprintUser fingerprintUser = mdaoSession.queryRaw(FingerprintUser.class, "where CARD_ID = '" + user.getUserId().toUpperCase() + "'").get(0);
                if (fingerprintUser.getCourType().equals(PersonType.KuGuan)) {
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                        sp.greenLight();
                        cg_User1.setUser(fingerprintUser);
                        cg_User1.setScenePhoto(Scene_Bitmap);
                        cg_User1.setFaceRecognition(Integer.parseInt(faceScore));
                        cg_User1.setSceneHeadPhoto(Scene_headphoto);
                        tv_info.setText("仓管员" + cg_User1.getUser().getName() + "操作成功,请继续仓管员操作");
                        DoorOpenOperation.getInstance().doNext();
                        Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        checkChange = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        checkRecord(String.valueOf(1));

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });

                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        if (!fingerprintUser.getCardId().equals(cg_User1.getUser().getCardId())) {
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            sp.greenLight();
                            cg_User2.setUser(fingerprintUser);
                            cg_User2.setScenePhoto(Scene_Bitmap);
                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(),false);
                        } else {
                            tv_info.setText("请不要连续输入相同的管理员信息");
                        }
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                        tv_info.setText("仓库门已解锁");
                    }
                } else if (fingerprintUser.equals(PersonType.XunJian)) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        Alarm.getInstance(this).messageAlarm("请注意，该人员为巡检员，无法正常解锁\n如需解锁还请两名仓管员到现场重新操作\n此次巡检记录已保存");
                        SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.HA);
                    }

                    cg_User1.setUser(fingerprintUser);
                    cg_User1.setScenePhoto(Scene_Bitmap);
                    checkRecord(String.valueOf(2));
                }
            } catch (Exception e) {
                ToastUtils.showLong(e.toString());
            }
        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bmp) {
        if (resultType.equals(Identify)) {
            Scene_Bitmap = bmp;
        } else if (resultType.equals(FacePresenter.FaceResultType.headphoto)) {
            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            Scene_headphoto = bmp;
        } else if (resultType.equals(FacePresenter.FaceResultType.AllView)) {
            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            if (unknownUser.getUser() != null) {
                unknownPeople(bmp);
            }
            if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                cg_User1.setScenePhoto(bmp);
                DoorOpenOperation.getInstance().doNext();
                tv_info.setText(String.format("管理员%s打卡成功,指纹ID:%s\n请继续管理员操作", cg_User1.getUser().getName(), cg_User1.getUser().getFingerprintId()));
                Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                        .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                checkChange = d;
                            }

                            @Override
                            public void onNext(Long aLong) {
                                checkRecord(String.valueOf(1));

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                if (checkChange != null) {
                    checkChange.dispose();
                }
                DoorOpenOperation.getInstance().doNext();
                tv_info.setText(String.format("管理员%s打卡成功,指纹ID:%s\n设备已撤防", cg_User2.getUser().getName(), cg_User2.getUser().getFingerprintId()));
                cg_User2.setScenePhoto(bmp);
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void syncTime() {
        RetrofitGenerator.getWyyConnectApi().withDataRs("getTime", config.getString("key"), null)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                String datetime = s;
                AppInit.getMyManager().setTime(Integer.parseInt(datetime.substring(0, 4)),
                        Integer.parseInt(datetime.substring(5, 7)),
                        Integer.parseInt(datetime.substring(8, 10)),
                        Integer.parseInt(datetime.substring(11, 13)),
                        Integer.parseInt(datetime.substring(14, 16)),
                        Integer.parseInt(datetime.substring(17, 19)));
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void deletePerson(String idcard, final String fingerId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idcard);
            jsonObject.put("fingerprintId", fingerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRr("deleteFinger", config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this, true) {

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String s = responseBody.string();
                            if (s.equals("true")) {
                                fpp.fpCancel(true);
                                Observable.timer(2, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe((l) -> {
                                            fpp.fpRemoveTmpl(fingerId);
                                            fpp.fpIdentify();
                                        });
                                ToastUtils.showLong("删除成功");
                            } else if (s.equals("false")) {
                                ToastUtils.showLong("删除失败");
                            } else if (s.equals("dataErr")) {
                                ToastUtils.showLong("服务出错");
                            } else if (s.equals("dbErr")) {
                                ToastUtils.showLong("数据库出错");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });
    }

    private void checkRecord(String type) {
        SwitchPresenter.getInstance().OutD9(false);
        final JSONObject checkRecordJson = new JSONObject();
        try {
            checkRecordJson.put("id", cg_User1.getUser().getCardId());
            checkRecordJson.put("name", cg_User1.getUser().getName());
            checkRecordJson.put("checkType", type);
            checkRecordJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRs("checkRecord", config.getString("key"), checkRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            if (cg_User1.getUser().getFingerprintId() != null) {
                                tv_info.setText("巡检员" + cg_User1.getUser().getName() + "巡检成功,指纹ID为" + cg_User1.getUser().getFingerprintId());
                            } else {
                                tv_info.setText("巡检员" + cg_User1.getUser().getName() + "巡检成功");
                            }
                        } else if (s.equals("false")) {
                            tv_info.setText("巡检失败");
                        } else if (s.equals("dataErr")) {
                            tv_info.setText("上传巡检数据失败");
                        } else if (s.equals("dataErr")) {
                            tv_info.setText("数据库操作有错");
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                        mdaoSession.insert(new ReUploadBean(null, "checkRecord", checkRecordJson.toString()));
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneFingerprintUser();
                        cg_User2 = new SceneFingerprintUser();
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }
                    }
                });
    }

    private void unknownPeople(Bitmap bmp) {
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("visitIdcard", unknownUser.getUser().getCardId());
            unknownPeopleJson.put("visitName", unknownUser.getUser().getName());
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRs("saveVisit", config.getString("key"), unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {
                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            tv_info.setText("访问人" + unknownUser.getUser().getName() + "数据上传成功,指纹号为" + unknownUser.getUser().getFingerprintId());
                        } else if (s.equals("false")) {
                            tv_info.setText("访问人上传失败");
                        } else if (s.equals("dataErr")) {
                            tv_info.setText("上传访问人数据失败");
                        } else if (s.equals("dbErr")) {
                            tv_info.setText("数据库操作有错");
                        }
                        unknownUser = new SceneFingerprintUser();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        unknownUser = new SceneFingerprintUser();
                        mdaoSession.insert(new ReUploadBean(null, "saveVisit", unknownPeopleJson.toString()));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    private void equipment_sync(final String old_devid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("oldDaid", old_devid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRr("searchFinger", config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this, true) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string().toString());
                            if (("true").equals(jsonObject.getString("result"))) {
                                final JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if (null != jsonArray && jsonArray.length() != 0) {
                                    fpp.fpRemoveAll();
                                    Observable.timer(1, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((l) -> {
                                                try {
                                                    mdaoSession.deleteAll(Employer.class);
                                                    mdaoSession.deleteAll(FingerprintUser.class);
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject item = jsonArray.getJSONObject(i);
                                                        fpp.fpDownTemplate(item.getString("pfpIds"), item.getString("fingerTemp"));
                                                        FingerprintUser fingerprintUser = new FingerprintUser();
                                                        fingerprintUser.setCourIds(item.getString("personIds"));
                                                        fingerprintUser.setName(item.getString("name"));
                                                        fingerprintUser.setCardId(item.getString("idcard"));
                                                        fingerprintUser.setCourType(item.getString("courType"));
                                                        mdaoSession.insert(fingerprintUser);

                                                        Employer employer = new Employer();
                                                        employer.setCardID(item.getString("idcard"));
                                                        employer.setType(Integer.parseInt(item.getString("courType")));
                                                        mdaoSession.insertOrReplace(employer);
                                                    }
                                                } catch (Exception e) {
                                                    ToastUtils.showLong(e.toString());
                                                }


                                                JSONObject jsonKey = new JSONObject();
                                                try {
                                                    jsonKey.put("daid", old_devid);
                                                    jsonKey.put("check", DESX.encrypt(old_devid));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                config.put("daid", old_devid);
                                                config.put("key", DESX.encrypt(jsonKey.toString()));
                                                ToastUtils.showLong("设备数据更新成功");
                                                fpp.fpIdentify();

                                            });
//
                                } else {
                                    ToastUtils.showLong("该设备号无人员数据");
                                    fpp.fpIdentify();

                                }
                            } else {
                                ToastUtils.showLong("设备号有误");
                                fpp.fpIdentify();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        fpp.fpIdentify();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    private void OpenDoorRecord(boolean leagl) {
        final JSONObject OpenDoorJson = new JSONObject();
        if (leagl) {
            try {
                OpenDoorJson.put("courIds1", cg_User1.getUser().getCourIds());
                OpenDoorJson.put("courIds2", cg_User2.getUser().getCourIds());
                OpenDoorJson.put("id1", cg_User1.getUser().getCardId());
                OpenDoorJson.put("id2", cg_User2.getUser().getCardId());
                OpenDoorJson.put("name1", cg_User1.getUser().getName());
                OpenDoorJson.put("name2", cg_User2.getUser().getName());
                OpenDoorJson.put("photo1", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
                OpenDoorJson.put("photo2", FileUtils.bitmapToBase64(cg_User2.getScenePhoto()));
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "y");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            try {
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RetrofitGenerator.getWyyConnectApi().withDataRs("openDoorRecord", config.getString("key"), OpenDoorJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {
                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            try {
                                if (OpenDoorJson.getString("state").equals("y")) {
                                    tv_info.setText("正常开门数据上传成功");
                                } else {
                                    tv_info.setText("非法开门数据上传成功");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else if (s.equals("false")) {
                            tv_info.setText("开门数据上传失败");
                        } else if (s.equals("dataErr")) {
                            tv_info.setText("上传的json数据有错");
                        } else if (s.equals("dbErr")) {
                            tv_info.setText("数据库操作有错");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        mdaoSession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorJson.toString()));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneFingerprintUser();
                        cg_User2 = new SceneFingerprintUser();
                    }
                });
    }

}