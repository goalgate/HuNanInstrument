package cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.ToastUtils;

import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.module.IDCardImpl;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.module.IIDCard;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.drv.card.ICardInfo;

/**
 * Created by zbsz on 2017/6/9.
 */

public class IDCardPresenter {
    private IIDCardView view;

    private static IDCardPresenter instance = null;

    private IDCardPresenter() {}

    public static IDCardPresenter getInstance() {
        if (instance == null)
            instance = new IDCardPresenter();
        return instance;
    }

    public void IDCardPresenterSetView(IIDCardView view) {
        this.view = view;
    }

    IIDCard idCardModule = new IDCardImpl();

    public void idCardOpen() {
        try {
            idCardModule.onOpen(new IIDCard.IIdCardListener() {
                @Override
                public void onSetImg(Bitmap bmp) {
                    view.onsetCardImg(bmp);
                }

                @Override
                public void onSetInfo(ICardInfo cardInfo) {
                    view.onsetCardInfo(cardInfo);
                }

                @Override
                public void onSetICInfo(ICardInfo cardInfo) {
                    view.onsetICCardInfo(cardInfo);
                }

                @Override
                public void onSetText(String Msg) {
                    view.onSetText(Msg);
                }
            });
        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }

    }

    public void readCard() {
        try {
            idCardModule.onReadCard();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }

    public void stopReadCard() {
        try {
            idCardModule.onStopReadCard();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }

    public void idCardClose() {
        try {
            idCardModule.onClose();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }

    public void readSam() {
        try {
            idCardModule.onReadSAM();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }

    public void StopReadIC() {
        try {
            idCardModule.onStopReadICCard();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }

    public void ReadIC() {
        try {
            idCardModule.onReadICCard();

        }catch (Exception e){
            ToastUtils.showLong(e.toString());
        }
    }
}
