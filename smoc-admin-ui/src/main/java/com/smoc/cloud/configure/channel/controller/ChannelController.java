package com.smoc.cloud.configure.channel.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SysUserService;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.qo.Dict;
import com.smoc.cloud.common.auth.qo.DictType;
import com.smoc.cloud.common.auth.qo.Users;
import com.smoc.cloud.common.auth.validator.UserValidator;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelAccountInfoQo;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelBasicInfoQo;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelBasicInfoValidator;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelInterfaceValidator;
import com.smoc.cloud.common.smoc.configuate.validator.ConfigChannelSpareChannelValidator;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticComplaintData;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticSendData;
import com.smoc.cloud.common.smoc.utils.ChannelUtils;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.configure.channel.service.ChannelInterfaceService;
import com.smoc.cloud.configure.channel.service.ChannelService;
import com.smoc.cloud.sequence.service.SequenceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ????????????
 **/
@Slf4j
@RestController
@RequestMapping("/configure/channel")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private ChannelInterfaceService channelInterfaceService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_list");

        ///???????????????
        PageParams<ChannelBasicInfoQo> params = new PageParams<ChannelBasicInfoQo>();
        params.setPageSize(8);
        params.setCurrentPage(1);
        ChannelBasicInfoQo channelBasicInfoQo = new ChannelBasicInfoQo();
        params.setParams(channelBasicInfoQo);

        //??????
        ResponseData<PageList<ChannelBasicInfoQo>> data = channelService.page(params);
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

        view.addObject("channelBasicInfoQo", channelBasicInfoQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        view.addObject("salesMap", salesMap);

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute ChannelBasicInfoQo channelBasicInfoQo, PageParams pageParams) {
        ModelAndView view = new ModelAndView("configure/channel/channel_list");

        //????????????
        pageParams.setParams(channelBasicInfoQo);

        ResponseData<PageList<ChannelBasicInfoQo>> data = channelService.page(pageParams);
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

        view.addObject("channelBasicInfoQo", channelBasicInfoQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        view.addObject("salesMap", salesMap);

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/center/{id}", method = RequestMethod.GET)
    public ModelAndView edit_center(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_center");
        view.addObject("id", id);

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????????????????????????????
        String priceStyle = "AREA_PRICE";
        if(!"base".equals(id)){
            ResponseData<ChannelBasicInfoValidator> data = channelService.findChannelById(id);
            if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
                view.addObject("error", data.getCode() + ":" + data.getMessage());
            }
            priceStyle = data.getData().getPriceStyle();
        }
        view.addObject("priceStyle", priceStyle);

        return view;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/base/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_base");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        view.addObject("salesList", sysUserService.salesList());

        /**
         * id???base???????????????
         */
        if ("base".equals(id)) {
            //???????????????
            ChannelBasicInfoValidator channelBasicInfoValidator = new ChannelBasicInfoValidator();
            channelBasicInfoValidator.setChannelStatus("002");//???????????????
            channelBasicInfoValidator.setReportEnable("1");//??????????????????
            channelBasicInfoValidator.setSignType("2");//???????????????????????????
            channelBasicInfoValidator.setUpMessageEnable("1");//??????????????????
            channelBasicInfoValidator.setTransferEnable("0");//????????????:???
            channelBasicInfoValidator.setBusinessAreaType("COUNTRY");//????????????????????????
            channelBasicInfoValidator.setPriceStyle("UNIFIED_PRICE");//??????????????????
            channelBasicInfoValidator.setChannelProcess("1000");//????????????
            channelBasicInfoValidator.setChannelRunStatus("1");//??????
            channelBasicInfoValidator.setIsRegister("1");//??????????????????

            //op???????????????add???????????????edit????????????
            view.addObject("op", "add");
            view.addObject("channelBasicInfoValidator", channelBasicInfoValidator);

            return view;
        }

        /**
         * ??????:????????????
         */
        ResponseData<ChannelBasicInfoValidator> data = channelService.findChannelById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");
        view.addObject("channelBasicInfoValidator", data.getData());

        return view;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated ChannelBasicInfoValidator channelBasicInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("configure/channel/channel_edit_base");

        //??????
        if("INTL".equals(channelBasicInfoValidator.getCarrier())){
            view.setViewName("configure/channel/international/channel_international_edit_base");
        }

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        channelBasicInfoValidator.setChannelName(StringEscapeUtils.unescapeHtml4(channelBasicInfoValidator.getChannelName()));

        //????????????
        result = paramsValidator(channelBasicInfoValidator, result);
        //????????????????????????
        if (result.hasErrors()) {
            view.addObject("channelBasicInfoValidator", channelBasicInfoValidator);
            view.addObject("op", op);
            return view;
        }

        String priceStyle = "";

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            //????????????ID??????
            String prefixId = sequenceService.getPrefixId("CHANNEL",channelBasicInfoValidator.getBusinessType());
            channelBasicInfoValidator.setChannelId(prefixId);
            channelBasicInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            channelBasicInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            channelBasicInfoValidator.setUpdatedTime(new Date());
            channelBasicInfoValidator.setUpdatedBy(user.getRealName());
            ResponseData<ChannelBasicInfoValidator> channelValidator = channelService.findChannelById(channelBasicInfoValidator.getChannelId());
            priceStyle = channelValidator.getData().getPriceStyle();
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //????????????
        ResponseData data = channelService.save(channelBasicInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("CHANNEL_BASE", channelBasicInfoValidator.getChannelId(), "add".equals(op) ? channelBasicInfoValidator.getCreatedBy() : channelBasicInfoValidator.getUpdatedBy(), op, "add".equals(op) ? "????????????" : "????????????", JSON.toJSONString(channelBasicInfoValidator));
        }

        //????????????
        log.info("[????????????][??????????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(channelBasicInfoValidator));

        //??????????????????????????????????????????iframe?????????????????????add?????????????????????????????????
        if ("add".equals(op) || !priceStyle.equals(channelBasicInfoValidator.getPriceStyle() )) {

            view.addObject("flag", "flag");
        } else {
            view.addObject("salesList", sysUserService.salesList());
        }

        ResponseData<ChannelBasicInfoValidator> channelValidator = channelService.findChannelById(channelBasicInfoValidator.getChannelId());

        view.addObject("channelBasicInfoValidator", channelValidator.getData());
        view.addObject("op", "edit");

        return view;
    }

    //?????????????????????????????????
    private BindingResult paramsValidator(ChannelBasicInfoValidator channelBasicInfoValidator, BindingResult result) {
        //????????????:??????????????????????????????????????????????????????
        if ("1".equals(channelBasicInfoValidator.getTransferEnable()) && StringUtils.isEmpty(channelBasicInfoValidator.getTransferType())) {
            FieldError err = new FieldError("??????????????????", "transferType", "??????????????????????????????");
            result.addError(err);
        }
        //????????????:???????????????????????????????????????????????????????????????
        if ("PROVINCE".equals(channelBasicInfoValidator.getBusinessAreaType()) && StringUtils.isEmpty(channelBasicInfoValidator.getProvince())) {
            FieldError err = new FieldError("????????????", "supportAreaCodes", "????????????????????????");
            result.addError(err);
        }
        //????????????:???????????????????????????????????????????????????????????????
        if ("INTL".equals(channelBasicInfoValidator.getBusinessAreaType()) && StringUtils.isEmpty(channelBasicInfoValidator.getSupportAreaCodes())) {
            FieldError err = new FieldError("????????????", "supportAreaCodes", "????????????????????????");
            result.addError(err);
        }
       /* //????????????:????????????????????????????????????????????????????????????????????????
        if("COUNTRY".equals(channelBasicInfoValidator.getBusinessAreaType()) && !"UNIFIED_PRICE".equals(channelBasicInfoValidator.getPriceStyle())){
            FieldError err = new FieldError("????????????", "priceStyle", "?????????????????????");
            result.addError(err);
        }
        //????????????:????????????????????????????????????????????????????????????????????????
        if(!"COUNTRY".equals(channelBasicInfoValidator.getBusinessAreaType()) && !"AREA_PRICE".equals(channelBasicInfoValidator.getPriceStyle())){
            FieldError err = new FieldError("????????????", "priceStyle", "?????????????????????");
            result.addError(err);
        }*/
        //????????????:?????????????????????????????????????????????????????????
        if ("UNIFIED_PRICE".equals(channelBasicInfoValidator.getPriceStyle()) && StringUtils.isEmpty(channelBasicInfoValidator.getChannelPrice())) {
            FieldError err = new FieldError("??????", "channelPrice", "??????????????????");
            result.addError(err);
        }

        //?????????????????????????????????????????????????????????
        if ("001".equals(channelBasicInfoValidator.getChannelStatus()) && !"1101".equals(channelBasicInfoValidator.getChannelProcess())) {
            FieldError err = new FieldError("????????????", "channelStatus", "?????????????????????????????????????????????");
            result.addError(err);
        }

        return result;
    }

    /**
     * ????????????????????????
     *
     * @param channelId
     * @param request
     * @return
     */
    @RequestMapping(value = "/findChannelBusinessArea/{channelId}", produces = "text/html;charset=utf-8", method = RequestMethod.GET)
    public String findChannelPrice(@PathVariable String channelId, HttpServletRequest request) {

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(channelId);
        if (!MpmValidatorUtil.validate(validator)) {
            return MpmValidatorUtil.validateMessage(validator);
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(channelId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            return data.getMessage();
        }

        //??????
        String dictType = "";
        if ("INTL".equals(data.getData().getBusinessAreaType())) {
            dictType = "internationalArea";
        } else {
            dictType = "provices";
        }

        String areaType = "????????????";
        String supportAreaCodes = data.getData().getSupportAreaCodes();
        //??????????????????ALL????????????????????????
        if ("ALL".equals(data.getData().getSupportAreaCodes()) ) {
            if (StringUtils.isEmpty(data.getData().getMaskProvince())) {
                return "???????????????";
            } else {
                areaType = "??????";
                supportAreaCodes = data.getData().getMaskProvince();
            }
        }

        //???????????????
        ServletContext context = request.getServletContext();
        Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");

        return areaType + "???" + ChannelUtils.getAreaProvince(dictMap,dictType,supportAreaCodes);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/filter/{id}", method = RequestMethod.GET)
    public ModelAndView filter(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_filter");

        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/extend/{id}", method = RequestMethod.GET)
    public ModelAndView extend(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_extend_param");

        Map<String, DictType> dictMap = (Map<String, DictType>) request.getSession().getServletContext().getAttribute("dict");
        DictType dictType = dictMap.get("channelExtendField");
        List<Dict> dictList = dictType.getDict();

        view.addObject("channelExtendFields", dictList);

        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView view_center(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_view_center");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            return view;
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("id", id);
        view.addObject("flag", data.getData().getCarrier());

        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/base/{id}", method = RequestMethod.GET)
    public ModelAndView view_base(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_view_base");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            return view;
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //????????????????????????
        String realName = "";
        if (!StringUtils.isEmpty(data.getData().getChannelAccessSales())) {
            ResponseData<UserValidator> userData = sysUserService.userProfile(data.getData().getChannelAccessSales());
            if (ResponseCode.SUCCESS.getCode().equals(userData.getCode()) && !StringUtils.isEmpty(userData.getData())) {
                realName = userData.getData().getBaseUserExtendsValidator().getRealName();
            }
        }

        //????????????????????????
        ChannelInterfaceValidator channelInterfaceValidator = new ChannelInterfaceValidator();
        ResponseData<ChannelInterfaceValidator> channelInterfaceData = channelInterfaceService.findChannelInterfaceByChannelId(id);
        if (ResponseCode.SUCCESS.getCode().equals(channelInterfaceData.getCode()) && !StringUtils.isEmpty(channelInterfaceData.getData())) {
            channelInterfaceValidator = channelInterfaceData.getData();
        }

        view.addObject("channelBasicInfoValidator", data.getData());
        view.addObject("channelInterfaceValidator", channelInterfaceValidator);
        view.addObject("realName", realName);

        return view;
    }

    /**
     * ???????????????
     *
     * @return
     */
    @RequestMapping(value = "/statistic/sendNumber/{businessId}", method = RequestMethod.GET)
    public ModelAndView statisticSend(@PathVariable String businessId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("configure/channel/channel_view_statistic_send");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(businessId);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(businessId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }
        view.addObject("businessId", businessId);

        return view;
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/statistic/sendNumberMonth/{businessId}/{type}", method = RequestMethod.GET)
    public AccountStatisticSendData sendNumberMonth(@PathVariable String businessId, @PathVariable String type, HttpServletRequest request) {

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(businessId);
        if (!MpmValidatorUtil.validate(validator)) {
            return new AccountStatisticSendData();
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(businessId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            return new AccountStatisticSendData();
        }

        AccountStatisticSendData statisticSendData = new AccountStatisticSendData();
        statisticSendData.setAccountId(businessId);
        statisticSendData.setDimension(type);

        statisticSendData = channelService.statisticChannelSendNumber(statisticSendData);

        return statisticSendData;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/statistic/complaint/{businessId}", method = RequestMethod.GET)
    public ModelAndView statisticComplaint(@PathVariable String businessId, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("configure/channel/channel_view_statistic_complaint");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(businessId);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        /**
         * ??????????????????
         */
        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(businessId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("businessId", businessId);

        return view;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/statistic/complaintMonth/{businessId}", method = RequestMethod.GET)
    public AccountStatisticComplaintData statisticComplaintMonth(@PathVariable String businessId, HttpServletRequest request) {

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(businessId);
        if (!MpmValidatorUtil.validate(validator)) {
            return new AccountStatisticComplaintData();
        }

        //????????????????????????
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(businessId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            return new AccountStatisticComplaintData();
        }

        AccountStatisticComplaintData statisticComplaintData = new AccountStatisticComplaintData();
        statisticComplaintData.setAccountId(businessId);

        statisticComplaintData = channelService.statisticComplaintMonth(statisticComplaintData);

        return statisticComplaintData;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/account/list/{channelId}", method = RequestMethod.GET)
    public ModelAndView accountList(@PathVariable String channelId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_view_account_list");

        ///???????????????
        PageParams<ChannelAccountInfoQo> params = new PageParams<ChannelAccountInfoQo>();
        params.setPageSize(15);
        params.setCurrentPage(1);
        ChannelAccountInfoQo channelAccountInfoQo = new ChannelAccountInfoQo();
        channelAccountInfoQo.setChannelId(channelId);
        channelAccountInfoQo.setConfigType("ACCOUNT_CHANNEL");
        params.setParams(channelAccountInfoQo);

        //??????
        ResponseData<PageList<ChannelAccountInfoQo>> data = channelService.channelAccountList(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("channelAccountInfoQo", channelAccountInfoQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/account/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute ChannelAccountInfoQo channelAccountInfoQo, PageParams pageParams) {
        ModelAndView view = new ModelAndView("configure/channel/channel_view_account_list");

        //????????????
        channelAccountInfoQo.setConfigType("ACCOUNT_CHANNEL");
        pageParams.setParams(channelAccountInfoQo);

        ResponseData<PageList<ChannelAccountInfoQo>> data = channelService.channelAccountList(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("channelAccountInfoQo", channelAccountInfoQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/channelCopy/{id}", method = RequestMethod.GET)
    public ModelAndView channelCopy(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_center");
        view.addObject("id", id);

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //????????????????????????????????????
        String priceStyle = "AREA_PRICE";
        if(!"base".equals(id)){
            ResponseData<ChannelBasicInfoValidator> data = channelService.findChannelById(id);
            if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
                view.addObject("error", data.getCode() + ":" + data.getMessage());
            }
            priceStyle = data.getData().getPriceStyle();
        }
        view.addObject("priceStyle", priceStyle);
        view.addObject("flag", "copy");

        return view;
    }

    /**
     * ????????????????????????
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/edit/copy/{id}", method = RequestMethod.GET)
    public ModelAndView copy(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/channel/channel_edit_base");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        view.addObject("salesList", sysUserService.salesList());

        /**
         * ??????:????????????
         */
        ResponseData<ChannelBasicInfoValidator> data = channelService.findChannelById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        ChannelBasicInfoValidator channelBasicInfoValidator = data.getData();
        channelBasicInfoValidator.setCopyChannelId(channelBasicInfoValidator.getChannelId());
        channelBasicInfoValidator.setChannelStatus("002");//???????????????
        channelBasicInfoValidator.setChannelProcess("1000");//????????????
        channelBasicInfoValidator.setChannelId("");

        //op???????????????add???????????????edit????????????
        view.addObject("op", "add");
        view.addObject("channelBasicInfoValidator", channelBasicInfoValidator);

        return view;
    }
}
