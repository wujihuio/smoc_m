package com.smoc.cloud.finance.service;


import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountRechargeValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import com.smoc.cloud.finance.repository.FinanceAccountRechargeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class FinanceAccountRechargeService {

    @Resource
    private FinanceAccountRechargeRepository financeAccountRechargeRepository;

    /**
     * 分页查询
     *
     * @param pageParams
     * @param flag       1表示业务账号 账户  2表示认证账号 账户  4表示共用账号
     * @return
     */
    public ResponseData<PageList<FinanceAccountRechargeValidator>> page(PageParams<FinanceAccountRechargeValidator> pageParams, String flag) {

        if ("1".equals(flag)) {
            PageList<FinanceAccountRechargeValidator> data = financeAccountRechargeRepository.pageBusiness(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }
        if ("2".equals(flag)) {
            PageList<FinanceAccountRechargeValidator> data = financeAccountRechargeRepository.pageIdentification(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }

        if ("4".equals(flag)) {
            if (StringUtils.isEmpty(pageParams.getParams().getRechargeSource())){
                return ResponseDataUtil.buildError("充值来源条件不能为空！");
            }
            PageList<FinanceAccountRechargeValidator> data = financeAccountRechargeRepository.pageSystem(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }

        return ResponseDataUtil.buildError();
    }

    /**
     * 统计充值金额
     *
     * @param qo
     * @return
     */
    public ResponseData<Map<String, Object>> countRechargeSum(FinanceAccountRechargeValidator qo) {
        Map<String, Object> data = financeAccountRechargeRepository.countRechargeSum(qo);
        return ResponseDataUtil.buildSuccess(data);
    }

    /**
     * 统计充值金额
     * @param financeAccountRechargeValidator
     * @param flag 1表示业务账号 账户  2表示认证账号 账户  4表示智能账号
     * @return
     */
    public ResponseData<Map<String, Object>> statisticRechargeSum(FinanceAccountRechargeValidator financeAccountRechargeValidator, String flag) {

        //智能短信
        if ("4".equals(flag)) {
            Map<String, Object> data = financeAccountRechargeRepository.intellectRechargeSum(financeAccountRechargeValidator);
            return ResponseDataUtil.buildSuccess(data);
        }

        return ResponseDataUtil.buildError();
    }
}
