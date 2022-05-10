package com.smoc.cloud.intelligence.remote.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.intelligence.remote.configuration.IntelligenceProperties;
import com.smoc.cloud.intelligence.remote.request.*;
import com.smoc.cloud.intelligence.remote.response.*;
import com.smoc.cloud.intelligence.remote.utils.Okhttp3Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 梦网智能短信请求服务
 */
@Service
public class RequestService {

    @Autowired
    private IntelligenceProperties intelligenceProperties;

    /**
     * 模板资源上传
     *
     * @param param
     * @return data 中数据 resourceId
     */
    public ResponseDataUtil<Map<String, String>> uploadTemplateResource(RequestTemplateResource param) {

        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/uploadAimTplRes";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<Map<String, String>>>() {
        }.getType();
        ResponseDataUtil<Map<String, String>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 智能短信模板提交
     *
     * @param param
     * @return data 中数据tplId
     */
    public ResponseDataUtil<Map<String, String>> submitTemplate(RequestSubmitTemplate param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/submitAimTpl";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<Map<String, String>>>() {
        }.getType();
        ResponseDataUtil<Map<String, String>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 删除智能短信模板
     *
     * @param templateId
     * @return
     */
    public ResponseDataUtil deleteTemplate(String templateId) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/delAimTpl";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(templateId), buildHeader());

        Type type = new TypeToken<ResponseDataUtil>() {
        }.getType();
        ResponseDataUtil result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 启用/禁用智能短信模板
     *
     * @param param
     * @return
     */
    public ResponseDataUtil setTemplateStatus(RequestSetTemplateStatus param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/setAimTplState";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil>() {
        }.getType();
        ResponseDataUtil result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 查询模板审核状态
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<ResponseGetTemplateStatus> getTemplateStatus(RequestGetTemplateStatus param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/getTplAuditState";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<ResponseGetTemplateStatus>>() {
        }.getType();
        ResponseDataUtil<ResponseGetTemplateStatus> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 获取号码接收智能短信能力
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<List<ResponseMobile>> queryAimAbility(RequestQueryAimAbility param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/AddressManage/queryAimAbility";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<List<ResponseMobile>>>() {
        }.getType();
        ResponseDataUtil<List<ResponseMobile>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 申请短链
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<ResponseShortUrl> applyShortUrl(RequestApplyShortUrl param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/SCodeManage/applyAimUrl";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<ResponseShortUrl>>() {
        }.getType();
        ResponseDataUtil<ResponseShortUrl> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 查询模板
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<List<ResponseTemplateInfo>> queryTemplates(RequestQueryTemplates param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/listECTemplates";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<List<ResponseTemplateInfo>>>() {
        }.getType();
        ResponseDataUtil<List<ResponseTemplateInfo>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 查询公共模板
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<List<ResponseTemplateInfo>> queryPublicTemplates(RequestQueryPublicTemplates param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/TemplateManage/listPublicTemplates";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<List<ResponseTemplateInfo>>>() {
        }.getType();
        ResponseDataUtil<List<ResponseTemplateInfo>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 账户余额查询
     *
     * @param param
     * @return
     */
    public ResponseDataUtil<List<ResponseAccountBalance>> queryPublicTemplates(RequestQueryAccountBalance param) {
        String requestUrl = intelligenceProperties.getServerAddress() + "/ApiService/v1/sendAccountManage/queryBalance";
        String response = Okhttp3Utils.postJson(requestUrl, new Gson().toJson(param), buildHeader());

        Type type = new TypeToken<ResponseDataUtil<List<ResponseAccountBalance>>>() {
        }.getType();
        ResponseDataUtil<List<ResponseAccountBalance>> result = new Gson().fromJson(response, type);
        return result;
    }

    /**
     * 创建请求header
     *
     * @return
     */
    public Map<String, String> buildHeader() {

        Map<String, String> header = new HashMap<>();
        header.put("account", intelligenceProperties.getAccount());
        String timestamp = DateTimeUtils.getDateFormat(new Date(), "MMddHHmmss");
        header.put("timestamp", timestamp);
        header.put("pwd", DigestUtils.md5Hex(intelligenceProperties.getAccount().toUpperCase() + "00000000" + intelligenceProperties.getPassword() + timestamp));
        return header;

    }


}