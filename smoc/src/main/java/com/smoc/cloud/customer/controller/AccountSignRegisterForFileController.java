package com.smoc.cloud.customer.controller;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.qo.CarrierCount;
import com.smoc.cloud.common.smoc.customer.qo.ExportModel;
import com.smoc.cloud.common.smoc.customer.qo.ExportRegisterModel;
import com.smoc.cloud.common.smoc.customer.validator.AccountSignRegisterForFileValidator;
import com.smoc.cloud.customer.service.AccountSignRegisterForFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("sign/register/file")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class AccountSignRegisterForFileController {

    @Autowired
    private AccountSignRegisterForFileService accountSignRegisterForFileService;

    /**
     * 查询列表
     *
     * @param pageParams
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ResponseData<PageList<AccountSignRegisterForFileValidator>> page(@RequestBody PageParams<AccountSignRegisterForFileValidator> pageParams) {

        return accountSignRegisterForFileService.page(pageParams);
    }

    /**
     * 根据运营商，统计未报备得条数
     *
     * @return
     */
    @RequestMapping(value = "/countByCarrier", method = RequestMethod.GET)
    public ResponseData<List<CarrierCount>> countByCarrier() {

        return accountSignRegisterForFileService.countByCarrier();
    }

    /**
     * 查询导出数据
     *
     * @param pageParams
     * @return
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public ResponseData<PageList<ExportModel>> export(@RequestBody PageParams<ExportModel> pageParams) {

        return accountSignRegisterForFileService.export(pageParams);
    }

    /**
     * 根据报备订单号查询导出数据
     *
     * @param pageParams
     * @param registerOrderNo
     * @return
     */
    @RequestMapping(value = "/query/{registerOrderNo}", method = RequestMethod.POST)
    public ResponseData<PageList<ExportModel>> query(@RequestBody PageParams pageParams, @PathVariable String registerOrderNo) {

        return accountSignRegisterForFileService.query(pageParams, registerOrderNo);
    }

    /**
     * 为导出数据生成报备订单号，并改变报备数据状态
     *
     * @param exportRegisterModel
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseData register(@RequestBody ExportRegisterModel exportRegisterModel) {

        return accountSignRegisterForFileService.register(exportRegisterModel);
    }

    /**
     * 为导出数据生成报备订单号，并改变报备数据状态
     *
     * @param registerOrderNo
     * @param status
     * @return
     */
    @RequestMapping(value = "/updateRegisterStatusByOrderNo/{status}/{registerOrderNo}", method = RequestMethod.GET)
    public ResponseData updateRegisterStatusByOrderNo(@PathVariable String status, @PathVariable  String registerOrderNo) {

        return accountSignRegisterForFileService.updateRegisterStatusByOrderNo(registerOrderNo,status);
    }
}
