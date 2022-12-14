package com.smoc.cloud.customer.controller;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.validator.AccountSignRegisterValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseSignCertifyValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.AccountSignRegisterService;
import com.smoc.cloud.customer.service.EnterpriseSignCertifyService;
import com.smoc.cloud.sequence.service.SequenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("sign/register")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class AccountSignRegisterController {

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private AccountSignRegisterService accountSignRegisterService;

    @Autowired
    private EnterpriseSignCertifyService enterpriseSignCertifyService;

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_list");

        //???????????????
        PageParams<AccountSignRegisterValidator> params = new PageParams<>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        AccountSignRegisterValidator accountSignRegisterValidator = new AccountSignRegisterValidator();
        params.setParams(accountSignRegisterValidator);

        //??????
        ResponseData<PageList<AccountSignRegisterValidator>> data = accountSignRegisterService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountSignRegisterValidator", accountSignRegisterValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute AccountSignRegisterValidator accountSignRegisterValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_list");

        //????????????
        pageParams.setParams(accountSignRegisterValidator);

        ResponseData<PageList<EnterpriseBasicInfoValidator>> data = accountSignRegisterService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountSignRegisterValidator", accountSignRegisterValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/add/{account}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String account) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_edit");

        //???????????????
        AccountSignRegisterValidator accountSignRegisterValidator = new AccountSignRegisterValidator();
        accountSignRegisterValidator.setId(UUID.uuid32());
        accountSignRegisterValidator.setAccount(account);
        accountSignRegisterValidator.setRegisterStatus("1");
        accountSignRegisterValidator.setExtendType("1");
        Integer extendNumber = sequenceService.findSequence(account);
        log.info("sequenceService extendNumber:{}",extendNumber);
        if(null == extendNumber){
            extendNumber = sequenceService.findSequence("SIGN_EXTEND_NUMBER");
        }
        accountSignRegisterValidator.setSignExtendNumber(extendNumber + "");

        view.addObject("accountSignRegisterValidator", accountSignRegisterValidator);
        view.addObject("op", "add");

        //???????????????????????????
        PageParams<EnterpriseSignCertifyValidator> params = new PageParams<>();
        params.setPageSize(100000);
        params.setCurrentPage(1);
        EnterpriseSignCertifyValidator enterpriseSignCertifyValidator = new EnterpriseSignCertifyValidator();
        params.setParams(enterpriseSignCertifyValidator);
        //??????
        ResponseData<PageList<EnterpriseSignCertifyValidator>> data = enterpriseSignCertifyService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("certifies", data.getData().getList());

//        //??????????????????????????????????????????
//        ResponseData<List<String>> signNumberList = this.accountSignRegisterService.findExtendDataByAccount(account, "null");
//        if (!ResponseCode.SUCCESS.getCode().equals(signNumberList.getCode())) {
//            view.addObject("error", signNumberList.getCode() + ":" + signNumberList.getMessage());
//            return view;
//        }
        //?????????????????????????????? ?????????map??????
//        Map<String, String> signNumbers = new HashMap<>();
//        if (null != signNumberList.getData() && signNumberList.getData().size() > 0) {
//            for (String numbers : signNumberList.getData()) {
//                String[] numberArray = numbers.split(",");
//                for (String number : numberArray) {
//                    signNumbers.put(number, number);
//                }
//            }
//        }
//        log.info("[signNumberList.getData()]:{}",new Gson().toJson(signNumberList.getData()));
//        log.info("[signNumbers]:{}",new Gson().toJson(signNumbers));
        //?????????????????????????????????????????????????????????????????????
        List<String> signExtendNumbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String number = "";
            if (i < 10) {
                number = "0"+i;
            } else {
                number = ""+i;
            }
            signExtendNumbers.add(number);
        }
        view.addObject("signExtendNumbers", signExtendNumbers);
        //log.info("[signExtendNumbers]:{}",new Gson().toJson(signExtendNumbers));

        /**
         * ??????id?????????????????????????????????????????????
         */
        Map<String,String> thisExtendNumberMap = new HashMap<>();
        view.addObject("thisExtendNumberMap", thisExtendNumberMap);

        /**
         * ??????id???????????????????????????????????????
         */
        Map<String,String> thisServiceTypeMap = new HashMap<>();
        view.addObject("thisServiceTypeMap", thisServiceTypeMap);

        return view;
    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_edit");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        /**
         * ??????:????????????
         */
        ResponseData<AccountSignRegisterValidator> responseData = accountSignRegisterService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
        }

        view.addObject("accountSignRegisterValidator", responseData.getData());
        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");

        /**
         * ???????????????????????????
         */
        PageParams<EnterpriseSignCertifyValidator> params = new PageParams<>();
        params.setPageSize(100000);
        params.setCurrentPage(1);
        EnterpriseSignCertifyValidator enterpriseSignCertifyValidator = new EnterpriseSignCertifyValidator();
        params.setParams(enterpriseSignCertifyValidator);
        //??????
        ResponseData<PageList<EnterpriseSignCertifyValidator>> data = enterpriseSignCertifyService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }
        view.addObject("certifies", data.getData().getList());

        /**
         * ??????????????????????????????????????????
         */
