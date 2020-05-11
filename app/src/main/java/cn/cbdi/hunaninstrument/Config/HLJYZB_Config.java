package cn.cbdi.hunaninstrument.Config;

import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.WYYFaceImpl;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.WYYFaceImpl2;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Service.HLJYZBService;
import cn.cbdi.hunaninstrument.Service.WYYService;

public class HLJYZB_Config extends BaseConfig{
    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return true;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getDev_prefix() {
        return "";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }

    @Override
    public String getUpDataPrefix() {
        return "da_gzmb_updata?";
    }

    @Override
    public String getServerId() {
        return "http://yzbyun.wxhxp.cn:81/";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-P-IC";
    }

    @Override
    public String getName() {
        return "剧毒库房采集器";
    }

    @Override
    public String getProject() {
        return "HLJYZB";
    }

    @Override
    public String getPower() {
        return "12-18V 2A";
    }

    @Override
    public boolean isCheckTime() {
        return false;
    }

    @Override
    public boolean disAlarm() {
        return true;
    }

    @Override
    public boolean collectBox() {
        return false;
    }

    @Override
    public boolean noise() {
        return false;
    }

    @Override
    public boolean doubleCheck() {
        return true;
    }

    @Override
    public void readCard() {
        IDCardPresenter.getInstance().ReadIC();

    }

    @Override
    public void stopReadCard() {
        IDCardPresenter.getInstance().StopReadIC();
    }

    @Override
    public boolean fingerprint() {
        return false;
    }

    @Override
    public Class getServiceName() {
        return HLJYZBService.class;
    }

    @Override
    public String getMainActivity() {
        return ".Activity_HLJYZB.MainActivity";
    }

    @Override
    public String getAddActivity() {
        return ".Activity_HLJYZB.AddActvity";
    }

    @Override
    public boolean TouchScreen() {
        return true;
    }

    @Override
    public boolean MenKongSuo() {
        return false;
    }

    @Override
    public IFace getFaceImpl() {
        return new WYYFaceImpl2();
    }

    @Override
    public boolean XungengCanOpen() {
        return true;
    }


    @Override
    public boolean DoorMonitorChosen() {
        return false;
    }

    @Override
    public boolean isHongWai() {
        return hongWai;
    }

    @Override
    public void setHongWai(boolean hongWai) {
        this.hongWai= hongWai;

    }
}

