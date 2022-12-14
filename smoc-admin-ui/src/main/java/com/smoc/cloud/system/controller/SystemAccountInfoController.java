package com.smoc.cloud.system.controller;


import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import com.smoc.cloud.common.smoc.identification.validator.IdentificationAccountInfoValidator;
import com.smoc.cloud.common.smoc.system.SystemAccountInfoValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.StringRandom;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.EnterpriseService;
import com.smoc.cloud.finance.service.FinanceAccountService;
import com.smoc.cloud.identification.model.AccountExcelModel;
import com.smoc.cloud.identification.service.IdentificationAccountInfoService;
import com.smoc.cloud.sequence.service.SequenceService;
import com.smoc.cloud.system.service.SystemAccountInfoService;
import com.smoc.cloud.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ????????????
 */
@Slf4j
//@Controller
//@RequestMapping("/system/account")
public class SystemAccountInfoController {

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private FinanceAccountService financeAccountService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private SystemAccountInfoService systemAccountInfoService;

    //??????????????????????????? ??????????????????;???????????????????????????
    private String businessType ="INTELLECT_ACCOUNT";

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("system/account/account_list");

        //???????????????
        PageParams<SystemAccountInfoValidator> params = new PageParams<SystemAccountInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        SystemAccountInfoValidator systemAccountInfoValidator = new SystemAccountInfoValidator();
        systemAccountInfoValidator.setBusinessType(this.businessType);
        params.setParams(systemAccountInfoValidator);

