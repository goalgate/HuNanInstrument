package cn.cbdi.hunaninstrument.Activity_LN;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Activity_HuNan.HuNanRegActivity;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.log.Lg;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl3.FEATURE_DATAS_UNREADY;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Error;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_False;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_True;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Reg_success;

public class LNRegActivity extends RxActivity implements IFaceView, IIDCardView {

    private String TAG = HuNanRegActivity.class.getSimpleName();

    private SPUtils config = SPUtils.getInstance("config");

    SwitchPresenter sp = SwitchPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    Disposable disposableTips;

    Disposable disposableTimer;

    ICardInfo global_cardInfo;
    Bitmap cardBitmap;

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

//    @OnClick(R.id.preview_view)
//    void change() {
//        fp.PreviewCease(() -> HuNanRegActivity.this.finish());
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.layout_reg);
        ButterKnife.bind(this);
        mapInit();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(30, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));

        disposableTimer = RxTextView.textChanges(tv_timer)
                .debounce(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((charSequence) -> fp.PreviewCease(() -> LNRegActivity.this.finish()));

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
        MediaHelper.play(MediaHelper.Text.reg_model);
        idp.IDCardPresenterSetView(this);
        fp.SetRegStatus(true);
        Observable.timer(2, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    AppInit.getInstrumentConfig().readCard();
                });
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
        ActivityCollector.removeActivity(this);
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        if (disposableTimer != null) {
            disposableTimer.dispose();
        }
    }

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (cardInfo.getUid().equals(AppInit.The_IC_UID)) {
            fp.PreviewCease(() -> LNRegActivity.this.finish());
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
        cardBitmap = bmp;
        if (bmp != null) {
            try {
                Log.e(TAG, global_cardInfo.cardId());
                mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + global_cardInfo.cardId().toUpperCase() + "'").get(0);
                tv_info.setText("等待人证比对结果返回");
                tv_timer.setText("等待人证比对结果返回");
                MediaHelper.play(MediaHelper.Text.waiting);
                can_recentPic = true;
                natural = true;
                fp.FaceReg(global_cardInfo, bmp);
            } catch (IndexOutOfBoundsException e) {
                RetrofitGenerator.getLNApi().queryPersonInfo("queryPersonInfo", config.getString("key"), global_cardInfo.cardId().toUpperCase())
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
                                    if (infoMap.get("result").equals("true")) {
                                        if (infoMap.get("status").equals(String.valueOf(0))) {
                                            String type = infoMap.get("courType");
                                            mdaosession.insertOrReplace(new Employer(global_cardInfo.cardId(), Integer.valueOf(type)));
                                            tv_info.setText("等待人证比对结果返回");
                                            tv_timer.setText("等待人证比对结果返回");
                                            MediaHelper.play(MediaHelper.Text.waiting);
                                            can_recentPic = true;
                                            natural = true;
                                            fp.FaceReg(global_cardInfo, bmp);
                                        } else {
                                            tv_info.setText("系统查无此人");
                                            tv_timer.setText("系统查无此人");
                                            MediaHelper.play(MediaHelper.Text.man_non);
                                            sp.redLight();
                                        }
                                    } else {
                                        tv_info.setText("系统查无此人");
                                        tv_timer.setText("系统查无此人");
                                        MediaHelper.play(MediaHelper.Text.man_non);
                                        sp.redLight();
                                    }
                                } catch (IOException e) {
                                    Lg.e(TAG, e.toString());
                                } catch (Exception e) {
                                    Lg.e(TAG, e.toString());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                ToastUtils.showLong("无法连接服务器，请检查网络");
                                sp.redLight();
                            }
                        });
            } catch (NullPointerException e) {
                tv_info.setText("刷卡时间太短，无法获得身份证照片数据");
                tv_timer.setText("刷卡时间太短，无法获得身份证照片数据");
            }
        } else {
            tv_info.setText("刷卡时间太短，无法获得身份证照片数据");
            tv_timer.setText("刷卡时间太短，无法获得身份证照片数据");

        }
    }


    @Override
    public void onSetText(String Msg) {

    }

    boolean can_recentPic = true;

    boolean natural = true;

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(IMG_MATCH_IMG_True)) {
            tv_info.setText(text);
            tv_timer.setText(text);
        } else if (resultType.equals(IMG_MATCH_IMG_False)) {
            sp.redLight();
            if (can_recentPic) {
                tv_info.setText("人证比对分数过低，正在调取系统上最新的人员照片");
                tv_timer.setText("人证比对分数过低，正在调取系统上最新的人员照片");
            } else {
                tv_info.setText(text);
                tv_timer.setText(text);
            }
        } else if (resultType.equals(IMG_MATCH_IMG_Error)) {
            sp.redLight();
            tv_info.setText(text);
            tv_timer.setText(text);
            ToastUtils.showLong(text);
        } else if (resultType.equals(Reg_success)) {
            tv_info.setText("人员数据已成功录入");
            tv_timer.setText("人员数据已成功录入");
            sp.greenLight();
        } else if (resultType.equals(Reg_failed)) {
            tv_info.setText(text);
            tv_timer.setText(text);
            sp.redLight();
        }
    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {

    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {

    }

    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
    }



    @Override
    public void onBackPressed() {

    }
}
