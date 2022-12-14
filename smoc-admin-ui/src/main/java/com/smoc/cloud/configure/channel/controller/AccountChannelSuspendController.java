package com.smoc.cloud.configure.channel.controller;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelBasicInfoQo;
import com.smoc.cloud.common.smoc.configuate.validator.ConfigChannelChangeItemValidator;
import com.smoc.cloud.common.smoc.configuate.validator.ConfigChannelChangeValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountChannelInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountRechargeValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import com.smoc.cloud.common.smoc.identification.validator.IdentificationAccountInfoValidator;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.configure.channel.service.ChannelService;
import com.smoc.cloud.configure.channel.service.ConfigChannelChangeItemService;
import com.smoc.cloud.configure.channel.service.ConfigChannelChangeService;
import com.smoc.cloud.customer.service.AccountChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ????????????????????????
 */
@Slf4j
@Controller
@RequestMapping("/configure/channel/suspend")
public class AccountChannelSuspendController {


    @Autowired
    private ChannelService channelService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private AccountChannelService accountChannelService;

    @Autowired
    private ConfigChannelChangeService configChannelChangeService;

    @Autowired
    private ConfigChannelChangeItemService configChannelChangeItemService;

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_list");
        //???????????????
        PageParams<ConfigChannelChangeValidator> params = new PageParams<ConfigChannelChangeValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        ConfigChannelChangeValidator configChannelChangeValidator = new ConfigChannelChangeValidator();
        configChannelChangeValidator.setChangeType("SUSPEND");
        params.setParams(configChannelChangeValidator);

