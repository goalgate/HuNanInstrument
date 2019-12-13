package cn.cbdi.hunaninstrument.Activity_Attendance;


import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
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
import java.util.List;
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
import cn.cbdi.hunaninstrument.Bean.AttendanceScene;
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
import cn.cbdi.hunaninstrument.greendao.AttendanceSceneDao;
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

public class MainActivity extends BaseActivity implements SuperWindow.OptionTypeListener {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Intent intent;
    Disposable disposableTips;

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
                .debounce(5, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi)));
        alert_password.PasswordViewInit(() -> {
            superWindow = new SuperWindow(MainActivity.this);
            superWindow.setOptionTypeListener(MainActivity.this);
            superWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        });
        alert_message.messageInit();
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
        intent = new Intent(MainActivity.this, AppInit.getInstrumentConfig().getServiceName());
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

    @Override
    public void onResume() {
        super.onResume();
        tv_daid.setText(config.getString("daid"));
        Observable.timer(3, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    fp.FaceIdentify_model();
                });
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
            ToastUtils.showLong("设备同步功能尚待开发");
        } else if (type == 4) {
            alert_ip.show();
        } else if (type == 5) {
            fp.PreviewCease(() -> ActivityUtils.startActivity(getPackageName(), getPackageName() + ".Activity_Attendance.SceneActivity"));
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
            sp.redLight();
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
                Alarm.getInstance(MainActivity.this).networkAlarm(network_state, new Alarm.networkCallback() {
                    @Override
                    public void onIsKnown() {
                    }

                    @Override
                    public void onTextBack(String msg) {
                        Alarm.getInstance(MainActivity.this).setKnown(true);
                        tv_info.setText(msg);
                    }
                });
            }
        });
    }


    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_non)) {
            sp.redLight();
            tv_info.setText(text);
        } else if (resultType.equals(Identify)) {
            sp.greenLight();
            tv_info.setText(text + "打卡成功");
        }

    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, com.baidu.aip.entity.User user) {

    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bmp) {
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}