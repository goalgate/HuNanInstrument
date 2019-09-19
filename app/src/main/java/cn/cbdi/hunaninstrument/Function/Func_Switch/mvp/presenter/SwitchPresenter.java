package cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter;


import com.blankj.utilcode.util.ToastUtils;

import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.ISwitching;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.SwitchImpl4;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.view.ISwitchView;

/**
 * Created by zbsz on 2017/8/23.
 */

public class SwitchPresenter {

    private ISwitchView view;

    private SwitchPresenter() {
    }

    private static SwitchPresenter instance = null;

    public static SwitchPresenter getInstance() {
        if (instance == null)
            instance = new SwitchPresenter();
        return instance;
    }

    public void SwitchPresenterSetView(ISwitchView view) {
        this.view = view;
    }

    ISwitching switchingModule = new SwitchImpl4();

    public void switch_Open() {
        try {
            switchingModule.onOpen(new ISwitching.ISwitchingListener() {
                @Override
                public void onSwitchingText(String value) {
                    if (view != null) {
                        view.onSwitchingText(value);
                    }
                }

                @Override
                public void onTemHum(int temperature, int humidity) {
                    if (view != null) {
                        view.onTemHum(temperature, humidity);
                    }
                }
            });
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }

    }

    public void readHum() {
        try {
            switchingModule.onReadHum();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void OutD8(boolean isOn) {
        try {
            switchingModule.onOutD8(isOn);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void OutD9(boolean isOn) {
        try {
            switchingModule.onOutD9(isOn);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void buzz(SwitchImpl.Hex hex) {
        try {
            switchingModule.onBuzz(hex);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void buzzOff() {
        try {
            switchingModule.onBuzzOff();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void doorOpen() {
        try {
            switchingModule.onDoorOpen();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void greenLight() {
        switchingModule.onGreenLightBlink();
    }

    public void redLight() {
        switchingModule.onRedLightBlink();
    }

    public void WhiteLighrOn() {
        switchingModule.onWhiteLighrOn();
    }

    public void WhiteLighrOff() {
        switchingModule.onWhiteLighrOff();
    }

    public void Close() {
        switchingModule.onClose();
    }

    public void relay(SwitchImpl.Relay relay, SwitchImpl.Hex hex, boolean status) {
        switch (relay) {
            case relay_D5:
                switchingModule.onD5Relay(hex, status);
                break;
            case relay_D10:
                switchingModule.onD10Relay(hex, status);

                break;
            case relay_12V:
                switchingModule.on12VRelay(hex, status);
                break;
            case relay_relay:
                switchingModule.onRelay(hex, status);
                break;
            default:
                break;

        }
    }
}
