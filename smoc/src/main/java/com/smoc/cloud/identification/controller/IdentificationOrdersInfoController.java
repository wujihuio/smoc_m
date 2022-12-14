package com.smoc.cloud.identification.controller;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.identification.validator.IdentificationOrdersInfoValidator;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.identification.service.IdentificationOrdersInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

/**
 * 认证订单管理
 */
@Slf4j
@RestController
@RequestMapping("identification/order")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class IdentificationOrdersInfoController {

    @Autowired
    private IdentificationOrdersInfoService identificationOrdersInfoService;

    /**
     * 查询列表
     * @param pageParams
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public PageList<IdentificationOrdersInfoValidator> page(@RequestBody PageParams<IdentificationOrdersInfoValidator> pageParams) {

        return identificationOrdersInfoService.page(pageParams);
    }

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
    public ResponseData findById(@PathVariable String id) {

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_ERROR.getCode(), MpmValidatorUtil.validateMessage(validator));
        }

        ResponseData data = identificationOrdersInfoService.findById(id);
        return data;
    }

    /**
     * 添加、修改
     * @param op 操作标记，add表示添加，edit表示修改
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ResponseData save(@RequestBody IdentificationOrdersInfoValidator identificationOrdersInfoValidator, @PathVariable String op) {

        //保存操作
        ResponseData data = identificationOrdersInfoService.save(identificationOrdersInfoValidator, op);

        return data;
    }

    /**
     * 添加、修改
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseData update(@RequestBody IdentificationOrdersInfoValidator identificationOrdersInfoValidator) {

        //修改订单
        ResponseData data = identificationOrdersInfoService.update(identificationOrdersInfoValidator);

        return data;
    }

}
