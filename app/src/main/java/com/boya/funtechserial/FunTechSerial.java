package com.boya.funtechserial;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.UnsupportedEncodingException;

public class FunTechSerial {
    static {
        System.loadLibrary("FunTechSerial");
    }
    protected native int OpenSerial(String path, int baudrate);
    protected native int CloseSerial();
    protected native int ReadCard();
    protected native int GetKind();
    protected native int GetSubKind();
    protected native byte[] DeviceSAM();
    protected native byte[] GetCardUID();
    protected native byte[] ReadIcCard();
    protected native byte[] GetName();
    protected native byte[] GetSex();
    protected native byte[] GetFolk();
    protected native byte[] GetBirthday();
    protected native byte[] GetCardID();
    protected native byte[] GetAddress();
    protected native byte[] GetIssue();
    protected native byte[] GetDateBegin();
    protected native byte[] GetDateEnd();
    protected native byte[] GetCount();
    protected native byte[] GetEnglishName();
    protected native byte[] GetAreaCode();
    protected native byte[] GetAgencyCode();
    protected native byte[] GetCardVersion();
    protected native byte[] GetPhoto();
    public int SubKind(){
        return GetSubKind();
    }
    protected  String Convert(int index,int count){
        String str =Integer.toString(index);
        String flag = "";
        if(str.length() < count ){
            for(int i=0;i<=count-str.length()-1;i++){
                flag +="0";
            }
        }
        return  flag + Integer.toString(index);
    }
    public String GetSAM(){
        byte buf[] = DeviceSAM();
        if(buf.length != 14){
            return  "获取SAM错误";
        }
        String Ret = Convert(buf[0],2) +"."+ Convert(buf[1],2)+"-";
        byte id[] = new byte[4];
        for(int i=0;i<=id.length-1;i++){
            id[i] = buf[i+2];
        }
        Ret += Convert(byteArrayToInt(id),8)+"-";
        for(int i=0;i<=id.length-1;i++){
            id[i] = buf[i+6];
        }
        Ret += Convert(byteArrayToInt(id),10)+"-";
        for(int i=0;i<=id.length-1;i++){
            id[i] = buf[i+10];
        }
        return  Ret  + Convert(byteArrayToInt(id),10);
    }
    public String CardVersion(){
        return  ParseUniCode(GetCardVersion());
    }
    public String CardAreaCode(){
        return  new String(GetAreaCode());
    }
    public String CardAgencyCode(){
        return  ParseUniCode(GetAgencyCode());
    }
    public String CardEnglishName(){
        return ParseUniCode(GetEnglishName());
    }
    public String CardCount(){
        return ParseUniCode(GetCount());
    }
    public String CareDateEnd(){
        return  ParseUniCode(GetDateEnd());
    }
    public String CardDateBegin(){
        return ParseUniCode(GetDateBegin());
    }
    public String CardIssue(){
        switch (CardKind()) {
            case 1:{
                return  ParseUniCode(GetIssue());
            }
            case 2:{
                return  ParseUniCode(GetIssue());
            }
            default: {
                return  "公安部 ";// Ministry of Public Security ";
            }
        }

    }
    public String CardAddress(){
        return  ParseUniCode(GetAddress());
    }
    public String CardID(){
        return ParseUniCode(GetCardID());
    }
    public String CardBirthday(){
        return ParseUniCode(GetBirthday());
    }
    public String CardFolk(){
        String folk = ParseUniCode(GetFolk());
        folk = folk.trim();
        if(folk.equals("")){
            return "";
        }else {
            return decodeNation(Integer.parseInt(folk));
        }
    }
    public String CardSex(){
        String sex=  ParseUniCode(GetSex());
        if(sex.equals("0")){
            return  "未知";
        } else if(sex.equals("1")){
            return  "男";
        }else if(sex.equals("2")){
            return "女";
        }else{
            return "未说明";
        }
    }
    public String CardName(){
        return  ParseUniCode(GetName());
    }
    public String IcCard(){
        String IC =   bytesToHex(ReadIcCard());
        if(IC.equals("ffffffff")){
            return "";
        }
        return IC;
    }
    public String CardUID(){

        return bytesToHex(GetCardUID());
    }
    public int CardKind(){
        return GetKind();
    }
    public int CardReader(){
        return  ReadCard();
    }
    public int StopSerial()
    {
        return  CloseSerial();
    }
    public int StartSerial(String path, int baudrate){
        return  OpenSerial(path,baudrate);
    }
    public Bitmap CardPhoto(){
        try{
            byte imgbuf[] = GetPhoto();
            Bitmap bmp = BitmapFactory.decodeByteArray(imgbuf,0,imgbuf.length).copy(Bitmap.Config.ARGB_8888, true);;
            for(int i = 0;i<bmp.getHeight();i++) {
                bmp.setPixel(0,i,0xFFFFFF);
            }
            return bmp;

        }catch (Exception e){
            return null;
        }

    }
    protected final String ParseUniCode(byte buff[]){
        String Result = null;
        String utf16 = null;
        try {
            utf16 = new String(buff, "UTF16-LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Result =  new String(utf16.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Result;
    }
    protected final String decodeNation(int code)
    {
        String nation;
        switch (code)
        {
            case 1:
                nation = "汉";
                break;
            case 2:
                nation = "蒙古";
                break;
            case 3:
                nation = "回";
                break;
            case 4:
                nation = "藏";
                break;
            case 5:
                nation = "维吾尔";
                break;
            case 6:
                nation = "苗";
                break;
            case 7:
                nation = "彝";
                break;
            case 8:
                nation = "壮";
                break;
            case 9:
                nation = "布依";
                break;
            case 10:
                nation = "朝鲜";
                break;
            case 11:
                nation = "满";
                break;
            case 12:
                nation = "侗";
                break;
            case 13:
                nation = "瑶";
                break;
            case 14:
                nation = "白";
                break;
            case 15:
                nation = "土家";
                break;
            case 16:
                nation = "哈尼";
                break;
            case 17:
                nation = "哈萨克";
                break;
            case 18:
                nation = "傣";
                break;
            case 19:
                nation = "黎";
                break;
            case 20:
                nation = "傈僳";
                break;
            case 21:
                nation = "佤";
                break;
            case 22:
                nation = "畲";
                break;
            case 23:
                nation = "高山";
                break;
            case 24:
                nation = "拉祜";
                break;
            case 25:
                nation = "水";
                break;
            case 26:
                nation = "东乡";
                break;
            case 27:
                nation = "纳西";
                break;
            case 28:
                nation = "景颇";
                break;
            case 29:
                nation = "柯尔克孜";
                break;
            case 30:
                nation = "土";
                break;
            case 31:
                nation = "达斡尔";
                break;
            case 32:
                nation = "仫佬";
                break;
            case 33:
                nation = "羌";
                break;
            case 34:
                nation = "布朗";
                break;
            case 35:
                nation = "撒拉";
                break;
            case 36:
                nation = "毛南";
                break;
            case 37:
                nation = "仡佬";
                break;
            case 38:
                nation = "锡伯";
                break;
            case 39:
                nation = "阿昌";
                break;
            case 40:
                nation = "普米";
                break;
            case 41:
                nation = "塔吉克";
                break;
            case 42:
                nation = "怒";
                break;
            case 43:
                nation = "乌孜别克";
                break;
            case 44:
                nation = "俄罗斯";
                break;
            case 45:
                nation = "鄂温克";
                break;
            case 46:
                nation = "德昂";
                break;
            case 47:
                nation = "保安";
                break;
            case 48:
                nation = "裕固";
                break;
            case 49:
                nation = "京";
                break;
            case 50:
                nation = "塔塔尔";
                break;
            case 51:
                nation = "独龙";
                break;
            case 52:
                nation = "鄂伦春";
                break;
            case 53:
                nation = "赫哲";
                break;
            case 54:
                nation = "门巴";
                break;
            case 55:
                nation = "珞巴";
                break;
            case 56:
                nation = "基诺";
                break;
            case 97:
                nation = "其他";
                break;
            case 98:
                nation = "外国血统中国籍人士";
                break;
            default:
                nation = "";
                break;
        }
        return nation;
    }
    protected final String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    protected final int byteArrayToInt(byte[] b) {
        return   b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }
}
