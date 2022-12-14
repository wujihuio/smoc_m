package com.smoc.cloud.customer.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.qo.Dict;
import com.smoc.cloud.common.auth.qo.DictType;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelBasicInfoValidator;
import com.smoc.cloud.common.smoc.configuate.validator.ConfigChannelRepairValidator;
import com.smoc.cloud.common.smoc.customer.qo.AccountContentRepairQo;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.ConfigRouteContentRuleValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.configure.channel.service.ChannelRepairService;
import com.smoc.cloud.configure.channel.service.ChannelService;
import com.smoc.cloud.customer.service.AccountRouteContentService;
import com.smoc.cloud.customer.service.BusinessAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * EC业务账号内容失败补发管理
 */
@Slf4j
@RestController
@RequestMapping("/account/content/repair")
public class AccountContentRepairController {

    @Autowired
    private AccountRouteContentService accountRouteContentService;

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private ChannelRepairService channelRepairService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    /**
     * 内容失败补发列表
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_list");

        //初始化数据
        PageParams<ConfigRouteContentRuleValidator> params = new PageParams<ConfigRouteContentRuleValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        ConfigRouteContentRuleValidator configContentRepairRuleValidator = new ConfigRouteContentRuleValidator();
        params.setParams(configContentRepairRuleValidator);

        //查询
        ResponseData<PageList<ConfigRouteContentRuleValidator>> data = accountRouteContentService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("configContentRepairRuleValidator", configContentRepairRuleValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * 内容失败补发列表查询
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute ConfigRouteContentRuleValidator configContentRepairRuleValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_list");

        //分页查询
        pageParams.setParams(configContentRepairRuleValidator);

        ResponseData<PageList<ConfigRouteContentRuleValidator>> data = accountRouteContentService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("configContentRepairRuleValidator", configContentRepairRuleValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * 业务账号列表
     *
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search() {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/account_search_list");

        //初始化数据
        PageParams<AccountContentRepairQo> params = new PageParams<AccountContentRepairQo>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        AccountContentRepairQo accountContentRepairQo = new AccountContentRepairQo();
        params.setParams(accountContentRepairQo);

        //查询
        ResponseData<PageList<AccountContentRepairQo>> data = accountRouteContentService.accountList(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountContentRepairQo", accountContentRepairQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * 业务账号列表查询
     *
     * @return
     */
    @RequestMapping(value = "/search/page", method = RequestMethod.POST)
    public ModelAndView searchPage(@ModelAttribute AccountContentRepairQo accountContentRepairQo, PageParams pageParams) {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/account_search_list");

        //分页查询
        pageParams.setParams(accountContentRepairQo);

        ResponseData<PageList<AccountContentRepairQo>> data = accountRouteContentService.accountList(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountContentRepairQo", accountContentRepairQo);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * 失败补发页面
     *
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/add/{id}/{carrier}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable String id, @PathVariable String carrier, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_edit");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询账号数据是否存在
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //根据运营商符合要求的备用通道
        ConfigChannelRepairValidator configChannelRepairValidator = new ConfigChannelRepairValidator();
        configChannelRepairValidator.setBusinessType(data.getData().getBusinessType());
        configChannelRepairValidator.setCarrier(carrier);
        configChannelRepairValidator.setFlag("ACCOUNT");
        configChannelRepairValidator.setChannelId(data.getData().getAccountId());
        ResponseData<List<ConfigChannelRepairValidator>> channelList = channelRepairService.findSpareChannel(configChannelRepairValidator);
        if (!ResponseCode.SUCCESS.getCode().equals(channelList.getCode())) {
            view.addObject("error", channelList.getCode() + ":" + channelList.getMessage());
            return view;
        }

        ResponseData<ConfigRouteContentRuleValidator> contentData = accountRouteContentService.findContentRepair(data.getData().getAccountId(), carrier);
        if (!ResponseCode.SUCCESS.getCode().equals(contentData.getCode())) {
            view.addObject("error", contentData.getCode() + ":" + contentData.getMessage());
            return view;
        }

        ConfigRouteContentRuleValidator configRouteContentRuleValidator = contentData.getData();
        if (!StringUtils.isEmpty(configRouteContentRuleValidator)) {
            //op操作标记，add表示添加，edit表示修改
            view.addObject("op", "edit");
            view.addObject("configRouteContentRuleValidator", configRouteContentRuleValidator);
        } else {
            //初始化数据
            configRouteContentRuleValidator = new ConfigRouteContentRuleValidator();
            configRouteContentRuleValidator.setId(UUID.uuid32());
            configRouteContentRuleValidator.setAccountId(data.getData().getAccountId());
            configRouteContentRuleValidator.setCarrier(carrier);
            configRouteContentRuleValidator.setRouteStatus("1");
            if (!"INTL".equals(carrier)) {
                configRouteContentRuleValidator.setAreaCodes("ALL");
            }
            //op操作标记，add表示添加，edit表示修改
            view.addObject("op", "add");
            view.addObject("configRouteContentRuleValidator", configRouteContentRuleValidator);
        }

        view.addObject("channelList", channelList.getData());
        view.addObject("accountBasicInfoValidator", data.getData());

        return view;
    }

    /**
     * 编辑
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_edit");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        ResponseData<ConfigRouteContentRuleValidator> data = accountRouteContentService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //查询账号数据是否存在
        ResponseData<AccountBasicInfoValidator> accountData = businessAccountService.findById(data.getData().getAccountId());
        if (!ResponseCode.SUCCESS.getCode().equals(accountData.getCode())) {
            view.addObject("error", accountData.getCode() + ":" + accountData.getMessage());
            return view;
        }

        //根据运营商符合要求的备用通道
        ConfigChannelRepairValidator configChannelRepairValidator = new ConfigChannelRepairValidator();
        configChannelRepairValidator.setBusinessType(accountData.getData().getBusinessType());
        configChannelRepairValidator.setCarrier(data.getData().getCarrier());
        configChannelRepairValidator.setFlag("ACCOUNT");
        configChannelRepairValidator.setChannelId(data.getData().getAccountId());
        ResponseData<List<ConfigChannelRepairValidator>> channelList = channelRepairService.findSpareChannel(configChannelRepairValidator);
        if (!ResponseCode.SUCCESS.getCode().equals(channelList.getCode())) {
            view.addObject("error", channelList.getCode() + ":" + channelList.getMessage());
            return view;
        }

        view.addObject("configRouteContentRuleValidator", data.getData());
        view.addObject("channelList", channelList.getData());
        view.addObject("accountBasicInfoValidator", accountData.getData());
        view.addObject("op", "edit");

        return view;

    }

    /**
     * 保存
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated ConfigRouteContentRuleValidator configRouteContentRuleValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //判断字数是否在范围内
        if(!StringUtils.isEmpty(configRouteContentRuleValidator.getMinContent()) && !StringUtils.isEmpty(configRouteContentRuleValidator.getMaxContent())){
            if(configRouteContentRuleValidator.getMinContent()>0 && configRouteContentRuleValidator.getMaxContent()>0
                    && (configRouteContentRuleValidator.getMinContent() - configRouteContentRuleValidator.getMaxContent()<0)){
                FieldError err = new FieldError("短信字数", "errorRemark", "短信字数不在范围内");
                result.addError(err);
            }
        }

        //完成参数规则验证
        if (result.hasErrors()) {
            //根据运营商符合要求的备用通道
            ConfigChannelRepairValidator configChannelRepairValidator = new ConfigChannelRepairValidator();
            configChannelRepairValidator.setBusinessType(configRouteContentRuleValidator.getBusinessType());
            configChannelRepairValidator.setCarrier(configRouteContentRuleValidator.getCarrier());
            configChannelRepairValidator.setFlag("ACCOUNT");
            configChannelRepairValidator.setChannelId(configRouteContentRuleValidator.getAccountId());
            ResponseData<List<ConfigChannelRepairValidator>> channelList = channelRepairService.findSpareChannel(configChannelRepairValidator);

            //查询账号数据是否存在
            ResponseData<AccountBasicInfoValidator> accountData = businessAccountService.findById(configRouteContentRuleValidator.getAccountId());
            if (!ResponseCode.SUCCESS.getCode().equals(accountData.getCode())) {
                view.addObject("error", accountData.getCode() + ":" + accountData.getMessage());
                return view;
            }

            view.addObject("configRouteContentRuleValidator", configRouteContentRuleValidator);
            view.addObject("accountBasicInfoValidator", accountData.getData());
            view.addObject("op", op);
            view.addObject("channelList", channelList.getData());
            return view;
        }

        //初始化其他变量
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            configRouteContentRuleValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            configRouteContentRuleValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            configRouteContentRuleValidator.setUpdatedTime(new Date());
            configRouteContentRuleValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //保存数据
        configRouteContentRuleValidator.setBusinessId(configRouteContentRuleValidator.getAccountId());
        configRouteContentRuleValidator.setBusinessType("ACCOUNT");
        ResponseData data = accountRouteContentService.save(configRouteContentRuleValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //保存操作记录
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("BUSINESS_ACCOUNT", configRouteContentRuleValidator.getAccountId(), "add".equals(op) ? configRouteContentRuleValidator.getCreatedBy() : configRouteContentRuleValidator.getUpdatedBy(), op, "add".equals(op) ? "添加内容路由通道" : "修改内容路由通道", JSON.toJSONString(configRouteContentRuleValidator));
        }

        //记录日志
        log.info("[内容路由配置][路由配置][{}][{}]数据:{}", op, user.getUserName(), JSON.toJSONString(configRouteContentRuleValidator));

        view.setView(new RedirectView("/account/content/repair/list", true, false));
        return view;

    }

    /**
     * 删除信息
     *
     * @return
     */
    @RequestMapping(value = "/deleteById/{id}", method = RequestMethod.GET)
    public ModelAndView deleteById(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("customer/account/account_route_content/repair_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询信息
        ResponseData<ConfigRouteContentRuleValidator> infoDate = accountRouteContentService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(infoDate.getCode())) {
            view.addObject("error", infoDate.getCode() + ":" + infoDate.getMessage());
            return view;
        }

        //删除操作
        ResponseData data = accountRouteContentService.deleteById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //保存操作记录
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("BUSINESS_ACCOUNT", infoDate.getData().getBusinessId(), user.getRealName(), "delete", "删除内容失败补发", JSON.toJSONString(infoDate.getData()));
        }

        //记录日志
        log.info("[内容路由配置][路由配置][{}]数据:{}", "delete", user.getUserName(), JSON.toJSONString(infoDate.getData()));

        view.setView(new RedirectView("/account/content/repair/list", true, false));
        return view;
    }

