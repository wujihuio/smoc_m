package com.smoc.cloud.reconciliation.remote;


import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.reconciliation.model.ReconciliationEnterpriseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 业务账号对账
 */
@FeignClient(name = "smoc", path = "/smoc")
public interface ReconciliationAccountRemoteClient {

    /**
     * 查询列表
     *
     * @param pageParams
     * @return
     */
    @RequestMapping(value = "/reconciliation/account/ec/page", method = RequestMethod.POST)
    ResponseData<PageList<ReconciliationEnterpriseModel>> page(@RequestBody PageParams<ReconciliationEnterpriseModel> pageParams) throws Exception;

    /**
     * 根据企业ID 和账期 查询企业账单明细
     *
     * @param enterpriseId
     * @param accountPeriod
     * @return
     */
    @RequestMapping(value = "/reconciliation/account/ec/getEnterpriseBills/{enterpriseId}/{accountPeriod}", method = RequestMethod.POST)
    ResponseData<Map<String, Object>> getEnterpriseBills(@PathVariable String enterpriseId, @PathVariable String accountPeriod) throws Exception;
}
