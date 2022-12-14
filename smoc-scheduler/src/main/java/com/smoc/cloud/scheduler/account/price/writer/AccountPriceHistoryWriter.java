package com.smoc.cloud.scheduler.account.price.writer;

import com.smoc.cloud.scheduler.account.price.service.AccountFinanceService;
import com.smoc.cloud.scheduler.account.price.service.model.AccountPriceModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务账号价格历史 批处理
 */
@Slf4j
@Component
public class AccountPriceHistoryWriter implements ItemWriter<AccountPriceModel> {

    @Autowired
    private AccountFinanceService accountFinanceService;

    @Override
    public void write(List<? extends AccountPriceModel> list) throws Exception {
        accountFinanceService.saveHistory(list);
    }
}
