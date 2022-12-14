package com.smoc.cloud.finance.controller;

import com.smoc.cloud.common.page.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 消费流水
 */
@Slf4j
@Controller
@RequestMapping("/finance")
public class FinanceConsumeController {

    /**
     * 财务充值列表
     * @return
     */
    @RequestMapping(value = "/consume/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("finance/finance_consume_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams",params);

        return view;
    }

    /**
     * 财务充值列表分页
     * @return
     */
    @RequestMapping(value = "/consume/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("finance/finance_consume_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams",params);
        return view;
    }

    /**
     * 账户中心-财务充值列表
     * @return
     */
    @RequestMapping(value = "/account/consume/list", method = RequestMethod.GET)
    public ModelAndView account_list() {
        ModelAndView view = new ModelAndView("finance/finance_account_consume_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams",params);

        return view;
    }

    /**
     * 账户中心-财务充值列表分页
     * @return
     */
    @RequestMapping(value = "/account/consume/page", method = RequestMethod.POST)
    public ModelAndView account_page() {
        ModelAndView view = new ModelAndView("finance/finance_account_consume_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(3);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(22);

        view.addObject("pageParams",params);
        return view;
    }

    /**
     * 消费明细
     * @return
     */
    @RequestMapping(value = "/consume/view", method = RequestMethod.GET)
    public ModelAndView view() {
        ModelAndView view = new ModelAndView("finance/finance_consume_view");

        return view;
    }


}
