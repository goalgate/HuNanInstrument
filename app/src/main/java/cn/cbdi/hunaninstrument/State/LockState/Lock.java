package cn.cbdi.hunaninstrument.State.LockState;


import android.util.Log;

import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by zbsz on 2017/9/28.
 */

public class Lock {

    public enum LockState {
        STATE_Lockup, STATE_Unlock
    }

    public void setState(LockState state) {
        this.state = state;
    }

    public LockState getState() {
        return state;
    }

    private LockState state = LockState.STATE_Lockup;

    private static Lock instance = null;

    private Lock(){

    }

    public static Lock getInstance() {
        if (instance == null)
            instance = new Lock();
        return instance;
    }

//    private Lock(LockState lockState) {
//        this.state = lockState;
//    }
//
//
//    public static Lock getInstance(LockState lockState) {
//        if (instance == null)
//            instance = new Lock(lockState);
//        return instance;
//    }

    public void doNext() {
        switch (state) {
            case STATE_Lockup:
                SwitchPresenter.getInstance().OutD9(true);
                if (AppInit.getInstrumentConfig().disAlarm()) {
                    Observable.timer(60, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Long aLong) {
                                    SwitchPresenter.getInstance().OutD9(false);
                                    Log.e("信息提示", "报警已消除");
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
                break;
            case STATE_Unlock:
                SwitchPresenter.getInstance().OutD9(false);
                break;
            default:
                break;
        }
    }
//    private LockState lockState;
//
//    private static Lock instance = null;
//
//    public static Lock getInstance(){
//        return instance;
//    }
//
//    public static Lock getInstance(LockState lockState){
//        if (instance == null)
//            instance = new Lock(lockState);
//        return instance;
//    }
//    private Lock(LockState lockState) {
//        this.lockState = lockState;
//    }
//
//    public LockState getLockState() {
//        return lockState;
//    }
//
//    public void setLockState(LockState lockState) {
//        this.lockState = lockState;
//    }
//
//    public void doNext(){
//        lockState.onHandle(this);
//    }


}
