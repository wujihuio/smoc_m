package com.smoc.cloud.finance.controller;


import com.smoc.cloud.common.page.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * EC对账
 */
@Slf4j
@Controller
@RequestMapping("/finance/reconciliation/ec")
public class ReconciliationEcController {

    /**
     * EC对账列表
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_list");

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
     * EC对账列表分页
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_list");

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
     * EC对账
     *
     * @return
     */
    @RequestMapping(value = "/reconciliation/{id}", method = RequestMethod.GET)
    public ModelAndView reconciliation(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_reconciliation");

        //查询数据

        return view;
    }

    /**
     * EC对账中心
     *
     * @return
     */
    @RequestMapping(value = "/reconciliation/center/{id}", method = RequestMethod.GET)
    public ModelAndView reconciliation_center(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_reconciliation_center");

        //查询数据

        return view;
    }

    /**
     * EC对账记录
     *
     * @return
     */
    @RequestMapping(value = "/reconciliation/record/{id}", method = RequestMethod.GET)
    public ModelAndView reconciliation_record(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_reconciliation_record");

        //查询数据

        return view;
    }

    /**
     * EC对账明细中心
     *
     * @return
     */
    @RequestMapping(value = "/view/center/{id}", method = RequestMethod.GET)
    public ModelAndView center(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_view_center");

        //查询数据

        return view;
    }

    /**
     * EC对账明细
     *
     * @return
     */
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public ModelAndView view(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("reconciliation/finance_reconciliation_ec_view");

        //查询数据

        return view;
    }
}
