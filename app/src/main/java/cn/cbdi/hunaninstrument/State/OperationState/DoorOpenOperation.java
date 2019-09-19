package cn.cbdi.hunaninstrument.State.OperationState;

public class DoorOpenOperation {

    public enum DoorOpenState {
        Locking, OneUnlock, TwoUnlock
    }


    private DoorOpenState state = DoorOpenState.Locking;

    private static DoorOpenOperation instance = null;

    private DoorOpenOperation() {

    }

    public static DoorOpenOperation getInstance() {
        if (instance == null)
            instance = new DoorOpenOperation();
        return instance;
    }

    public void setmDoorOpenOperation(DoorOpenState mDoorOpenState) {
        this.state = mDoorOpenState;
    }

    public DoorOpenState getmDoorOpenOperation() {
        return state;
    }

    public void doNext() {
        switch (state) {
            case Locking:
                state = DoorOpenState.OneUnlock;
                break;
            case OneUnlock:
                state = DoorOpenState.TwoUnlock;
                break;
            case TwoUnlock:
                state = DoorOpenState.Locking;
                break;
            default:
                break;
        }
    }

}
