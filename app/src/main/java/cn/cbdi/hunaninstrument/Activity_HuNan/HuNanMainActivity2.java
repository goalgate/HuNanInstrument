package cn.cbdi.hunaninstrument.Activity_HuNan;

import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;

import com.baidu.aip.entity.User;
import com.bigkoo.alertview.OnDismissListener;
import com.blankj.utilcode.util.ActivityUtils;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Alert.Alert_IP;
import cn.cbdi.hunaninstrument.Alert.Alert_Message;
import cn.cbdi.hunaninstrument.Alert.Alert_Password;
import cn.cbdi.hunaninstrument.Alert.Alert_Server;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.ISwitching;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.UI.NormalWindow;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.AllView;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify_non;

public class HuNanMainActivity2 extends BaseActivity implements NormalWindow.OptionTypeListener {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Disposable disposableTips;

    Disposable checkChange;

    Intent intent;

    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);

    Alert_Password alert_password = new Alert_Password(this);

    private NormalWindow normalWindow;

    SceneKeeper cg_User1 = new SceneKeeper();

    SceneKeeper cg_User2 = new SceneKeeper();

    SceneKeeper unknownUser = new SceneKeeper();

    Bitmap Scene_Bitmap;

    Bitmap Scene_headphoto;

    Bitmap headphoto;

    String faceScore;

    String CompareScore;

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    GestureLibrary mGestureLib;

    @OnClick(R.id.lay_setting)
    void setting() {
        alert_password.show();
    }

    @OnClick(R.id.lay_network)
    void showMessage() {
        alert_message.showMessage();
    }

    @OnClick(R.id.lay_lock)
    void ds(){
//        EventBus.getDefault().post(new PassEvent());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        UIReady();
        openService();
//        syncTime();

    }

    private void UIReady() {
        setGestures();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(15, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.iv_wifi)));

        alert_message.messageInit();
        alert_password.PasswordViewInit(() -> {
            normalWindow = new NormalWindow(HuNanMainActivity2.this);
            normalWindow.setOptionTypeListener(HuNanMainActivity2.this);
            normalWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                    Gravity.CENTER, 0, 0);
        });
    }

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
        intent = new Intent(HuNanMainActivity2.this, AppInit.getInstrumentConfig().getServiceName());
        startService(intent);
    }


    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        fp.FaceIdentify_model();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj));
