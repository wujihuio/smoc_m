package com.smoc.cloud.complaint.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.validator.CodeNumberInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.message.MessageComplaintInfoValidator;
import com.smoc.cloud.common.smoc.message.MessageDetailInfoValidator;
import com.smoc.cloud.common.smoc.message.model.ComplaintExcelModel;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.complaint.service.ComplaintService;
import com.smoc.cloud.customer.service.BusinessAccountService;
import com.smoc.cloud.customer.service.EnterpriseService;
import com.smoc.cloud.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * ??????????????????
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("complaint/complaint_list");

        //???????????????
        PageParams<MessageComplaintInfoValidator> params = new PageParams<MessageComplaintInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        MessageComplaintInfoValidator messageComplaintInfoValidator = new MessageComplaintInfoValidator();
        messageComplaintInfoValidator.setComplaintSource("day");
        Date startDate = DateTimeUtils.getFirstMonth(12);
        messageComplaintInfoValidator.setStartDate(DateTimeUtils.getDateFormat(startDate));
        messageComplaintInfoValidator.setEndDate(DateTimeUtils.getDateFormat(new Date()));
        params.setParams(messageComplaintInfoValidator);

        //??????
        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????????????????
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute MessageComplaintInfoValidator messageComplaintInfoValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("complaint/complaint_list");

        //????????????
        messageComplaintInfoValidator.setComplaintSource("day");
        pageParams.setParams(messageComplaintInfoValidator);
        //????????????
        if (!StringUtils.isEmpty(messageComplaintInfoValidator.getStartDate())) {
            String[] date = messageComplaintInfoValidator.getStartDate().split(" - ");
            messageComplaintInfoValidator.setStartDate(StringUtils.trimWhitespace(date[0]));
            messageComplaintInfoValidator.setEndDate(StringUtils.trimWhitespace(date[1]));
        }

        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    @RequestMapping(value = "/add/{complaintSource}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String complaintSource) {

        ModelAndView view = new ModelAndView("complaint/complaint_add");

        MessageComplaintInfoValidator messageComplaintInfoValidator = new MessageComplaintInfoValidator();
        messageComplaintInfoValidator.setComplaintSource(complaintSource);
        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);

        return view;

    }

    /**
     * ??????
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id) {
        ModelAndView view = new ModelAndView("complaint/complaint_edit");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????
        ResponseData<MessageComplaintInfoValidator> data = complaintService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        MessageComplaintInfoValidator messageComplaintInfoValidator = data.getData();

        //????????????
        if(!StringUtils.isEmpty(messageComplaintInfoValidator.getBusinessAccount())){
            ResponseData<AccountBasicInfoValidator> info = businessAccountService.findById(messageComplaintInfoValidator.getBusinessAccount());
            if (ResponseCode.SUCCESS.getCode().equals(info.getCode())) {
                messageComplaintInfoValidator.setAccountName(info.getData().getAccountName());
                if(StringUtils.isEmpty(messageComplaintInfoValidator.getBusinessType())){
                    messageComplaintInfoValidator.setBusinessType(info.getData().getBusinessType());
                }
            }
        }

        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");
        view.addObject("messageComplaintInfoValidator", data.getData());

        return view;
    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated MessageComplaintInfoValidator messageComplaintInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("complaint/complaint_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        if(StringUtils.isEmpty(messageComplaintInfoValidator.getBusinessAccount())){
            // ????????????????????????
            FieldError err = new FieldError("????????????", "businessAccount", "????????????????????????");
            result.addError(err);
        }
        //????????????????????????
        if (result.hasErrors()) {
            view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
            view.addObject("op", op);
            return view;
        }

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            messageComplaintInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            messageComplaintInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            messageComplaintInfoValidator.setUpdatedTime(new Date());
            messageComplaintInfoValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //????????????
        ResponseData data = complaintService.save(messageComplaintInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //????????????
        log.info("[????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(messageComplaintInfoValidator));

        if("day".equals(messageComplaintInfoValidator.getComplaintSource())){
            view.setView(new RedirectView("/complaint/list" , true, false));
        }else if("12321".equals(messageComplaintInfoValidator.getComplaintSource())){
            view.setView(new RedirectView("/complaint/complaintSource" , true, false));
        }
        return view;

    }


    /**
     * ??????
     * @return
     */
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable String id) {
        ModelAndView view = new ModelAndView("complaint/complaint_detail");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????
        ResponseData<MessageComplaintInfoValidator> data = complaintService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        MessageComplaintInfoValidator messageComplaintInfoValidator = data.getData();

        //????????????
        if(!StringUtils.isEmpty(data.getData().getBusinessAccount())){
            ResponseData<AccountBasicInfoValidator> info = businessAccountService.findById(data.getData().getBusinessAccount());
            if (ResponseCode.SUCCESS.getCode().equals(info.getCode())) {
                messageComplaintInfoValidator.setAccountName(info.getData().getAccountName());
                if(StringUtils.isEmpty(messageComplaintInfoValidator.getBusinessType())){
                    messageComplaintInfoValidator.setBusinessType(info.getData().getBusinessType());
                }

                //???????????????????????????10?????????????????????
                ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(info.getData().getEnterpriseId());
                if (ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())){
                    MessageDetailInfoValidator detail = new MessageDetailInfoValidator();
                    detail.setEnterpriseFlag(enterpriseData.getData().getEnterpriseFlag());
                    detail.setPhoneNumber(data.getData().getReportNumber());
                    detail.setMessageContent(data.getData().getReportContent());
                    detail.setBusinessAccount(info.getData().getAccountId());
                    Date startDate = DateTimeUtils.dateAddDays(new Date(),-10);
                    detail.setStartDate(DateTimeUtils.getDateFormat(startDate));
                    detail.setEndDate(DateTimeUtils.getDateFormat(new Date()));
                    ResponseData<List<MessageDetailInfoValidator>> list = complaintService.sendMessageList(detail);
                    if (!ResponseCode.SUCCESS.getCode().equals(list.getCode())) {
                        view.addObject("error", list.getCode() + ":" + list.getMessage());
                        return view;
                    }
                    view.addObject("list", list.getData());
                }

            }
        }

        view.addObject("messageComplaintInfoValidator", data.getData());

        return view;
    }

    /**
     * ??????
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteById/{id}", method = RequestMethod.GET)
    public ModelAndView deleteById(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("complaint_detail");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????
        ResponseData<MessageComplaintInfoValidator> infoData = complaintService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(infoData.getCode())) {
            view.addObject("error", infoData.getCode() + ":" + infoData.getMessage());
        }

        //????????????
        ResponseData data = complaintService.deleteById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //????????????
        log.info("[????????????][{}]??????:{}", "delete", user.getUserName(), JSON.toJSONString(infoData.getData()));

        if("day".equals(infoData.getData().getComplaintSource())){
            view.setView(new RedirectView("/complaint/list" , true, false));
        }else if("12321".equals(infoData.getData().getComplaintSource())){
            view.setView(new RedirectView("/complaint/complaintSource" , true, false));
        }
        return view;
    }

    /**
     * ??????
     * @param request
     * @return
     */
    @RequestMapping(value = "/upComplaint", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute MessageComplaintInfoValidator messageComplaintInfoValidator, BindingResult result, HttpServletRequest request) {
        SecurityUser user = (SecurityUser)request.getSession().getAttribute("user");

        ModelAndView view = new ModelAndView("complaint/complaint_add");

        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = mRequest.getFile("file");
        if (file != null && file.getSize() <= 0){
            // ????????????????????????
            FieldError err = new FieldError("????????????", "complaintFiles", "??????????????????");
            result.addError(err);
        }

        //????????????????????????
        if (result.hasErrors()) {
            view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
            return view;
        }

        /**
         * ??????????????????
         */
        List<ComplaintExcelModel> list = FileUtils.readComplaintExcelFile(file);
        //????????????
        if(!StringUtils.isEmpty(list) && list.size()>0){
            messageComplaintInfoValidator.setComplaintList(list);
            messageComplaintInfoValidator.setCreatedBy(user.getRealName());
            ResponseData data  = complaintService.batchSave(messageComplaintInfoValidator);
        }

        log.info("[????????????][??????][{}]??????::{}", user.getUserName(), list.size());

        if("day".equals(messageComplaintInfoValidator.getComplaintSource())){
            view.setView(new RedirectView("/complaint/list" , true, false));
        }else if("12321".equals(messageComplaintInfoValidator.getComplaintSource())){
            view.setView(new RedirectView("/complaint/complaintSource" , true, false));
        }

        return view;
    }

    /**
     * ??????????????????
     * @return
     */
    @RequestMapping(value = "/complaintSource", method = RequestMethod.GET)
    public ModelAndView complaintSource() {
        ModelAndView view = new ModelAndView("complaint/complaint_12321");

        //???????????????
        PageParams<MessageComplaintInfoValidator> params = new PageParams<MessageComplaintInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        MessageComplaintInfoValidator messageComplaintInfoValidator = new MessageComplaintInfoValidator();
        messageComplaintInfoValidator.setComplaintSource("12321");
        Date startDate = DateTimeUtils.getFirstMonth(12);
        messageComplaintInfoValidator.setStartDate(DateTimeUtils.getDateFormat(startDate));
        messageComplaintInfoValidator.setEndDate(DateTimeUtils.getDateFormat(new Date()));
        params.setParams(messageComplaintInfoValidator);

        //??????
        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????????????????
     * @return
     */
    @RequestMapping(value = "/complaintSourcePage", method = RequestMethod.POST)
    public ModelAndView complaintSourcePage(@ModelAttribute MessageComplaintInfoValidator messageComplaintInfoValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("complaint/complaint_12321");

        //????????????
        messageComplaintInfoValidator.setComplaintSource("12321");
        pageParams.setParams(messageComplaintInfoValidator);
        //????????????
        if (!StringUtils.isEmpty(messageComplaintInfoValidator.getStartDate())) {
            String[] date = messageComplaintInfoValidator.getStartDate().split(" - ");
            messageComplaintInfoValidator.setStartDate(StringUtils.trimWhitespace(date[0]));
            messageComplaintInfoValidator.setEndDate(StringUtils.trimWhitespace(date[1]));
        }

        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/downFileTemp", method = RequestMethod.GET)
    public void downFileTemp(HttpServletRequest request, HttpServletResponse response) {

        String fileName = "????????????.xlsx";

        //??????????????????
        ClassPathResource classPathResource = new ClassPathResource("static/files/" + fileName);
        try {
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // ?????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // ??????????????????
        byte[] buffer = new byte[1024];
        InputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = classPathResource.getInputStream();;
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            return ;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ??????????????????????????????
     * @param enterpriseId
     * @return
     */
    @RequestMapping(value = "/ec/customer/center/list/{enterpriseId}", method = RequestMethod.GET)
    public ModelAndView customerComplaintList(@PathVariable String enterpriseId) {
        ModelAndView view = new ModelAndView("complaint/customer_center_complaint_list");

        //???????????????
        PageParams<MessageComplaintInfoValidator> params = new PageParams<MessageComplaintInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        MessageComplaintInfoValidator messageComplaintInfoValidator = new MessageComplaintInfoValidator();
        messageComplaintInfoValidator.setComplaintSource("day");
        messageComplaintInfoValidator.setEnterpriseId(enterpriseId);
        params.setParams(messageComplaintInfoValidator);

        //??????
        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ??????????????????????????????
     * @return
     */
    @RequestMapping(value = "/ec/customer/center/page", method = RequestMethod.POST)
    public ModelAndView customerComplaintPage(@ModelAttribute MessageComplaintInfoValidator messageComplaintInfoValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("complaint/customer_center_complaint_list");

        //????????????
        messageComplaintInfoValidator.setComplaintSource("day");
        pageParams.setParams(messageComplaintInfoValidator);

        ResponseData<PageList<MessageComplaintInfoValidator>> data = complaintService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("messageComplaintInfoValidator", messageComplaintInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }
}
