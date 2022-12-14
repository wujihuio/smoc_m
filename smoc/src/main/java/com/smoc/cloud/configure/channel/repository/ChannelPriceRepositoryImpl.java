package com.smoc.cloud.configure.channel.repository;


import com.smoc.cloud.common.BasePageRepository;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelPriceValidator;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.configure.channel.rowmapper.ChannelPriceRowMapper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ChannelPriceRepositoryImpl extends BasePageRepository {

    @Resource
    public JdbcTemplate jdbcTemplate;

    public List<ChannelPriceValidator> findChannelPrice(ChannelPriceValidator channelPriceValidator) {

        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  t.ID");
        sqlBuffer.append(", t.CHANNEL_ID");
        sqlBuffer.append(", t.PRICE_STYLE");
        sqlBuffer.append(", t.AREA_CODE");
        sqlBuffer.append(", t.CHANNEL_PRICE");
        sqlBuffer.append(", DATE_FORMAT(t.CREATED_TIME, '%Y-%m-%d %H:%i:%S')CREATED_TIME");
        sqlBuffer.append("  from config_channel_price t where 1=1 ");

        List<Object> paramsList = new ArrayList<Object>();

        if (!StringUtils.isEmpty(channelPriceValidator.getChannelId())) {
            sqlBuffer.append(" and t.CHANNEL_ID = ?");
            paramsList.add( channelPriceValidator.getChannelId().trim());
        }
        if (!StringUtils.isEmpty(channelPriceValidator.getPriceStyle())) {
            sqlBuffer.append(" and t.PRICE_STYLE = ?");
            paramsList.add( channelPriceValidator.getPriceStyle().trim());
        }
        if (!StringUtils.isEmpty(channelPriceValidator.getAreaCode())) {
            sqlBuffer.append(" and t.AREA_CODE in ("+channelPriceValidator.getAreaCode().trim()+") ");
        }


        sqlBuffer.append(" order by t.AREA_CODE ");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<ChannelPriceValidator> list = this.queryForObjectList(sqlBuffer.toString(), params,  new ChannelPriceRowMapper());
        return list;

    }

    public void deleteByChannelIdAndAreaCode(String channelId, String areaCode) {
        String[] carriers = areaCode.split(",");
        StringBuilder sql = new StringBuilder("delete from config_channel_price where CHANNEL_ID = '" + channelId + "'  ");
        for(int i=0;i<carriers.length;i++){
            sql.append("and  AREA_CODE !='"+carriers[i]+"' ");
        }
        jdbcTemplate.execute(sql.toString());
    }

    public void batchSave(ChannelPriceValidator channelPriceValidator) {

        List<ChannelPriceValidator> list = channelPriceValidator.getPrices();

        String[] sql = new String[list.size()+ 1];

        for(int i = 0; i < list.size(); i++){

            ChannelPriceValidator info = list.get(i);

            if("1".equals(info.getFlag())){
                StringBuffer sqlBuffer = new StringBuffer("insert into config_channel_price(ID,CHANNEL_ID,PRICE_STYLE,AREA_CODE,CHANNEL_PRICE,CREATED_BY,CREATED_TIME) ");
                sqlBuffer.append(" values('"+UUID.uuid32()+"','"+channelPriceValidator.getChannelId()+"','"+channelPriceValidator.getPriceStyle()+"','"+info.getAreaCode()+"','"+info.getChannelPrice()+"','"+info.getCreatedBy()+"',now()) ");
                sql[i] = sqlBuffer.toString();
            }

            if("2".equals(info.getFlag())){
                StringBuffer sqlBuffer = new StringBuffer("update config_channel_price set CHANNEL_PRICE = '"+info.getChannelPrice()+"',UPDATED_BY='"+info.getCreatedBy()+"',UPDATED_TIME=now() where ID = '"+info.getId()+"' ");
                sql[i] = sqlBuffer.toString();
            }

        }

        this.jdbcTemplate.batchUpdate(sql);

        /*final String sql = "insert into config_channel_price(ID,CHANNEL_ID,PRICE_STYLE,AREA_CODE,CHANNEL_PRICE,LASTTIME_HISTORY,UPDATED_TIME,UPDATED_BY) values(?,?,?,?,?,now(),now(),?) ";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return list.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChannelPriceValidator channelPrice = list.get(i);
                ps.setString(1, UUID.uuid32());
                ps.setString(2, channelPriceValidator.getChannelId());
                ps.setString(3, channelPriceValidator.getPriceStyle());
                ps.setString(4, channelPrice.getAreaCode());
                ps.setBigDecimal(5, channelPrice.getChannelPrice());
                ps.setString(6, channelPrice.getCreatedBy());
            }

        })*/;
    }
}
