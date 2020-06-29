package cn.cbdi.hunaninstrument.Activity_XinWeiGuan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Fingerprint.mvp.view.IFingerPrintView;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class XinWeiGuanAddActivity2 extends Activity implements IFingerPrintView {

    private ArrayAdapter<String> adapter;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    private SPUtils fingerprintBooks = SPUtils.getInstance("fingerprintBooks");

    private SPUtils fingerprintBooksRevert = SPUtils.getInstance("fingerprintBooksRevert");

    List<String> peoples = new ArrayList<String>();

    List<ICardInfo> peoplesInfo = new ArrayList<ICardInfo>();

    ICardInfo choose_cardInfo = new CardInfoBean();

    String alertTitle = "请选择接下来的操作";

    String userFaceID = "EMPTY";

    String fingerprintID = "EMPTY";

    boolean FingerReady = false;

    @BindView(R.id.peopleSpinner)
    Spinner peopleSpinner;

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    @BindView(R.id.tv_peopleTips)
    TextView tv_peopleTips;

    @BindView(R.id.iv_finger)
    ImageView iv_finger;

    @BindView(R.id.tv_finger)
    TextView tv_finger;

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
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, XinWeiGuanAddActivity2.this, AlertView.Style.Alert, new OnItemClickListener() {
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
                    if (FingerReady) {
                        fpp.fpRemoveTmpl(fingerprintID);
                    }
                    FingerReady = false;
                    userFaceID = "EMPTY";
                    fingerprintID = "EMPTY";
                    peopleSpinner.setEnabled(true);
                    tv_finger.setText("需选择人员获得指纹编号");
                    iv_finger.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.zw_icon));
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
                    if (!fingerprintID.equals("EMPTY")) {
                        fpp.fpRemoveTmpl(fingerprintID);
                    }
                    finish();
                }
            }
        }).show();
    }

    @OnClick(R.id.btn_commit)
    void commit() {
        if ((!userFaceID.equals("EMPTY")) && (!fingerprintID.equals("EMPTY"))) {
            fingerprintBooks.put(fingerprintID, userFaceID);
            fingerprintBooksRevert.put(userFaceID, fingerprintID);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!fingerprintID.equals("EMPTY")) {
            String uid = UUID.randomUUID().toString();
            fingerprintBooks.put(fingerprintID, uid);
            fingerprintBooksRevert.put(uid, fingerprintID);
            Keeper keeper = new Keeper(choose_cardInfo.cardId(), choose_cardInfo.name(),
                    null, null, null, uid, null);
            mdaoSession.insertOrReplace(keeper);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            peopleSpinner.setEnabled(true);
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!userFaceID.equals("EMPTY")) {
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else {
            Alarm.getInstance(XinWeiGuanAddActivity2.this).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person4);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        peopleSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        try {
            DataPrepare();
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }

        RxView.clicks(iv_finger).throttleFirst(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((o) -> {
                    fingerprintID = String.valueOf(fpp.fpGetEmptyID());
                    fpp.fpEnroll(fingerprintID);
                    iv_finger.setClickable(false);
                });
        iv_finger.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fpp.FingerPrintPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);
    }


    @Override
    public void onBackPressed() {
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
                                    if (peoples.size() > 0) {
                                        tv_peopleTips.setText("工作人员列表如下");
                                        peopleSpinner.setVisibility(View.VISIBLE);
                                        adapter = new TestArrayAdapter(XinWeiGuanAddActivity2.this, peoples);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        peopleSpinner.setAdapter(adapter);
                                    } else {
                                        tv_peopleTips.setText("设备没有找到相应人员的信息");
                                    }
                                }

                            }
                        });
            }
        } else {
            tv_peopleTips.setText("设备没有找到相应人员的信息");

        }

    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            fpp.fpCancel(true);
            ToastUtils.showLong(("您已挑选：" + peoplesInfo.get(arg2).name()));
            choose_cardInfo = peoplesInfo.get(arg2);
            try {
                Thread.sleep(500);
                fingerprintID = String.valueOf(fpp.fpGetEmptyID());
                fpp.fpEnroll(fingerprintID);
            } catch (InterruptedException e) {
                ToastUtils.showLong(e.toString());
            }
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

    @Override
    public void onFpSucc(String msg) {

    }

    @Override
    public void onSetImg(Bitmap bmp) {
        iv_finger.setImageBitmap(bmp);

    }

    @Override
    public void onText(String msg) {
        if (!msg.equals("Canceled")) {
            tv_finger.setText(msg);
        }
        if (msg.endsWith("录入成功")) {
            peopleSpinner.setEnabled(false);
            FingerReady = true;
            iv_userPic.setClickable(true);
            ToastUtils.showLong("您现在可以点击人像获取人脸信息。");

//            Observable.interval(0, 1, TimeUnit.SECONDS)
//                    .take(count + 1)
//                    .map(new Function<Long, Long>() {
//                        @Override
//                        public Long apply(@NonNull Long aLong) throws Exception {
//                            return count - aLong;
//                        }
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Long>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(@NonNull Long aLong) {
//                            ToastUtils.showLong(aLong + "秒后进入人脸识别界面");
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//                            iv_userPic.setClickable(true);
//                            FaceDetect(choose_cardInfo.cardId(), choose_cardInfo.name());
//                        }
//                    });
        }
        if (msg.endsWith("点我重试")) {
            iv_finger.setClickable(true);
        }
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
