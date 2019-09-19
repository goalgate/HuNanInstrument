package cn.cbdi.hunaninstrument.Activity_byyj;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.baidu.aip.entity.User;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Activity_HuNan.BaseActivity;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Error;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_False;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_True;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify_non;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_success;

public class IdentifyActivity extends BaseActivity {

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    Disposable disposableTips;

    @OnClick(R.id.lay_setting)
    void setting() {
        new AlertView("请选择要使用的摄像头", null, "取消"
                , new String[]{"机器自带摄像头", "外接摄像头"},
                null, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    camera_config.put("hardware", "inside");
                } else if (position == 1) {
                    camera_config.put("hardware", "outside");
                }
            }
        }).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(15, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws Exception {
                        return Observable.just("等待用户操作...");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tv_info.setText(s);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposableTips != null) {
            disposableTips.dispose();
        }
    }

    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
//        if (reg_prepare) {
//            global_cardInfo = cardInfo;
//        } else {
        try {
            Keeper keeper = mdaosession.queryRaw(Keeper.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
            tv_info.setText("等待人脸比对结果返回");
            MediaHelper.play(MediaHelper.Text.waiting);
            fp.FaceIdentify();
            idp.stopReadCard();
        } catch (IndexOutOfBoundsException e) {
            tv_info.setText("系统查无此人");
            MediaHelper.play(MediaHelper.Text.man_non);
            sp.redLight();
            idp.readCard();
        }
//        }
    }

    SPUtils camera_config = SPUtils.getInstance("camera_config");

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
//        ToastUtils.showLong(cardInfo.getUid());
//        if (cardInfo.getUid().equals("0F23932B")) {
        if (cardInfo.getUid().equals("1B17ECC2")||
                cardInfo.getUid().equals("ABBE00C3")||
                cardInfo.getUid().equals("AB737840")) {
            Log.e("uid", "uid触发");
            fp.PreviewCease(new IFace.CeaseListener() {
                @Override
                public void CeaseCallBack() {
                    ActivityUtils.startActivity(getPackageName(), getPackageName() + ".RegActivity");
                }
            });
        } else {
            ToastUtils.showLong("非法IC卡");
            sp.redLight();
        }
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
//        if (reg_prepare) {
//            idp.stopReadCard();
//            tv_info.setText("等待人证比对结果返回");
//            MediaHelper.play(MediaHelper.Text.waiting);
//            fp.FaceReg(global_cardInfo, bmp);
//        }
    }


    @Override
    public void onSetText(String Msg) {

    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {

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
        } else if (resultType.equals(Identify_non)) {
            fp.FaceSetNoAction();
            tv_info.setText(text);
            idp.readCard();
            sp.redLight();
        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(Identify)) {
            Keeper keeper = mdaosession.queryRaw(Keeper.class, "where CARD_ID = '" + user.getUserId().toUpperCase() + "'").get(0);
            tv_info.setText(keeper.getName() + "操作成功");
            fp.FaceSetNoAction();
            idp.readCard();
            sp.greenLight();

        }
    }
}
