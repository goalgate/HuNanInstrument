package cn.cbdi.hunaninstrument.Tool;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.cbdi.hunaninstrument.AppInit;

public class MediaHelper {
    public enum Text {
        Reg_success, Reg_failed, imgtoimg_low, imgtoimg_failed,
        reg_model, Extract_features_failed,
        opertion_success, waiting, identify_non, searchFace_outofTime,
        man_non, normal_model
    }

    private static MediaPlayer mediaPlayer;

    public static void mediaOpen() {
        mediaPlayer = new MediaPlayer();

        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(AppInit.getContext(), null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaPlayer, subtitleInstance, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loudly() {
        AudioManager audioMgr = (AudioManager) AppInit.getContext().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
        Log.e("信息提示", "打开音量");
    }

    public static void play(Text text) {
        if (AppInit.getInstrumentConfig().noise()) {
            try {
                AssetFileDescriptor fileDescriptor;
                switch (text) {
                    case Reg_success:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "人员数据已成功录入.mp3");
                        play(fileDescriptor);
                        break;
                    case Reg_failed:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "人员数据录入失败.mp3");
                        play(fileDescriptor);
                        break;
                    case imgtoimg_low:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "人证比对分数过低,请确认身份证是否与本人相符.mp3");
                        play(fileDescriptor);
                        break;
                    case imgtoimg_failed:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "人证比对失败,请重试.mp3");
                        play(fileDescriptor);
                        break;
                    case reg_model:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "您已进入录入人员模式.mp3");
                        play(fileDescriptor);
                        break;
                    case Extract_features_failed:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "抽取特征失败.mp3");
                        play(fileDescriptor);
                        break;
                    case opertion_success:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "操作成功.mp3");
                        play(fileDescriptor);
                        break;
                    case waiting:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "等待处理结果.mp3");
                        play(fileDescriptor);
                        break;
                    case identify_non:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "系统没有找到相关人脸信息.mp3");
                        play(fileDescriptor);
                        break;
                    case searchFace_outofTime:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "尝试获取人脸超时,请重试.mp3");
                        play(fileDescriptor);
                        break;
                    case man_non:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "系统查无此人.mp3");
                        play(fileDescriptor);
                        break;
                    case normal_model:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "您已进入正常操作模式.mp3");
                        play(fileDescriptor);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void play(AssetFileDescriptor fileDescriptor) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void mediaRealese() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            Log.e("信息提示", "mediaPlayer解除函数被触发");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}