    /**
     * 查询通道区域
     *
     * @return
     */
    @RequestMapping(value = "/findByChannelId/{channelId}", method = RequestMethod.GET)
    public List<Dict> findByChannelId(@PathVariable String channelId, HttpServletRequest request) {

        List<Dict> areaList = new ArrayList<>();

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(channelId);
        if (!MpmValidatorUtil.validate(validator)) {
            return areaList;
        }

        //查询账号数据是否存在
        ResponseData<ChannelBasicInfoValidator> data = channelService.findById(channelId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            return areaList;
        }

        //取字典数据
        ServletContext context = request.getServletContext();
        Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");
        List<Dict> dictList = new ArrayList<>();

        //国际
        if ("INTL".equals(data.getData().getBusinessAreaType())) {
            DictType dictType = dictMap.get("internationalArea");
            dictList = dictType.getDict();
        } else {
            DictType dictType = dictMap.get("provices");
            dictList = dictType.getDict();
        }

        if (dictList.size() < 1) {
            return areaList;
        }

        String areaCodes = data.getData().getSupportAreaCodes();
        if (!StringUtils.isEmpty(areaCodes)) {
            String[] codes = areaCodes.split(",");
            for (int i = 0; i < codes.length; i++) {
                for (int a = 0; a < dictList.size(); a++) {
                    Dict d = new Dict();
                    Dict dict = dictList.get(a);
                    if (codes[i].equals(dict.getFieldCode())) {
                        d.setId(dict.getId());
                        d.setFieldName(dict.getFieldName());
                        d.setFieldCode(dict.getFieldCode());
                        areaList.add(d);
                        break;
                    }
                }
            }
        }

        return areaList;

    }
}
