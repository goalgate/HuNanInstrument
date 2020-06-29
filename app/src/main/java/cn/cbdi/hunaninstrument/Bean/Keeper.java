package cn.cbdi.hunaninstrument.Bean;

import android.graphics.Bitmap;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;


@Entity
public class Keeper {

    @Id(autoincrement = true)
    private Long id;

    String cardID;

    String name;

    String headphoto;

    String headphotoRGB;

    String headphotoBW;

    String FaceUserId;

    byte[] feature;

    @Generated(hash = 2033730960)
    public Keeper(Long id, String cardID, String name, String headphoto,
            String headphotoRGB, String headphotoBW, String FaceUserId,
            byte[] feature) {
        this.id = id;
        this.cardID = cardID;
        this.name = name;
        this.headphoto = headphoto;
        this.headphotoRGB = headphotoRGB;
        this.headphotoBW = headphotoBW;
        this.FaceUserId = FaceUserId;
        this.feature = feature;
    }

    public Keeper(String cardID, String name, String headphoto,
                  String headphotoRGB, String headphotoBW, String FaceUserId,
                  byte[] feature) {
        this.cardID = cardID;
        this.name = name;
        this.headphoto = headphoto;
        this.headphotoRGB = headphotoRGB;
        this.headphotoBW = headphotoBW;
        this.FaceUserId = FaceUserId;
        this.feature = feature;
    }

    @Generated(hash = 419749442)
    public Keeper() {
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
