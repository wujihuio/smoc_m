package com.smoc.cloud.http.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.smoc.cloud.common.http.server.message.request.ReportStatusRequestParams;
import com.smoc.cloud.common.http.server.message.response.ReportResponseParams;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.Utils;
import com.smoc.cloud.http.entity.MobileOriginalAccount;
import com.smoc.cloud.http.repository.MessageRepository;
import com.smoc.cloud.http.repository.MoRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PushReportService {

    @Autowired
    private MoRepository moRepository;

    @Autowired
    private MessageRepository messageRepository;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 启动报告推送任务
     */
    public void push() {
        
        log.info("[推送状态报告]：{}", System.currentTimeMillis());
        List<MobileOriginalAccount> accounts = moRepository.getMobileOriginalAccount();
        if (null == accounts || accounts.size() < 1) {
            return;
        }

        try {

            for (MobileOriginalAccount account : accounts) {
                if (StringUtils.isEmpty(account.getReportUrl())) {
                    continue;
                }
                pushReport(account);
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            log.info("[报告推送失败]：原因:{}", e.getMessage());
        }

    }

    @Async
    public void pushReport(MobileOriginalAccount account) {

        ReportStatusRequestParams params = new ReportStatusRequestParams();
        params.setAccount(account.getAccount());
        List<ReportResponseParams> rrps = messageRepository.getReport(params);

        if (null == rrps || rrps.size() < 1) {
            return;
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orderNo", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));
        responseData.put("data", rrps);
        responseData.put("timestamp", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS"));
        String jsonData = new Gson().toJson(responseData);
        //log.info("[json]:{}", jsonData);
        //log.info("[url]:{}", account.getMoUrl());
        postJson(account.getReportUrl(), jsonData, rrps);
    }

    /**
     * 异步响应
     * json格式post请求接口调用
     *
     * @param url             接口地址
     * @param requestJsonData json格式请求参数体
     * @return
     */
    public void postJson(String url, String requestJsonData, List<ReportResponseParams> rrps) {

        RequestBody body = RequestBody.create(JSON, requestJsonData);

        //自定义header协议
        Map<String, String> header = new HashMap<>();
        //signature-nonce 为17位数字，并且每次请求signature-nonce不能重复
        header.put("signature-nonce", DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(10));

        //循环添加header
        Request.Builder requestBuilder = new Request.Builder();
        for (Map.Entry<String, String> entry : header.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        try {
            //构建 client
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
            //构建request请求
            Request request = requestBuilder.url(url).post(body).build();
            //获得call对象
            Call call = client.newCall(request);
            //执行异步步请求
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //子线程
                    log.info("[报告推送失败]：url:{};原因:{}", url, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //子线程
                    //log.info("[报告推送成功]：{}");
                    String code = response.body().string();
                    Map map = (Map) JSONObject.parse(code);
                    if ("0000".equals(map.get("code"))) {
                        messageRepository.batchDeleteReports(rrps);
                    } else {
                        log.info("[报告推送失败]：url:{};原因:{}", url, code);
                    }
                }
            });
        } catch (Exception e) {
            log.info("[报告推送失败]：原因:{}", e.getMessage());
        }
    }


}
