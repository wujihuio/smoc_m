package com.smoc.cloud.customer.controller;

import com.smoc.cloud.common.page.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Slf4j
@Controller
@RequestMapping("/ec")
public class CustomerController {

    /**
     * 查询EC列表
     *
     * @return
     */
    @RequestMapping(value = "/customer/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("customer/customer_list");

        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(12);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(115);

        view.addObject("pageParams",params);

        return view;

    }

    /**
     * 分页查询EC
     *
     * @return
     */
    @RequestMapping(value = "/customer/page", method = RequestMethod.POST)
    public ModelAndView page() {
        ModelAndView view = new ModelAndView("customer/customer_list");
        //查询数据
        PageParams params = new PageParams<>();
        params.setPages(12);
        params.setPageSize(10);
        params.setStartRow(1);
        params.setEndRow(10);
        params.setCurrentPage(1);
        params.setTotalRows(115);

        view.addObject("pageParams",params);
        return view;

    }

    /**
     * EC开户
     * level表示企业层级
     * @return
     */
    @RequestMapping(value = "/customer/add/{level}/{parentId}", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String level,@PathVariable String parentId) {
        ModelAndView view = new ModelAndView("customer/customer_edit");
        view.addObject("level",level);
        view.addObject("parentId",parentId);
        return view;

    }

    /**
     * EC开户
     * level表示企业层级
     * @return
     */
    @RequestMapping(value = "/customer/config/{id}", method = RequestMethod.GET)
    public ModelAndView config(@PathVariable String id) {
        ModelAndView view = new ModelAndView("customer/customer_config");

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
     * 查看EC明细
     *
     * @return
     */
    @RequestMapping(value = "/customer/view/{id}", method = RequestMethod.GET)
    public ModelAndView view(@PathVariable String id) {
        ModelAndView view = new ModelAndView("customer/customer_view");

        return view;

    }

}