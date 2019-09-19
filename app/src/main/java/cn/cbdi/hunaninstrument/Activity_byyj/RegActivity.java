package cn.cbdi.hunaninstrument.Activity_byyj;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
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
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Error;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_False;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_True;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_success;

public class RegActivity extends RxActivity implements IFaceView, IIDCardView {

    private String TAG = RegActivity.class.getSimpleName();

    SwitchPresenter sp = SwitchPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    ICardInfo global_cardInfo;

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.preview_view1)
    AutoTexturePreviewView previewView1;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.iv_personRect)
    ImageView iv_personRect;

    @BindView(R.id.tv_info)
    TextView tv_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.layout_reg);
        ButterKnife.bind(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
//        MediaHelper.play(MediaHelper.Text.reg_model);
        idp.IDCardPresenterSetView(this);
        fp.SetRegStatus(true);

   /*     idp.ReadIC();
        idp.readCard();*/
        Observable.timer(2, TimeUnit.SECONDS)
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
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
//        if (cardInfo.getUid().equals("0F23932B")) {
            if (cardInfo.getUid().equals("1B17ECC2")||
                    cardInfo.getUid().equals("ABBE00C3")||
                    cardInfo.getUid().equals("AB737840")) {
            Log.e("uid", "uid触发");
            fp.PreviewCease(new IFace.CeaseListener() {
                @Override
                public void CeaseCallBack() {
                    RegActivity.this.finish();
                }
            });
        } else {
            ToastUtils.showLong("非法IC卡");
            sp.redLight();
        }
    }


    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
        global_cardInfo = cardInfo;

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        idp.stopReadCard();
        tv_info.setText("等待人证比对结果返回");
        MediaHelper.play(MediaHelper.Text.waiting);
        fp.FaceReg(global_cardInfo, bmp);
    }


    @Override
    public void onSetText(String Msg) {

    }


    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(IMG_MATCH_IMG_True)) {
            tv_info.setText(text);
        } else if (resultType.equals(IMG_MATCH_IMG_False)) {
            sp.redLight();
            tv_info.setText(text);
        } else if (resultType.equals(IMG_MATCH_IMG_Error)) {
            sp.redLight();
            tv_info.setText(text);
            ToastUtils.showLong(text);
        } else if (resultType.equals(Reg_success)) {
            tv_info.setText("人员数据已成功录入");
            sp.greenLight();
        } else if (resultType.equals(Reg_failed)) {
            tv_info.setText(text);
            sp.redLight();
        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {

    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {

    }


}
