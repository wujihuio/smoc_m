package com.smoc.examples.identity.example;

import com.google.gson.Gson;
import com.smoc.examples.utils.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 身份证号、姓名、人的图像认证 示例
 */
public class IdentityIdNumberNamePhoto {

   public  static void main(String[] args) throws Exception {
       String url = "http://localhost:18088/smoc-gateway/identity/smoc-identity/idNumberNamePhoto";

       //自定义header协议
       Map<String, String> header = new HashMap<>();
       //signature-nonce 为17位数字，并且每次请求signature-nonce不能重复
       header.put("signature-nonce", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));
       header.put("account","XYIA100001560");

       //对要传输的敏感信息进行AES加密。AES密钥（AES_KEY）、AES偏移量（IV）参见给的账号EXCEL文件
       String cardNo = AESUtil.encrypt("130636200001125417","L1bl6AM22y1d677H1K00Gs36NX47c1G0","9Gb7c1tDhpj2L6g6");

       //请求的数据
       Map<String,String> requestDataMap = new HashMap<>();
       //订单号，成功后的订单不能重复
       requestDataMap.put("orderNo",DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));
       //认证账号；参见给的账号EXCEL文件
       requestDataMap.put("identifyAccount","XYIA100001560");
       //认证的姓名
       requestDataMap.put("name","何大胜");
       //AES加密后的 身份证号
       requestDataMap.put("cardNo",cardNo);
       //人的图像照片
       requestDataMap.put("faceBase64", FileBASE64Utils.getFileToBase64("/Users/wujihui/Desktop/smoc4.0/WechatIMG3635.jpeg"));
       //转JSON请求数据
       String requestJsonData = new Gson().toJson(requestDataMap);

       //组织签名字符串
       StringBuffer signData = new StringBuffer();
       //header中的signature-nonce
       signData.append(header.get("signature-nonce"));
       //订单号
       signData.append(requestDataMap.get("orderNo"));
       //认证账号；
       signData.append(requestDataMap.get("identifyAccount"));
       //加密后的身份证号
       signData.append(requestDataMap.get("cardNo"));
       //签名 MD5_HMAC 签名KEY,参见给的账号EXCEL文件
       String sign = HMACUtil.md5_HMAC_sign(signData.toString(),"P5nM7A9l1D8UKb5f21046A01r83UG829");
       System.out.println("[接口请求][签名数据]数据:" +signData);
       System.out.println("[接口请求][签名]数据:" +sign);

       header.put("signature", sign);

       String result = Okhttp3Utils.postJson(url,requestJsonData,header);
       System.out.println("[请求响应]数据:" +result);
   }
}
