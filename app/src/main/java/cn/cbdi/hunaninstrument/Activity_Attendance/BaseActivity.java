package cn.cbdi.hunaninstrument.Activity_Attendance;

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

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.AttendanceScene;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.view.IFingerPrintView;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.FaceImpl2.FEATURE_DATAS_UNREADY;

public abstract class BaseActivity extends RxActivity implements IFaceView, IIDCardView, IFingerPrintView {



    private String TAG = BaseActivity.class.getSimpleName();

    public SwitchPresenter sp = SwitchPresenter.getInstance();

    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public FacePresenter fp = FacePresenter.getInstance();

    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    @BindView(R.id.tv_info)
    public TextView tv_info;

    @BindView(R.id.iv_network)
    public ImageView iv_network;

    @BindView(R.id.iv_setting)
    public ImageView iv_setting;

    @BindView(R.id.tv_time)
    public TextView tv_time;

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

    @BindView(R.id.tv_daid)
    public TextView tv_daid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        idp.idCardOpen();
        sp.switch_Open();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        idp.IDCardPresenterSetView(this);
        fpp.FingerPrintPresenterSetView(this);
        fp.FacePresenterSetView(this);
        if (config.getBoolean("firstUse", true)) {
            try {
                mdaoSession.deleteAll(AttendanceScene.class);
                mdaoSession.deleteAll(Keeper.class);
                fpp.fpRemoveAll();
                FaceApi.getInstance().groupDelete("1");
                config.put("firstUse",false);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Observable.timer(3, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    fpp.fpIdentify();
                    AppInit.getInstrumentConfig().readCard();
                });
        fp.SetRegStatus(false);
        fp.FaceIdentifyReady();
        MediaHelper.play(MediaHelper.Text.normal_model);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
//        fp.PreviewCease();
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        AppInit.getInstrumentConfig().stopReadCard();
        fp.FaceSetNoAction();
        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
        sp.WhiteLighrOff();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        idp.idCardClose();
        fpp.fpClose();
        sp.WhiteLighrOff();
        MediaHelper.mediaRealese();
        ActivityCollector.removeActivity(this);
    }



    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }
}
