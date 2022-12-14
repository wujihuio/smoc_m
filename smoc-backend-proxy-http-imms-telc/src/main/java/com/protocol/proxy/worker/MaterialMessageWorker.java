package com.protocol.proxy.worker;
import com.alibaba.fastjson.JSONObject;
import com.base.common.constant.DynamicConstant;
import com.base.common.constant.FixedConstant;
import com.base.common.manager.AccountInfoManager;
import com.base.common.manager.ResourceManager;
import com.base.common.worker.SuperQueueWorker;
import com.protocol.proxy.manager.ChannelInteractiveStatusManager;
import com.protocol.proxy.message.AccountTemplateInfo;
import com.protocol.proxy.message.ResponseMessage;
import com.protocol.proxy.util.ChannelInterfaceUtil;
import com.protocol.proxy.util.DAO;
import com.protocol.proxy.util.TemplateTransition;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaterialMessageWorker extends SuperQueueWorker<String> {
    private String channelID;
    public static String MMS_PATH = ResourceManager.getInstance().getValue("mms.resource.path");
    private static int TIMEOUT = ResourceManager.getInstance().getIntValue("timeout");
    private static int RESPONSE_TIMEOUT = ResourceManager.getInstance().getIntValue("response.timeout");

    public MaterialMessageWorker(String channelID) {
        this.channelID = channelID;
    }
    /**
     *
     */
    @Override
    protected void doRun() throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            // 从数据库account_template_info和account_channel_template_info,获取平台多模板信息，按照通道素材组织格式提交
            List<AccountTemplateInfo> accountTemplateInfoList = DAO.getAccountTemplateInfo(channelID);
            if (accountTemplateInfoList.size() > 0) {
                for (AccountTemplateInfo accounttemplateinfo : accountTemplateInfoList) {
                    startTime = System.currentTimeMillis();
                    String businessAccount = accounttemplateinfo.getBusinessAccount();
                    // 根据账号获取配置的通道是否包含该通道
                    Set<String> channelIDs = DAO.getChannels(businessAccount);
                    if (channelIDs.contains(channelID)) {
                        //获取账号的扩展码
                        String extend = AccountInfoManager.getInstance().getAccountExtendCode(businessAccount);

                        // 按照通道模板规范提交模板信息
                        Map<String, String> resultMap = TemplateTransition.getTemplate(
                                accounttemplateinfo.getMmAttchnent(), channelID,
                                accounttemplateinfo.getTemplateTitle(), extend);

                        String response = submitTemplate(resultMap.get("mmdl"), resultMap.get("urlpath"), channelID);
                        logger.info("响应消息={}", response);
                        ChannelInteractiveStatusManager.getInstance().process(channelID, response);
                        String options = resultMap.get("options");

                        if (StringUtils.isNotEmpty(response) && response.contains("ResCode")) {
                            JSONObject object = null;
                            try {
                                object = JSONObject.parseObject(response);

                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }

                            if (object.containsKey("ResCode") && DynamicConstant.RESPONSE_SUCCESS_CODE.equals(object.getString("ResCode"))) {
                                ResponseMessage responsemessage = new ResponseMessage();
                                responsemessage.setMessage(object.getString("ResMsg"));
                                responsemessage.setCode(object.getString("ResCode"));
                                responsemessage.setMsgId(object.getString("MsgID"));

                                DAO.insertAccountChannelTemplateInfo(responsemessage, accounttemplateinfo, String.valueOf(FixedConstant.TemplateStatus.NO_APPROVED.ordinal()),
                                        channelID, options, extend);
                                logger.info("平台模板ID={},通道模板ID={}", accounttemplateinfo.getTemplateId(), responsemessage.getMsgId());
                            } else {
                                ResponseMessage responsemessage = new ResponseMessage();
                                responsemessage.setCode(object.getString("ResCode"));
                                responsemessage.setMessage(object.getString("ResMsg"));
                                DAO.insertAccountChannelTemplateInfo(responsemessage, accounttemplateinfo, String.valueOf(FixedConstant.TemplateStatus.REJECT.ordinal()),
                                        channelID, options, extend);
                                logger.info("提交失败的平台模板ID={},失败原因={}", accounttemplateinfo.getTemplateId(), responsemessage.getMessage());

                            }
                        }
                    }
                }
            }
            controlSubmitSpeed(INTERVAL, (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * 按照通道规范提交多媒体素材
     *
     * @param
     * @return
     */
    private String submitTemplate(String jsonReqElements, String urlpath, String channelID) {

        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        String result = null;
        try {
            //获取通道接口参数
            Map<String, String> resultMap = ChannelInterfaceUtil.getArgMap(channelID);
            String urls = resultMap.get("url") + "/sapi/material";

            httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(urls);
            httppost.setConfig(RequestConfig.custom().setConnectTimeout(TIMEOUT).setSocketTimeout(RESPONSE_TIMEOUT).build());
            // 将字符串转换成集合
            List<String> urlList = Arrays.asList(urlpath.split(","));

            StringBody stringBody = new StringBody(jsonReqElements, ContentType.APPLICATION_JSON);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addPart("Data", stringBody).setCharset(Charset.forName("UTF-8"));
            logger.info("上传的附件为:"+urlList.toString());
            for (String url : urlList) {

                File uploadFile1 = new File(MMS_PATH + url);
                if (!uploadFile1.exists()) {
                    logger.error("文件不存在：" + uploadFile1.getPath());
                    continue;
                }

                FileBody uploadFileBody1 = new FileBody(uploadFile1);

                builder.addPart(uploadFile1.getName(), uploadFileBody1);
            }
            // 生成 HTTP POST 实体
            HttpEntity reqEntity = builder.build();
            httppost.setEntity(reqEntity);

            response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);
            }
            EntityUtils.consume(resEntity);
            logger.info(new StringBuilder().append("请求报文")
                                           .append("{}jsonReqElements={}")
                                           .append("{}urls={}").toString(),
                                           FixedConstant.SPLICER,jsonReqElements,
                                           FixedConstant.SPLICER, urls
            );
        } catch (Exception e) {
            logger.error("提交多媒体素材异常：" + e.getMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

}
