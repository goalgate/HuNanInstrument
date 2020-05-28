package cn.cbdi.hunaninstrument.Activity_XinWeiGuan;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.attribute.AclEntryType;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.drv.card.CardInfo;
import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Activity_HLJYZB.AddActvity;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.log.Lg;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class XinWeiGuanAddActivity extends Activity {

    private ArrayAdapter<String> adapter;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    private SPUtils config = SPUtils.getInstance("config");

    List<String> peoples = new ArrayList<String>();

    List<ICardInfo> peoplesInfo = new ArrayList<ICardInfo>();

    ICardInfo choose_cardInfo = new CardInfoBean();

    String alertTitle = "请选择接下来的操作";

    String userFaceID = "EMPTY";

    @BindView(R.id.peopleSpinner)
    Spinner peopleSpinner;

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    @BindView(R.id.tv_peopleTips)
    TextView tv_peopleTips;

    @OnClick(R.id.iv_userPic)
    void getPic() {
        try {
            if (!userFaceID.equals("EMPTY")) {
                FaceApi.getInstance().userDelete(userFaceID, "1");
                Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where FACE_USER_ID = '" + userFaceID + "'").get(0);
                mdaoSession.delete(keeper);
            }
            if (choose_cardInfo.cardId() != null) {
                FaceDetect(choose_cardInfo.cardId(), choose_cardInfo.name());
            } else {
                ToastUtils.showLong("没有可供录入的人员信息");
            }

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    void FaceDetect(String cardId, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("cardId", cardId);
        bundle.putString("name", name);
        ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + ".Activity_XinWeiGuan.XinWeiGuanFaceDetectActivity");
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, XinWeiGuanAddActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    alertTitle = "请选择接下来的操作";
                    if (!userFaceID.equals("EMPTY")) {
                        try {
                            FaceApi.getInstance().userDelete(userFaceID, "1");
                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where FACE_USER_ID = '" + userFaceID + "'").get(0);
                            mdaoSession.delete(keeper);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    iv_userPic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.user_icon));
                } else {
                    if (!userFaceID.equals("EMPTY")) {
                        try {
                            FaceApi.getInstance().userDelete(userFaceID, "1");
                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where FACE_USER_ID = '" + userFaceID + "'").get(0);
                            mdaoSession.delete(keeper);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    finish();
                }
            }
        }).show();
    }

    @OnClick(R.id.btn_commit)
    void commit() {
        if (!userFaceID.equals("EMPTY")) {
            userFaceID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else {
            Alarm.getInstance(XinWeiGuanAddActivity.this).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person3);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        peopleSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        try {
            DataPrepare();
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this).release();
        EventBus.getDefault().unregister(this);
    }

    int count = 0;

    private void DataPrepare() {
        peoples.clear();
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getXinWeiGuanApi()
                        .queryPersonInfo("recentPic", config.getString("key"), employer.getCardID())
                        .subscribeOn(Schedulers.single())
                        .unsubscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {

                                try {
                                    String s = ParsingTool.extractMainContent(responseBody);
                                    JSONObject jsonObject = new JSONObject(s);
                                    String result = jsonObject.getString("result");
                                    if (result.equals("true")) {
                                        String name = jsonObject.getString("personName");
                                        peoples.add(name);
                                        CardInfoBean cardInfo = new CardInfoBean(employer.getCardID(), name);
                                        peoplesInfo.add(cardInfo);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                count++;
                                if (count == employers.size()) {
                                    tv_peopleTips.setText("设备未联网，无法获得人员数据");

                                }

                            }

                            @Override
                            public void onComplete() {
                                count++;
                                if (count == employers.size()) {
                                    tv_peopleTips.setText("工作人员列表如下");
                                    peopleSpinner.setVisibility(View.VISIBLE);
                                    adapter = new TestArrayAdapter(XinWeiGuanAddActivity.this, peoples);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    peopleSpinner.setAdapter(adapter);
                                }

                            }
                        });
            }
        }

    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            ToastUtils.showLong(("您已挑选：" + peoplesInfo.get(arg2).name() + ",点击图片进入照片采集界面"));
            choose_cardInfo = peoplesInfo.get(arg2);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        iv_userPic.setImageBitmap(event.getBitmap());
        userFaceID = event.getUserId();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        final JSONObject OpenDoorjson = new JSONObject();
        try {
            OpenDoorjson.put("datetime", TimeUtils.getNowString());
            OpenDoorjson.put("state", "n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("openDoorRecord", config.getString("key"), OpenDoorjson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorjson.toString()));

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    class TestArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private List<String> mlist;

        public TestArrayAdapter(Context context, List<String> stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mlist = stringArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mlist.get(position));
            tv.setTextSize(30f);
            tv.setTextColor(Color.BLACK);

            return convertView;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mlist.get(position));
            tv.setTextSize(18f);
            tv.setTextColor(Color.BLACK);
            return convertView;
        }

    }
}