//        ResponseData<List<String>> signNumberList = this.accountSignRegisterService.findExtendDataByAccount(responseData.getData().getAccount(), responseData.getData().getId());
//        if (!ResponseCode.SUCCESS.getCode().equals(signNumberList.getCode())) {
//            view.addObject("error", signNumberList.getCode() + ":" + signNumberList.getMessage());
//            return view;
//        }
        //?????????????????????????????? ?????????map??????
//        Map<String, String> signNumbers = new HashMap<>();
//        if (null != signNumberList.getData() && signNumberList.getData().size() > 0) {
//            for (String numbers : signNumberList.getData()) {
//                String[] numberArray = numbers.split(",");
//                for (String number : numberArray) {
//                    signNumbers.put(number, number);
//                }
//            }
//        }
        //?????????????????????????????????????????????????????????????????????
        List<String> signExtendNumbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String number = "";
            if (i < 10) {
                number = "0"+i;
            } else {
                number = ""+i;
            }
            signExtendNumbers.add(number);
        }
        view.addObject("signExtendNumbers", signExtendNumbers);

        /**
         * ??????id?????????????????????????????????????????????
         */
        String thisExtendNumbers = responseData.getData().getExtendData();
        Map<String,String> thisExtendNumberMap = new HashMap<>();
        String[] thisExtendNumbersArray = thisExtendNumbers.split(",");
        for(String number:thisExtendNumbersArray){
            thisExtendNumberMap.put(number,number);
        }
        view.addObject("thisExtendNumberMap", thisExtendNumberMap);

        /**
         * ??????id???????????????????????????????????????
         */
        Map<String,String> thisServiceTypeMap = new HashMap<>();
        String[] thisServiceTypeArray = responseData.getData().getServiceType().split(",");
        for(String serviceType:thisServiceTypeArray){
            thisServiceTypeMap.put(serviceType,serviceType);
        }
        view.addObject("thisServiceTypeMap", thisServiceTypeMap);

        return view;
    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated AccountSignRegisterValidator accountSignRegisterValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        if (result.hasErrors()) {
            view.addObject("accountSignRegisterValidator", accountSignRegisterValidator);
            view.addObject("op", op);
            return view;
        }

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            accountSignRegisterValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            accountSignRegisterValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            accountSignRegisterValidator.setUpdatedTime(new Date());
            accountSignRegisterValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        accountSignRegisterValidator.setAppName(accountSignRegisterValidator.getSign());
        accountSignRegisterValidator.setMainApplication(accountSignRegisterValidator.getServiceType());

        //????????????
        ResponseData data = accountSignRegisterService.save(accountSignRegisterValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("SIGN_REGISTER", accountSignRegisterValidator.getId(), "add".equals(op) ? accountSignRegisterValidator.getCreatedBy() : accountSignRegisterValidator.getUpdatedBy(), op, "add".equals(op) ? "??????????????????" : "??????????????????", JSON.toJSONString(accountSignRegisterValidator));
        }

        //????????????
        log.info("[????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(accountSignRegisterValidator));

        view.setView(new RedirectView("/sign/register/list", true, false));
        return view;

    }

    /**
     * ??????
     *
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteById/{id}", method = RequestMethod.GET)
    public ModelAndView deleteById(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        ResponseData<AccountSignRegisterValidator> responseData = accountSignRegisterService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
        }

        //???????????????????????????
        ResponseData data = accountSignRegisterService.deleteById(id);

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("SIGN_REGISTER", responseData.getData().getId(), user.getRealName(), "edit", "??????????????????", JSON.toJSONString(responseData.getData()));
        }

        //????????????
        log.info("[????????????][delete][{}]??????:{}", user.getUserName(), JSON.toJSONString(responseData.getData()));

        view.setView(new RedirectView("/sign/register/list", true, false));
        return view;

    }
}
