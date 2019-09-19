package cn.cbdi.hunaninstrument.State.DoorState;

import org.greenrobot.eventbus.EventBus;

import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.State.LockState.Lock;

/**
 * Created by zbsz on 2017/9/27.
 */

public class Door {

    public enum DoorState {
        State_Open, State_Close
    }

    private DoorState mdoorState = DoorState.State_Close;

    private Door() {
    }

    private static Door instance = null;

    public static Door getInstance() {
        if (instance == null)
            instance = new Door();
        return instance;
    }

    public void setMdoorState(DoorState mdoorState) {
        this.mdoorState = mdoorState;
    }

    public DoorState getMdoorState() {
        return mdoorState;
    }

    public void doNext() {
        switch (mdoorState) {
            case State_Open:
                if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                    EventBus.getDefault().post(new OpenDoorEvent(false));
                } else if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Unlock)) {
                    EventBus.getDefault().post(new OpenDoorEvent(true));
                }
                Lock.getInstance().doNext();
                break;
            case State_Close:
                break;
            default:
                break;
        }
    }

//    private DoorState doorState;
//
//    private Door(DoorState doorState) {
//        this.doorState = doorState;
//    }
//
//    public DoorState getDoorState() {
//        return doorState;
//    }
//
//    public void setDoorState(DoorState doorState) {
//        this.doorState = doorState;
//    }
//
//
//    public void doNext(){
//        doorState.onHandle(this);
//    }
}
