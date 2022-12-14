package com.smoc.cloud.template.controller;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.smoc.cloud.admin.security.remote.service.FlowApproveService;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.validator.FlowApproveValidator;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import com.smoc.cloud.common.smoc.template.AccountResourceInfoValidator;
import com.smoc.cloud.common.smoc.template.AccountTemplateInfoValidator;
import com.smoc.cloud.common.smoc.template.MessageFrameParamers;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.BusinessAccountService;
import com.smoc.cloud.customer.service.EnterpriseService;
import com.smoc.cloud.finance.service.FinanceAccountService;
import com.smoc.cloud.properties.ResourceProperties;
import com.smoc.cloud.sequence.service.SequenceService;
import com.smoc.cloud.template.service.AccountTemplateInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ????????????
 */
@Slf4j
@Controller
@RequestMapping("/template/web")
public class WebTemplateController {

    @Autowired
    private ResourceProperties resourceProperties;

    @Autowired
    private FlowApproveService flowApproveService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private AccountTemplateInfoService accountTemplateInfoService;

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("templates/template_web_list");

        //???????????????
        PageParams<AccountTemplateInfoValidator> params = new PageParams<AccountTemplateInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        AccountTemplateInfoValidator accountTemplateInfoValidator = new AccountTemplateInfoValidator();
        accountTemplateInfoValidator.setTemplateAgreementType("WEB");
        params.setParams(accountTemplateInfoValidator);

        //??????
        ResponseData<PageList<AccountTemplateInfoValidator>> data = accountTemplateInfoService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //log.info("[page]:{}", new Gson().toJson(data));

