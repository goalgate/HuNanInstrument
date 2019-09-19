package com.baidu.aip.face.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.baidu.aip.callback.CameraDataCallback;
import com.baidu.aip.face.ArgbPool;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;
import java.util.List;

public class UVCFrontPreviewManager implements TextureView.SurfaceTextureListener {


    Handler handler;
    Object Sync;
    private Context mContext;
    AutoTexturePreviewView mTextureView;
    // 预览尺寸
    private Size previewSize;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;

    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_USB = 2;

    private ArgbPool argbPool = new ArgbPool();

    /**
     * 垂直方向
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    public static final int ORIENTATION_HORIZONTAL = 1;

    /**
     * 当前相机的ID。
     */
    private int cameraFacing = CAMERA_FACING_FRONT;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private int previewWidth;
    private int previewHeight;

    private int videoWidth;
    private int videoHeight;

    private int tempWidth;
    private int tempHeight;

    private int textureWidth;
    private int textureHeight;

//    private Camera mCamera;

    private UVCCamera mUVCCamera;

    private int mCameraNum;

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int mirror = 1; // 镜像处理
    private CameraDataCallback mCameraDataCallback;
    private static volatile UVCFrontPreviewManager instance = null;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public static UVCFrontPreviewManager getInstance() {
        if (instance == null) {
            synchronized (UVCFrontPreviewManager.class) {
                if (instance == null) {
                    instance = new UVCFrontPreviewManager();
                }
            }
        }
        return instance;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    public void setCtrlBlock(USBMonitor.UsbControlBlock ctrlBlock, Handler handler, Object mSync) {
        this.ctrlBlock = ctrlBlock;
        this.Sync = mSync;
        this.handler = handler;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    /**
     * 开启预览
     *
     * @param context
     * @param textureView
     */
    public void startPreview(Context context, AutoTexturePreviewView textureView, int width,
                             int height, CameraDataCallback cameraDataCallback) {
        Log.e("chaixiaogang", "开启预览模式");
        this.mContext = context;
        this.mCameraDataCallback = cameraDataCallback;
        mTextureView = textureView;
        this.previewWidth = width;
        this.previewHeight = height;
        mSurfaceTexture = mTextureView.getTextureView().getSurfaceTexture();
        mTextureView.getTextureView().setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        Log.e("chaixiaogang", "--surfaceTexture--SurfaceTextureAvailable");
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        if (mSurfaceCreated) {
            textureWidth = i;
            textureHeight = i1;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openCamera();
                }
            }, 500);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
        Log.e("chaixiaogang", "--surfaceTexture--TextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        Log.e("chaixiaogang", "--surfaceTexture--destroyed");
        mSurfaceCreated = false;
        if (mUVCCamera != null) {
            try {
                mUVCCamera.setPreviewTexture(null);
                mSurfaceCreated = false;
                mTextureView = null;
                mUVCCamera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_YUV420SP);
                mUVCCamera.stopPreview();
                mUVCCamera.close();
                mUVCCamera.destroy();
                mUVCCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // Log.e("chaixiaogang", "--surfaceTexture--Updated");
    }


    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mUVCCamera != null) {
            try {
                mUVCCamera.setPreviewTexture(null);
                mSurfaceCreated = false;
                mTextureView = null;
                mUVCCamera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_YUV420SP);
                mUVCCamera.stopPreview();
                mUVCCamera.close();
                mUVCCamera.destroy();
                mUVCCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开启摄像头
     */

    private void openCamera() {
        try {
            if (mUVCCamera == null) {
//                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
//                    Camera.getCameraInfo(i, cameraInfo);
//                    if (cameraInfo.facing == cameraFacing) {
//                        cameraId = i;
//                    }
//                }
                mUVCCamera = new UVCCamera();
                mUVCCamera.open(ctrlBlock);
                try {
                    mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG, 0.5f);
                } catch (final IllegalArgumentException e) {
                    // fallback to YUV mode
                    try {
                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE, 1.0f);
                    } catch (final IllegalArgumentException e1) {
                        mUVCCamera.destroy();
                        return;
                    }
                }
                Log.e("chaixiaogang", "initCamera---open camera");
            }

            int detectRotation = 0; // 人脸实际检测角度
            int cameraRotation = 0; // 摄像头图像预览角度
//            if (cameraFacing == CAMERA_FACING_FRONT) {
//                cameraRotation = ORIENTATIONS.get(displayOrientation);
//                cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId, mCamera);
//                mCamera.setDisplayOrientation(cameraRotation);
//                detectRotation = cameraRotation;
//                if (detectRotation == 90 || detectRotation == 270) {
//                    detectRotation = (detectRotation + 180) % 360;
//                }
//            } else if (cameraFacing == CAMERA_FACING_BACK) {
//                cameraRotation = ORIENTATIONS.get(displayOrientation);
//                cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId, mCamera);
//                mCamera.setDisplayOrientation(cameraRotation);
//                detectRotation = cameraRotation;
//            } else if (cameraFacing == CAMERA_USB) {
//                mCamera.setDisplayOrientation(0);
//                detectRotation = 0;
//            }
//            if (cameraRotation == 90 || cameraRotation == 270) {
//                // 旋转90度或者270，需要调整宽高
//                mTextureView.setPreviewSize(previewHeight, previewWidth);
//            } else {
//                mTextureView.setRotationY(180); // TextureView旋转90度
//                mTextureView.setPreviewSize(previewWidth, previewHeight);
//            }
//            Camera.Parameters params = mCamera.getParameters();
//            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
//            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, previewWidth,
//                    previewHeight); // 获取一个最为适配的camera.size
//            if (optionSize.width == previewWidth && optionSize.height == previewHeight) {
//                videoWidth = previewWidth;
//                videoHeight = previewHeight;
//            } else {
//                videoWidth = optionSize.width;
//                videoHeight = optionSize.height;
//            }
//            tempWidth = videoWidth;
//            tempHeight = videoHeight;
//            params.setPreviewSize(videoWidth, videoHeight);
//            mCamera.setParameters(params);
            videoWidth = previewWidth;
            videoHeight = previewHeight;
            tempWidth = videoWidth;
            tempHeight = videoHeight;
            try {
                mUVCCamera.setPreviewTexture(mSurfaceTexture);
                mUVCCamera.startPreview();
                final int finalDetectRotation = detectRotation;
                mUVCCamera.setFrameCallback(new IFrameCallback() {
                    @Override
                    public void onFrame(ByteBuffer frame) {
                        byte[] bytes = decodeValue(frame);
                        int[] argb = argbPool.acquire(videoWidth, videoHeight);
                        if (argb == null || argb.length != videoWidth * videoHeight) {
                            argb = new int[videoWidth * videoHeight];
                        }
                        // 人脸检测的角度旋转了90或270度。高宽需要替换
                        if (finalDetectRotation % 180 == 90) {
                            if (videoWidth != tempHeight && videoHeight != tempWidth) {
                                int temp = videoWidth;
                                videoWidth = videoHeight;
                                videoHeight = temp;
                            }
                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoHeight,
                                    videoWidth, argb, finalDetectRotation, 1);
                        } else {
                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoWidth,
                                    videoHeight, argb, finalDetectRotation, 1);
                        }
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(argb, mUVCCamera,
                                    videoWidth, videoHeight);
                        }
                        argbPool.release(argb);
                    }
                }, UVCCamera.PIXEL_FORMAT_YUV420SP);
//                mUVCCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] bytes, Camera camera) {
//                        int[] argb = argbPool.acquire(videoWidth, videoHeight);
//                        if (argb == null || argb.length != videoWidth * videoHeight) {
//                            argb = new int[videoWidth * videoHeight];
//                        }
//                        // 人脸检测的角度旋转了90或270度。高宽需要替换
//                        if (finalDetectRotation % 180 == 90) {
//                            if (videoWidth != tempHeight && videoHeight != tempWidth) {
//                                int temp = videoWidth;
//                                videoWidth = videoHeight;
//                                videoHeight = temp;
//                            }
//                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoHeight,
//                                    videoWidth, argb, finalDetectRotation, 1);
//                        } else {
//                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoWidth,
//                                    videoHeight, argb, finalDetectRotation, 1);
//                        }
//                        if (mCameraDataCallback != null) {
//                            mCameraDataCallback.onGetCameraData(argb, camera,
//                                    videoWidth, videoHeight);
//                        }
//                        argbPool.release(argb);
//                    }
//                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("chaixiaogang", e.getMessage());
            }
        } catch (RuntimeException e) {
            Log.e("chaixiaogang", e.getMessage());
        }
    }


    private int getCameraDisplayOrientation(int degrees, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }


    /**
     * 解决预览变形问题
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void release() {
        instance = null;
    }

    public byte[] decodeValue(ByteBuffer bytes) {
        int len = bytes.limit() - bytes.position();
        byte[] bytes1 = new byte[len];
        bytes.get(bytes1);
        return bytes1;
    }

}
