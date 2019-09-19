package cn.cbdi.drv.card;

import android.graphics.Bitmap;

public interface ICardInfo {
    void close();
    void clearIsReadOk();
    Bitmap getBmp();
    void readCard();
    void stopReadCard();
    void ReadIC();
    void stopReadIC();
    void setDevType(String sType);
    int open();
    void readSam();
    String getSam();
    String cardId();
    String name();
    String getUid();
}
