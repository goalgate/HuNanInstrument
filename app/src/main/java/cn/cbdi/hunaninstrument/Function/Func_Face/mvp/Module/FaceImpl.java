package cn.cbdi.hunaninstrument.Function.Func_Face.mvp.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.callback.CameraDataCallback;
import com.baidu.aip.callback.FaceDetectCallBack;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceTrackManager;
import com.baidu.aip.face.camera.AnotherPreviewManager;
import com.baidu.aip.face.camera.Camera1PreviewManager;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.blankj.utilcode.util.ToastUtils;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.UVCCamera;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.drv.card.ICardInfo;
import cn.cbdi.hunaninstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.hunaninstrument.Tool.FileUtils;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class FaceImpl implements IFace {

    boolean reg_status = false;

    private static final String TAG = BaseActivity.class.getSimpleName();


    private final static double livnessScore = 0;

    static FacePresenter.FaceAction action = FacePresenter.FaceAction.No_ACTION;

    private Handler handler = new Handler(Looper.getMainLooper());

    public static final int FEATURE_DATAS_UNREADY = 1;

    public static final int IDENTITY_IDLE = 2;

    public static final int IDENTITYING = 3;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private static final String TYPE_LIVENSS = "TYPE_LIVENSS";

    private static final int TYPE_RGB_LIVENSS = 2;

    private static final int mWidth = 640;

    private static final int mHeight = 480;

    private AutoTexturePreviewView mPreviewView;
    private AutoTexturePreviewView mPreviewView1;
    TextureView textureView;

    IFace.IFaceListener listener;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
        DBManager.getInstance().init(context);
        FaceSDKManager.getInstance().init(context, listener);
        livnessTypeTip();
    }


    @Override
    public void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView, IFaceListener listener) {
        this.listener = listener;
        startCameraPreview(context, previewView, previewView1, textureView);
    }

    Bitmap InputBitmap;

    Bitmap global_bitmap;

    ICardInfo InputCardInfo;

    Bitmap headphotoBW;

    Bitmap headphotoRGB;

    @Override
    public void Face_to_IMG(Bitmap bitmap) {
        action = FacePresenter.FaceAction.Headphoto_MATCH_IMG;
        this.InputBitmap = bitmap;
    }

    @Override
    public void onActivityDestroy() {

    }

    @Override
    public void Face_allView() {
//        Bitmap bmp = Bitmap.createBitmap(global_IFrame.getArgb(), global_IFrame.getWidth(), global_IFrame.getHeight(), Bitmap.Config.ARGB_8888);
        listener.onBitmap(FacePresenter.FaceResultType.AllView, global_bitmap);
    }


    @Override
    public void FaceSetNoAction() {
        action = FacePresenter.FaceAction.No_ACTION;
    }

    @Override
    public void setIdentifyStatus(int i) {
        identityStatus = i;
    }

    @Override
    public void FaceIdentify_model() {
        action = FacePresenter.FaceAction.Identify_Model;
    }

    @Override
    public void FaceIdentify() {
        IDCardPresenter.getInstance().stopReadCard();
        action = FacePresenter.FaceAction.Identify_ACTION;
        outOfIdentifyTime = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.onText(FacePresenter.FaceResultType.Identify_non, "尝试获取人脸超时,请重试");
                        MediaHelper.play(MediaHelper.Text.searchFace_outofTime);
                        IDCardPresenter.getInstance().readCard();

                    }
                });
    }

    @Override
    public void IMG_to_IMG(final Bitmap bmp1, final Bitmap bmp2,boolean register) {
        es.submit(new Runnable() {
            @Override
            public void run() {
                final byte[] bytes1 = new byte[512];
                final byte[] bytes2 = new byte[512];
                float ret1 = FaceApi.getInstance().extractVisFeature(bmp1, bytes1, 50);
                if (ret1 == 128) {
                    float ret2 = FaceApi.getInstance().extractVisFeature(bmp2, bytes2, 50);
                    if (ret2 == 128) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, String.valueOf((int) FaceApi.getInstance().match(bytes1, bytes2)));
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void FaceReg(ICardInfo cardInfo) {
        this.InputCardInfo = cardInfo;
        reg_status = false;
        action = FacePresenter.FaceAction.Reg_ACTION;
    }

    @Override
    public boolean FaceRegInBackGround(ICardInfo cardInfo, Bitmap bitmap) {
        return false;
    }

    @Override
    public void FaceReg(ICardInfo cardInfo, Bitmap bitmap) {
        IDCardPresenter.getInstance().stopReadCard();
        action = FacePresenter.FaceAction.Headphoto_MATCH_IMG;
        outOfRegTime = Observable.timer(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.onText(FacePresenter.FaceResultType.Reg_failed, "尝试获取人脸超时,请重试");
                        MediaHelper.play(MediaHelper.Text.searchFace_outofTime);
                        action = FacePresenter.FaceAction.No_ACTION;
                        IDCardPresenter.getInstance().readCard();
                    }
                });
        this.InputBitmap = bitmap;
        this.InputCardInfo = cardInfo;
    }

    @Override
    public void FaceIdentifyReady() {
//        if (identityStatus != FEATURE_DATAS_UNREADY) {
//            return;
//        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Log.e("sdsdsad", "人脸更新线程已启动");
                // android.os.Process.setThreadPriority (-4);
                FaceApi.getInstance().loadFacesFromDB("1");
                int count = FaceApi.getInstance().getGroup2Facesets().get("1").size();
                Log.e("人脸库内人脸数目:", String.valueOf(count));
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    @Override
    public void PreviewCease() {

        mPreviewView = null;
        mPreviewView1 = null;
        textureView = null;
        System.gc();
    }


    @Override
    public void PreviewCease(CeaseListener ceaseListener) {
        mPreviewView = null;
        mPreviewView1 = null;
        textureView = null;
        System.gc();
        ceaseListener.CeaseCallBack();
    }

    public void SetRegStatus(boolean reg_status) {
        this.reg_status = reg_status;
    }

    private void livnessTypeTip() {
        PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_LIVENSS);
    }

    private void startCameraPreview(final Context context, AutoTexturePreviewView previewView, final AutoTexturePreviewView previewView1, TextureView textureView) {
        // 设置前置摄像头
        // Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        this.mPreviewView = previewView;
        this.mPreviewView1 = previewView1;
        this.textureView = textureView;
        this.textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        this.textureView.setKeepScreenOn(true);
        this.mPreviewView.getTextureView().setScaleX(-1);
//        this.textureView.setScaleX(-1);
        FaceTrackManager.getInstance().setAliving(true); // 活体检测
        AnotherPreviewManager.getInstance().setCameraFacing(AnotherPreviewManager.CAMERA_FACING_BACK);
        AnotherPreviewManager.getInstance().startPreview(context, previewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(final int[] data, Camera camera, final int width, final int height) {
                    dealCameraData(data, width, height);
            }

            @Override
            public void onGetCameraData(int[] data, UVCCamera camera, int width, int height) {

            }
        });
    }

    public void dealCameraData(int[] data, int width, int height) {
//        if (selectType == TYPE_PREVIEWIMAGE_OPEN) {
//            showDetectImage(width, height, data); // 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
//        }
        // 摄像头预览数据进行人脸检测
        faceDetect(data, width, height);

    }


    private void faceDetect(int[] argb, int width, int height) {
//        if (liveType == LivenessSettingActivity.TYPE_NO_LIVENSS) {
//            FaceTrackManager.getInstance().setAliving(false); // 无活体检测
//        } else if (liveType == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
//            FaceTrackManager.getInstance().setAliving(true); // 活体检测
//        }
        FaceTrackManager.getInstance().faceTrack(argb, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                if(showFrame(livenessModel)){
                    checkResult(livenessModel);

                }
            }

            @Override
            public void onTip(int code, final String msg) {
//                displayTip(msg);
            }
        });
    }

    boolean livenessSuccess = false;

    private void checkResult(LivenessModel model) {
        if (model == null) {
            return;
        }
        livenessSuccess = (model.getRgbLivenessScore() > livnessScore) ? true : false;
        if (livenessSuccess) {
            switch (action) {
                case No_ACTION:
                    break;
                case Reg_ACTION:
                    outOfRegTime.dispose();
                    action = FacePresenter.FaceAction.No_ACTION;
                    reg_status = true;
                    headphotoBW = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
                    register(model.getFaceInfo(), model.getImageFrame(), InputCardInfo);
                    IDCardPresenter.getInstance().readCard();
                    //                    outOfRegTime.dispose();
//                    headphotoRGB = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
////                    action = FacePresenter.FaceAction.No_ACTION;
//                    if (Img_match_Img(model.getFaceInfo(), model.getImageFrame())) {
//                        reg_status = false;
//                        register(model.getFaceInfo(), model.getImageFrame(), InputCardInfo);
//                    } else {
////                        action = FacePresenter.FaceAction.Reg_ACTION;
//                    }
                    break;
                case Headphoto_MATCH_IMG:
                    headphotoRGB = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
                    if (Img_match_Img(model.getFaceInfo(), model.getImageFrame())) {
                        reg_status = false;
                        action = FacePresenter.FaceAction.Reg_ACTION;
                    } else {
                        action = FacePresenter.FaceAction.No_ACTION;
                        IDCardPresenter.getInstance().readCard();
                        outOfRegTime.dispose();
                    }
                    break;
                case Identify_ACTION:
                    identity(model.getImageFrame(), model.getFaceInfo());
                    break;
                default:
                    break;
            }
        }
//        }
    }


    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
        paint.setStrokeWidth(5);
    }

    RectF rectF = new RectF();

    private boolean showFrame(LivenessModel model) {
        boolean returnType = true;
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return false;
        }
        if (model == null) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
