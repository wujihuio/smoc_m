package com.smoc.cloud.scheduler.account.price.configuation;

import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.scheduler.account.price.listener.AccountPriceHistoryListener;
import com.smoc.cloud.scheduler.account.price.processor.AccountPriceHistoryProcessor;
import com.smoc.cloud.scheduler.account.price.service.model.AccountPriceModel;
import com.smoc.cloud.scheduler.account.price.service.rowmapper.AccountPriceRowMapper;
import com.smoc.cloud.scheduler.account.price.writer.AccountPriceHistoryWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 业务账号价格历史 批处理
 */
@Slf4j
@Configuration
public class AccountPriceHistoryJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Autowired
    public AccountPriceHistoryListener accountPriceHistoryListener;

    @Autowired
    private AccountPriceHistoryProcessor accountPriceHistoryProcessor;

    @Autowired
    private AccountPriceHistoryWriter accountPriceHistoryWriter;

    @Bean("accountPriceHistoryJob")
    public Job accountPriceHistoryJob() {
        return jobBuilderFactory.get("accountPriceHistoryJob").incrementer(new RunIdIncrementer()).listener(accountPriceHistoryListener).start(accountPriceJobConfigurationStep()).build();
    }

    /**
     * @return
     */
    @Bean
    public Step accountPriceJobConfigurationStep() {
        return stepBuilderFactory.get("accountPriceJobConfigurationStep")
                .<AccountPriceModel, AccountPriceModel>chunk(10)
                .reader(accountPriceReader())
                .processor(compositeAccountPriceProcessor())
                .writer(accountPriceHistoryWriter)
                .taskExecutor(accountPriceHistoryExecutor())
                .throttleLimit(5)
                .build();
    }

    /**
     * 查询非今天创建、批处理日期小于今天的数据
     *
     * @return
     */
    @Bean
    @StepScope
    public JdbcPagingItemReader<AccountPriceModel> accountPriceReader() {
        JdbcPagingItemReader<AccountPriceModel> reader = new JdbcPagingItemReader<>();
        // 设置数据源
        reader.setDataSource(dataSource);
        // 设置一次最大读取条数
        reader.setFetchSize(10);
        // 把数据库中的每条数据映射到ChannelPriceValidator对像中
        reader.setRowMapper(new AccountPriceRowMapper());
        //获取当前日期
        String today = DateTimeUtils.getDateFormat(new Date());
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause(" ID,ACCOUNT_ID,CARRIER,CARRIER_PRICE,BATCH_DATE,CREATED_TIME,'" + today + "' PRICE_DATE,TIMESTAMPDIFF(DAY,DATE_FORMAT(BATCH_DATE, '%Y-%m-%d'),'" + today + "') DAYS "); // 设置查询的列
        queryProvider.setFromClause(" from smoc.account_finance_info "); // 设置要查询的表
        queryProvider.setWhereClause(" where BATCH_DATE is not null and DATE_FORMAT(CREATED_TIME,'%Y-%m-%d') < :today  and DATE_FORMAT(BATCH_DATE,'%Y-%m-%d') < :today ");
        queryProvider.setSortKeys(new HashMap<String, Order>() {{
            put("BATCH_DATE", Order.ASCENDING);
        }});
        reader.setParameterValues(new HashMap<String, Object>() {{
            put("today", today);
        }});
        reader.setQueryProvider(queryProvider);// 设置排序列
        return reader;
    }


    @Bean
    @StepScope
    public CompositeItemProcessor<AccountPriceModel, AccountPriceModel> compositeAccountPriceProcessor() {
        CompositeItemProcessor<AccountPriceModel, AccountPriceModel> processor = new CompositeItemProcessor<>();
        List<ItemProcessor<AccountPriceModel, AccountPriceModel>> listProcessor = new ArrayList<>();
        listProcessor.add(accountPriceHistoryProcessor);
        processor.setDelegates(listProcessor);
        return processor;
    }

    @Bean
    public TaskExecutor accountPriceHistoryExecutor() {
        return new SimpleAsyncTaskExecutor("accountPriceHistoryExecutorTask-");
    }

}
