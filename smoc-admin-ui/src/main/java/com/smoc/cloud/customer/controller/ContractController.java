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
 * EC合同管理
 */
@Slf4j
@Controller
@RequestMapping("/ec")
public class ContractController {

    /**
     * 查询EC合同
     *
     * @return
     */
    @RequestMapping(value = "/contract/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("contract/contract_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);

        return view;

    }

    /**
     * 分页查询EC合同
     *
     * @return
     */
    @RequestMapping(value = "/contract/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("contract/contract_list");
        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        return view;

    }

    /**
     * 添加
     * @return
     */
    @RequestMapping(value = "/contract/add", method = RequestMethod.GET)
    public ModelAndView add() {

        ModelAndView view = new ModelAndView("contract/contract_edit");

        return view;

    }

    /**
     * 编辑
     * @return
     */
    @RequestMapping(value = "/contract/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("contract/contract_edit");

        return view;

    }


    /**
     * 查询EC合同审核
     *
     * @return
     */
    @RequestMapping(value = "/contract/checkList", method = RequestMethod.GET)
    public ModelAndView checkList() {
        ModelAndView view = new ModelAndView("contract/contract_check_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);

        return view;

    }

    /**
     * 分页查询EC合同审核
     *
     * @return
     */
    @RequestMapping(value = "/contract/checkPage", method = RequestMethod.POST)
    public ModelAndView checkPage() {
        ModelAndView view = new ModelAndView("contract/contract_check_list");
        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        return view;

    }

    /**
     * EC中心查询EC合同
     *
     * @return
     */
    @RequestMapping(value = "/customer/contract/list", method = RequestMethod.GET)
    public ModelAndView customer_list() {
        ModelAndView view = new ModelAndView("contract/customer_contract_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        view.addObject("type","contract");

        return view;

    }

    /**
     * 分页查询EC合同
     *
     * @return
     */
    @RequestMapping(value = "/customer/contract/page", method = RequestMethod.POST)
    public ModelAndView customer_page() {
        ModelAndView view = new ModelAndView("contract/customer_contract_list");
        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        view.addObject("type","contract");
        return view;

    }

    /**
     * EC中心查询EC资料
     *
     * @return
     */
    @RequestMapping(value = "/customer/data/list", method = RequestMethod.GET)
    public ModelAndView customer_data_list() {
        ModelAndView view = new ModelAndView("contract/customer_data_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        view.addObject("type","contract");

        return view;

    }

    /**
     * 分页查询EC资料
     *
     * @return
     */
    @RequestMapping(value = "/customer/data/page", method = RequestMethod.POST)
    public ModelAndView customer_data_page() {
        ModelAndView view = new ModelAndView("contract/customer_data_list");
        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(10);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(100);

        view.addObject("pageParams",params);
        view.addObject("type","contract");
        return view;

    }
}