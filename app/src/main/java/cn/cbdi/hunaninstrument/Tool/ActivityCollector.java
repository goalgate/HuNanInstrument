package cn.cbdi.hunaninstrument.Tool;

import android.app.Activity;

import com.baidu.aip.manager.FaceSDKManager;

import java.util.ArrayList;
import java.util.List;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;

public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static void finishAll(){
        FaceSDKManager.getInstance().release();
        FacePresenter.getInstance().onDestroy();
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        SwitchPresenter.getInstance().WhiteLighrOff();

        for (Activity activity:activities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
        SwitchPresenter.getInstance().Close();

    }
}
