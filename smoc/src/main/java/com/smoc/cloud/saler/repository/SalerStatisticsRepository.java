package com.smoc.cloud.saler.repository;

import com.smoc.cloud.common.BasePageRepository;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticSendData;
import com.smoc.cloud.common.smoc.saler.qo.CustomerComplaintQo;
import com.smoc.cloud.customer.rowmapper.AccountStatisticSendRowMapper;
import com.smoc.cloud.saler.rowmapper.SalerCustomerComplaintRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计数据查询
 */
@Service
public class SalerStatisticsRepository extends BasePageRepository {

    @Resource
    public JdbcTemplate jdbcTemplate;

    /**
     * 根据起始日期 统计客户短信成功发送量
     *
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return
     */
    public Long getMessageSendTotal(String startDate, String endDate,String salerId) {

        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  IFNULL(sum(m.MESSAGE_SUCCESS_NUM),0) messageTotal ");
        sqlBuffer.append("  from message_daily_statistics m left join account_base_info a on m.BUSINESS_ACCOUNT = a.ACCOUNT_ID");
        sqlBuffer.append("  left join enterprise_basic_info e on a.ENTERPRISE_ID = e.ENTERPRISE_ID");
        sqlBuffer.append("  where m.MESSAGE_DATE>=? and m.MESSAGE_DATE<=? and e.SALER = ? ");

        Object[] params = new Object[3];
        params[0] = startDate;
        params[1] = endDate;
        params[2] = salerId;
        Long messageTotal = jdbcTemplate.queryForObject(sqlBuffer.toString(), params, Long.class);

        return messageTotal;

    }

    /**
     * 客户账户总余额（预付费）
     * @return
     */
    public Map<String, Object> getCustomerUsableTotal(String salerId) {
        StringBuffer sqlBuffer = new StringBuffer("select");
        sqlBuffer.append("  IFNULL(ROUND(sum(t.ACCOUNT_USABLE_SUM),2),0) ACCOUNT_USABLE_SUM ");
        sqlBuffer.append("  from (select t.ACCOUNT_ID from account_finance_info t where t.PAY_TYPE=1 group by t.ACCOUNT_ID)a ");
        sqlBuffer.append("  left join finance_account t on a.ACCOUNT_ID=t.ACCOUNT_ID ");
        sqlBuffer.append("  left join account_base_info n ON a.ACCOUNT_ID = n.ACCOUNT_ID ");
        sqlBuffer.append("  left join enterprise_basic_info e ON n.ENTERPRISE_ID = e.ENTERPRISE_ID ");
        sqlBuffer.append("  where e.SALER = ? ");

        Object[] params = new Object[1];
        params[0] = salerId;

        Map<String, Object> map = jdbcTemplate.queryForMap(sqlBuffer.toString(), params);
        return map;
    }

    /**
     * 客户账户总余额（后付费）
     * @return
     */
    public Map<String, Object> getCustomerUsableTotalLater(String salerId) {
        StringBuffer sqlBuffer = new StringBuffer("select");
        sqlBuffer.append("  IFNULL(ROUND(sum(t.ACCOUNT_USABLE_SUM),2),0) ACCOUNT_LATER_USABLE_SUM ");
        sqlBuffer.append("  from (select t.ACCOUNT_ID from account_finance_info t where t.PAY_TYPE=2 group by t.ACCOUNT_ID)a ");
        sqlBuffer.append("  left join finance_account t on a.ACCOUNT_ID=t.ACCOUNT_ID ");
        sqlBuffer.append("  left join account_base_info n ON a.ACCOUNT_ID = n.ACCOUNT_ID ");
        sqlBuffer.append("  left join enterprise_basic_info e ON n.ENTERPRISE_ID = e.ENTERPRISE_ID ");
        sqlBuffer.append("  where e.SALER = ? ");

        Object[] params = new Object[1];
        params[0] = salerId;

        Map<String, Object> map = jdbcTemplate.queryForMap(sqlBuffer.toString(), params);
        return map;
    }

