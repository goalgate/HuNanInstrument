package cn.cbdi.hunaninstrument.Activity_byyj;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl.FEATURE_DATAS_UNREADY;

//import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.FaceImpl4.FEATURE_DATAS_UNREADY;
//import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.FaceImpl3.FEATURE_DATAS_UNREADY;

public abstract class BaseActivity extends RxActivity implements IFaceView, IIDCardView {

    private String TAG = IdentifyActivity.class.getSimpleName();

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
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        idp.ReadIC();
                        idp.readCard();
                    }
                });
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
//        fp.PreviewCease();
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        idp.StopReadIC();
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

}