        //??????
        ResponseData<PageList<SystemAccountInfoValidator>> data = systemAccountInfoService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute SystemAccountInfoValidator systemAccountInfoValidator, PageParams pageParams) {

        ModelAndView view = new ModelAndView("system/account/account_list");
        //????????????
        systemAccountInfoValidator.setBusinessType(this.businessType);
        pageParams.setParams(systemAccountInfoValidator);

        ResponseData<PageList<SystemAccountInfoValidator>> data = systemAccountInfoService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search_list() {
        ModelAndView view = new ModelAndView("system/account/account_search_list");

        //???????????????
        PageParams<SystemAccountInfoValidator> params = new PageParams<SystemAccountInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        SystemAccountInfoValidator systemAccountInfoValidator = new SystemAccountInfoValidator();
        systemAccountInfoValidator.setBusinessType(this.businessType);
        params.setParams(systemAccountInfoValidator);

        //??????
        ResponseData<PageList<SystemAccountInfoValidator>> data = systemAccountInfoService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/search/page", method = RequestMethod.POST)
    public ModelAndView search_page(@ModelAttribute SystemAccountInfoValidator systemAccountInfoValidator, PageParams pageParams) {

        ModelAndView view = new ModelAndView("system/account/account_search_list");
        //????????????
        systemAccountInfoValidator.setBusinessType(this.businessType);
        pageParams.setParams(systemAccountInfoValidator);

        ResponseData<PageList<SystemAccountInfoValidator>> data = systemAccountInfoService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/add/{enterpriseId}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String enterpriseId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("system/account/account_edit");

        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(enterpriseId);
        SystemAccountInfoValidator systemAccountInfoValidator = new SystemAccountInfoValidator();

        systemAccountInfoValidator.setEnterpriseId(enterpriseData.getData().getEnterpriseId());
        systemAccountInfoValidator.setEnterpriseName(enterpriseData.getData().getEnterpriseName());
        //??????????????????
        systemAccountInfoValidator.setAccountStatus("1");
        //??????????????????
        systemAccountInfoValidator.setAccountType("1");

        //??????????????????
        String account = "INTEL"+sequenceService.findSequence("BUSINESS_ACCOUNT");
        systemAccountInfoValidator.setId(account);
        systemAccountInfoValidator.setAccount(account);

        //????????????md5Hmac??????
        String md5HmacKey = StringRandom.getStringRandom(32);
        systemAccountInfoValidator.setMd5HmacKey(md5HmacKey);

        //????????????AES??????
        String aesKey = StringRandom.getStringRandom(32);
        systemAccountInfoValidator.setAesKey(aesKey);

        systemAccountInfoValidator.setSubmitLimiter(5);

        //????????????AESIV
        String aesIv = StringRandom.getStringRandom(16);
        systemAccountInfoValidator.setAesIv(aesIv);

        systemAccountInfoValidator.setPrice(new BigDecimal("0.00"));
        systemAccountInfoValidator.setSecondPrice(new BigDecimal("0.00"));
        systemAccountInfoValidator.setThirdPrice(new BigDecimal("0.00"));
        systemAccountInfoValidator.setGrantingCredit(new BigDecimal("0.00"));
        systemAccountInfoValidator.setIsGateway("1");
        systemAccountInfoValidator.setBusinessType(this.businessType);

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        view.addObject("op", "add");

        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated SystemAccountInfoValidator systemAccountInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("system/account/account_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        if (result.hasErrors()) {
            view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
            view.addObject("op", op);
            return view;
        }

        systemAccountInfoValidator.setBusinessType(this.businessType);
        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            systemAccountInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            systemAccountInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            systemAccountInfoValidator.setUpdatedTime(new Date());
            systemAccountInfoValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }


        //????????????
        ResponseData data = systemAccountInfoService.save(systemAccountInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync(businessType, systemAccountInfoValidator.getId(), user.getRealName(), op, "add".equals(op) ? "????????????" : "????????????", JSON.toJSONString(systemAccountInfoValidator));
        }

        //????????????
        log.info("[????????????][??????????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(systemAccountInfoValidator));

        view.setView(new RedirectView("/system/account/list", true, false));
        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("system/account/account_edit");
        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????:????????????
        ResponseData<SystemAccountInfoValidator> data = systemAccountInfoService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        SystemAccountInfoValidator systemAccountInfoValidator = data.getData();
        systemAccountInfoValidator.setBusinessType(this.businessType);

        view.addObject("systemAccountInfoValidator", systemAccountInfoValidator);
        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");
        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView center(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("system/account/account_view_center");

        //??????????????????
        ResponseData<SystemAccountInfoValidator> data = systemAccountInfoService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        //????????????????????????
        ResponseData<FinanceAccountValidator> finance = financeAccountService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(finance.getCode())) {
            view.addObject("error", finance.getCode() + ":" + finance.getMessage());
        }

        ResponseData<EnterpriseBasicInfoValidator> enterprise = enterpriseService.findById(data.getData().getEnterpriseId());
        if (!ResponseCode.SUCCESS.getCode().equals(enterprise.getCode())) {
            view.addObject("error", enterprise.getCode() + ":" + enterprise.getMessage());
        }

        view.addObject("enterprise", enterprise.getData());
        view.addObject("financeAccountValidator", finance.getData());
        view.addObject("systemAccountInfoValidator", data.getData());

        return view;
    }


    /**
     * ??????excel
     *
     * @return
     */
    @RequestMapping(value = "/excel/{id}", method = RequestMethod.GET)
    public void view(@PathVariable String id, HttpServletResponse response) {

        //????????????????????????
        ResponseData<SystemAccountInfoValidator> data = systemAccountInfoService.findById(id);
        SystemAccountInfoValidator systemAccountInfoValidator = data.getData();

        CopyOnWriteArrayList<AccountExcelModel> list = new CopyOnWriteArrayList<>();
        AccountExcelModel excelModel = new AccountExcelModel();
        excelModel.setKey("??????");
        excelModel.setValue(systemAccountInfoValidator.getAccount());
        list.add(excelModel);
        AccountExcelModel excelModel1 = new AccountExcelModel();
        excelModel1.setKey("MD5-HMAC-KEY??????");
        excelModel1.setValue(systemAccountInfoValidator.getMd5HmacKey());
        list.add(excelModel1);
        AccountExcelModel excelModel2 = new AccountExcelModel();
        excelModel2.setKey("AES(256)-KEY??????");
        excelModel2.setValue(systemAccountInfoValidator.getAesKey());
        list.add(excelModel2);
        AccountExcelModel excelModel3 = new AccountExcelModel();
        excelModel3.setKey("AES(256)-IV?????????");
        excelModel3.setValue(systemAccountInfoValidator.getAesIv());
        list.add(excelModel3);

        AccountExcelModel excelModel4 = new AccountExcelModel();
        excelModel4.setKey("??????????????????/??????");
        excelModel4.setValue(systemAccountInfoValidator.getSubmitLimiter()+"");
        list.add(excelModel4);

        AccountExcelModel excelModel8 = new AccountExcelModel();
        excelModel8.setKey("IP??????");
        excelModel8.setValue(systemAccountInfoValidator.getIdentifyIp());
        list.add(excelModel8);

        AccountExcelModel excelModel5 = new AccountExcelModel();
        excelModel5.setKey("??????????????????");
        excelModel5.setValue("???MD5-HMAC?????????AES(256)??????????????????????????????????????????AES(256)????????????:AES/CBC/PKCS7Padding???AES_NAME???AES");
        list.add(excelModel5);

        AccountExcelModel excelModel6 = new AccountExcelModel();
        excelModel6.setKey("????????????");
        excelModel6.setValue("?????????????????????????????????????????????????????????????????????");
        list.add(excelModel6);

        String fileName = systemAccountInfoValidator.getAccount();
        try {
            ExcelUtils.writeExcel(fileName, AccountExcelModel.class ,response,list);
        } catch (Exception e) {
            log.error("??????excel????????????:", e);
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/logout/{id}", method = RequestMethod.GET)
    public ModelAndView logout(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("system/account/account_list");
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        ResponseData data = systemAccountInfoService.logout(id);

        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync(businessType, id, user.getRealName(), "logout", "????????????", id);
        }

        view.setView(new RedirectView("/system/account/list", true, false));
        return view;

    }

}
