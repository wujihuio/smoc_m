package com.smoc.cloud.customer.controller;

import com.smoc.cloud.common.page.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 业务账号通道挂起
 */
@Slf4j
@Controller
@RequestMapping("/account/channel/suspend")
public class AccountChannelSuspendController {

    /**
     * 业务账号通道挂起列表
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {

        ModelAndView view = new ModelAndView("customer/account_channel_change/account_channel_suspend_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams", params);
        return view;

    }

    /**
     * 业务账号通道挂起分页查询
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("customer/account_channel_change/account_channel_suspend_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams", params);
        return view;

    }

    /**
     * 业务账号通道挂起明细中心
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView view_center(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account_channel_change/account_channel_suspend_view_center");

        return view;

    }

    /**
     * 业务账号通道挂起明细
     *
     * @return
     */
    @RequestMapping(value = "/view/base/{id}", method = RequestMethod.GET)
    public ModelAndView view(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account_channel_change/account_channel_suspend_view");

        return view;

    }

    /**
     * 业务账号通道挂起维护
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account_channel_change/account_channel_suspend_edit");

        return view;

    }

}
