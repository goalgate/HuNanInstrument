package cn.cbdi.hunaninstrument;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import java.util.regex.Pattern;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Tool.AssetsUtils;

/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private String regEx = "^\\d{4}$";

    private SPUtils config = SPUtils.getInstance("config");

    Pattern pattern = Pattern.compile(regEx);

    private static final String TAG = StartActivity.class.getSimpleName() + ">>>>>";

    @BindView(R.id.dev_prefix)
    TextView dev_prefix;

    @BindView(R.id.devid_input)
    EditText dev_suffix;


    @OnClick(R.id.next)
    void next() {
        if (pattern.matcher(dev_suffix.getText().toString()).matches()) {
            config.put("firstStart", false);
            config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
            config.put("daid", AppInit.getInstrumentConfig().getDev_prefix() + dev_suffix.getText().toString());
            ToastUtils.showLong("设备ID设置成功");
            AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
            StartActivity.this.finish();
        } else {
            ToastUtils.showLong("设备ID输入错误，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
        dev_prefix.setText(AppInit.getInstrumentConfig().getDev_prefix());
    }


}
