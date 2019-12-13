package cn.cbdi.hunaninstrument.UI;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Config.Attendance_Config;
import cn.cbdi.hunaninstrument.R;


/**
 * Created by zbsz on 2017/7/16.
 */


public class SuperWindow2 extends PopupWindow implements View.OnClickListener {

    private View mContentView;
    private Activity mActivity;
    OptionTypeListener listener;
    Button add;
    Button server;
    Button ipSetting;


    public SuperWindow2(Activity activity) {
        mActivity = activity;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mContentView = LayoutInflater.from(activity).inflate(R.layout.window_super2, null);
        setContentView(mContentView);
        add = (Button) mContentView.findViewById(R.id.btn_add);
        server = (Button) mContentView.findViewById(R.id.btn_server);
        ipSetting = (Button) mContentView.findViewById(R.id.btn_ipSetting);

        add.setOnClickListener(this);
        server.setOnClickListener(this);
        ipSetting.setOnClickListener(this);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setAnimationStyle(R.style.Person_type_Popup);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                lighton();
            }
        });
    }


    private void lighton() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = 1.0f;
        mActivity.getWindow().setAttributes(lp);
    }

    private void lightoff() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();

        lp.alpha = 0.3f;
        mActivity.getWindow().setAttributes(lp);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        lightoff();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        lightoff();
        super.showAtLocation(parent, gravity, x, y);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                listener.onSuperOptionType(add, 1);
                break;
            case R.id.btn_server:
                listener.onSuperOptionType(server, 2);
                break;
            case R.id.btn_ipSetting:
                listener.onSuperOptionType(ipSetting, 3);
                break;
            default:
                break;
        }
    }

    public interface OptionTypeListener {
        void onSuperOptionType(Button view, int type);
    }

    public void setOptionTypeListener(OptionTypeListener listener) {
        this.listener = listener;
    }

}

