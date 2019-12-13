package cn.cbdi.hunaninstrument.Activity_Attendance;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

import cn.cbdi.hunaninstrument.R;


public class Alert_twoPic {

    private Context context;

    private AlertView twoPic;

    ImageView iv_headPhoto;

    ImageView iv_ScenePhoto;

    public Alert_twoPic(Context context) {
        this.context = context;
    }

    public void Init() {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.layout_twopic, null);
        iv_headPhoto = (ImageView) extView1.findViewById(R.id.iv_headPhoto);
        iv_ScenePhoto = (ImageView) extView1.findViewById(R.id.iv_ScenePhoto);
        twoPic = new AlertView("身份证头像照片及现场照片", null, "关闭", null, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if(position == 0){

                }
            }
        });
        twoPic.addExtView(extView1);
    }

    public void show(Bitmap headPhoto, Bitmap ScenePhoto){
        iv_headPhoto.setImageBitmap(headPhoto);
        iv_ScenePhoto.setImageBitmap(ScenePhoto);
        twoPic.show();
    }

    public boolean isShowing(){
        return twoPic.isShowing();
    }
}
