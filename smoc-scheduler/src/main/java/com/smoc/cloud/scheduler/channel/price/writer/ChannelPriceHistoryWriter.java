package com.smoc.cloud.scheduler.channel.price.writer;

import com.smoc.cloud.scheduler.channel.price.service.ChannelPriceHistoryService;
import com.smoc.cloud.scheduler.channel.price.service.model.ChannelPriceModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通道价格历史 批处理
 */
@Slf4j
@Component
public class ChannelPriceHistoryWriter implements ItemWriter<ChannelPriceModel> {

    @Autowired
    private ChannelPriceHistoryService channelPriceHistoryService;

    @Override
    public void write(List<? extends ChannelPriceModel> list) throws Exception {
        channelPriceHistoryService.saveHistory(list);
    }
}
