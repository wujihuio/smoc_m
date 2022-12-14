package com.smoc.cloud.http.api.common;

import com.google.gson.Gson;
import com.smoc.cloud.common.gateway.utils.ValidatorUtil;
import com.smoc.cloud.common.http.server.message.request.ReportBatchParams;
import com.smoc.cloud.common.http.server.message.request.ReportStatusRequestParams;

import com.smoc.cloud.common.http.server.message.response.ReportResponseParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.http.service.ReportStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**
 * 获取状态报告
 */
@Slf4j
@RestController
@RequestMapping("report")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ReportStatusController {

    @Autowired
    private ReportStatusService reportStatusService;

    /**
     * 根据订单号获取状态报告
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/getReport", method = RequestMethod.POST)
    public ResponseData<List<ReportResponseParams>> getReport(@RequestBody ReportStatusRequestParams params) {

        log.info("[根据账号获取状态报告]：{}", new Gson().toJson(params));

        if (!ValidatorUtil.validate(params)) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_ERROR.getCode(), ValidatorUtil.validateMessage(params));
        }

        return reportStatusService.getReport(params);
    }

//    /**
//     * 批量获取状态报告
//     *
//     * @param params
//     * @return
//     */
//    @RequestMapping(value = "/getReportBatch", method = RequestMethod.POST)
//    public ResponseData<List<ReportResponseParams>> getReportBatch(@RequestBody ReportBatchParams params) {
//
//        log.info("[批量获取状态报告]：{}", new Gson().toJson(params));
//
//        if (!ValidatorUtil.validate(params)) {
//            return ResponseDataUtil.buildError(ResponseCode.PARAM_ERROR.getCode(), ValidatorUtil.validateMessage(params));
//        }
//
//        return reportStatusService.getReportBatch(params);
//    }
}
