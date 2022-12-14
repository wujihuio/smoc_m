package com.smoc.cloud.configure.channel.group.repository;


import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelBasicInfoQo;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelGroupInfoValidator;
import com.smoc.cloud.configure.channel.group.entity.ConfigChannelGroupInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * 通道组操作类
 */
public interface ChannelGroupRepository extends CrudRepository<ConfigChannelGroupInfo, String>, JpaRepository<ConfigChannelGroupInfo, String> {


    PageList<ChannelGroupInfoValidator> page(PageParams<ChannelGroupInfoValidator> pageParams);

    Iterable<ConfigChannelGroupInfo> findByChannelGroupId(String channelGroupId);

    @Modifying
    @Query(value = "update config_channel_group_info set CHANNEL_GROUP_PROCESS = :status where CHANNEL_GROUP_ID = :channelGroupId",nativeQuery = true)
    void updateChannelGroupProcessByChannelGroupId(@Param("channelGroupId") String channelGroupId, @Param("status") String status);

    /**
     * 通道组详情里已配置通道列表
     * @param channelGroupInfoValidator
     * @return
     */
    List<ChannelBasicInfoQo> centerConfigChannelList(ChannelGroupInfoValidator channelGroupInfoValidator);
}
