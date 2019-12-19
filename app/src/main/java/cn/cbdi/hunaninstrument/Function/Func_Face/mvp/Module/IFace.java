package cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;

import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.drv.card.ICardInfo;


public interface IFace {

    void FaceInit(Context context, FaceSDKManager.SdkInitListener listener);

    void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1,TextureView textureView, IFaceListener listener);

    void FaceIdentify();

    void FaceIdentify_model();

    void FaceReg(ICardInfo cardInfo, Bitmap bitmap) ;

    void FaceReg(ICardInfo cardInfo);

    boolean FaceRegInBackGround(ICardInfo cardInfo, Bitmap bitmap, String ps);

    void Face_to_IMG(Bitmap bitmap);

    void IMG_to_IMG(Bitmap bmp1,Bitmap bmp2,boolean register);

    void Face_allView();

    void FaceSetNoAction();

    void setIdentifyStatus(int i);

    void FaceIdentifyReady();

    void SetRegStatus(boolean status);

    void PreviewCease();

    void PreviewCease(CeaseListener listener);

    void onActivityDestroy();

    interface IFaceListener{
        void onText(FacePresenter.FaceResultType resultType, String text);

        void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap);

        void onUser(FacePresenter.FaceResultType resultType, User user);
    }

    interface CeaseListener{
        void CeaseCallBack();
    }



}
