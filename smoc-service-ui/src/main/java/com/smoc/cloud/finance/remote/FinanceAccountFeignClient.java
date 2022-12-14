package com.smoc.cloud.finance.remote;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountRechargeValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * 财务账号
 */
@FeignClient(name = "smoc", path = "/smoc")
public interface FinanceAccountFeignClient {

    /**
     * 分查询列表
     * @param pageParams
     * @param flag 1表示业务账号 账户  2表示认证账号 账户 3表示财务共享账号
     * @return
     */
    @RequestMapping(value = "/finance/account/page/{flag}", method = RequestMethod.POST)
    ResponseData<PageList<FinanceAccountValidator>> page(@RequestBody PageParams<FinanceAccountValidator> pageParams, @PathVariable String flag) throws Exception;

    /**
     * 分查询列表
     * @param pageParams
     * @param flag 1表示业务账号 账户  2表示认证账号 账户
     * @return
     */
    @RequestMapping(value = "/finance/recharge/page/{flag}", method = RequestMethod.POST)
    ResponseData<PageList<FinanceAccountRechargeValidator>> rechargePage(@RequestBody PageParams<FinanceAccountRechargeValidator> pageParams, @PathVariable String flag) throws Exception;

    /**
     * 根据id查询
     *
     * @param accountId
     * @return
     */
    @RequestMapping(value = "/finance/account/findById/{accountId}", method = RequestMethod.GET)
    ResponseData<FinanceAccountValidator> findById(@PathVariable String accountId);

    /**
     * 查询共享账号的子账号信息
     * @param accountId
     * @return
     */
    @RequestMapping(value = "/finance/account/findSubsidiaryFinanceAccountByAccountId/{accountId}", method = RequestMethod.GET)
    ResponseData<List<FinanceAccountValidator>> findSubsidiaryFinanceAccountByAccountId(@PathVariable String accountId);

    /**
     * 统计账户金额
     * @param flag  1表示业务账号 账户  2表示认证账号 账户 3表示财务共享账号 4表示共用的账号财务账户
     * @return
     */
    @RequestMapping(value = "/finance/account/count/{flag}", method = RequestMethod.POST)
    ResponseData<Map<String,Object>> count(@RequestBody FinanceAccountValidator financeAccountValidator,@PathVariable String flag) throws Exception;

    /**
     * 统计充值金额
     * @param financeAccountRechargeValidator
     * @return
     */
    @RequestMapping(value = "/finance/recharge/statisticRechargeSum/{flag}", method = RequestMethod.POST)
    ResponseData<Map<String, Object>> statisticRechargeSum(@RequestBody FinanceAccountRechargeValidator financeAccountRechargeValidator,@PathVariable String flag) throws Exception;

    /**
     * 统计充值金额
     * @param financeAccountRechargeValidator
     * @return
     */
    @RequestMapping(value = "/finance/recharge/countRechargeSum", method = RequestMethod.POST)
    ResponseData<Map<String, Object>> countRechargeSum(@RequestBody FinanceAccountRechargeValidator financeAccountRechargeValidator) throws Exception;
}
