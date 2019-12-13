package cn.cbdi.hunaninstrument.Activity_Attendance;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.AttendanceScene;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.greendao.DaoSession;

public class SceneActivity extends Activity {

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    Alert_twoPic alert_twoPic = new Alert_twoPic(this);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    @BindView(R.id.swipe_refresh_widget)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.id_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_choosePass)
    TextView tv_choosePass;

    String sql;

    @OnClick(R.id.turnback)
    void turnBack() {
        ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
        finish();
    }

    @OnClick(R.id.tv_choosePass)
    void choosePass() {
        new AlertView("请选择识别类型", null, "取消",
                new String[]{"上班打卡数据","下班打卡数据", "未识别人员", "活体检测不通过人员"}, null,
                this, AlertView.Style.ActionSheet, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position != -1) {
                    if (position == 0) {
                        sql = "select _id,NAME,SCENE_PHOTO,SCENE_HEAD_PHOTO,ATTENDANCE_TIME,ALIVE,FACE_RECOGNITION  from " + mdaoSession.getAttendanceSceneDao().getTablename() + " where CARD_ID IS NOT NULL and FACE_RECOGNITION = 1 order by ATTENDANCE_TIME desc";
                        tv_choosePass.setText("上班打卡数据");
                    } else if (position == 1) {
                        sql = "select _id,NAME,SCENE_PHOTO,SCENE_HEAD_PHOTO,ATTENDANCE_TIME,ALIVE,FACE_RECOGNITION from " + mdaoSession.getAttendanceSceneDao().getTablename() + " where CARD_ID IS NOT NULL and FACE_RECOGNITION = 2  order by ATTENDANCE_TIME desc";
                        tv_choosePass.setText("下班打卡数据");
                    } else if (position == 2) {
                        sql = "select _id,NAME,SCENE_PHOTO,SCENE_HEAD_PHOTO,ATTENDANCE_TIME,ALIVE,FACE_RECOGNITION from " + mdaoSession.getAttendanceSceneDao().getTablename() + " where CARD_ID IS NULL order by ATTENDANCE_TIME desc";
                        tv_choosePass.setText("未识别人员");
                    }
                    else if (position == 3) {
                        sql = "select _id,NAME,SCENE_PHOTO,SCENE_HEAD_PHOTO,ATTENDANCE_TIME,ALIVE,FACE_RECOGNITION from " + mdaoSession.getAttendanceSceneDao().getTablename() + " where ALIVE = 2 order by ATTENDANCE_TIME desc";
                        tv_choosePass.setText("活体检测不通过人员");
                    }
                    swipeRefreshLayout.setRefreshing(true);
                    refresh();

                }
            }
        }).show();


    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_scene);
        ActivityCollector.addActivity(this);
        ButterKnife.bind(this);
        alert_twoPic.Init();
        recycleViewInit();
    }

    ChoosePeopleAdapter choosePeopleAdapter;
    List<AttendanceScene> list = new ArrayList<AttendanceScene>();

    private void recycleViewInit() {
        choosePeopleAdapter = new ChoosePeopleAdapter(SceneActivity.this, list);
        mRecyclerView.setAdapter(choosePeopleAdapter);
        choosePeopleAdapter.setOnItemClickListener(new ChoosePeopleAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                alert_twoPic.show(FileUtils.base64ToBitmap(list.get(position).getSceneHeadPhoto()), FileUtils.base64ToBitmap(list.get(position).getScenePhoto()));
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SceneActivity.this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(SceneActivity.this, DividerItemDecoration.VERTICAL));
        swipeRefreshLayout.setDistanceToTriggerSync(300);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(() -> refresh());

    }

    private void refresh() {
        try {
            if (list.size() != 0) {
                list.clear();
            }
            Cursor cursor = mdaoSession.getDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                list.add(new AttendanceScene(cursor.getLong(cursor.getColumnIndex("_id")),
                        cursor.getString(cursor.getColumnIndex("NAME")),
                        cursor.getString(cursor.getColumnIndex("SCENE_PHOTO")),
                        cursor.getString(cursor.getColumnIndex("SCENE_HEAD_PHOTO")),
                        cursor.getString(cursor.getColumnIndex("ATTENDANCE_TIME")),
                        cursor.getInt(cursor.getColumnIndex("ALIVE")),
                        cursor.getInt(cursor.getColumnIndex("FACE_RECOGNITION"))));

            }
            if (list.size() == 0) {
                Alarm.getInstance(SceneActivity.this).messageDelay("没有数据");
            } else {
                Alarm.getInstance(SceneActivity.this).messageDelay("当前搜索统计为" + list.size() + "条");
            }
            choosePeopleAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            swipeRefreshLayout.setRefreshing(false);
            Alarm.getInstance(SceneActivity.this).messageDelay(e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Alarm.getInstance(this).release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);

    }
}