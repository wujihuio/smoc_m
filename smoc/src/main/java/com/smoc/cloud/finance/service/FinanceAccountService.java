package com.smoc.cloud.finance.service;

import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountConsumeValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountRechargeValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountRefundValidator;
import com.smoc.cloud.common.smoc.finance.validator.FinanceAccountValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.customer.repository.BusinessAccountRepository;
import com.smoc.cloud.customer.repository.EnterpriseRepository;
import com.smoc.cloud.finance.entity.FinanceAccount;
import com.smoc.cloud.finance.entity.FinanceAccountRecharge;
import com.smoc.cloud.finance.entity.FinanceAccountRefund;
import com.smoc.cloud.finance.repository.FinanceAccountRechargeRepository;
import com.smoc.cloud.finance.repository.FinanceAccountRefundRepository;
import com.smoc.cloud.finance.repository.FinanceAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class FinanceAccountService {

    @Resource
    private EnterpriseRepository enterpriseRepository;

    @Resource
    private FinanceAccountRepository financeAccountRepository;

    @Resource
    private FinanceAccountRechargeRepository financeAccountRechargeRepository;

    @Resource
    private FinanceAccountRefundRepository financeAccountRefundRepository;


    /**
     * 分页查询
     *
     * @param pageParams
     * @param flag       1表示业务账号 账户  2表示认证账号 账户 3表示财务共享账号 4表示共用的账号财务账户
     * @return
     */
    public ResponseData<PageList<FinanceAccountValidator>> page(PageParams<FinanceAccountValidator> pageParams, String flag) {

        if ("1".equals(flag)) {
            PageList<FinanceAccountValidator> data = financeAccountRepository.pageBusinessType(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }
        if ("2".equals(flag)) {
            PageList<FinanceAccountValidator> data = financeAccountRepository.pageIdentification(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }
        if ("3".equals(flag)) {
            PageList<FinanceAccountValidator> data = financeAccountRepository.pageShare(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }

        if ("4".equals(flag)) {
            if (StringUtils.isEmpty(pageParams.getParams().getAccountType())) {
                return ResponseDataUtil.buildError("业务类型参数不能为空！");
            }
            PageList<FinanceAccountValidator> data = financeAccountRepository.pageSystemAccount(pageParams);
            return ResponseDataUtil.buildSuccess(data);
        }

        return ResponseDataUtil.buildError();
    }

    /**
     * 统计账户金额
     *
     * @param flag  1表示业务账号 账户  2表示认证账号 账户 3表示财务共享账号 4表示共用的账号财务账户
     * @return
     */
    public ResponseData<Map<String, Object>> countSum(String flag, FinanceAccountValidator op) {
        Map<String, Object> data = financeAccountRepository.countSum(flag, op);
        return ResponseDataUtil.buildSuccess(data);
    }

    /**
     * 账户充值,保存充值记录，变更财务账户
     *
     * @return
     */
    @Transactional
    public ResponseData recharge(FinanceAccountRechargeValidator financeAccountRechargeValidator) {

        financeAccountRepository.recharge(financeAccountRechargeValidator.getRechargeSum(), financeAccountRechargeValidator.getAccountId());
        FinanceAccountRecharge entity = new FinanceAccountRecharge();
        BeanUtils.copyProperties(financeAccountRechargeValidator, entity);
        //转换日期格式
        entity.setCreatedTime(DateTimeUtils.getDateTimeFormat(financeAccountRechargeValidator.getCreatedTime()));
        financeAccountRechargeRepository.saveAndFlush(entity);

        return ResponseDataUtil.buildSuccess();

    }

    /**
     * 账户冻结 冻结要消费的金额，冻结时，会根据余额、授信额度来进行计算。冻结失败会提示余额不足
     *
     * @return
     */
    @Transactional
    public ResponseData freeze(FinanceAccountConsumeValidator financeAccountConsumeValidator) {

        return ResponseDataUtil.buildSuccess();

    }

    /**
     * 账户消费（从冻结中消费）
     *
     * @return
     */
    @Transactional
    public ResponseData consume(FinanceAccountConsumeValidator financeAccountConsumeValidator) {

        return ResponseDataUtil.buildSuccess();

    }

    /**
     * 账户解冻 解冻冻结金额
     *
     * @return
     */
    @Transactional
    public ResponseData unfreeze(FinanceAccountConsumeValidator financeAccountConsumeValidator) {

        return ResponseDataUtil.buildSuccess();

    }

    /**
     * 根据id查询
     *
     * @param accountId
     * @return
     */
    public ResponseData<FinanceAccountValidator> findById(String accountId) {
        Optional<FinanceAccount> optional = financeAccountRepository.findById(accountId);
        FinanceAccountValidator financeAccountValidator = new FinanceAccountValidator();
        BeanUtils.copyProperties(optional.get(), financeAccountValidator);
        //转换日期
        financeAccountValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(optional.get().getCreatedTime()));

        //去除多余的0
        financeAccountValidator.setAccountTotalSum(new BigDecimal(optional.get().getAccountTotalSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountUsableSum(new BigDecimal(optional.get().getAccountUsableSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountFrozenSum(new BigDecimal(optional.get().getAccountFrozenSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountConsumeSum(new BigDecimal(optional.get().getAccountConsumeSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountRechargeSum(new BigDecimal(optional.get().getAccountRechargeSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountCreditSum(new BigDecimal(optional.get().getAccountCreditSum().stripTrailingZeros().toPlainString()));
        financeAccountValidator.setAccountRefundSum(new BigDecimal(optional.get().getAccountRefundSum().stripTrailingZeros().toPlainString()));

        return ResponseDataUtil.buildSuccess(financeAccountValidator);
    }

    /**
     * 根据enterpriseId，查询企业所有财务账户
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<FinanceAccountValidator>> findEnterpriseFinanceAccounts(String enterpriseId) {

        List<FinanceAccountValidator> list = financeAccountRepository.findEnterpriseFinanceAccount(enterpriseId);
        return ResponseDataUtil.buildSuccess(list);

    }

    /**
     * 根据企业enterpriseId，查询企业所有财务账户(包括子企业财务账户)
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<FinanceAccountValidator>> findEnterpriseAndSubsidiaryFinanceAccount(String enterpriseId) {
        //log.info("[enterpriseId]:{}", new Gson().toJson(enterpriseId));
        List<String> enterpriseIds = enterpriseRepository.findEnterpriseAndSubsidiaryId(enterpriseId);
        //log.info("[enterpriseIds]:{}", new Gson().toJson(enterpriseIds));
        if (null != enterpriseIds && enterpriseIds.size() > 0) {
            //log.info("[enterpriseIds]:{}", new Gson().toJson(enterpriseIds));
            List<FinanceAccountValidator> data = financeAccountRepository.findEnterpriseAndSubsidiaryFinanceAccount(enterpriseIds);
            //log.info("[data]:{}", new Gson().toJson(data));
            return ResponseDataUtil.buildSuccess(data);
        }

        return ResponseDataUtil.buildError();

    }

    /**
     * 根据enterpriseId 汇总企业金额统计
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<Map<String, Object>> countEnterpriseSum(String enterpriseId) {

        Map<String, Object> data = financeAccountRepository.countEnterpriseSum(enterpriseId);
        return ResponseDataUtil.buildSuccess(data);

    }

    /**
     * 创建共享账户
     * 包括了创建共享账户流水记录，修改原账户状态，并创建共享账户
     *
     * @param financeAccountValidator
     * @param op                      操作类型 为add、edit
     * @return
     */
    @Transactional
    public ResponseData save(FinanceAccountValidator financeAccountValidator, String op) {

        //op 不为 edit 或 add
        if (!("edit".equals(op) || "add".equals(op))) {
            return ResponseDataUtil.buildError();
        }

        if ("add".equals(op)) {
            financeAccountRepository.createShareFinanceAccount(financeAccountValidator);
            return ResponseDataUtil.buildSuccess();
        } else {
            ResponseData<FinanceAccountValidator> financeAccountValidatorResponseData = this.findById(financeAccountValidator.getAccountId());
            financeAccountRepository.editShareFinanceAccount(financeAccountValidator, financeAccountValidatorResponseData.getData());
            return ResponseDataUtil.buildSuccess();
        }
    }

    /**
     * 查询共享账号的子账号信息
     *
     * @param accountId
     * @return
     */
    public ResponseData<List<FinanceAccountValidator>> findSubsidiaryFinanceAccountByAccountId(String accountId) {
        Optional<FinanceAccount> optional = financeAccountRepository.findById(accountId);
        if (optional.isPresent()) {
            List<FinanceAccountValidator> data = financeAccountRepository.findSubsidiaryFinanceAccountByAccountId(optional.get().getShareId());
            return ResponseDataUtil.buildSuccess(data);
        }

        return ResponseDataUtil.buildSuccess();
    }

    /**
     * 账户退款
     * @param financeAccountRefundValidator
     * @return
     */
    @Transactional
    public ResponseData refund(FinanceAccountRefundValidator financeAccountRefundValidator) {
        financeAccountRepository.refund(financeAccountRefundValidator.getRefundSum(), financeAccountRefundValidator.getAccountId());
        FinanceAccountRefund entity = new FinanceAccountRefund();
        BeanUtils.copyProperties(financeAccountRefundValidator, entity);
        //转换日期格式
        entity.setCreatedTime(DateTimeUtils.getDateTimeFormat(financeAccountRefundValidator.getCreatedTime()));
        financeAccountRefundRepository.saveAndFlush(entity);

        return ResponseDataUtil.buildSuccess();
    }
}