        //??????
        ResponseData<PageList<ConfigChannelChangeValidator>> data = configChannelChangeService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }
        view.addObject("configChannelChangeValidator", configChannelChangeValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute ConfigChannelChangeValidator configChannelChangeValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_list");

        configChannelChangeValidator.setChangeType("SUSPEND");
        //????????????
        pageParams.setParams(configChannelChangeValidator);

        ResponseData<PageList<ConfigChannelChangeValidator>> data = configChannelChangeService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("configChannelChangeValidator", configChannelChangeValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView view_center(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_view_center");
        view.addObject("id",id);
        return view;

    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/view/base/{id}", method = RequestMethod.GET)
    public ModelAndView view(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_view");

        //??????????????????
        ResponseData<ConfigChannelChangeValidator> changeData = configChannelChangeService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(changeData.getCode())) {
            view.addObject("error", changeData.getCode() + ":" + changeData.getMessage());
            return view;
        }

        //??????????????????
        PageParams<ChannelBasicInfoQo> params = new PageParams<ChannelBasicInfoQo>();
        params.setPageSize(8);
        params.setCurrentPage(1);
        ChannelBasicInfoQo channelBasicInfoQo = new ChannelBasicInfoQo();
        channelBasicInfoQo.setChannelId(changeData.getData().getChannelId());
        params.setParams(channelBasicInfoQo);

        //??????
        ResponseData<PageList<ChannelBasicInfoQo>> data = channelService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //???????????????????????? ??????????????????
        ResponseData<List<AccountChannelInfoValidator>> accountChannelData = accountChannelService.channelDetail(changeData.getData().getChannelId());
        if (!ResponseCode.SUCCESS.getCode().equals(accountChannelData.getCode())) {
            view.addObject("error", accountChannelData.getCode() + ":" + accountChannelData.getMessage());
            return view;
        }

        //??????????????????????????????
        ResponseData<List<ConfigChannelChangeItemValidator>> changeItemsData = configChannelChangeItemService.findChangeItems(id);
        if (!ResponseCode.SUCCESS.getCode().equals(changeItemsData.getCode())) {
            view.addObject("error", changeItemsData.getCode() + ":" + changeItemsData.getMessage());
            return view;
        }
        Map<String,Boolean> selectedMap = new HashMap<>();
        if(null != changeItemsData.getData() && changeItemsData.getData().size()>0){
            for(ConfigChannelChangeItemValidator obj:changeItemsData.getData()){
                selectedMap.put(obj.getBusinessAccount(),true);
            }
        }

        ConfigChannelChangeValidator configChannelChangeValidator = changeData.getData();

        view.addObject("selectedMap", selectedMap);
        view.addObject("configChannelChangeValidator", configChannelChangeValidator);
        view.addObject("accountList", accountChannelData.getData());
        view.addObject("channel", data.getData().getList().get(0));
        return view;

    }

    /**
     * ???????????????????????? ??????
     *
     * @return
     */
    @RequestMapping(value = "/add/{channelId}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String channelId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_add");
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //??????????????????
        PageParams<ChannelBasicInfoQo> params = new PageParams<ChannelBasicInfoQo>();
        params.setPageSize(8);
        params.setCurrentPage(1);
        ChannelBasicInfoQo channelBasicInfoQo = new ChannelBasicInfoQo();
        channelBasicInfoQo.setChannelId(channelId);
        params.setParams(channelBasicInfoQo);

        //??????
        ResponseData<PageList<ChannelBasicInfoQo>> data = channelService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //???????????????????????? ??????????????????
        ResponseData<List<AccountChannelInfoValidator>> accountChannelData = accountChannelService.channelDetail(channelId);
        if (!ResponseCode.SUCCESS.getCode().equals(accountChannelData.getCode())) {
            view.addObject("error", accountChannelData.getCode() + ":" + accountChannelData.getMessage());
            return view;
        }

        ConfigChannelChangeValidator configChannelChangeValidator = new ConfigChannelChangeValidator();
        configChannelChangeValidator.setId(UUID.uuid32());
        configChannelChangeValidator.setChangeType("SUSPEND");
        configChannelChangeValidator.setChannelId(channelId);
        configChannelChangeValidator.setChangeStatus("1");
        configChannelChangeValidator.setCreatedBy(user.getRealName());

        //log.info("accountList:{}", new Gson().toJson(accountChannelData.getData()));
        view.addObject("op", "add");
        view.addObject("configChannelChangeValidator", configChannelChangeValidator);
        view.addObject("accountList", accountChannelData.getData());
        view.addObject("channel", data.getData().getList().get(0));
        return view;

    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_edit");

        //??????????????????
        ResponseData<ConfigChannelChangeValidator> changeData = configChannelChangeService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(changeData.getCode())) {
            view.addObject("error", changeData.getCode() + ":" + changeData.getMessage());
            return view;
        }

        //??????????????????
        PageParams<ChannelBasicInfoQo> params = new PageParams<ChannelBasicInfoQo>();
        params.setPageSize(8);
        params.setCurrentPage(1);
        ChannelBasicInfoQo channelBasicInfoQo = new ChannelBasicInfoQo();
        channelBasicInfoQo.setChannelId(changeData.getData().getChannelId());
        params.setParams(channelBasicInfoQo);

        //??????
        ResponseData<PageList<ChannelBasicInfoQo>> data = channelService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //???????????????????????? ??????????????????
        ResponseData<List<AccountChannelInfoValidator>> accountChannelData = accountChannelService.channelDetail(changeData.getData().getChannelId());
        if (!ResponseCode.SUCCESS.getCode().equals(accountChannelData.getCode())) {
            view.addObject("error", accountChannelData.getCode() + ":" + accountChannelData.getMessage());
            return view;
        }

        //??????????????????????????????
        ResponseData<List<ConfigChannelChangeItemValidator>> changeItemsData = configChannelChangeItemService.findChangeItems(id);
        if (!ResponseCode.SUCCESS.getCode().equals(changeItemsData.getCode())) {
            view.addObject("error", changeItemsData.getCode() + ":" + changeItemsData.getMessage());
            return view;
        }
        Map<String,Boolean> selectedMap = new HashMap<>();
        if(null != changeItemsData.getData() && changeItemsData.getData().size()>0){
           for(ConfigChannelChangeItemValidator obj:changeItemsData.getData()){
               selectedMap.put(obj.getBusinessAccount(),true);
           }
        }

        ConfigChannelChangeValidator configChannelChangeValidator = changeData.getData();

        view.addObject("op", "edit");
        view.addObject("selectedMap", selectedMap);
        view.addObject("configChannelChangeValidator", configChannelChangeValidator);
        view.addObject("accountList", accountChannelData.getData());
        view.addObject("channel", data.getData().getList().get(0));
        return view;

    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView Save(@ModelAttribute @Validated ConfigChannelChangeValidator configChannelChangeValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        configChannelChangeValidator.setCreatedBy(user.getRealName());
        configChannelChangeValidator.setUpdatedBy(user.getRealName());

        ResponseData data = configChannelChangeService.save(configChannelChangeValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("CHANNEL_CHANGE", configChannelChangeValidator.getId(), user.getRealName(), "SUSPEND", "????????????", JSON.toJSONString(configChannelChangeValidator));
        }

        //????????????
        log.info("[????????????][????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(configChannelChangeValidator));


        view.setView(new RedirectView("/configure/channel/suspend/list", true, false));

        return view;

    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.GET)
    public ModelAndView cancel(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_list");

        ResponseData data = configChannelChangeService.cancelChannelChange(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }
        view.setView(new RedirectView("/configure/channel/suspend/list", true, false));
        return view;

    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/change/record/{id}", method = RequestMethod.GET)
    public ModelAndView record(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("configure/account_channel_change/account_channel_suspend_change_list");

        //??????????????????????????????
        ResponseData<List<ConfigChannelChangeItemValidator>> changeItemsData = configChannelChangeItemService.findChangeAllItems(id);
        if (!ResponseCode.SUCCESS.getCode().equals(changeItemsData.getCode())) {
            view.addObject("error", changeItemsData.getCode() + ":" + changeItemsData.getMessage());
            return view;
        }
        view.addObject("changeItems", changeItemsData.getData());
        return view;

    }


}
