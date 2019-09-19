package cn.cbdi.hunaninstrument.Activity_Hebei;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.cbdi.hunaninstrument.Activity_HuNan.HuNanMainActivity;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.ReUploadWithBsBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.FaceImpl2.FEATURE_DATAS_UNREADY;

public abstract class BaseActivity extends RxActivity implements IFaceView, IIDCardView {

    protected String TAG = HuNanMainActivity.class.getSimpleName();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    SPUtils config = SPUtils.getInstance("config");

    SceneKeeper cg_User1 = new SceneKeeper();

    SceneKeeper cg_User2 = new SceneKeeper();

    SceneKeeper unknownUser = new SceneKeeper();

    Disposable checkChange;

    @BindView(R.id.tv_info)
    public TextView tv_info;

    @BindView(R.id.iv_network)
    public ImageView iv_network;

    @BindView(R.id.iv_setting)
    public ImageView iv_setting;

    @BindView(R.id.tv_time)
    public TextView tv_time;

    @BindView(R.id.tv_daid)
    public TextView tv_daid;

    @BindView(R.id.iv_lock)
    public ImageView iv_lock;

    @BindView(R.id.tv_temperature)
    public TextView tv_temperature;

    @BindView(R.id.tv_humidity)
    public TextView tv_humidity;

    @BindView(R.id.preview_view)
    public AutoTexturePreviewView previewView;

    @BindView(R.id.preview_view1)
    public AutoTexturePreviewView previewView1;

    @BindView(R.id.texture_view)
    public TextureView textureView;

    public SwitchPresenter sp = SwitchPresenter.getInstance();

    public FacePresenter fp = FacePresenter.getInstance();

    public IDCardPresenter idp = IDCardPresenter.getInstance();

    String ver = "sync1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        idp.idCardOpen();
        sp.switch_Open();
        firstUse();
        Log.e(TAG, "onCreate");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        idp.IDCardPresenterSetView(this);
        fp.SetRegStatus(false);
        MediaHelper.play(MediaHelper.Text.normal_model);
        Observable.timer(1, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> AppInit.getInstrumentConfig().readCard());
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        AppInit.getInstrumentConfig().stopReadCard();
        fp.FaceSetNoAction();
        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sp.WhiteLighrOff();
        idp.idCardClose();
        MediaHelper.mediaRealese();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }


    private void firstUse() {
        if (config.getBoolean("firstUse", true)) {
            try {
                mdaosession.deleteAll(ReUploadWithBsBean.class);
                mdaosession.deleteAll(ReUploadBean.class);
                mdaosession.deleteAll(Employer.class);
                mdaosession.deleteAll(Keeper.class);
                FaceApi.getInstance().groupDelete("1");
                config.put("firstUse", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        OpenDoor();
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
        cg_User1 = new SceneKeeper();
        cg_User2 = new SceneKeeper();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
    }


    public abstract void OpenDoor();
}