        view.addObject("accountTemplateInfoValidator", accountTemplateInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute AccountTemplateInfoValidator accountTemplateInfoValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("templates/template_web_list");

        //????????????
        pageParams.setParams(accountTemplateInfoValidator);

        ResponseData<PageList<FinanceAccountValidator>> data = accountTemplateInfoService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountTemplateInfoValidator", accountTemplateInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/check/{templateId}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String templateId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("templates/template_web_check");

        //??????????????????
        ResponseData<AccountTemplateInfoValidator> data = accountTemplateInfoService.findById(templateId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(data.getData().getEnterpriseId());
        if (!ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())) {
            view.addObject("error", enterpriseData.getCode() + ":" + enterpriseData.getMessage());
            return view;
        }

        ResponseData<List<FlowApproveValidator>> checkRecordData = flowApproveService.checkRecord(templateId);
        if (!ResponseCode.SUCCESS.getCode().equals(checkRecordData.getCode())) {
            view.addObject("error", checkRecordData.getCode() + ":" + checkRecordData.getMessage());
            return view;
        }

        //????????????????????????????????????????????????
        if("MULTI_SMS".equals(data.getData().getTemplateType()) || "MMS".equals(data.getData().getTemplateType())){
            //???????????????
            List<MessageFrameParamers> paramsSort = new ArrayList<MessageFrameParamers>();
            if (!StringUtils.isEmpty(data.getData().getMmAttchment())) {
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonArray Jarray = parser.parse(data.getData().getMmAttchment()).getAsJsonArray();
                for(JsonElement obj : Jarray){
                    MessageFrameParamers p = gson.fromJson(obj , MessageFrameParamers.class);
                    paramsSort.add(p);
                }
            }
            int allSize = 0;
            for(MessageFrameParamers p:paramsSort){
                allSize += new Integer(p.getResSize());
            }
            view.addObject("params", paramsSort);
            view.addObject("allSize", allSize);
        }

        //log.info("[checkRecord]:{}",new Gson().toJson(checkRecordData.getData()));
        view.addObject("checkRecord", checkRecordData.getData());
        view.addObject("accountTemplateInfoValidator", data.getData());
        view.addObject("enterpriseBasicInfoValidator", enterpriseData.getData());

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView center(@PathVariable String id) {
        ModelAndView view = new ModelAndView("templates/template_web_view_center");
        view.addObject("templateId", id);
        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public ModelAndView view(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("templates/template_web_view");
        //??????????????????
        ResponseData<AccountTemplateInfoValidator> data = accountTemplateInfoService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(data.getData().getEnterpriseId());
        if (!ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())) {
            view.addObject("error", enterpriseData.getCode() + ":" + enterpriseData.getMessage());
            return view;
        }

        //????????????????????????????????????????????????
        if("MULTI_SMS".equals(data.getData().getTemplateType()) || "MMS".equals(data.getData().getTemplateType())){
            //???????????????
            List<MessageFrameParamers> paramsSort = new ArrayList<MessageFrameParamers>();
            if (!StringUtils.isEmpty(data.getData().getMmAttchment())) {
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonArray Jarray = parser.parse(data.getData().getMmAttchment()).getAsJsonArray();
                for(JsonElement obj : Jarray){
                    MessageFrameParamers p = gson.fromJson(obj , MessageFrameParamers.class);
                    paramsSort.add(p);
                }
            }
            int allSize = 0;
            for(MessageFrameParamers p:paramsSort){
                allSize += new Integer(p.getResSize());
            }
            view.addObject("params", paramsSort);
            view.addObject("allSize", allSize);
        }

        view.addObject("enterpriseBasicInfoValidator", enterpriseData.getData());
        view.addObject("accountTemplateInfoValidator", data.getData());
        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated AccountTemplateInfoValidator accountTemplateInfoValidator, BindingResult result, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("templates/template_web_list");
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //??????????????????
        FlowApproveValidator flowApproveValidator = new FlowApproveValidator();
        flowApproveValidator.setId(UUID.uuid32());
        flowApproveValidator.setApproveId(accountTemplateInfoValidator.getTemplateId());
        flowApproveValidator.setApproveType("TEMPLATE_INFO");
        flowApproveValidator.setSubmitTime(new Date());
        flowApproveValidator.setBusiUrl(user.getRealName());
        flowApproveValidator.setUserId(user.getId());
        flowApproveValidator.setApproveAdvice(accountTemplateInfoValidator.getCheckOpinions());
        flowApproveValidator.setApproveStatus(new Integer(accountTemplateInfoValidator.getCheckStatus()));
        flowApproveValidator.setFlowStatus(new Integer(accountTemplateInfoValidator.getCheckStatus()));
        flowApproveValidator.setUserApproveId("");
        ResponseData data = flowApproveService.saveFlowApprove(flowApproveValidator, "add");
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        accountTemplateInfoValidator.setTemplateStatus(accountTemplateInfoValidator.getCheckStatus());

        ResponseData deleteResponseData = accountTemplateInfoService.cancelTemplate(accountTemplateInfoValidator.getTemplateId(),accountTemplateInfoValidator.getTemplateStatus());
        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(deleteResponseData.getCode())) {
            systemUserLogService.logsAsync("TEMPLATE_INFO", accountTemplateInfoValidator.getTemplateId(), user.getRealName(),"check","????????????", JSON.toJSONString(accountTemplateInfoValidator));
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("TEMPLATE_INFO", accountTemplateInfoValidator.getTemplateId(), user.getUserName(), "check", "????????????", JSON.toJSONString(flowApproveValidator));
        }

        //????????????
        log.info("[????????????][????????????][{}][{}]??????:{}", "check", user.getUserName(), JSON.toJSONString(flowApproveValidator));

        view.setView(new RedirectView("/template/web/list", true, false));
        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/check/list/{templateId}", method = RequestMethod.GET)
    public ModelAndView checkList(@PathVariable String templateId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("templates/template_web_check_list");

        ResponseData<List<FlowApproveValidator>> checkRecordData = flowApproveService.checkRecord(templateId);
        if (!ResponseCode.SUCCESS.getCode().equals(checkRecordData.getCode())) {
            view.addObject("error", checkRecordData.getCode() + ":" + checkRecordData.getMessage());
            return view;
        }
        view.addObject("checkRecord", checkRecordData.getData());
        return view;
    }

    /**
     * ???????????????
     * @param resUrl
     * @param request
     * @param response
     */
    @RequestMapping(value = "/resource/show/{resUrl}", method = RequestMethod.GET)
    public void show(@PathVariable String resUrl, HttpServletRequest request, HttpServletResponse response) {

        SecurityUser user = (SecurityUser)request.getSession().getAttribute("user");

        File downloadFile = new File(resourceProperties.getResourceFileRootPath() + resUrl);
        //???????????????
        if(!downloadFile.exists()){
            log.info("[????????????][download][{}]??????::{}", user.getUserName(), "???????????????:"+downloadFile.getAbsolutePath());
            return;
        }

        String suffixType = resUrl.substring(resUrl.lastIndexOf(".") + 1);

        //??????????????????????????????
        InputStream in = null;
        try {
            //??????????????????,???????????????????????????????????????
            response.setContentType("image/jpeg");
            //????????????????????????????????????????????????????????????
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);

            in = new FileInputStream(downloadFile);
            BufferedImage bImage = ImageIO.read(in);
            ImageIO.write(bImage, suffixType, response.getOutputStream());
        } catch (Exception e) {
            log.error("[????????????][download][{}]??????::{}", user.getUserName(), e.getMessage());
            e.printStackTrace();
        } finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param resUrl
     * @param request
     * @return
     */
    @RequestMapping(value = "/resource/download/{resUrl}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadFile(@PathVariable String resUrl, HttpServletRequest request) {
        ResponseEntity<byte[]> entity = null;

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");


        File downloadFile = new File(resourceProperties.getResourceFileRootPath() + resUrl);
        //???????????????
        if (!downloadFile.exists()) {
            log.info("[????????????][download][{}]??????::{}", user.getUserName(), "???????????????:" + downloadFile.getAbsolutePath());
            return entity;
        }

        //??????????????????????????????
        InputStream in = null;
        try {
            in = new FileInputStream(downloadFile);
            byte[] body = new byte[in.available()];
            in.read(body);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attchement;filename=" + downloadFile.getName());
            HttpStatus statusCode = HttpStatus.OK;
            entity = new ResponseEntity<byte[]>(body, headers, statusCode);
        } catch (Exception e) {
            log.error("[????????????][download][{}]??????::{}", user.getUserName(), e.getMessage());
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return entity;
    }
}
