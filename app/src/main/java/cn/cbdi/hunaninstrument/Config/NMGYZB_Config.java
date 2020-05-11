package cn.cbdi.hunaninstrument.Config;

import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl3;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Service.HeBeiService;
import cn.cbdi.hunaninstrument.Service.SXService;

public class NMGYZB_Config extends BaseConfig {

    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return false;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getDev_prefix() {
        return "800100";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "cjy/s/fbcjy_updata?";
    }

    @Override
    public String getUpDataPrefix() {
        return "cjy/s/fbcjy_updata?";
    }

    @Override
    public String getServerId() {
        return "http://113.140.1.138:8890/";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-DA-01";
    }

    @Override
    public String getName() {
        return "防爆采集器";
    }

    @Override
    public String getProject() {
        return "NMGYZB";        //内蒙古易制爆
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
        IDCardPresenter.getInstance().readCard();
        IDCardPresenter.getInstance().ReadIC();
    }

    @Override
    public void stopReadCard() {
        IDCardPresenter.getInstance().stopReadCard();
        IDCardPresenter.getInstance().StopReadIC();
    }

    @Override
    public boolean fingerprint() {
        return false;
    }

    @Override
    public Class getServiceName() {
        return SXService.class;
    }

    @Override
    public String getMainActivity() {
        return ".Activity_SX.SXMainActivity";
    }

    @Override
    public String getAddActivity() {
        return ".Activity_SX.SXRegActivity";
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
        return false;
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
