package cn.cbdi.hunaninstrument.Tool;

/**
 * Created by zbsz on 2017/11/28.
 */


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *数据的安全检查(通过的数据才处理)
 *1、通过设备设置的服务器地址作加密因子，可防止接到其它服务器上获取到加密数据
 *2、通过设备ID（设备ID或登录系统后取到的ID）作加密因子，不同的设备使用不同的加密因子
 *
 *
 */

public class SafeCheck {


    private byte[] key_=new byte[5];

    public SafeCheck()
    {
        //加密因子设置
        SafeCheckKey sck=new SafeCheckKey();
        setURL(sck.getUrl());

    }

    private String id_ = "";

    public void setDaid(String id)
    {
        id_ = id;
    }

    //认证信息
    public String daidPass()
    {
        String s = "&daid="+id_+"&pass="+getPass(id_);
        return s;
    }

    //取认证数据
    public String getPass(String id)
    {
        if (id == null) {return "";}
        SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String   date   =   sDateFormat.format(new   java.util.Date());

        String s=id+"|"+date;
        byte[] data;
        try {
            data =encrypt(s, id).getBytes("utf-8");
            return new BASE64Encoder().encode(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    //设置上传的服务
    public void setURL(String d)
    {
        String s = d.toLowerCase();
        try {
            byte[] keyArray = s.getBytes("utf-8");
            if (keyArray == null) {
                return;
            }
            int j = 0;
            int k = keyArray.length;

            for (int i = 0; i < 5; i++) {
                key_[i] = 0;
            }
            for (int i = 0; i < k; i++) {
                if (j > 4) {
                    j = 0;
                }
                key_[j] = (byte) (key_[j] ^ keyArray[i]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    //是否是采集器登录数据
    public boolean isData(String pass,String id)
    {
        if (pass == null || id == null)
            return false;
        try {
            byte[] bytes = new BASE64Decoder().decodeBuffer(pass);
            String s=new String(bytes, "utf-8");
            s = decrypt(s, id);
            if (s.equals("")) {
                return false;
            } else if (s.indexOf(id) >= 0) {
                return true;
            }
        } catch (Exception ex) {
        }
        ;

        return false;

    }

    //解密
    public  String decrypt(String str, String id) {
        if (str == null || id == null)
            return "";
        try {
            if (str.length() < 5) {
                return "";
            }
            String ky = str.substring(0, 4) + id + "cb";
            if (ky == null) {
                return "";
            }
            if (ky.length() < 16) {
                return "";
            }
            byte[] keyArray = ky.getBytes("utf-8");
            int j = 0;
            for (int i = 0; i < 10; i++) {
                if (j > 4) {
                    j = 0;
                }
                keyArray[i] = (byte) (key_[j] ^ keyArray[i]);
            }
            String data = str.substring(4);

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyArray,"AES"));
            byte[] bytes = new BASE64Decoder().decodeBuffer(data);
            bytes = cipher.doFinal(bytes);
            return new String(bytes, "utf-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    //随机数
    private String ramStr()
    {
        byte[] bs = new byte[10];
        Random rd = new Random();
        for (int i = 0; i < 5; i++)
        {
            int ir = rd.nextInt(255)+1;
            bs[i] = (byte)ir;
        }
        String s=new BASE64Encoder().encode(bs);
        return s.substring(0, 4);
    }


    //加密
    public String encrypt(String toEncrypt,String id)
    {
        String rs = ramStr();
        String ky = rs + id + "cb";
        if (ky == null) { return ""; }
        if (ky.length() < 16) { return ""; }
        try
        {

            byte[] keyArray =ky.getBytes("utf-8");
            int j = 0;
            for (int i = 0; i < 10; i++)
            {
                if (j > 4)
                {
                    j = 0;
                }
                keyArray[i] = (byte)(key_[j] ^ keyArray[i]);
            }

            byte[] toEncryptArray =toEncrypt.getBytes("utf-8");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyArray,"AES"));
            byte[] resultArray = cipher.doFinal(toEncryptArray);
            return rs + new BASE64Encoder().encode(resultArray);
        }
        catch(Exception ex) { return ""; }
    }

}