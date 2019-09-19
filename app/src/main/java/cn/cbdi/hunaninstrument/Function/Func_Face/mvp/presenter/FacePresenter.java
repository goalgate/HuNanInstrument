package cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;
import com.blankj.utilcode.util.ToastUtils;

//import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.FaceImpl2;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.HuNanFaceImpl;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module.IFace;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.drv.card.ICardInfo;


public class FacePresenter {
    private IFaceView view;

    //    private IFace iFace = AppInit.getInstance().getFaceImpl();
    private IFace iFace = AppInit.getInstrumentConfig().getFaceImpl();

    private static FacePresenter instance = null;

    public enum FaceAction {
        No_ACTION, Reg_ACTION, Identify_ACTION, Headphoto_MATCH_IMG,
        IMG_MATCH_IMG, Identify_Model
    }

    public enum FaceResultType {
        Reg_success, Reg_failed, Identify, IMG_MATCH_IMG_False, IMG_MATCH_IMG_True, IMG_MATCH_IMG_Error,
        AllView, Identify_non, headphoto, IMG_MATCH_IMG_Score
    }

    private FacePresenter() {
    }

    public static FacePresenter getInstance() {
        if (instance == null) {
            instance = new FacePresenter();
        }
        return instance;
    }

    public void FacePresenterSetView(IFaceView view) {
        this.view = view;
    }

    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
        try {
            iFace.FaceInit(context, listener);
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceSetNoAction() {
        try {
            iFace.FaceSetNoAction();
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView) {

        try {

            iFace.CameraPreview(context, previewView, previewView1, textureView, new IFace.IFaceListener() {
                @Override
                public void onText(FaceResultType resultType, String text) {
                    try {
                        view.onText(resultType, text);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmap(FaceResultType resultType, Bitmap bitmap) {
                    try {
                        view.onBitmap(resultType, bitmap);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUser(FaceResultType resultType, User user) {
                    try {
                        view.onUser(resultType, user);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceIdentify() {
        try {
            iFace.FaceIdentify();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }


    public void FaceIdentifyReady() {
        try {
            iFace.FaceIdentifyReady();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceIdentify_model() {
        try {
            iFace.FaceIdentify_model();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceGetAllView() {
        try {
            iFace.Face_allView();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceReg(ICardInfo cardInfo, Bitmap bitmap) {
        try {
            iFace.FaceReg(cardInfo, bitmap);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void FaceReg(ICardInfo cardInfo) {
        try {
            iFace.FaceReg(cardInfo);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }


    public void Face_to_IMG(Bitmap bitmap) {
        try {
            iFace.Face_to_IMG(bitmap);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void IMG_to_IMG(Bitmap bmp1, Bitmap bmp2,boolean register) {
        try {
            iFace.IMG_to_IMG(bmp1, bmp2, register);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void setIdentifyStatus(int identifyStatus) {
        try {
            iFace.setIdentifyStatus(identifyStatus);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void PreviewCease() {
        try {
            iFace.PreviewCease();

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void PreviewCease(IFace.CeaseListener listener) {
        try {
            iFace.PreviewCease(listener);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void SetRegStatus(boolean status) {
        try {
            iFace.SetRegStatus(status);

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    public void onDestroy() {
        try {
            iFace.onActivityDestroy();
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }

    }


}
