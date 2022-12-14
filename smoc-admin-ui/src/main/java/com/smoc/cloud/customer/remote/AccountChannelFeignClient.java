package com.smoc.cloud.customer.remote;

import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.qo.ChannelBasicInfoQo;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelGroupInfoValidator;
import com.smoc.cloud.common.smoc.customer.qo.AccountChannelInfoQo;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountChannelInfoValidator;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;


/**
 * 账号通道配置管理远程服务接口
 **/
@FeignClient(name = "smoc", path = "/smoc")
public interface AccountChannelFeignClient {


    /**
     * 查询配置的业务账号通道
     * @param accountChannelInfoQo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/account/channel/findAccountChannelConfig", method = RequestMethod.POST)
    ResponseData<Map<String, AccountChannelInfoQo>> findAccountChannelConfig(@RequestBody AccountChannelInfoQo accountChannelInfoQo) throws Exception;

    /**
     *  检索通道列表
     * @param channelBasicInfoQo
     * @return
     */
    @RequestMapping(value = "/account/channel/findChannelList", method = RequestMethod.POST)
    ResponseData<List<ChannelBasicInfoQo>> findChannelList(@RequestBody ChannelBasicInfoQo channelBasicInfoQo) throws Exception;

    /**
     * 保存通道
     * @param accountChannelInfoValidator
     * @param op
     * @return
     */
    @RequestMapping(value = "/account/channel/save/{op}", method = RequestMethod.POST)
    ResponseData save(@RequestBody AccountChannelInfoValidator accountChannelInfoValidator, @PathVariable String op) throws Exception;

    /**
     * 查询账号下运营商是否配置过通道
     * @param accountId
     * @param carrier
     * @return
     */
    @RequestMapping(value = "/account/channel/findByAccountIdAndCarrier/{accountId}/{carrier}", method = RequestMethod.GET)
    ResponseData<List<AccountChannelInfoValidator>> findByAccountIdAndCarrier(@PathVariable String accountId, @PathVariable String carrier) throws Exception;

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/account/channel/findById/{id}", method = RequestMethod.GET)
    ResponseData<AccountChannelInfoValidator> findById(@PathVariable String id) throws Exception;

    /**
     * 根据id删除信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/account/channel/deleteById/{id}", method = RequestMethod.GET)
    ResponseData deleteById(@PathVariable String id) throws Exception;

    /**
     * 检索通道组
     * @param channelGroupInfoValidator
     * @return
     */
    @RequestMapping(value = "/account/channelGroup/findChannelGroupList", method = RequestMethod.POST)
    ResponseData<List<ChannelGroupInfoValidator>> findChannelGroupList(@RequestBody ChannelGroupInfoValidator channelGroupInfoValidator) throws Exception;

    /**
     * 查询已配置通道组数据
     * @param accountId
     * @param carrier
     * @param channelGroupId
     * @return
     */
    @RequestMapping(value = "/account/channelGroup/findByAccountIdAndCarrierAndChannelGroupId/{accountId}/{carrier}/{channelGroupId}", method = RequestMethod.GET)
    ResponseData<List<AccountChannelInfoValidator>> findByAccountIdAndCarrierAndChannelGroupId(@PathVariable String accountId, @PathVariable String carrier, @PathVariable String channelGroupId) throws Exception;

    /**
     * 移除已配置通道组
     * @param accountId
     * @param carrier
     * @param channelGroupId
     * @return
     */
    @RequestMapping(value = "/account/channelGroup/deleteChannelGroup/{accountId}/{carrier}/{channelGroupId}", method = RequestMethod.GET)
    ResponseData deleteChannelGroup(@PathVariable String accountId, @PathVariable String carrier, @PathVariable String channelGroupId) throws Exception;

    /**
     * 业务账号通道明细
     * @param accountChannelInfoValidator
     * @return
     */
    @RequestMapping(value = "/account/channel/channelDetail", method = RequestMethod.POST)
    ResponseData<List<AccountChannelInfoValidator>> channelDetail(@RequestBody AccountChannelInfoValidator accountChannelInfoValidator) throws Exception;

    /**
     * 通过channelId 查询 该通道的业务账号引用情况
     * @param channelId
     * @return
     */
    @RequestMapping(value = "/account/channel/channelDetail/{channelId}", method = RequestMethod.POST)
    ResponseData<List<AccountChannelInfoValidator>> channelDetail(@PathVariable String channelId) throws Exception;
}
