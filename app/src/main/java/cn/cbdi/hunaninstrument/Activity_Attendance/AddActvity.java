package cn.cbdi.hunaninstrument.Activity_Attendance;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.aip.api.FaceApi;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AddActvity extends Activity {
    private String TAG = AddActvity.class.getSimpleName();

    SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    FingerprintUser user = new FingerprintUser();


    @BindView(R.id.et_idcard)
    EditText et_idcard;

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    int count = 3;

    @OnClick(R.id.iv_userPic)
    void choose() {
        if (!TextUtils.isEmpty(et_idcard.getText().toString().toUpperCase())) {
            FaceApi.getInstance().userDelete(user.getCardId(),"1");
            user.setName(et_idcard.getText().toString());
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
                            FaceDetect(user.getCardId(), user.getName());
                        }
                    });
        } else {
            ToastUtils.showLong("您的输入为空，请输入编号");
        }
    }



    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView("请选择接下来的操作", null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, AddActvity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    user.setCardId(UUID.randomUUID().toString());
                    et_idcard.setText(null);
                    iv_userPic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.user_icon));
                } else {
                    finish();
                }
            }
        }).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_attendance_add);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        user.setCardId(UUID.randomUUID().toString());

    }

    void FaceDetect(String cardId, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("cardId", cardId);
        bundle.putString("name", name);
        ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + ".Activity_Attendance.FaceDetectActivity");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        iv_userPic.setImageBitmap(event.getBitmap());
        user.setFaceUserId(event.getUserId());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this).release();
        EventBus.getDefault().unregister(this);

    }

    public void onBackPressed() {

    }
}
