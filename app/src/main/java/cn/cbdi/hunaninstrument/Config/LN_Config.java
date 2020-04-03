package cn.cbdi.hunaninstrument.Config;

import cn.cbdi.hunaninstrument.Activity_LN.LNService;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl3;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;

public class LN_Config extends BaseConfig {
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
        return "";
    }

    @Override
    public String getUpDataPrefix() {
        return "";
    }

    @Override
    public String getServerId() {
        return "http://124.172.232.89:8050/daServer/";
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
        return "LN";        //辽宁剧毒
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
        return LNService.class;
    }

    @Override
    public String getMainActivity() {
        return ".Activity_LN.LNMainActivity";
    }

    @Override
    public String getAddActivity() {
        return ".Activity_LN.LNRegActivity";
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
    public boolean isHongWai() {
        return false;
    }
}
