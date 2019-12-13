package cn.cbdi.hunaninstrument;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.aip.utils.PreferencesUtil;
import com.blankj.utilcode.util.Utils;
import com.squareup.leakcanary.LeakCanary;

import cn.cbdi.hunaninstrument.Config.Attendance_Config;
import cn.cbdi.hunaninstrument.Config.BaseConfig;

import cn.cbdi.hunaninstrument.Config.HLJYZB_Config;
import cn.cbdi.hunaninstrument.Config.HLJ_Config;
import cn.cbdi.hunaninstrument.Config.WYYConfig;
import cn.cbdi.hunaninstrument.Config.XAJD_Config;
import cn.cbdi.hunaninstrument.Config.YUNPINGTAI_Config;

import cn.cbdi.hunaninstrument.Tool.DataBase.GreendaoContext;
import cn.cbdi.hunaninstrument.Tool.WZWManager;
import cn.cbdi.hunaninstrument.greendao.DaoMaster;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.MyOpenHelper;

public class AppInit extends Application {

    public static String The_IC_UID = "0AE8B023";

    private DaoMaster.OpenHelper mHelper;

    private SQLiteDatabase db;

    private DaoMaster mDaoMaster;

    private DaoSession mDaoSession;

    protected static BaseConfig InstrumentConfig;

    public static BaseConfig getInstrumentConfig() {
        return InstrumentConfig;
    }

    protected static AppInit instance;

    protected static WZWManager manager;

    public static WZWManager getMyManager() {
        return manager;
    }

    public static AppInit getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;

        InstrumentConfig = new HLJYZB_Config();

//        Lg.setIsSave(true);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        manager = WZWManager.getInstance(this);

        manager.bindAIDLService(this);

        Utils.init(getContext());

        PreferencesUtil.initPrefs(getApplicationContext());

        setDatabase();
    }

    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
//        mHelper = new DaoMaster.DevOpenHelper(new GreendaoContext(), "hnInstrument-db", null);
//        db = mHelper.getWritableDatabase();
//        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
//        mDaoMaster = new DaoMaster(db);
//        mDaoSession = mDaoMaster.newSession();

        mHelper = new MyOpenHelper(AppInit.getInstance(), "hnInstrument-db", null);//建库
        mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
