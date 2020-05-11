//package cn.cbdi.hunaninstrument.Activity_HLJYZB;
//
//
//import android.content.Intent;
//import android.gesture.GestureLibraries;
//import android.gesture.GestureLibrary;
//import android.gesture.GestureOverlayView;
//import android.gesture.Prediction;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.Gravity;
//import android.widget.Button;
//
//import com.blankj.utilcode.util.ActivityUtils;
//import com.blankj.utilcode.util.TimeUtils;
//import com.blankj.utilcode.util.ToastUtils;
//import com.jakewharton.rxbinding2.widget.RxTextView;
//import com.trello.rxlifecycle2.android.ActivityEvent;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import cn.cbdi.drv.card.ICardInfo;
//import cn.cbdi.hunaninstrument.Alert.Alarm;
//import cn.cbdi.hunaninstrument.Alert.Alert_IP;
//import cn.cbdi.hunaninstrument.Alert.Alert_Message;
//import cn.cbdi.hunaninstrument.Alert.Alert_Password;
//import cn.cbdi.hunaninstrument.Alert.Alert_Server;
//import cn.cbdi.hunaninstrument.AppInit;
//import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
//import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
//import cn.cbdi.hunaninstrument.Bean.SceneFingerprintUser;
//import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
//import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
//import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
//import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
//import cn.cbdi.hunaninstrument.EventBus.PassEvent;
//import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
//import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
//import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
//import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
//import cn.cbdi.hunaninstrument.R;
//import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
//import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
//import cn.cbdi.hunaninstrument.Tool.FileUtils;
//import cn.cbdi.hunaninstrument.Tool.MyObserver;
//import cn.cbdi.hunaninstrument.Tool.PersonType;
//import cn.cbdi.hunaninstrument.UI.SuperWindow2;
//import cn.cbdi.log.Lg;
//import io.reactivex.Observable;
//import io.reactivex.Observer;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.annotations.NonNull;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//
//import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
//import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify;
//import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify_non;
//
//public class MainActivity extends BaseActivity implements SuperWindow2.OptionTypeListener {
//
//    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    Intent intent;
//
//    Disposable checkChange;
//
//    Disposable disposableTips;
//
//    SceneFingerprintUser cg_User1 = new SceneFingerprintUser();
//
//    SceneFingerprintUser cg_User2 = new SceneFingerprintUser();
//
//    SceneFingerprintUser unknownUser = new SceneFingerprintUser();
//
//    private SuperWindow2 superWindow;
//
//    Alert_Message alert_message = new Alert_Message(this);
//
//    Alert_Server alert_server = new Alert_Server(this);
//
//    Alert_IP alert_ip = new Alert_IP(this);
//
//    Alert_Password alert_password = new Alert_Password(this);
//
//    @OnClick(R.id.lay_setting)
//    void option() {
//        alert_password.show();
//    }
//
//    @OnClick(R.id.lay_network)
//    void showMessage() {
//        alert_message.showMessage();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_newmain);
//        ButterKnife.bind(this);
//        EventBus.getDefault().register(this);
//        Log.e("key",config.getString("key"));
//
//        UIReady();
//        openService();
//        Lg.setIsOut(false);
//        network_state = false;
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
//
//    }
//
//
//
//    private void UIReady() {
//        setGestures();
//        disposableTips = RxTextView.textChanges(tv_info)
//                .debounce(60, TimeUnit.SECONDS)
//                .switchMap(charSequence -> Observable.just("等待用户操作..."))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((s) -> tv_info.setText(s));
//        alert_ip.IpviewInit();
//        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi)));
//        alert_password.PasswordViewInit(() -> {
//            superWindow = new SuperWindow2(MainActivity.this);
//            superWindow.setOptionTypeListener(MainActivity.this);
//            superWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
//        });
//        alert_message.messageInit();
//        syncTime();
//    }
//
//    @BindView(R.id.gestures_overlay)
//    GestureOverlayView gestures;
//    GestureLibrary mGestureLib;
//
//    private void setGestures() {
//        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
//        gestures.setGestureVisible(false);
//        gestures.addOnGesturePerformedListener((overlayView, gesture) -> {
//            ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
//            if (predictions.size() > 0) {
//                Prediction prediction = (Prediction) predictions.get(0);
//                // 匹配的手势
//                if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
//                    if (prediction.name.equals("setting")) {
//                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
//                        startActivity(intent);
//                    }
//                }
//            }
//        });
//        if (mGestureLib == null) {
//            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
//            mGestureLib.load();
//        }
//    }
//
//    void openService() {
//        intent = new Intent(MainActivity.this, AppInit.getInstrumentConfig().getServiceName());
//        startService(intent);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetTemHumEvent(TemHumEvent event) {
//        tv_temperature.setText(event.getTem() + "℃");
//        tv_humidity.setText(event.getHum() + "%");
//    }
//
//    boolean network_state;
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetNetworkEvent(NetworkEvent event) {
//        if (event.getNetwork_state()) {
//            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi));
//            network_state = true;
//        } else {
//            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi1));
//            network_state = false;
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetAlarmEvent(AlarmEvent event) {
//        Alarm.getInstance(this).messageAlarm("门磁打开报警，请检查门磁情况");
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetOpenDoorEvent(OpenDoorEvent event) {
//        OpenDoorRecord(event.getLegal());
//        if (checkChange != null) {
//            checkChange.dispose();
//        }
//        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
//            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetLockUpEvent(LockUpEvent event) {
//        Alarm.getInstance(this).setKnown(false);
//        tv_info.setText("仓库已重新上锁");
//        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj));
//        cg_User1 = new SceneFingerprintUser();
//        cg_User2 = new SceneFingerprintUser();
//        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        tv_daid.setText(config.getString("daid"));
//        fp.FaceIdentify_model();
//        cg_User1 = new SceneFingerprintUser();
//        cg_User2 = new SceneFingerprintUser();
//        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
//        tv_info.setText("等待用户操作...");
//        Observable.interval(0, 1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
//                .subscribe((l) -> tv_time.setText(formatter.format(new Date(System.currentTimeMillis()))));
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        SwitchPresenter.getInstance().WhiteLighrOff();
//        Alarm.getInstance(this).release();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//        stopService(intent);
//        disposableTips.dispose();
//    }
//
//    @Override
//    public void onSuperOptionType(Button view, int type) {
//        superWindow.dismiss();
//        if (type == 1) {
//            fp.PreviewCease(() -> ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getAddActivity()));
//        } else if (type == 2) {
//            alert_server.show();
//        } else if (type == 3) {
//            alert_ip.show();
//        }
//    }
//
//    @Override
//    public void onsetCardInfo(ICardInfo cardInfo) {
//
//    }
//
//    @Override
//    public void onsetCardImg(Bitmap bmp) {
//
//    }
//
//    @Override
//    public void onsetICCardInfo(ICardInfo cardInfo) {
//        if (alert_message.Showing()) {
//            alert_message.setICCardText(cardInfo.getUid());
//        }
//    }
//
//    @Override
//    public void onSetText(String Msg) {
//
//    }
//
//    Bitmap Scene_Bitmap;
//
//    Bitmap Scene_headphoto;
//
//    String faceScore;
//
//    String CompareScore;
//
//    @Override
//    public void onText(FacePresenter.FaceResultType resultType, String text) {
//        if (resultType.equals(Identify_non)) {
//            tv_info.setText(text);
//            sp.redLight();
//            fp.FaceGetAllView();
////            SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.H2);
//        } else if (resultType.equals(Identify)) {
//            faceScore = text;
//        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
//            sp.greenLight();
//            CompareScore = text;
//            tv_info.setText("仓管员" + cg_User2.getUser().getName() + "操作成功,仓库门已解锁");
//            DoorOpenOperation.getInstance().doNext();
//            EventBus.getDefault().post(new PassEvent());
//            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
//        }
//    }
//
//    @Override
//    public void onUser(FacePresenter.FaceResultType resultType, com.baidu.aip.entity.User user) {
//        if (resultType.equals(Identify)) {
//            try {
//                FingerprintUser fingerprintUser = mdaoSession.queryRaw(FingerprintUser.class, "where CARD_ID = '" + user.getUserId().toUpperCase() + "'").get(0);
//                if (fingerprintUser.getCourType().equals(PersonType.KuGuan)) {
//                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
//                        sp.greenLight();
//                        cg_User1.setUser(fingerprintUser);
//                        cg_User1.setScenePhoto(Scene_Bitmap);
//                        cg_User1.setFaceRecognition(Integer.parseInt(faceScore));
//                        cg_User1.setSceneHeadPhoto(Scene_headphoto);
//                        tv_info.setText("仓管员" + cg_User1.getUser().getName() + "操作成功,请继续仓管员操作");
//                        DoorOpenOperation.getInstance().doNext();
//                        Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
//                                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(new Observer<Long>() {
//                                    @Override
//                                    public void onSubscribe(Disposable d) {
//                                        checkChange = d;
//                                    }
//
//                                    @Override
//                                    public void onNext(Long aLong) {
//                                        checkRecord(String.valueOf(1));
//
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//
//                                    }
//
//                                    @Override
//                                    public void onComplete() {
//
//                                    }
//                                });
//
//                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
//                        if (!fingerprintUser.getCardId().equals(cg_User1.getUser().getCardId())) {
//                            if (checkChange != null) {
//                                checkChange.dispose();
//                            }
//                            cg_User2.setUser(fingerprintUser);
//                            cg_User2.setScenePhoto(Scene_Bitmap);
//                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
//                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
//                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(),false);
//                        } else {
//                            SwitchPresenter.getInstance().redLight();
//                            tv_info.setText("请不要连续输入相同的管理员信息");
//                        }
//                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
//                        tv_info.setText("仓库门已解锁");
//                    }
//                } else if (fingerprintUser.equals(PersonType.XunJian)) {
//                    if (checkChange != null) {
//                        checkChange.dispose();
//                    }
//                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
//                        Alarm.getInstance(this).messageAlarm("请注意，该人员为巡检员，无法正常解锁\n如需解锁还请两名仓管员到现场重新操作\n此次巡检记录已保存");
//                        SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.HA);
//                        SwitchPresenter.getInstance().redLight();
//                    }
//                    cg_User1.setUser(fingerprintUser);
//                    cg_User1.setScenePhoto(Scene_Bitmap);
//                    checkRecord(String.valueOf(2));
//                }
//            } catch (Exception e) {
//                ToastUtils.showLong(e.toString());
//            }
//        }
//    }
//
//    @Override
//    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bmp) {
//        if (resultType.equals(Identify)) {
//            Scene_Bitmap = bmp;
//        } else if (resultType.equals(FacePresenter.FaceResultType.headphoto)) {
//            Matrix matrix = new Matrix();
//            matrix.postScale(0.5f, 0.5f);
//            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//            Scene_headphoto = bmp;
//        } else if (resultType.equals(FacePresenter.FaceResultType.AllView)) {
//            Matrix matrix = new Matrix();
//            matrix.postScale(0.5f, 0.5f);
//            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//            unknownPeople(bmp);
//
//
//        }
//    }
//
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//
//
//    private void syncTime() {
//        RetrofitGenerator.getWyyConnectApi().withDataRs("getTime", config.getString("key"), null)
//                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io()).subscribe(new Observer<String>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(String s) {
//                String datetime = s;
//                AppInit.getMyManager().setTime(Integer.parseInt(datetime.substring(0, 4)),
//                        Integer.parseInt(datetime.substring(5, 7)),
//                        Integer.parseInt(datetime.substring(8, 10)),
//                        Integer.parseInt(datetime.substring(11, 13)),
//                        Integer.parseInt(datetime.substring(14, 16)),
//                        Integer.parseInt(datetime.substring(17, 19)));
//            }
//
//            @Override
//            public void onError(Throwable e) {
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
//    }
//
//
//    private void checkRecord(String type) {
//        SwitchPresenter.getInstance().OutD9(false);
//        final JSONObject checkRecordJson = new JSONObject();
//        try {
//            checkRecordJson.put("id", cg_User1.getUser().getCardId());
//            checkRecordJson.put("name", cg_User1.getUser().getName());
//            checkRecordJson.put("checkType", type);
//            checkRecordJson.put("datetime", TimeUtils.getNowString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        RetrofitGenerator.getWyyConnectApi().withDataRs("checkRecord", config.getString("key"), checkRecordJson.toString())
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MyObserver<String>(this) {
//
//                    @Override
//                    public void onNext(String s) {
//                        if (s.equals("true")) {
//                            if (cg_User1.getUser().getFingerprintId() != null) {
//                                tv_info.setText("巡检员" + cg_User1.getUser().getName() + "巡检成功,指纹ID为" + cg_User1.getUser().getFingerprintId());
//                            } else {
//                                tv_info.setText("巡检员" + cg_User1.getUser().getName() + "巡检成功");
//                            }
//                        } else if (s.equals("false")) {
//                            tv_info.setText("巡检失败");
//                        } else if (s.equals("dataErr")) {
//                            tv_info.setText("上传巡检数据失败");
//                        } else if (s.equals("dataErr")) {
//                            tv_info.setText("数据库操作有错");
//                        }
//
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        super.onError(e);
//                        tv_info.setText("无法连接到服务器");
//                        SwitchPresenter.getInstance().redLight();
//
//                        mdaoSession.insert(new ReUploadBean(null, "checkRecord", checkRecordJson.toString()));
//                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
//                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                        cg_User1 = new SceneFingerprintUser();
//                        cg_User2 = new SceneFingerprintUser();
//                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
//                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
//                        }
//                    }
//                });
//    }
//
//    private void unknownPeople(Bitmap bmp) {
//        final JSONObject unknownPeopleJson = new JSONObject();
//        try {
////            unknownPeopleJson.put("visitIdcard", unknownUser.getUser().getCardId());
////            unknownPeopleJson.put("visitName", unknownUser.getUser().getName());
//            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        RetrofitGenerator.getWyyConnectApi().withDataRs("saveVisit", config.getString("key"), unknownPeopleJson.toString())
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MyObserver<String>(this) {
//                    @Override
//                    public void onNext(String s) {
//                        if (s.equals("true")) {
//                            tv_info.setText("未知人员图片已上传");
//                        } else if (s.equals("false")) {
//                            tv_info.setText("访问人上传失败");
//                        } else if (s.equals("dataErr")) {
//                            tv_info.setText("上传访问人数据失败");
//                        } else if (s.equals("dbErr")) {
//                            tv_info.setText("数据库操作有错");
//                        }
//                        unknownUser = new SceneFingerprintUser();
//
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        super.onError(e);
//                        unknownUser = new SceneFingerprintUser();
//                        mdaoSession.insert(new ReUploadBean(null, "saveVisit", unknownPeopleJson.toString()));
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                    }
//                });
//    }
//
//
//    private void OpenDoorRecord(boolean leagl) {
//        final JSONObject OpenDoorJson = new JSONObject();
//        if (leagl) {
//            try {
//                OpenDoorJson.put("courIds1", cg_User1.getUser().getCourIds());
//                OpenDoorJson.put("courIds2", cg_User2.getUser().getCourIds());
//                OpenDoorJson.put("id1", cg_User1.getUser().getCardId());
//                OpenDoorJson.put("id2", cg_User2.getUser().getCardId());
//                OpenDoorJson.put("name1", cg_User1.getUser().getName());
//                OpenDoorJson.put("name2", cg_User2.getUser().getName());
//                OpenDoorJson.put("photo1", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
//                OpenDoorJson.put("photo2", FileUtils.bitmapToBase64(cg_User2.getScenePhoto()));
//                OpenDoorJson.put("datetime", TimeUtils.getNowString());
//                OpenDoorJson.put("state", "y");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            try {
//                OpenDoorJson.put("datetime", TimeUtils.getNowString());
//                OpenDoorJson.put("state", "n");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        RetrofitGenerator.getWyyConnectApi().withDataRs("openDoorRecord", config.getString("key"), OpenDoorJson.toString())
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MyObserver<String>(this) {
//                    @Override
//                    public void onNext(String s) {
//                        if (s.equals("true")) {
//                            try {
//                                if (OpenDoorJson.getString("state").equals("y")) {
//                                    tv_info.setText("正常开门数据上传成功");
//                                } else {
//                                    tv_info.setText("非法开门数据上传成功");
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else if (s.equals("false")) {
//                            tv_info.setText("开门数据上传失败");
//                        } else if (s.equals("dataErr")) {
//                            tv_info.setText("上传的json数据有错");
//                        } else if (s.equals("dbErr")) {
//                            tv_info.setText("数据库操作有错");
//                        }
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        super.onError(e);
//                        mdaoSession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorJson.toString()));
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                        cg_User1 = new SceneFingerprintUser();
//                        cg_User2 = new SceneFingerprintUser();
//                    }
//                });
//    }
//
//}