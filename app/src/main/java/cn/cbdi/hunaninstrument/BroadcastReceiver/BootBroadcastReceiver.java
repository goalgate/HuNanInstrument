package cn.cbdi.hunaninstrument.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.module.ISwitching;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.SplashActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// boot;
            Intent intent2 = new Intent(context, SplashActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
            if(AppInit.getInstrumentConfig().MenKongSuo()){
                SwitchPresenter.getInstance().switch_Open();
                SwitchPresenter.getInstance().relay(ISwitching.Relay.relay_D5, ISwitching.Hex.H0,true);

            }
        }
    }
}
