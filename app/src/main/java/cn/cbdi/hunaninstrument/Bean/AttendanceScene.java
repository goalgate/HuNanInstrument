package cn.cbdi.hunaninstrument.Bean;

import android.graphics.Bitmap;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AttendanceScene {

    @Id(autoincrement = true)
    private Long id;

    private String cardID;

    private String name;

    private String headphoto;

    private String headphotoRGB;

    private String headphotoBW;

    private String FaceUserId;

    private byte[] feature;

    private String ScenePhoto;

    private String SceneHeadPhoto;

    private int faceRecognition;

    private String AttendanceTime;

    private Integer alive;


    public AttendanceScene(Long id, String name, String scenePhoto, String sceneHeadPhoto, String attendanceTime,int alive, int faceRecognition) {
        this.id = id;
        this.name = name;
        this.ScenePhoto = scenePhoto;
        this.SceneHeadPhoto = sceneHeadPhoto;
        this.AttendanceTime = attendanceTime;
        this.alive = alive;
        this.faceRecognition = faceRecognition;
    }

    @Generated(hash = 1589705504)
    public AttendanceScene(Long id, String cardID, String name, String headphoto, String headphotoRGB, String headphotoBW,
            String FaceUserId, byte[] feature, String ScenePhoto, String SceneHeadPhoto, int faceRecognition, String AttendanceTime,
            Integer alive) {
        this.id = id;
        this.cardID = cardID;
        this.name = name;
        this.headphoto = headphoto;
        this.headphotoRGB = headphotoRGB;
        this.headphotoBW = headphotoBW;
        this.FaceUserId = FaceUserId;
        this.feature = feature;
        this.ScenePhoto = ScenePhoto;
        this.SceneHeadPhoto = SceneHeadPhoto;
        this.faceRecognition = faceRecognition;
        this.AttendanceTime = AttendanceTime;
        this.alive = alive;
    }

    @Generated(hash = 1931040225)
    public AttendanceScene() {
    }

    public void setKeeper(Keeper keeper) {
        this.cardID = keeper.cardID;
        this.name = keeper.name;
        this.headphoto = keeper.headphoto;
        this.headphotoRGB = keeper.headphotoRGB;
        this.headphotoBW = keeper.headphotoBW;
        this.FaceUserId = keeper.FaceUserId;
        this.feature = keeper.feature;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardID() {
        return this.cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadphoto() {
        return this.headphoto;
    }

    public void setHeadphoto(String headphoto) {
        this.headphoto = headphoto;
    }

    public String getHeadphotoRGB() {
        return this.headphotoRGB;
    }

    public void setHeadphotoRGB(String headphotoRGB) {
        this.headphotoRGB = headphotoRGB;
    }

    public String getHeadphotoBW() {
        return this.headphotoBW;
    }

    public void setHeadphotoBW(String headphotoBW) {
        this.headphotoBW = headphotoBW;
    }

    public String getFaceUserId() {
        return this.FaceUserId;
    }

    public void setFaceUserId(String FaceUserId) {
        this.FaceUserId = FaceUserId;
    }

    public byte[] getFeature() {
        return this.feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public String getScenePhoto() {
        return this.ScenePhoto;
    }

    public void setScenePhoto(String ScenePhoto) {
        this.ScenePhoto = ScenePhoto;
    }

    public String getSceneHeadPhoto() {
        return this.SceneHeadPhoto;
    }

    public void setSceneHeadPhoto(String SceneHeadPhoto) {
        this.SceneHeadPhoto = SceneHeadPhoto;
    }

    public int getFaceRecognition() {
        return this.faceRecognition;
    }

    public void setFaceRecognition(int faceRecognition) {
        this.faceRecognition = faceRecognition;
    }

    public String getAttendanceTime() {
        return this.AttendanceTime;
    }

    public void setAttendanceTime(String AttendanceTime) {
        this.AttendanceTime = AttendanceTime;
    }

    public Integer getAlive() {
        return this.alive;
    }

    public void setAlive(Integer alive) {
        this.alive = alive;
    }




}
