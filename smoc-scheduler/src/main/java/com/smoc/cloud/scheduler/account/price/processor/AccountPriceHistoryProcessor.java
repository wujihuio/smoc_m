package com.smoc.cloud.scheduler.account.price.processor;

import com.google.gson.Gson;
import com.smoc.cloud.scheduler.account.price.service.model.AccountPriceModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


/**
 * 业务账号价格历史 批处理
 */
@Slf4j
@Component
public class AccountPriceHistoryProcessor implements ItemProcessor<AccountPriceModel, AccountPriceModel> {
    @Override
    public AccountPriceModel process(AccountPriceModel accountPriceModel) throws Exception {
        //log.info("[AccountPriceModel]:{}",new Gson().toJson(accountPriceModel));
        return accountPriceModel;
    }
}