    /**
     * 客户消费总金额和条数
     * @return
     */
    public Map<String, Object> getCustomerConsumeTotal(String startDate, String endDate,String salerId) {
        StringBuffer sqlBuffer = new StringBuffer("select");
        sqlBuffer.append("  IFNULL(ROUND(sum(m.ACCOUNT_PRICE*m.MESSAGE_SUCCESS_NUM),2),0) ACCOUNT_CONSUME_SUM, ");
        sqlBuffer.append("  IFNULL(sum(m.MESSAGE_SUCCESS_NUM),0) MESSAGE_TOTAL ");
        sqlBuffer.append("  from message_daily_statistics m left join account_base_info a on m.BUSINESS_ACCOUNT = a.ACCOUNT_ID");
        sqlBuffer.append("  left join enterprise_basic_info e on a.ENTERPRISE_ID = e.ENTERPRISE_ID");
        sqlBuffer.append("  where m.MESSAGE_DATE>=? and m.MESSAGE_DATE<=? and e.SALER = ? ");

        Object[] params = new Object[3];
        params[0] = startDate;
        params[1] = endDate;
        params[2] = salerId;

        Map<String, Object> map = jdbcTemplate.queryForMap(sqlBuffer.toString(), params);
        return map;
    }

    /**
     * 查看首页信息:通道发送总量、通道消费总额
     * @param startDate
     * @param endDate
     * @param salerId
     * @return
     */
    public Map<String, Object> getChannelConsumeTotal(String startDate, String endDate, String salerId) {
        StringBuffer sqlBuffer = new StringBuffer("select");
        sqlBuffer.append("  IFNULL(ROUND(sum(m.CHANNEL_PRICE*m.MESSAGE_SUCCESS_NUM),2),0) CHANNEL_CONSUME_SUM, ");
        sqlBuffer.append("  IFNULL(sum(m.MESSAGE_SUCCESS_NUM),0) MESSAGE_TOTAL ");
        sqlBuffer.append("  from message_daily_statistics m left join config_channel_basic_info t on t.CHANNEL_ID = m.CHANNEL_ID");
        sqlBuffer.append(" WHERE m.MESSAGE_DATE>=? and m.MESSAGE_DATE<=? and t.CHANNEL_ACCESS_SALES = ? ");

        Object[] params = new Object[3];
        params[0] = startDate;
        params[1] = endDate;
        params[2] = salerId;

        Map<String, Object> map = jdbcTemplate.queryForMap(sqlBuffer.toString(), params);
        return map;
    }

    /**
     * 首页：客户近12个月账号短信发送量统计
     * @param statisticSendData
     * @return
     */
    public List<AccountStatisticSendData> indexStatisticAccountMessageSendSum(AccountStatisticSendData statisticSendData) {
        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  a.MONTH_DAY");
        sqlBuffer.append(", IFNULL(b.MESSAGE_SUCCESS_NUM,0)SEND_NUMBER from ");
        sqlBuffer.append(" (SELECT @s :=@s + 1 as `INDEX`, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL @s MONTH),'%Y-%m') AS `MONTH_DAY` ");
        sqlBuffer.append("  from mysql.help_topic, (SELECT @s := -1) temp WHERE  @s < 23 ORDER BY MONTH_DAY desc");
        sqlBuffer.append(" )a  left join ");
        sqlBuffer.append(" (SELECT DATE_FORMAT(t.MESSAGE_DATE, '%Y-%m')MESSAGE_DATE, ROUND(sum(t.MESSAGE_SUCCESS_NUM)/10000,2) MESSAGE_SUCCESS_NUM ");
        sqlBuffer.append(" FROM message_daily_statistics t left join account_base_info m on t.BUSINESS_ACCOUNT = m.ACCOUNT_ID");
        sqlBuffer.append(" left join enterprise_basic_info n on m.ENTERPRISE_ID = n.ENTERPRISE_ID WHERE n.SALER = ? ");
        sqlBuffer.append(" GROUP BY DATE_FORMAT(t.MESSAGE_DATE, '%Y-%m')");
        sqlBuffer.append(" )b on a.MONTH_DAY = b.MESSAGE_DATE  order by a.MONTH_DAY asc");

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(statisticSendData.getSaler());

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<AccountStatisticSendData> list = this.queryForObjectList(sqlBuffer.toString(), params, new AccountStatisticSendRowMapper());
        return list;
    }

