package cn.cbdi.hunaninstrument.Activity_HLJYZB;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.view.IFingerPrintView;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class AddActvity extends Activity {
    private String TAG = AddActvity.class.getSimpleName();

    SPUtils config = SPUtils.getInstance("config");

    boolean commitable = false;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    FingerprintUser user = new FingerprintUser();

    String alertTitle = "请选择接下来的操作";



    @BindView(R.id.btn_commit)
    Button btn_commit;

    @BindView(R.id.et_idcard)
    EditText et_idcard;

    @BindView(R.id.btn_query)
    Button query;

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    int count = 3;

    @OnClick(R.id.iv_userPic)
    void choose() {
        if (user.getCardId() != null) {
            if (user.getFaceUserId() != null) {
                FaceApi.getInstance().userDelete(user.getFaceUserId(), "1");
            }
            FaceDetect(user.getCardId(), user.getName());
        }
    }

    @OnClick(R.id.btn_query)
    void queryPerson() {
        if (!TextUtils.isEmpty(et_idcard.getText().toString().toUpperCase())) {
            RetrofitGenerator.getWyyConnectApi().queryPersonInfo("queryPersonInfo", config.getString("key"), et_idcard.getText().toString().toUpperCase())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                        new TypeToken<HashMap<String, String>>() {
                                        }.getType());
                                if (infoMap.size() > 0) {
//                                    if (infoMap.get("status").equals(String.valueOf(0))) {
                                    if (infoMap.get("status").equals(String.valueOf(0)) || infoMap.get("status").equals(String.valueOf(1))) {
                                        user = new FingerprintUser();
                                        user.setCardId(et_idcard.getText().toString().toUpperCase());
                                        user.setName(infoMap.get("name"));
                                        user.setCourIds(infoMap.get("courIds"));
                                        user.setCourType(infoMap.get("courType"));
                                        query.setText(infoMap.get("name") + ",欢迎您！");
                                        query.setClickable(false);
                                        Observable.interval(0, 1, TimeUnit.SECONDS)
                                                .take(count + 1)
                                                .map(new Function<Long, Long>() {
                                                    @Override
                                                    public Long apply(@NonNull Long aLong) throws Exception {
                                                        return count - aLong;
                                                    }
                                                })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<Long>() {
                                                    @Override
                                                    public void onSubscribe(@NonNull Disposable d) {

                                                    }

                                                    @Override
                                                    public void onNext(@NonNull Long aLong) {
                                                        ToastUtils.showLong(aLong + "秒后进入人脸识别界面");
                                                    }

                                                    @Override
                                                    public void onError(@NonNull Throwable e) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        iv_userPic.setClickable(true);
                                                        FaceDetect(user.getCardId(), user.getName());
                                                    }
                                                });
                                    } else {
                                        Alarm.getInstance(AddActvity.this).messageAlarm("您的身份有误，如有疑问请联系客服处理");
                                    }
                                } else {
                                    Alarm.getInstance(AddActvity.this).messageAlarm("系统未能查询到该人员信息，如有疑问请联系客服处理");
                                }
                            } catch (IOException e) {
                                Lg.e(TAG, e.toString());
                            } catch (Exception e) {
                                Lg.e(TAG, e.toString());
                            }
                        }
                    });
        } else {
            ToastUtils.showLong("身份证号为空，请输入身份证号");
        }
    }

    @OnClick(R.id.btn_commit)
    void commit() {
        if (commitable) {
            AppInit.getInstance().getDaoSession().insert(user);
            user = new FingerprintUser();
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else {
            Alarm.getInstance(AddActvity.this).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, AddActvity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    alertTitle = "请选择接下来的操作";
                    commitable = false;
                    query.setClickable(true);
                    query.setText("校验人员信息");
                    et_idcard.setHint("请填写身份证信息");
                    et_idcard.setText(null);
                    user = new FingerprintUser();
                    iv_userPic.setClickable(false);
                    if (user.getFaceUserId() != null) {
                        FaceApi.getInstance().userDelete(user.getFaceUserId(), "1");
                    }
                    iv_userPic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.user_icon));
                } else {
                    if (user.getFaceUserId() != null) {
                        FaceApi.getInstance().userDelete(user.getFaceUserId(), "1");
                    }
                    finish();
                }
            }
        }).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person2);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        iv_userPic.setClickable(false);
}

    void FaceDetect(String cardId, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("cardId", cardId);
        bundle.putString("name", name);
        ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + ".Activity_HLJYZB.FaceDetectActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this).release();
        EventBus.getDefault().unregister(this);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        iv_userPic.setImageBitmap(event.getBitmap());
        user.setFaceUserId(event.getUserId());
        commitable = true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        final JSONObject OpenDoorjson = new JSONObject();
        try {
            OpenDoorjson.put("datetime", TimeUtils.getNowString());
            OpenDoorjson.put("state", "n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getWyyConnectApi().withDataRr("openDoorRecord", config.getString("key"), OpenDoorjson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorjson.toString()));

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    public void onBackPressed() {

    }
}
