package cn.cbdi.hunaninstrument.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FingerprintUser {

    @Id(autoincrement = true)
    private Long id;

    private String courIds;

    private String cardId;

    private String name;

    private String fingerprintPhoto;

    private String fingerprintId;

    private String fingerprintKey;

    private String courType;

    private String headphoto;

    private String headphotoRGB;

    private String headphotoBW;

    private String FaceUserId;

    private byte[] feature;

    @Generated(hash = 1435164010)
    public FingerprintUser(Long id, String courIds, String cardId, String name,
            String fingerprintPhoto, String fingerprintId, String fingerprintKey,
            String courType, String headphoto, String headphotoRGB,
            String headphotoBW, String FaceUserId, byte[] feature) {
        this.id = id;
        this.courIds = courIds;
        this.cardId = cardId;
        this.name = name;
        this.fingerprintPhoto = fingerprintPhoto;
        this.fingerprintId = fingerprintId;
        this.fingerprintKey = fingerprintKey;
        this.courType = courType;
        this.headphoto = headphoto;
        this.headphotoRGB = headphotoRGB;
        this.headphotoBW = headphotoBW;
        this.FaceUserId = FaceUserId;
        this.feature = feature;
    }

    @Generated(hash = 350681683)
    public FingerprintUser() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourIds() {
        return this.courIds;
    }

    public void setCourIds(String courIds) {
        this.courIds = courIds;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFingerprintPhoto() {
        return this.fingerprintPhoto;
    }

    public void setFingerprintPhoto(String fingerprintPhoto) {
        this.fingerprintPhoto = fingerprintPhoto;
    }

    public String getFingerprintId() {
        return this.fingerprintId;
    }

    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public String getFingerprintKey() {
        return this.fingerprintKey;
    }

    public void setFingerprintKey(String fingerprintKey) {
        this.fingerprintKey = fingerprintKey;
    }

    public String getCourType() {
        return this.courType;
    }

    public void setCourType(String courType) {
        this.courType = courType;
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

 

}
