package com.smoc.cloud.spss.service;

import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.spss.repository.IndexStatisticsDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计首页数据
 */
@Slf4j
@Service
public class IndexStatisticsService {

    @Resource
    private IndexStatisticsDataRepository indexStatisticsDataRepository;

    /**
     * 营收总额、毛利总额、发送总量、客户总数、新增客户、通道总数、新增通道
     * @param startDate
     * @param endDate
     * @return
     */
    public ResponseData<Map<String, Object>> statisticsIndexData(String startDate, String endDate) {

        Map<String, Object> map = new HashMap<>();

        Map<String, Object> profit = indexStatisticsDataRepository.getProfitSum(startDate,endDate);
        //营收总额
        map.put("PROFIT_SUM",new BigDecimal(""+profit.get("ACCOUNT_PROFIT_SUM")).subtract(new BigDecimal(""+profit.get("CHANNEL_PROFIT_SUM"))));
        //毛利总额
        map.put("GROSS_PROFIT_SUM",profit.get("ACCOUNT_PROFIT_SUM"));

        //发送总量
        Long messageSendTotal = indexStatisticsDataRepository.getMessageSendTotal(startDate,endDate);
        map.put("MESSAGE_SEND_TOTAL",messageSendTotal);

        //所有客户数
        Long totalAccount = indexStatisticsDataRepository.getAccountCount();
        map.put("TOTAL_ACCOUNT",totalAccount);

        //统计本年度新增账户
        Long totalNewlyAccount = indexStatisticsDataRepository.getAccountCountByYear(startDate.substring(0,4));
        map.put("TOTAL_NEWLY_ACCOUNT",totalNewlyAccount);

        //所有通道数
        Long totalChannel = indexStatisticsDataRepository.getChannelCount();
        map.put("TOTAL_CHANNEL",totalChannel);

        //统计本年度新增通道
        Long totalNewlyChannel = indexStatisticsDataRepository.getChannelCountByYear(startDate.substring(0,4));
        map.put("TOTAL_NEWLY_CHANNEL",totalNewlyChannel);

        return ResponseDataUtil.buildSuccess(map);
    }


}