//            Log.e("canvasClear", "canvas by model is null");
            SwitchPresenter.getInstance().WhiteLighrOff();
            return false;
        }
        FaceInfo[] faceInfos = model.getTrackFaceInfo();
        ImageFrame imageFrame = model.getImageFrame();
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            Log.e("canvasClear", "canvas by faceInfo is null");
            return false;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        FaceInfo faceInfo = faceInfos[0];
        rectF.set(getFaceRectTwo(faceInfo, imageFrame));
        // 检测图片的坐标和显示的坐标不一样，需要转换。
        mapFromOriginalRect(rectF, faceInfo, imageFrame);
        float yaw2 = Math.abs(faceInfo.headPose[0]);
        float patch2 = Math.abs(faceInfo.headPose[1]);
        float roll2 = Math.abs(faceInfo.headPose[2]);
        if (yaw2 > 20 || patch2 > 20 || roll2 > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);
            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
        } else {
            // 符合检测要求，绘制绿框
            if (reg_status) {
                Log.e("rectF", "left:" + String.valueOf(rectF.left) + ",right" + String.valueOf(rectF.right));
                if (rectF.left < 100 || rectF.right > 400) {
                    returnType = false;
                }
            }
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
        if (returnType) {
            canvas.drawRect(rectF, paint);
            SwitchPresenter.getInstance().WhiteLighrOn();
        }
        textureView.unlockCanvasAndPost(canvas);
        return returnType;
    }

