package com.smoc.cloud.customer.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SysUserService;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.qo.Users;
import com.smoc.cloud.common.auth.validator.UserValidator;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseExpressInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseInvoiceInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseWebAccountInfoValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.EnterpriseExpressService;
import com.smoc.cloud.customer.service.EnterpriseInvoiceService;
import com.smoc.cloud.customer.service.EnterpriseService;
import com.smoc.cloud.customer.service.EnterpriseWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private EnterpriseWebService enterpriseWebService;

    @Autowired
    private EnterpriseExpressService enterpriseExpressService;

    @Autowired
    private EnterpriseInvoiceService enterpriseInvoiceService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * ??????EC??????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_list");

        //???????????????
        PageParams<EnterpriseBasicInfoValidator> params = new PageParams<EnterpriseBasicInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        EnterpriseBasicInfoValidator enterpriseBasicInfoValidator = new EnterpriseBasicInfoValidator();
        params.setParams(enterpriseBasicInfoValidator);

        //??????
        ResponseData<PageList<EnterpriseBasicInfoValidator>> data = enterpriseService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //????????????
        List<Users> list = sysUserService.salesList();
        Map<String, Users> salesMap = new HashMap<>();
        if(!StringUtils.isEmpty(list) && list.size()>0){
            salesMap = list.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        }

        view.addObject("enterpriseBasicInfoValidator", enterpriseBasicInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        view.addObject("salesMap", salesMap);
        view.addObject("salesList", list);

        return view;

    }

    /**
     * ????????????EC
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute EnterpriseBasicInfoValidator enterpriseBasicInfoValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_list");

        //????????????
        pageParams.setParams(enterpriseBasicInfoValidator);

        ResponseData<PageList<EnterpriseBasicInfoValidator>> data = enterpriseService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //????????????
        List<Users> list = sysUserService.salesList();
        Map<String, Users> salesMap = new HashMap<>();
        if(!StringUtils.isEmpty(list) && list.size()>0){
            salesMap = list.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        }

        view.addObject("enterpriseBasicInfoValidator", enterpriseBasicInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        view.addObject("salesMap", salesMap);
        view.addObject("salesList", list);
        return view;

    }

    /**
     * ??????EC??????
     * level??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/add/{level}/{parentId}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String level, @PathVariable String parentId) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_edit");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(parentId);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //???????????????
        EnterpriseBasicInfoValidator enterpriseBasicInfoValidator = new EnterpriseBasicInfoValidator();
        enterpriseBasicInfoValidator.setEnterpriseId(UUID.uuid32());
        enterpriseBasicInfoValidator.setEnterpriseStatus("1");//????????????
        enterpriseBasicInfoValidator.setEnterpriseParentId(parentId);//???ID
        enterpriseBasicInfoValidator.setEnterpriseProcess("10000");//????????????
        if (!"0000".equals(parentId)) {
            //??????????????????????????????????????????????????????
            ResponseData<EnterpriseBasicInfoValidator> data = enterpriseService.findById(parentId);
            if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
                view.addObject("error", data.getCode() + ":" + data.getMessage());
            }
            enterpriseBasicInfoValidator.setParentEnterpriseName(data.getData().getEnterpriseName());
            enterpriseBasicInfoValidator.setEnterpriseType(data.getData().getEnterpriseType());
            enterpriseBasicInfoValidator.setEnterpriseProcess("10000");
            enterpriseBasicInfoValidator.setSaler(data.getData().getSaler());
            enterpriseBasicInfoValidator.setAccessCorporation(data.getData().getAccessCorporation());
        }

        //??????????????????
        ResponseData<String> enterpriseFlag = enterpriseService.createEnterpriseFlag();
        enterpriseBasicInfoValidator.setEnterpriseFlag(enterpriseFlag.getMessage());

        //??????????????????
        view.addObject("salesList", sysUserService.salesList());

        view.addObject("level", level);
        view.addObject("enterpriseBasicInfoValidator", enterpriseBasicInfoValidator);
        view.addObject("op", "add");

        return view;
    }

    /**
     * ??????EC??????
     * level??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{level}/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String level, @PathVariable String id) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_edit");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        view.addObject("salesList", sysUserService.salesList());

        //??????:????????????
        ResponseData<EnterpriseBasicInfoValidator> data = enterpriseService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        EnterpriseBasicInfoValidator enterpriseBasicInfoValidator = data.getData();

        if ("0000".equals(data.getData().getEnterpriseParentId())) {
            level = "1";
        } else {
            level = "2";
            //??????????????????
            ResponseData<EnterpriseBasicInfoValidator> levelData = enterpriseService.findById(data.getData().getEnterpriseParentId());
            if (!ResponseCode.SUCCESS.getCode().equals(levelData.getCode())) {
                view.addObject("error", levelData.getCode() + ":" + levelData.getMessage());
            }
            enterpriseBasicInfoValidator.setParentEnterpriseName(levelData.getData().getEnterpriseName());
        }

        view.addObject("level", level);
        view.addObject("enterpriseBasicInfoValidator", enterpriseBasicInfoValidator);
        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated EnterpriseBasicInfoValidator enterpriseBasicInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        if (result.hasErrors()) {
            //??????????????????
            view.addObject("salesList", sysUserService.salesList());
            view.addObject("enterpriseBasicInfoValidator", enterpriseBasicInfoValidator);
            view.addObject("op", op);
            return view;
        }

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            enterpriseBasicInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            enterpriseBasicInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            enterpriseBasicInfoValidator.setUpdatedTime(new Date());
            enterpriseBasicInfoValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //??????????????????:0000
        if (StringUtils.isEmpty(enterpriseBasicInfoValidator.getSaler())) {
            enterpriseBasicInfoValidator.setSaler("0000");
        }

        //????????????
        ResponseData data = enterpriseService.save(enterpriseBasicInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("ENTERPRISE_INFO", enterpriseBasicInfoValidator.getEnterpriseId(), "add".equals(op) ? enterpriseBasicInfoValidator.getCreatedBy() : enterpriseBasicInfoValidator.getUpdatedBy(), op, "add".equals(op) ? "??????????????????" : "??????????????????", JSON.toJSONString(enterpriseBasicInfoValidator));
        }

        //????????????
        log.info("[????????????][??????????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(enterpriseBasicInfoValidator));

        view.setView(new RedirectView("/enterprise/center/" + enterpriseBasicInfoValidator.getEnterpriseId(), true, false));
        return view;

    }

    /**
     * EC??????
     * level??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/center/{id}", method = RequestMethod.GET)
    public ModelAndView config(@PathVariable String id) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_center");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????????????????
        ResponseData<EnterpriseBasicInfoValidator> data = enterpriseService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        //????????????
        if (!StringUtils.isEmpty(data.getData().getSaler())) {
            ResponseData<UserValidator> userValidator = sysUserService.userProfile(data.getData().getSaler());
            if (ResponseCode.SUCCESS.getCode().equals(userValidator.getCode())) {
                view.addObject("salerName", userValidator.getData().getBaseUserExtendsValidator().getRealName());
            }
        }

        //??????WEB????????????
        findEnterpriseWebAccountInfo(view, data.getData());

        //??????????????????
        findEnterpriseExpressInfo(view, data.getData());

        //??????????????????
        findEnterpriseInvoiceInfo(view, data.getData());

        view.addObject("enterpriseBasicInfoValidator", data.getData());

        return view;

    }

    //??????????????????
    private void findEnterpriseExpressInfo(ModelAndView view, EnterpriseBasicInfoValidator enterpriseBasicInfoValidator) {
        EnterpriseExpressInfoValidator enterpriseExpressInfoValidator = new EnterpriseExpressInfoValidator();
        enterpriseExpressInfoValidator.setEnterpriseId(enterpriseBasicInfoValidator.getEnterpriseId());
        ResponseData<List<EnterpriseExpressInfoValidator>> expressData = enterpriseExpressService.page(enterpriseExpressInfoValidator);
        if (!ResponseCode.SUCCESS.getCode().equals(expressData.getCode())) {
            view.addObject("error", expressData.getCode() + ":" + expressData.getMessage());
        }
        view.addObject("enterpriseExpressInfoValidator", enterpriseExpressInfoValidator);
        view.addObject("expressList", expressData.getData());
    }

    //??????WEB????????????
    private void findEnterpriseWebAccountInfo(ModelAndView view, EnterpriseBasicInfoValidator enterpriseBasicInfoValidator) {
        EnterpriseWebAccountInfoValidator enterpriseWebAccountInfoValidator = new EnterpriseWebAccountInfoValidator();
        enterpriseWebAccountInfoValidator.setEnterpriseId(enterpriseBasicInfoValidator.getEnterpriseId());
        ResponseData<List<EnterpriseWebAccountInfoValidator>> webData = enterpriseWebService.page(enterpriseWebAccountInfoValidator);
        if (!ResponseCode.SUCCESS.getCode().equals(webData.getCode())) {
            view.addObject("error", webData.getCode() + ":" + webData.getMessage());
        }
        view.addObject("enterpriseWebAccountInfoValidator", enterpriseWebAccountInfoValidator);
        view.addObject("webList", webData.getData());
    }

    //??????????????????
    private void findEnterpriseInvoiceInfo(ModelAndView view, EnterpriseBasicInfoValidator enterpriseBasicInfoValidator) {
        //????????????
        ResponseData<EnterpriseInvoiceInfoValidator> commonData = enterpriseInvoiceService.findByEnterpriseIdAndInvoiceType(enterpriseBasicInfoValidator.getEnterpriseId(),"1");
        if (!ResponseCode.SUCCESS.getCode().equals(commonData.getCode())) {
            view.addObject("error", commonData.getCode() + ":" + commonData.getMessage());
        }
        view.addObject("commonInvoice", commonData.getData());

        //????????????
        ResponseData<EnterpriseInvoiceInfoValidator> specialData = enterpriseInvoiceService.findByEnterpriseIdAndInvoiceType(enterpriseBasicInfoValidator.getEnterpriseId(),"2");
        if (!ResponseCode.SUCCESS.getCode().equals(specialData.getCode())) {
            view.addObject("error", specialData.getCode() + ":" + specialData.getMessage());
        }
        view.addObject("specialInvoice", specialData.getData());

        EnterpriseInvoiceInfoValidator enterpriseInvoiceInfoValidator = new EnterpriseInvoiceInfoValidator();
        enterpriseInvoiceInfoValidator.setEnterpriseId(enterpriseBasicInfoValidator.getEnterpriseId());
        view.addObject("enterpriseInvoiceInfoValidator", enterpriseInvoiceInfoValidator);
    }

    /**
     *  ???????????????????????????
     * @param id
     * @param status
     * @param request
     * @return
     */
    @RequestMapping(value = "/forbiddenEnterprise/{id}/{status}", method = RequestMethod.GET)
    public ModelAndView forbiddenEnterprise(@PathVariable String id,@PathVariable String status, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("customer/enterprise/enterprise_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())) {
            view.addObject("error", enterpriseData.getCode() + ":" + enterpriseData.getMessage());
        }

        //???????????????????????????
        ResponseData data = enterpriseService.forbiddenEnterprise(id,status);

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("ENTERPRISE_INFO", enterpriseData.getData().getEnterpriseId(), user.getRealName(), "edit", "1".equals(status) ? "?????????????????????WEB??????":"?????????????????????WEB??????" , JSON.toJSONString(enterpriseData.getData()));
        }

        //????????????
        log.info("[????????????][{}][{}][{}]??????:{}", "1".equals(status) ? "?????????????????????WEB??????":"?????????????????????WEB??????","edit" , user.getUserName(), JSON.toJSONString(enterpriseData.getData()));

        view.setView(new RedirectView("/enterprise/center/"+enterpriseData.getData().getEnterpriseId(), true, false));
        return view;

    }

}
