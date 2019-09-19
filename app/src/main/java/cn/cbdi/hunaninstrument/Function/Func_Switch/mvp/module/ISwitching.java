package cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module;

/**
 * Created by zbsz on 2017/8/23.
 */

public interface ISwitching {

    enum Hex {
        H0, H1, H2, H3, H4, H5, H6, H7, H8, H9, HA
    }

    enum Relay {
        relay_12V,relay_D10,relay_D5, relay_relay
    }

    void onOpen(ISwitchingListener listener);

    void onReadHum();

    void onOutD8(boolean status);

    void onOutD9(boolean status);

    void onBuzz(SwitchImpl.Hex hex);

    void onBuzzOff();

    void onDoorOpen();

    void onRedLightBlink();

    void onGreenLightBlink();

    void onWhiteLighrOn();

    void onWhiteLighrOff();

    void on12VRelay(SwitchImpl.Hex hex,boolean status);

    void onRelay(SwitchImpl.Hex hex,boolean status);

    void onD10Relay(SwitchImpl.Hex hex,boolean status);

    void onD5Relay(SwitchImpl.Hex hex, boolean status);

    void onClose();

    interface ISwitchingListener{

        void onSwitchingText(String value);

        void onTemHum(int temperature, int humidity);

    }

}