//        sp.relay(ISwitching.Relay.relay_D5, ISwitching.Hex.H0, true);
        tv_info.setText("等待用户操作...");
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe((l) -> tv_time.setText(formatter.format(new Date(System.currentTimeMillis()))));
    }

    @Override
    public void onPause() {
        super.onPause();
//        sp.relay(ISwitching.Relay.relay_D5, ISwitching.Hex.H0, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        stopService(intent);
    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.cardId());
            return;
        }
        try {
            mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
//            try {
//                mdaosession.queryRaw(Keeper.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
//                tv_info.setText("等待人脸比对结果返回");
//                MediaHelper.play(MediaHelper.Text.waiting);
//                fp.FaceIdentify();
//            } catch (IndexOutOfBoundsException e) {
//                tv_info.setText("该人员尚未登记人脸信息");
//                sp.redLight();
//            }
        } catch (IndexOutOfBoundsException e) {
            RetrofitGenerator.getHnmbyApi().queryPersonInfo("queryPersion", config.getString("key"), cardInfo.cardId())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String s = responseBody.string().toString();
                                if (s.equals("false")) {
                                    Keeper inside_keeper = new Keeper();
                                    inside_keeper.setName(cardInfo.name());
                                    inside_keeper.setCardID(cardInfo.cardId());
                                    unknownUser.setKeeper(inside_keeper);
                                    fp.FaceGetAllView();
                                    tv_info.setText("系统查无此人");
                                    MediaHelper.play(MediaHelper.Text.man_non);
                                    sp.redLight();
                                } else if (s.startsWith("true")) {
                                    String type = s.substring(5, s.length());
                                    mdaosession.insertOrReplace(new Employer(cardInfo.cardId(), Integer.valueOf(type)));
                                    tv_info.setText("该人员尚未登记人脸信息");
                                    sp.redLight();

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                tv_info.setText("Exception");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            Keeper inside_keeper = new Keeper();
                            inside_keeper.setName(cardInfo.name());
                            inside_keeper.setCardID(cardInfo.cardId());
                            unknownUser.setKeeper(inside_keeper);
                            fp.FaceGetAllView();
                            tv_info.setText("系统查无此人");
                            MediaHelper.play(MediaHelper.Text.man_non);
                            sp.redLight();
                        }
                    });
        }
    }


    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.getUid());
            return;
        }
        if (cardInfo.getUid().equals(AppInit.The_IC_UID)) {
            fp.PreviewCease(() -> ActivityUtils.startActivity(getPackageName(), getPackageName() + ".Activity_HuNan" +
                    ".HuNanRegActivity"));
        } else {
            ToastUtils.showShort("非法IC卡");
            sp.redLight();
        }
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }


    @Override
    public void onSetText(String Msg) {
        if(Msg.startsWith("SAM")){
            ToastUtils.showLong(Msg);
        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType.equals(Identify)) {
            Scene_Bitmap = bitmap;
        } else if (resultType.equals(FacePresenter.FaceResultType.headphoto)) {
            Scene_headphoto = bitmap;
        } else if (resultType.equals(AllView)) {
            unknownPeople(bitmap);
        }

    }

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_non)) {
            tv_info.setText(text);
            sp.redLight();
        } else if (resultType.equals(Identify)) {
            faceScore = text;
        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
            CompareScore = text;
            SwitchPresenter.getInstance().buzz(ISwitching.Hex.HA);
            sp.greenLight();
            tv_info.setText("信息处理完毕,仓库门已解锁");
            DoorOpenOperation.getInstance().doNext();
            EventBus.getDefault().post(new PassEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj1));
        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(Identify)) {
            try {
                Keeper keeper = mdaosession.queryRaw(Keeper.class,
                        "where CARD_ID = '" + user.getUserId().toUpperCase() + "'").get(0);
                Employer employer = mdaosession.queryRaw(Employer.class,
                        "where CARD_ID = '" + user.getUserId().toUpperCase() + "'").get(0);
                if (employer.getType() == 1) {
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                        cg_User1.setKeeper(keeper);
                        cg_User1.setScenePhoto(Scene_Bitmap);
                        cg_User1.setFaceRecognition(Integer.parseInt(faceScore));
                        cg_User1.setSceneHeadPhoto(Scene_headphoto);
                        tv_info.setText("仓管员" + cg_User1.getKeeper().getName() + "操作成功,请继续仓管员操作");
                        sp.greenLight();
                        DoorOpenOperation.getInstance().doNext();
                        Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .compose(HuNanMainActivity2.this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        checkChange = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        checkRecord(String.valueOf(2));
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        if (!keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            cg_User2.setKeeper(keeper);
                            cg_User2.setScenePhoto(Scene_Bitmap);
                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                            tv_info.setText("仓管员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(),false);
                        } else {
                            sp.redLight();
                            tv_info.setText("请不要连续输入相同的管理员信息");
                            return;
                        }
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                        tv_info.setText("仓库门已解锁");
                    }
                } else if (employer.getType() == 2) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        if (!keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
                            sp.greenLight();
                            cg_User2.setKeeper(keeper);
                            cg_User2.setScenePhoto(Scene_Bitmap);
                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                            tv_info.setText("巡检员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(),false);
                        } else {
                            sp.redLight();
                            tv_info.setText("请不要连续输入相同的管理员信息");
                            return;
                        }
                    } else {
                        cg_User1.setKeeper(keeper);
                        cg_User1.setScenePhoto(Scene_Bitmap);
                        checkRecord("2");
                    }
                } else if (employer.getType() == 3) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    cg_User1.setKeeper(keeper);
                    cg_User1.setScenePhoto(Scene_Bitmap);
                    checkRecord("3");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onOptionType(Button view, int type) {
        normalWindow.dismiss();
        if (type == 1) {
            alert_server.show();
        } else if (type == 2) {
            alert_ip.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi));
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi1));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        tv_info.setText("门磁打开报警,请检查门磁情况");
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
        tv_info.setText("仓库已重新上锁");
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj));
//        sp.relay(ISwitching.Relay.relay_D5, ISwitching.Hex.H0, true);

        cg_User1 = new SceneKeeper();
        cg_User2 = new SceneKeeper();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
    }

    private void syncTime() {
        RetrofitGenerator.getHnmbyApi().withDataRs("getTime", config.getString("key"), null)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                try {
                    String datetime = s;
                    AppInit.getMyManager().setTime(Integer.parseInt(datetime.substring(0, 4)),
                            Integer.parseInt(datetime.substring(5, 7)),
                            Integer.parseInt(datetime.substring(8, 10)),
                            Integer.parseInt(datetime.substring(11, 13)),
                            Integer.parseInt(datetime.substring(14, 16)),
                            Integer.parseInt(datetime.substring(17, 19)));
                } catch (Exception e) {
                    Lg.e("Exception", e.toString());
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void checkRecord(String type) {
        SwitchPresenter.getInstance().OutD9(false);
        final JSONObject checkRecordJson = new JSONObject();
        try {
            checkRecordJson.put("id", cg_User1.getKeeper().getCardID());
            checkRecordJson.put("name", cg_User1.getKeeper().getName());
            checkRecordJson.put("photos", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
            checkRecordJson.put("checkType", type);
            checkRecordJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("saveVisit", config.getString("key"), checkRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {
                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            tv_info.setText("巡检员" + cg_User1.getKeeper().getName() + "巡检成功");
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
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadBean(null, "saveVisit", checkRecordJson.toString()));
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }

                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneKeeper();
                        cg_User2 = new SceneKeeper();
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }

                    }
                });
    }

    private void unknownPeople(Bitmap bmp) {
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("visitIdcard", unknownUser.getKeeper().getCardID());
            unknownPeopleJson.put("visitName", unknownUser.getKeeper().getName());
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
            unknownPeopleJson.put("photoSfz", FileUtils.bitmapToBase64(headphoto));
            unknownPeopleJson.put("datetime", TimeUtils.getNowString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("persionRecord", config.getString("key"),
                unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            tv_info.setText("访问人" + unknownUser.getKeeper().getName() + "数据上传成功");
                        } else if (s.equals("false")) {
                            tv_info.setText("访问人上传失败");
                        } else if (s.equals("dataErr")) {
                            tv_info.setText("上传访问人数据失败");
                        } else if (s.equals("dbErr")) {
                            tv_info.setText("数据库操作有错");
                        }
                        unknownUser = new SceneKeeper();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        unknownUser = new SceneKeeper();
                        mdaosession.insert(new ReUploadBean(null, "persionRecord", unknownPeopleJson.toString()));
                    }
                });
    }

    private void OpenDoorRecord(boolean leagl) {
        final JSONObject OpenDoorJson = new JSONObject();
        if (leagl) {
            try {
                OpenDoorJson.put("id1", cg_User1.getKeeper().getCardID());
                OpenDoorJson.put("id2", cg_User2.getKeeper().getCardID());
                OpenDoorJson.put("name1", cg_User1.getKeeper().getName());
                OpenDoorJson.put("name2", cg_User2.getKeeper().getName());
                OpenDoorJson.put("photo1", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
                OpenDoorJson.put("photo2", FileUtils.bitmapToBase64(cg_User2.getScenePhoto()));
                OpenDoorJson.put("faceRecognition1", cg_User1.getFaceRecognition());
                OpenDoorJson.put("faceRecognition2", cg_User2.getFaceRecognition());
                OpenDoorJson.put("faceRecognition3", CompareScore);
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "y");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return;
            //            try {
            //                OpenDoorJson.put("datetime", TimeUtils.getNowString());
            //                OpenDoorJson.put("state", "n");
            //            } catch (JSONException e) {
            //                e.printStackTrace();
            //            }
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("openDoorRecord", config.getString("key"), OpenDoorJson.toString())
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
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorJson.toString()));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneKeeper();
                        cg_User2 = new SceneKeeper();
                    }
                });
    }
}
