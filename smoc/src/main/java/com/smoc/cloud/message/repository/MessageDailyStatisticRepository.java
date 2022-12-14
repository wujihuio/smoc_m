package com.smoc.cloud.message.repository;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.smoc.message.MessageDailyStatisticValidator;
import com.smoc.cloud.common.smoc.message.model.StatisticMessageSendData;
import com.smoc.cloud.common.smoc.query.model.AccountSendStatisticItemsModel;
import com.smoc.cloud.common.smoc.query.model.ChannelSendStatisticModel;
import com.smoc.cloud.message.entity.MessageDailyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;

/**
 * 短信日统计
 */
public interface MessageDailyStatisticRepository extends JpaRepository<MessageDailyStatistic, String> {

    /**
     * 分页查询
     * @param pageParams
     * @return
     */
    PageList<MessageDailyStatisticValidator> page(PageParams<MessageDailyStatisticValidator> pageParams);


    /**
     * 统计发送数量
     *
     * @param qo
     * @return
     */
    Map<String, Object> countSum(MessageDailyStatisticValidator qo);

    /**
     * 查询通道发送量
     * @param params
     * @return
     */
    PageList<ChannelSendStatisticModel> queryChannelSendStatistics(PageParams<ChannelSendStatisticModel> params);

    /**
     * 查询通道下面账号发送量
     * @param params
     * @return
     */
    PageList<AccountSendStatisticItemsModel> accountMessageSendListByChannel(PageParams<AccountSendStatisticItemsModel> params);

    /**
     * 通道发送量统计
     * @param qo
     * @return
     */
    Map<String, Object> channelSendCountSum(MessageDailyStatisticValidator qo);

    /**
     * 统计web端发送量
     * @param qo
     * @return
     */
    Map<String, Object> webStatisticMessageCount(StatisticMessageSendData qo);
}