    /**
     * 首页：通道近12个月短信发送量统计
     * @param statisticSendData
     * @return
     */
    public List<AccountStatisticSendData> indexStatisticChannelMessageSendSum(AccountStatisticSendData statisticSendData) {
        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  a.MONTH_DAY");
        sqlBuffer.append(", IFNULL(b.MESSAGE_SUCCESS_NUM,0)SEND_NUMBER from ");
        sqlBuffer.append(" (SELECT @s :=@s + 1 as `INDEX`, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL @s MONTH),'%Y-%m') AS `MONTH_DAY` ");
        sqlBuffer.append("  from mysql.help_topic, (SELECT @s := -1) temp WHERE  @s < 23 ORDER BY MONTH_DAY desc");
        sqlBuffer.append(" )a  left join ");
        sqlBuffer.append(" (SELECT DATE_FORMAT(t.MESSAGE_DATE, '%Y-%m')MESSAGE_DATE, ROUND(sum(t.MESSAGE_SUCCESS_NUM)/10000,2) MESSAGE_SUCCESS_NUM ");
        sqlBuffer.append(" FROM message_daily_statistics t left join config_channel_basic_info m on t.CHANNEL_ID = m.CHANNEL_ID");
        sqlBuffer.append(" WHERE m.CHANNEL_ACCESS_SALES = ? GROUP BY DATE_FORMAT(t.MESSAGE_DATE, '%Y-%m') ");
        sqlBuffer.append(" )b on a.MONTH_DAY = b.MESSAGE_DATE  order by a.MONTH_DAY asc");

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(statisticSendData.getSaler());

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<AccountStatisticSendData> list = this.queryForObjectList(sqlBuffer.toString(), params, new AccountStatisticSendRowMapper());
        return list;
    }

    /**
     * 首页：客户近12个月投诉率统计
     * @param customerComplaintQo
     * @return
     */
    public List<CustomerComplaintQo> indexStatisticAccountComplaint(CustomerComplaintQo customerComplaintQo) {
        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  a.MONTH_DAY");
        sqlBuffer.append(", IFNULL(b.COMPLAINT_NUM,0)COMPLAINT_NUM from ");
        sqlBuffer.append(" (SELECT @s :=@s + 1 as `INDEX`, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL @s MONTH),'%Y-%m') AS `MONTH_DAY` ");
        sqlBuffer.append("  from mysql.help_topic, (SELECT @s := -1) temp WHERE  @s < 23 ORDER BY MONTH_DAY desc");
        sqlBuffer.append(" )a  left join ");
        sqlBuffer.append(" (SELECT DATE_FORMAT(t.REPORT_DATE, '%Y-%m')MONTH_DAY,count(*)COMPLAINT_NUM ");
        sqlBuffer.append(" FROM message_complaint_info t join account_base_info m on t.BUSINESS_ACCOUNT = m.ACCOUNT_ID ");
        sqlBuffer.append(" left join enterprise_basic_info n on m.ENTERPRISE_ID = n.ENTERPRISE_ID WHERE t.COMPLAINT_SOURCE = 'day' and n.SALER = ? ");
        sqlBuffer.append(" GROUP BY DATE_FORMAT(t.REPORT_DATE, '%Y-%m')");
        sqlBuffer.append(" )b on a.MONTH_DAY = b.MONTH_DAY  order by a.MONTH_DAY asc");

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(customerComplaintQo.getSaler());

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<CustomerComplaintQo> list = this.queryForObjectList(sqlBuffer.toString(), params, new SalerCustomerComplaintRowMapper());
        return list;
    }

    /**
     * 首页：通道近12个月投诉率统计
     * @param customerComplaintQo
     * @return
     */
    public List<CustomerComplaintQo> indexStatisticChannelComplaint(CustomerComplaintQo customerComplaintQo) {
        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  a.MONTH_DAY");
        sqlBuffer.append(", IFNULL(b.COMPLAINT_NUM,0)COMPLAINT_NUM from ");
        sqlBuffer.append(" (SELECT @s :=@s + 1 as `INDEX`, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL @s MONTH),'%Y-%m') AS `MONTH_DAY` ");
        sqlBuffer.append("  from mysql.help_topic, (SELECT @s := -1) temp WHERE  @s < 23 ORDER BY MONTH_DAY desc");
        sqlBuffer.append(" )a  left join ");
        sqlBuffer.append(" (SELECT DATE_FORMAT(t.REPORT_DATE, '%Y-%m')MONTH_DAY,count(*)COMPLAINT_NUM ");
        sqlBuffer.append(" FROM message_complaint_info t left join config_channel_basic_info m on t.CHANNEL_ID = m.CHANNEL_ID");
        sqlBuffer.append(" WHERE t.COMPLAINT_SOURCE = 'day' and m.CHANNEL_ACCESS_SALES = ? GROUP BY DATE_FORMAT(t.REPORT_DATE, '%Y-%m') ");
        sqlBuffer.append(" )b on a.MONTH_DAY = b.MONTH_DAY  order by a.MONTH_DAY asc");

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(customerComplaintQo.getSaler());

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<CustomerComplaintQo> list = this.queryForObjectList(sqlBuffer.toString(), params, new SalerCustomerComplaintRowMapper());
        return list;
    }


}
