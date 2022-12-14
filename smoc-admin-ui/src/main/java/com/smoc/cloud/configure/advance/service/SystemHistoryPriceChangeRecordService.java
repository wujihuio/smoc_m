package com.smoc.cloud.configure.advance.service;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.configuate.validator.SystemHistoryPriceChangeRecordValidator;
import com.smoc.cloud.configure.advance.remote.SystemHistoryPriceChangeRecordFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SystemHistoryPriceChangeRecordService {

    @Autowired
    private SystemHistoryPriceChangeRecordFeignClient systemHistoryPriceChangeRecordFeignClient;

    /**
     * 查询列表
     *
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<SystemHistoryPriceChangeRecordValidator>> page(PageParams<SystemHistoryPriceChangeRecordValidator> pageParams) {
        try {
            ResponseData<PageList<SystemHistoryPriceChangeRecordValidator>> pageList = this.systemHistoryPriceChangeRecordFeignClient.page(pageParams);
            return pageList;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * 历史价格调整
     * @param validators
     * @param changeType
     * @return
     */
    public ResponseData save(List<SystemHistoryPriceChangeRecordValidator> validators,String changeType,String taskType) {
        try {
            ResponseData data = this.systemHistoryPriceChangeRecordFeignClient.save(validators,changeType,taskType);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }
}
