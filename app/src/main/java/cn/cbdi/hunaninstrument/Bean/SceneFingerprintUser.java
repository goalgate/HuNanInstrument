package cn.cbdi.hunaninstrument.Bean;

import android.graphics.Bitmap;

public class SceneFingerprintUser {

    FingerprintUser user;

    private Bitmap ScenePhoto;

    private Bitmap SceneHeadPhoto;

    private int faceRecognition;

    public FingerprintUser getUser() {
        return user;
    }

    public Bitmap getScenePhoto() {
        return ScenePhoto;
    }

    public Bitmap getSceneHeadPhoto() {
        return SceneHeadPhoto;
    }

    public int getFaceRecognition() {
        return faceRecognition;
    }

    public void setUser(FingerprintUser user) {

        this.user = user;
    }

    public void setScenePhoto(Bitmap scenePhoto) {
        ScenePhoto = scenePhoto;
    }

    public void setSceneHeadPhoto(Bitmap sceneHeadPhoto) {
        SceneHeadPhoto = sceneHeadPhoto;
    }

    public void setFaceRecognition(int faceRecognition) {
        this.faceRecognition = faceRecognition;
    }
}
