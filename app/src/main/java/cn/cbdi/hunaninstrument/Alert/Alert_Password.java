package cn.cbdi.hunaninstrument.Alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.baidu.aip.api.FaceApi;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ToastUtils;


import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.UI.PasswordInputView;



public class Alert_Password {
    private Context context;

    public Alert_Password(Context context) {
        this.context = context;
    }
    private AlertView passwordAlert;
    private PasswordInputView passwordInputView;
    public void PasswordViewInit(final Callback callback) {
        ViewGroup passwordView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputpassword_form, null);
        passwordInputView = (PasswordInputView) passwordView.findViewById(R.id.passwordInputView);
        passwordAlert = new AlertView("通知:", "请输入密码以进入设置界面", "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (passwordInputView.getText().toString().equals("665901")) {
                        callback.normal_call();
                    }  else if (passwordInputView.getText().toString().equals("578412")) {
                        FaceApi.getInstance().groupDelete("1");
                        ToastUtils.showLong("人脸数据库已被全部清除");
                        FacePresenter.getInstance().FaceIdentifyReady();
                        FacePresenter.getInstance().FaceIdentify_model();
                    }else{
                        ToastUtils.showLong("密码错误，请重试");
                    }
                }

            }
        });
        passwordAlert.addExtView(passwordView);
    }

    public void show(){
        passwordInputView.setText(null);
        passwordAlert.show();
    }

    public interface Callback{
        void normal_call();
    }
}
