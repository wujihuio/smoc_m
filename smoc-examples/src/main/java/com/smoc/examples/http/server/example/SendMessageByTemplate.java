package com.smoc.examples.http.server.example;

import com.google.gson.Gson;
import com.smoc.examples.utils.DateTimeUtils;
import com.smoc.examples.utils.HMACUtil;
import com.smoc.examples.utils.Okhttp3Utils;
import com.smoc.examples.utils.Utils;

import java.util.*;

/**
 * 根据模板发送普通消息
 */
public class SendMessageByTemplate {

    public static void main(String[] args) throws Exception {

        String url = "http://localhost:18088/smoc-gateway/http-server/message/sendMessageByTemplate";

        for (int j = 0; j < 1; j++) {
            //自定义header协议
            Map<String, String> header = new HashMap<>();
            //signature-nonce 为17位数字，并且每次请求signature-nonce不能重复
            header.put("signature-nonce", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));
//            header.put("signature-nonce", "202206221043192902026871940");
            header.put("account", "123113");

            //请求的数据
            Map<String, Object> requestDataMap = new HashMap<>();
            //订单号，成功后的订单不能重复
            requestDataMap.put("orderNo", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));
            //业务账号；参见给的账号EXCEL文件
            requestDataMap.put("account", "123113");
            //模板ID
            requestDataMap.put("templateId", "TEMP100950");

            //模板短信内容
            List<String> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                String item = "13" + Utils.getRandom(9 )+"|白皮猪";
                list.add(item);
            }
            requestDataMap.put("content", list);

            //扩展号码
            requestDataMap.put("extNumber", Utils.getRandom(4));
            //客户可选业务类型
            requestDataMap.put("business", "");

            //时间戳
            requestDataMap.put("timestamp", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS"));
            //转JSON请求数据
            String requestJsonData = new Gson().toJson(requestDataMap);

            //组织签名字符串
            StringBuffer signData = new StringBuffer();
            //header中的signature-nonce
            signData.append(header.get("signature-nonce"));
            //订单号
            signData.append(requestDataMap.get("orderNo"));
            //认证账号；
            signData.append(requestDataMap.get("account"));
            //加密后的身份证号
            signData.append(requestDataMap.get("timestamp"));
            //签名 MD5_HMAC 签名KEY,参见给的账号EXCEL文件
            String sign = HMACUtil.md5_HMAC_sign(signData.toString(), "@cA#gY3%");
            //System.out.println("[接口请求][签名数据]数据:" + signData);
            //System.out.println("[接口请求][签名]数据:" + sign);

            header.put("signature", sign);

            System.out.println(requestJsonData);

            String result = Okhttp3Utils.postJson(url, requestJsonData, header);
            System.out.println("[请求响应]数据:" + result);
        }
    }
}
