package com.smoc.cloud.configure.channel.group.repository;


import com.smoc.cloud.common.BasePageRepository;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelBasicInfoQo;
import com.smoc.cloud.common.smoc.configuate.qo.ConfigChannelGroupQo;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelGroupInfoValidator;
import com.smoc.cloud.configure.channel.group.rowmapper.ChannelGroupBaseInfoRowMapper;
import com.smoc.cloud.configure.channel.group.rowmapper.ChannelInfoRowMapper;
import com.smoc.cloud.configure.channel.group.rowmapper.ConfigChannelGroupRowMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class ConfigChannelGroupRepositoryImpl extends BasePageRepository {

    public List<ChannelBasicInfoQo> findChannelList(ChannelBasicInfoQo qo) {

        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  t.CHANNEL_ID");
        sqlBuffer.append(", t.CHANNEL_NAME");
        sqlBuffer.append(", t.CARRIER");
        sqlBuffer.append(", t.INFO_TYPE");
        sqlBuffer.append(", i.SRC_ID");
        sqlBuffer.append(", i.PROTOCOL");
        sqlBuffer.append(", t.CHANNEL_INTRODUCE ");
        sqlBuffer.append("  from config_channel_basic_info t left join config_channel_interface i on t.CHANNEL_ID=i.CHANNEL_ID");
        sqlBuffer.append("  left join (select t.id,t.CHANNEL_ID from config_channel_group t where t.CHANNEL_GROUP_ID=? )g ON t.CHANNEL_ID = g.CHANNEL_ID");
        sqlBuffer.append("  where g.ID is null ");

        List<Object> paramsList = new ArrayList<Object>();

        paramsList.add( qo.getChanneGroupId());
        if (!StringUtils.isEmpty(qo.getChannelName())) {
            sqlBuffer.append(" and t.CHANNEL_NAME like ?");
            paramsList.add( "%"+qo.getChannelName().trim()+"%");
        }

        if (!StringUtils.isEmpty(qo.getSrcId())) {
            sqlBuffer.append(" and i.SRC_ID like ?");
            paramsList.add( "%"+qo.getSrcId().trim()+"%");
        }

        if (!StringUtils.isEmpty(qo.getCarrier())) {
            sqlBuffer.append(" and t.CARRIER like ?");
            paramsList.add( "%"+qo.getCarrier().trim()+"%");
        }

        if (!StringUtils.isEmpty(qo.getBusinessType())) {
            sqlBuffer.append(" and t.BUSINESS_TYPE = ?");
            paramsList.add(qo.getBusinessType().trim());
        }

        if (!StringUtils.isEmpty(qo.getInfoType())) {
            sqlBuffer.append(" and t.INFO_TYPE like ?");
            paramsList.add( "%"+qo.getInfoType().trim()+"%");
        }

        if (!StringUtils.isEmpty(qo.getChannelStatus())) {
            sqlBuffer.append(" and t.CHANNEL_STATUS = ?");
            paramsList.add(qo.getChannelStatus().trim());
        }

        sqlBuffer.append(" order by t.CREATED_TIME desc");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<ChannelBasicInfoQo> list = this.queryForObjectList(sqlBuffer.toString(), params, new ChannelInfoRowMapper());
        return list;
    }

    public List<ConfigChannelGroupQo> findConfigChannelGroupList(ConfigChannelGroupQo qo) {

        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select ");
        sqlBuffer.append("  t.ID");
        sqlBuffer.append(", b.CHANNEL_ID");
        sqlBuffer.append(", b.CHANNEL_NAME");
        sqlBuffer.append(", b.INFO_TYPE");
        sqlBuffer.append(", b.CARRIER");
        sqlBuffer.append(", i.PROTOCOL");
        sqlBuffer.append(", t.CHANNEL_PRIORITY");
        sqlBuffer.append(", t.CHANNEL_WEIGHT");
        sqlBuffer.append(", b.CHANNEL_INTRODUCE ");
        sqlBuffer.append("  from config_channel_group t left join config_channel_basic_info b on t.CHANNEL_ID=b.CHANNEL_ID");
        sqlBuffer.append("  left join config_channel_interface i on t.CHANNEL_ID=i.CHANNEL_ID ");
        sqlBuffer.append("  where 1=1 ");

        List<Object> paramsList = new ArrayList<Object>();

        if (!StringUtils.isEmpty(qo.getChannelGroupId())) {
            sqlBuffer.append(" and t.CHANNEL_GROUP_ID = ?");
            paramsList.add(qo.getChannelGroupId().trim());
        }

        sqlBuffer.append(" order by t.CHANNEL_PRIORITY,t.CHANNEL_WEIGHT desc ");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        List<ConfigChannelGroupQo> list = this.queryForObjectList(sqlBuffer.toString(), params, new ConfigChannelGroupRowMapper());
        return list;
    }

}
