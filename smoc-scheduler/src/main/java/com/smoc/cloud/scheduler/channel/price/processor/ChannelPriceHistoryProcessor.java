package com.smoc.cloud.scheduler.channel.price.processor;

import com.google.gson.Gson;
import com.smoc.cloud.scheduler.channel.price.service.model.ChannelPriceModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


/**
 * 通道价格历史 批处理
 */
@Slf4j
@Component
public class ChannelPriceHistoryProcessor implements ItemProcessor<ChannelPriceModel, ChannelPriceModel> {
    @Override
    public ChannelPriceModel process(ChannelPriceModel channelPriceModel) throws Exception {
        //log.info("[channelPriceModel]:{}",new Gson().toJson(channelPriceModel));
        return channelPriceModel;
    }
}
