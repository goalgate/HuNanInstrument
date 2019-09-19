package cn.cbdi.hunaninstrument.Bean;

import android.graphics.Bitmap;

public class SceneKeeper {

    private Keeper keeper;

    private Bitmap ScenePhoto;

    private Bitmap SceneHeadPhoto;

    private int faceRecognition;
    public void setSceneHeadPhoto(Bitmap sceneHeadPhoto) {
        SceneHeadPhoto = sceneHeadPhoto;
    }

    public Bitmap getSceneHeadPhoto() {

        return SceneHeadPhoto;
    }

    public void setFaceRecognition(int faceRecognition) {
        this.faceRecognition = faceRecognition;
    }

    public Keeper getKeeper() {
        return keeper;
    }

    public Bitmap getScenePhoto() {
        return ScenePhoto;
    }

    public int getFaceRecognition() {
        return faceRecognition;
    }

    public void setKeeper(Keeper keeper) {
        this.keeper = keeper;
    }

    public void setScenePhoto(Bitmap scenePhoto) {
        ScenePhoto = scenePhoto;
    }


}