//    private void showFrame(LivenessModel model) {
//        Canvas canvas = textureView.lockCanvas();
//        if (canvas == null) {
//            textureView.unlockCanvasAndPost(canvas);
//            return;
//        }
//        if (model == null) {
//            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
//            Log.e("canvasClear", "canvas by model is null");
//            SwitchPresenter.getInstance().WhiteLighrOff();
//            return;
//        }
//        FaceInfo[] faceInfos = model.getTrackFaceInfo();
//        ImageFrame imageFrame = model.getImageFrame();
//        if (faceInfos == null || faceInfos.length == 0) {
//            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
//            Log.e("canvasClear", "canvas by faceInfo is null");
//            return;
//        }
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        FaceInfo faceInfo = faceInfos[0];
//        rectF.set(getFaceRectTwo(faceInfo, imageFrame));
//        // 检测图片的坐标和显示的坐标不一样，需要转换。
//        mapFromOriginalRect(rectF, faceInfo, imageFrame);
//        float yaw2 = Math.abs(faceInfo.headPose[0]);
//        float patch2 = Math.abs(faceInfo.headPose[1]);
//        float roll2 = Math.abs(faceInfo.headPose[2]);
//        if (yaw2 > 20 || patch2 > 20 || roll2 > 20) {
//            // 不符合要求，绘制黄框
//            paint.setColor(Color.YELLOW);
//            String text = "请正视屏幕";
//            float width = paint.measureText(text) + 50;
//            float x = rectF.centerX() - width / 2;
//            paint.setColor(Color.RED);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, x + 25, rectF.top - 20, paint);
//            paint.setColor(Color.YELLOW);
//        } else {
//            // 符合检测要求，绘制绿框
//            paint.setColor(Color.GREEN);
//        }
//        paint.setStyle(Paint.Style.STROKE);
//        // 绘制框
//        canvas.drawRect(rectF, paint);
//        SwitchPresenter.getInstance().WhiteLighrOn();
//        textureView.unlockCanvasAndPost(canvas);
//
//    }

    public Rect getFaceRectTwo(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);
        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        int width = (right - left);
        int height = (bottom - top);

        left = (int) ((faceInfo.mCenter_x - width / 2));
        top = (int) ((faceInfo.mCenter_y - height / 2));

        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (int) ((faceInfo.mCenter_x + width / 2));
        rect.bottom = (int) ((faceInfo.mCenter_y + height / 2));
        return rect;
    }

    public void mapFromOriginalRect(RectF rectF, FaceInfo faceInfo, ImageFrame imageFrame) {
        int selfWidth = mPreviewView.getPreviewWidth();
        int selfHeight = mPreviewView.getPreviewHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.getHeight() > selfHeight * imageFrame.getWidth()) {
            int targetHeight = imageFrame.getHeight() * selfWidth / imageFrame.getWidth();
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.getWidth();
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.getWidth() * selfHeight / imageFrame.getHeight();
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.getHeight();
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);
        if (false) { // 根据镜像调整
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    private boolean Img_match_Img(FaceInfo faceInfo, ImageFrame imageFrame) {
        final byte[] bytes1 = new byte[512];
        final byte[] bytes2 = new byte[512];
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
//        float ret1 = FaceApi.getInstance().extractVisFeature(bitmap, bytes1, 50);
        float ret1 = FaceApi.getInstance().extractVisFeature(headphotoRGB, bytes1, 50);
        if (ret1 == 128) {
            float ret2 = FaceApi.getInstance().extractVisFeature(InputBitmap, bytes2, 50);
            if (ret2 == 128) {
                if (FaceApi.getInstance().match(bytes1, bytes2) > 50) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(FacePresenter.FaceResultType.IMG_MATCH_IMG_True, bitmap);
                            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "人证比对通过,等待抽取特征值");
                        }
                    });
                    return true;
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, bitmap);
                            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对分数过低,请确认身份证是否与本人相符");
                            MediaHelper.play(MediaHelper.Text.imgtoimg_low);
                        }
                    });
                }
            }
        } else if (ret1 == -100) {
            toast("未完成人脸比对，可能原因，图片为空");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
                    MediaHelper.play(MediaHelper.Text.imgtoimg_failed);

                }
            });
        } else if (ret1 == -102) {
            toast("未完成人脸比对，可能原因，图片未检测到人脸");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
                    MediaHelper.play(MediaHelper.Text.imgtoimg_failed);

                }
            });
        } else {
            toast("人脸照片质量过低,未能完成人证比对,");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
                    MediaHelper.play(MediaHelper.Text.imgtoimg_failed);
                }
            });
        }
        return false;
    }

    private void register(final FaceInfo faceInfo, final ImageFrame imageFrame, final ICardInfo cardInfo) {
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
        // String uid = 修改为自己用户系统中用户的id;
        final User user = new User();
//        final String uid = UUID.randomUUID().toString();
        user.setUserId(cardInfo.cardId());
        user.setUserInfo(cardInfo.name());
        user.setGroupId("1");
        es.submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
                ARGBImg argbImg = FeatureUtils.getImageInfo(headphotoBW);
                byte[] bytes = new byte[512];
                float ret = FaceApi.getInstance().extractVisFeature(argbImg, bytes, 50);
//                if (ret == -1) {
//                    toast("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
////                    action = FacePresenter.FaceAction.Reg_ACTION;
//                } else
                if (ret != -1) {
                    Feature feature = new Feature();
                    feature.setGroupId("1");
                    feature.setUserId(cardInfo.cardId());
                    feature.setFeature(bytes);
                    user.getFeatureList().add(feature);
                    if (FaceApi.getInstance().userAdd(user)) {
                        Keeper keeper = new Keeper(cardInfo.cardId(),
                                cardInfo.name(),
                                FileUtils.bitmapToBase64(InputBitmap),
                                FileUtils.bitmapToBase64(headphotoRGB),
                                FileUtils.bitmapToBase64(headphotoBW),
                                user.getUserId(), bytes);
                        AppInit.getInstance().getDaoSession().getKeeperDao().insertOrReplace(keeper);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onBitmap(FacePresenter.FaceResultType.Reg_success, bitmap);
                                listener.onUser(FacePresenter.FaceResultType.Reg_success, user);
                                listener.onText(FacePresenter.FaceResultType.Reg_success, "人员数据已成功录入");
                                MediaHelper.play(MediaHelper.Text.Reg_success);

                            }
                        });
                        //saveFace(faceInfo,imageFrame,cardInfo);
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onText(FacePresenter.FaceResultType.Reg_failed, "人员数据录入失败");
                                MediaHelper.play(MediaHelper.Text.Reg_failed);

                            }
                        });
                        action = FacePresenter.FaceAction.Reg_ACTION;
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onText(FacePresenter.FaceResultType.Reg_failed, "抽取特征失败");
                            MediaHelper.play(MediaHelper.Text.Extract_features_failed);
                        }
                    });
                }
            }
        });
    }

    Disposable outOfIdentifyTime;
    Disposable outOfRegTime;

    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }
        final Bitmap scene_Bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }
        identityStatus = IDENTITYING;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        final IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, "1");
        if (identifyRet.getScore() < 80) {
            outOfIdentifyTime.dispose();
            identityStatus = IDENTITY_IDLE;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onText(FacePresenter.FaceResultType.Identify_non, "系统没有找到相关人脸信息");
                    MediaHelper.play(MediaHelper.Text.identify_non);
                    IDCardPresenter.getInstance().readCard();

                }
            });
            return;
        } else {
            outOfIdentifyTime.dispose();
            identityStatus = IDENTITY_IDLE;
            final User user = FaceApi.getInstance().getUserInfo("1", identifyRet.getUserId());
            if (user == null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onText(FacePresenter.FaceResultType.Identify_non, "系统没有找到相关人脸信息");
                        MediaHelper.play(MediaHelper.Text.identify_non);
                        IDCardPresenter.getInstance().readCard();

                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        FaceSetNoAction();
                        IDCardPresenter.getInstance().readCard();
                        listener.onBitmap(FacePresenter.FaceResultType.Identify, global_bitmap);
                        listener.onBitmap(FacePresenter.FaceResultType.headphoto, scene_Bitmap);
                        listener.onText(FacePresenter.FaceResultType.Identify, String.valueOf((int) identifyRet.getScore()));
                        listener.onUser(FacePresenter.FaceResultType.Identify, user);
                        MediaHelper.play(MediaHelper.Text.opertion_success);

                    }
                });
            }
        }
    }

    private void toast(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showLong(msg);
            }
        });
    }

    private void saveFace(FaceInfo faceInfo, ImageFrame imageFrame, ICardInfo cardInfo) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null) {
            String imageName = cardInfo.name() + "_" + cardInfo.cardId();
            File file = new File(faceDir, imageName);
            // 压缩人脸图片至300 * 300，减少网络传输时间
            ImageUtils.resize(bitmap, file, 300, 300);
        } else {
            toast("注册人脸目录未找到");
        }

    }

}
