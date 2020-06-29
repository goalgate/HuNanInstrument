package cn.cbdi.hunaninstrument.Tool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPState {

    private String ip_ = "127.0.0.1";
    private int port_ = 8000;
    private String name_ = "yzb_gz";
    private String daid_ = "000";
    private String data_ = null;

    //设置参数：服务器的IP和端口，daid设备ID，name项目名称
    public void setPar(String ip, int port, String daid, String name) {
        ip_ = ip;
        port_ = port;
        daid_ = daid;
        name_ = name;
    }

    //状态信息：door:1为关门，其它为开门；t为温度，h为湿度
    public String setState(int door, float t, float h) {
        data_ = daid_ + "," + name_ + "," + door + "," + t + "," + h + "#";
        return data_;
    }

    public void send() {
        if (data_ != null) {
            send(ip_, port_, data_);
        }
    }


    public void sendData(int door, float t, float h) {
        String s = setState(door, t, h);
        send(ip_, port_, s);
    }

    //发送数据
    public void send(String ip, int port, String data) {
        if (data == null || data.length() < 1) {
            return;
        }
        byte[] ds = data.getBytes();
        //创建InetAdress对象，封装自己的IP地址
        try {
            InetAddress inet = InetAddress.getByName(ip);
            DatagramPacket dp = new DatagramPacket(ds, ds.length, inet, port);
            //创建DatagramSocket对象，数据包的发送和接收对象
            DatagramSocket dsk = new DatagramSocket();
            //调用ds对象的方法send，发送数据包
            try {
                dsk.send(dp);
            } finally {
                dsk.close();
            }
        } catch (Exception ex) {

        }
    }

}


