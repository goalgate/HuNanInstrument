package cn.cbdi.hunaninstrument.Config;

import cn.cbdi.hunaninstrument.Activity_YZBYPT.YZBYPT_Service;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl3;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;

public class YZBYPT_Config extends BaseConfig {

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
        return "库房采集器";
    }

    @Override
    public String getProject() {
        return "YZBYPT";        //易制爆云平台
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
        return true;
    }

    @Override
    public Class getServiceName() {
        return YZBYPT_Service.class;
    }

    @Override
    public String getMainActivity() {
        return ".Activity_YZBYPT.YZBYPT_MainActivity";
    }

    @Override
    public String getAddActivity() {
        return ".Activity_YZBYPT.YZBYPT_AddActvity";
    }

    @Override
    public boolean TouchScreen() {
        return false;
    }

    @Override
    public boolean MenKongSuo() {
        return false;
    }

    @Override
    public IFace getFaceImpl() {
        return new HuNanFaceImpl3();
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
