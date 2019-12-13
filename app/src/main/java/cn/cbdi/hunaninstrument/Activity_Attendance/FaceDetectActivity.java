package cn.cbdi.hunaninstrument.Activity_Attendance;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.hunaninstrument.Activity_HuNan.HuNanRegActivity;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl3.FEATURE_DATAS_UNREADY;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_success;

public class FaceDetectActivity extends RxActivity implements IFaceView {
    private String TAG = HuNanRegActivity.class.getSimpleName();

    SwitchPresenter sp = SwitchPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    Disposable disposableTips;

    Disposable disposableTimer;

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.preview_view1)
    AutoTexturePreviewView previewView1;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.tv_info)
    TextView tv_info;

    @BindView(R.id.tv_timer)
    TextView tv_timer;

    CardInfoBean cardInfo = new CardInfoBean();

    Bitmap headBmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.layout_reg);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        cardInfo.setCardID(bundle.getString("cardId"));
        cardInfo.setName(bundle.getString("name"));
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(30, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));

        disposableTimer = RxTextView.textChanges(tv_timer)
                .debounce(60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((charSequence) -> {
                    ToastUtils.showLong("抽取特征超时，你可以点击头像图片再次抽取人脸特征");
                    fp.PreviewCease(() -> FaceDetectActivity.this.finish());
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.SetRegStatus(true);
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        MediaHelper.play(MediaHelper.Text.reg_model);
        fp.FacePresenterSetView(this);
        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> fp.FaceReg(cardInfo));
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fp.FacePresenterSetView(null);
        fp.FaceSetNoAction();
        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        if (disposableTimer != null) {
            disposableTimer.dispose();
        }
    }

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Reg_success)) {
            tv_info.setText("人员数据已成功录入");
            tv_timer.setText("人员数据已成功录入");
            sp.greenLight();
            ToastUtils.showLong("抽取特征成功");
            fp.PreviewCease(() -> FaceDetectActivity.this.finish());
        } else if (resultType.equals(Reg_failed)) {
            tv_info.setText(text);
            tv_timer.setText(text);
            sp.redLight();
            ToastUtils.showLong(text+",你可以点击头像图片再次抽取人脸特征");
            fp.PreviewCease(() -> FaceDetectActivity.this.finish());

        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(Reg_success)) {
            EventBus.getDefault().post(new FaceDetectEvent(headBmp, user.getUserId()));
        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType.equals(Reg_success)) {
            headBmp = bitmap;
        }
    }
}
