package com.smoc.cloud.identification.controller;


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
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.StringRandom;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.EnterpriseService;
import com.smoc.cloud.finance.service.FinanceAccountService;
import com.smoc.cloud.identification.model.AccountExcelModel;
import com.smoc.cloud.identification.service.IdentificationAccountInfoService;
import com.smoc.cloud.sequence.service.SequenceService;
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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ??????????????????
 */
@Slf4j
@Controller
@RequestMapping("/identification/account")
public class IdentificationAccountController {

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private FinanceAccountService financeAccountService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private IdentificationAccountInfoService identificationAccountInfoService;

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("identification/account/identification_account_list");

        //???????????????
        PageParams<IdentificationAccountInfoValidator> params = new PageParams<IdentificationAccountInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        IdentificationAccountInfoValidator identificationAccountInfoValidator = new IdentificationAccountInfoValidator();
        params.setParams(identificationAccountInfoValidator);

        //??????
        ResponseData<PageList<IdentificationAccountInfoValidator>> data = identificationAccountInfoService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("identificationAccountInfoValidator", identificationAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute IdentificationAccountInfoValidator identificationAccountInfoValidator, PageParams pageParams) {

        ModelAndView view = new ModelAndView("identification/account/identification_account_list");
        //????????????
        pageParams.setParams(identificationAccountInfoValidator);

        ResponseData<PageList<IdentificationAccountInfoValidator>> data = identificationAccountInfoService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("identificationAccountInfoValidator", identificationAccountInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/add/{enterpriseId}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String enterpriseId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("identification/account/identification_account_edit");

        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(enterpriseId);
        IdentificationAccountInfoValidator identificationAccountInfoValidator = new IdentificationAccountInfoValidator();

        identificationAccountInfoValidator.setEnterpriseId(enterpriseData.getData().getEnterpriseId());
        identificationAccountInfoValidator.setEnterpriseName(enterpriseData.getData().getEnterpriseName());
        //??????????????????
        identificationAccountInfoValidator.setAccountStatus("001");
        //??????????????????
        identificationAccountInfoValidator.setAccountType("2");

        //????????????????????????
        String identificationAccount = "XYIA"+sequenceService.findSequence("BUSINESS_ACCOUNT");
        identificationAccountInfoValidator.setId(identificationAccount);
        identificationAccountInfoValidator.setIdentificationAccount(identificationAccount);

        //????????????md5Hmac??????
        String md5HmacKey = StringRandom.getStringRandom(32);
        identificationAccountInfoValidator.setMd5HmacKey(md5HmacKey);

        //????????????AES??????
        String aesKey = StringRandom.getStringRandom(32);
        identificationAccountInfoValidator.setAesKey(aesKey);

        identificationAccountInfoValidator.setSubmitLimiter(5);

        //????????????AESIV
        String aesIv = StringRandom.getStringRandom(16);
        identificationAccountInfoValidator.setAesIv(aesIv);

        identificationAccountInfoValidator.setGrantingCredit(new BigDecimal("0.00"));

        view.addObject("identificationAccountInfoValidator", identificationAccountInfoValidator);
        view.addObject("op", "add");

        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated IdentificationAccountInfoValidator identificationAccountInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("identification/account/identification_account_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        if (result.hasErrors()) {
            //??????????????????
            view.addObject("identificationAccountInfoValidator", identificationAccountInfoValidator);
            view.addObject("op", op);
            return view;
        }

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            identificationAccountInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            identificationAccountInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            identificationAccountInfoValidator.setUpdatedTime(new Date());
            identificationAccountInfoValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }


        //????????????
        ResponseData data = identificationAccountInfoService.save(identificationAccountInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("IDENTIFICATION_ACCOUNT", identificationAccountInfoValidator.getId(), user.getRealName(), op, "add".equals(op) ? "??????????????????" : "??????????????????", JSON.toJSONString(identificationAccountInfoValidator));
        }

        //????????????
        log.info("[????????????][????????????????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(identificationAccountInfoValidator));

        view.setView(new RedirectView("/identification/account/list", true, false));
        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("identification/account/identification_account_edit");
        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????:????????????
        ResponseData<IdentificationAccountInfoValidator> data = identificationAccountInfoService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        IdentificationAccountInfoValidator identificationAccountInfoValidator = data.getData();

        view.addObject("identificationAccountInfoValidator", identificationAccountInfoValidator);
        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");
        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView center(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("identification/account/identification_account_view_center");

        //????????????????????????
        ResponseData<IdentificationAccountInfoValidator> data = identificationAccountInfoService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        //??????????????????????????????
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
        view.addObject("identificationAccountInfoValidator", data.getData());

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
        ResponseData<IdentificationAccountInfoValidator> data = identificationAccountInfoService.findById(id);
        IdentificationAccountInfoValidator identificationAccountInfoValidator = data.getData();

        CopyOnWriteArrayList<AccountExcelModel> list = new CopyOnWriteArrayList<>();
        AccountExcelModel excelModel = new AccountExcelModel();
        excelModel.setKey("??????????????????");
        excelModel.setValue(identificationAccountInfoValidator.getIdentificationAccount());
        list.add(excelModel);
        AccountExcelModel excelModel1 = new AccountExcelModel();
        excelModel1.setKey("MD5-HMAC-KEY??????");
        excelModel1.setValue(identificationAccountInfoValidator.getMd5HmacKey());
        list.add(excelModel1);
        AccountExcelModel excelModel2 = new AccountExcelModel();
        excelModel2.setKey("AES(256)-KEY??????");
        excelModel2.setValue(identificationAccountInfoValidator.getAesKey());
        list.add(excelModel2);
        AccountExcelModel excelModel3 = new AccountExcelModel();
        excelModel3.setKey("AES(256)-IV?????????");
        excelModel3.setValue(identificationAccountInfoValidator.getAesIv());
        list.add(excelModel3);

        AccountExcelModel excelModel4 = new AccountExcelModel();
        excelModel4.setKey("??????????????????/??????");
        excelModel4.setValue(identificationAccountInfoValidator.getSubmitLimiter()+"");
        list.add(excelModel4);

        AccountExcelModel excelModel8 = new AccountExcelModel();
        excelModel8.setKey("IP??????");
        excelModel8.setValue(identificationAccountInfoValidator.getIdentifyIp());
        list.add(excelModel8);

        AccountExcelModel excelModel5 = new AccountExcelModel();
        excelModel5.setKey("??????????????????");
        excelModel5.setValue("???MD5-HMAC?????????AES(256)??????????????????????????????????????????AES(256)????????????:AES/CBC/PKCS7Padding???AES_NAME???AES");
        list.add(excelModel5);

        AccountExcelModel excelModel6 = new AccountExcelModel();
        excelModel6.setKey("????????????");
        excelModel6.setValue("?????????????????????????????????????????????????????????????????????");
        list.add(excelModel6);

        String fileName = identificationAccountInfoValidator.getIdentificationAccount();
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
        ModelAndView view = new ModelAndView("identification/account/identification_account_list");
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        ResponseData data = identificationAccountInfoService.logout(id);

        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("IDENTIFICATION_ACCOUNT", id, user.getRealName(), "logout", "??????????????????", id);
        }

        view.setView(new RedirectView("/identification/account/list", true, false));
        return view;

    }

}
