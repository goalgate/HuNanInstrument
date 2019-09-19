package cn.cbdi.hunaninstrument.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
@Entity
public class Employer {

    @Id
    String cardID;

    int type;

    @Generated(hash = 461306795)
    public Employer(String cardID, int type) {
        this.cardID = cardID;
        this.type = type;
    }

    @Generated(hash = 807684890)
    public Employer() {
    }

    public String getCardID() {
        return this.cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
