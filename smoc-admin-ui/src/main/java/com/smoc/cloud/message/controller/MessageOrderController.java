package com.smoc.cloud.message.controller;


import com.smoc.cloud.common.page.PageParams;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 短消息任务单
 **/
@Controller
@RequestMapping("/message/order")
public class MessageOrderController {

    /**
     * 短消息任务单查询
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request) {

        ModelAndView view = new ModelAndView("message/message_order_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(8);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(80);

        view.addObject("pageParams",params);

        return view;

    }

    /**
     * 短消息任务单分页查询
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("message/message_order_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(8);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(80);

        view.addObject("pageParams",params);

        return view;

    }
}
