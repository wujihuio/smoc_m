package com.smoc.cloud.customer.service;


import com.alibaba.fastjson.JSON;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.redis.RedisConstant;
import com.smoc.cloud.common.redis.RedisModel;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.customer.qo.AccountChannelInfoQo;
import com.smoc.cloud.common.smoc.customer.qo.AccountInfoQo;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticComplaintData;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticSendData;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountChannelInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountFinanceInfoValidator;
import com.smoc.cloud.common.smoc.message.MessageAccountValidator;
import com.smoc.cloud.common.smoc.parameter.ParameterExtendFiltersValueValidator;
import com.smoc.cloud.common.smoc.query.model.AccountSendStatisticModel;
import com.smoc.cloud.common.utils.DES;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.PasswordUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.customer.entity.AccountBasicInfo;
import com.smoc.cloud.customer.entity.AccountChannelInfo;
import com.smoc.cloud.customer.entity.AccountInterfaceInfo;
import com.smoc.cloud.customer.repository.AccountChannelRepository;
import com.smoc.cloud.customer.repository.AccountFinanceRepository;
import com.smoc.cloud.customer.repository.AccountInterfaceRepository;
import com.smoc.cloud.customer.repository.BusinessAccountRepository;
import com.smoc.cloud.finance.entity.FinanceAccount;
import com.smoc.cloud.finance.repository.FinanceAccountRepository;
import com.smoc.cloud.parameter.entity.ParameterExtendFiltersValue;
import com.smoc.cloud.parameter.repository.ParameterExtendFiltersValueRepository;
import com.smoc.cloud.parameter.service.ParameterExtendFiltersValueService;
import com.smoc.cloud.sequence.repository.SequenceRepository;
import com.smoc.cloud.utils.RandomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ??????????????????
 **/
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BusinessAccountService {

    @Resource
    private SequenceRepository sequenceRepository;

    @Resource
    private BusinessAccountRepository businessAccountRepository;

    @Resource
    private AccountFinanceRepository accountFinanceRepository;

    @Resource
    private FinanceAccountRepository financeAccountRepository;

    @Resource
    private AccountChannelRepository accountChannelRepository;

    @Resource
    private AccountInterfaceRepository accountInterfaceRepository;

    @Resource
    private ParameterExtendFiltersValueRepository parameterExtendFiltersValueRepository;
    @Resource
    private ParameterExtendFiltersValueService parameterExtendFiltersValueService;

    @Autowired
    private RandomService randomService;

    @Resource(name = "defaultRedisTemplate")
    private RedisTemplate<String, String> stringRedisTemplate;

    @Resource(name = "redisTemplate4")
    private RedisTemplate<String, RedisModel> redisTemplate4;


    /**
     * ????????????
     *
     * @param pageParams
     * @return
     */
    public PageList<AccountBasicInfoValidator> page(PageParams<AccountBasicInfoValidator> pageParams) {
        return businessAccountRepository.page(pageParams);
    }

    /**
     * ??????id????????????
     *
     * @param id
     * @return
     */
    public ResponseData findById(String id) {
        Optional<AccountBasicInfo> data = businessAccountRepository.findById(id);

        if (!data.isPresent()) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_QUERY_ERROR);
        }

        AccountBasicInfo entity = data.get();
        AccountBasicInfoValidator accountBaseInfoValidator = new AccountBasicInfoValidator();
        BeanUtils.copyProperties(entity, accountBaseInfoValidator);

        //????????????
        accountBaseInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(entity.getCreatedTime()));

        return ResponseDataUtil.buildSuccess(accountBaseInfoValidator);
    }

    /**
     * ???????????????
     *
     * @param accountBasicInfoValidator
     * @param op                        ???????????? ???add???edit
     * @return
     */
    @Transactional
    public ResponseData<AccountBasicInfo> save(AccountBasicInfoValidator accountBasicInfoValidator, String op) {

        Iterable<AccountBasicInfo> data = businessAccountRepository.findByAccountId(accountBasicInfoValidator.getAccountId());

        AccountBasicInfo entity = new AccountBasicInfo();
        BeanUtils.copyProperties(accountBasicInfoValidator, entity);

        String accountType = "";
        String accountChannelType = "";
        String carrier = "";
        String countryCode = "";

        //add??????
        if (data != null && data.iterator().hasNext() && "add".equals(op)) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
        }
        //edit??????
        else if (data != null && data.iterator().hasNext() && "edit".equals(op)) {
            boolean status = false;
            Iterator iter = data.iterator();
            while (iter.hasNext()) {
                AccountBasicInfo info = (AccountBasicInfo) iter.next();
                accountType = info.getBusinessType();
                accountChannelType = info.getAccountChannelType();
                carrier = info.getCarrier();
                countryCode = info.getCountryCode();
                if (!entity.getAccountId().equals(info.getAccountId())) {
                    status = true;
                    break;
                }
            }
            if (status) {
                return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
            }
        }

        //???????????????
        Iterable<AccountBasicInfo> extendNumber = businessAccountRepository.findByExtendNumber(accountBasicInfoValidator.getExtendNumber());
        //add??????
        if (extendNumber != null && extendNumber.iterator().hasNext() && "add".equals(op)) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
        }
        //edit??????
        if (extendNumber != null && extendNumber.iterator().hasNext()) {
            boolean status = false;
            Iterator iterator = extendNumber.iterator();
            while (iterator.hasNext()) {
                AccountBasicInfo accountBasicInfo = (AccountBasicInfo) iterator.next();
                if (!entity.getAccountId().equals(accountBasicInfo.getAccountId())) {
                    status = true;
                    break;
                }
            }
            if (status) {
                return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
            }
        }

        //??????????????????
        entity.setCreatedTime(DateTimeUtils.getDateTimeFormat(accountBasicInfoValidator.getCreatedTime()));

        //op ?????? edit ??? add
        if (!("edit".equals(op) || "add".equals(op))) {
            return ResponseDataUtil.buildError();
        }

        //??????????????????
        saveFinanceAccount(entity, op, accountType);

        if ("edit".equals(op)) {
            //?????????????????????????????????????????????????????????(?????????????????????????????????3???????????????????????????????????????2???????????????????????????????????????1??????????????????)
            if ("INTL".equals(accountBasicInfoValidator.getCarrier())) {
                accountFinanceRepository.deleteByAccountIdAndCarrier(accountBasicInfoValidator.getAccountId(), accountBasicInfoValidator.getCountryCode());
            } else {
                accountFinanceRepository.deleteByAccountIdAndCarrier(accountBasicInfoValidator.getAccountId(), accountBasicInfoValidator.getCarrier());
            }


            //???????????????????????????????????????????????????????????????????????????????????????
            if (!accountBasicInfoValidator.getAccountChannelType().equals(accountChannelType)) {
                deleteByAccountId(entity);
            }

            //?????????????????????????????????????????????????????????????????????
            if ("INTL".equals(accountBasicInfoValidator.getCarrier())) {
                if (!accountBasicInfoValidator.getCountryCode().equals(countryCode)) {
                    deleteConfigChannelByCarrier(entity);
                }
            } else {
                if (!accountBasicInfoValidator.getCarrier().equals(carrier)) {
                    deleteConfigChannelByCarrier(entity);
                }
            }
        }

        //???????????????????????????????????????????????????????????????
        if (StringUtils.hasText(accountBasicInfoValidator.getAccountCopyId())) {
            String process = copyAccount(accountBasicInfoValidator.getAccountCopyId(), entity);
            entity.setAccountProcess(process);
        }

        //????????????
        log.info("[EC??????????????????][????????????????????????][{}]??????:{}", op, JSON.toJSONString(entity));
        businessAccountRepository.saveAndFlush(entity);

        //??????????????????????????????????????????
        if("add".equals(op)){
            sequenceRepository.createSequence(entity.getAccountId());
        }

        //?????????id??????redis???
        stringRedisTemplate.opsForValue().set(RandomService.PREFIX + ":" + entity.getAccountId(), entity.getAccountId());

        return ResponseDataUtil.buildSuccess();
    }

    //??????????????????
    private void saveFinanceAccount(AccountBasicInfo accountBasicInfo, String op, String accountType) {

        if ("add".equals(op)) {
            FinanceAccount financeAccount = new FinanceAccount();
            financeAccount.setAccountId(accountBasicInfo.getAccountId());
            financeAccount.setAccountType(accountBasicInfo.getBusinessType());
            financeAccount.setAccountTotalSum(new BigDecimal("0.00000"));
            financeAccount.setAccountUsableSum(new BigDecimal("0.00000"));
            financeAccount.setAccountFrozenSum(new BigDecimal("0.00000"));
            financeAccount.setAccountConsumeSum(new BigDecimal("0.00000"));
            financeAccount.setAccountRechargeSum(new BigDecimal("0.00000"));
            financeAccount.setAccountRefundSum(new BigDecimal("0.00000"));
            financeAccount.setAccountCreditSum(new BigDecimal("0.00000"));
            financeAccount.setAccountStatus("1");
            financeAccount.setIsShare("0");
            financeAccount.setCreatedTime(DateTimeUtils.getNowDateTime());
            financeAccount.setCreatedBy(accountBasicInfo.getCreatedBy());
            financeAccountRepository.saveAndFlush(financeAccount);
        }

        //??????????????????????????????????????????????????????????????????
        if ("edit".equals(op) && !accountType.equals(accountBasicInfo.getBusinessType())) {
            financeAccountRepository.updateAccountTypeByAccountId(accountBasicInfo.getBusinessType(), accountBasicInfo.getAccountId());
        }
    }

    //????????????????????????????????????????????????????????????
    private void deleteConfigChannelByCarrier(AccountBasicInfo entity) {
        String carrier = entity.getCarrier();
        if ("INTL".equals(entity.getCarrier())) {
            carrier = entity.getCountryCode();
        }
        List<AccountChannelInfoQo> list = accountChannelRepository.accountChannelByAccountIdAndCarrier(entity.getAccountId(), carrier, entity.getAccountChannelType());
        String[] carrierLength = carrier.split(",");
        if (StringUtils.isEmpty(list) || list.size() < carrierLength.length) {
            //????????????
            accountProcess(entity, "0");
        } else {
            accountProcess(entity, "1");
        }
        accountChannelRepository.deleteConfigChannelByCarrier(entity.getAccountId(), carrier);
    }

    //???????????????????????????????????????????????????
    private void deleteByAccountId(AccountBasicInfo entity) {
        accountChannelRepository.deleteByAccountId(entity.getAccountId());
        //????????????
        accountProcess(entity, "0");
    }

    //????????????
    private void accountProcess(AccountBasicInfo entity, String process) {
        Optional<AccountBasicInfo> optional = businessAccountRepository.findById(entity.getAccountId());
        if (optional.isPresent()) {
            AccountBasicInfo accountBasicInfo = optional.get();
            StringBuffer accountProcess = new StringBuffer(accountBasicInfo.getAccountProcess());
            accountProcess = accountProcess.replace(3, 4, process);
            entity.setAccountProcess(accountProcess.toString());
        }
    }

    //??????????????????accountCopyId:????????????accountId:?????????
    private String copyAccount(String accountCopyId, AccountBasicInfo entity) {
        StringBuffer process = new StringBuffer(entity.getAccountProcess());
        Optional<AccountInterfaceInfo> accountInterfaceInfo = accountInterfaceRepository.findById(accountCopyId);
        //????????????
        if (accountInterfaceInfo.isPresent()) {
            AccountInterfaceInfo terface = new AccountInterfaceInfo();
            BeanUtils.copyProperties(accountInterfaceInfo.get(), terface);
            terface.setAccountId(entity.getAccountId());
            terface.setAccountPassword(DES.encrypt(PasswordUtils.getRandomPassword(9)));//??????
            terface.setCreatedBy(entity.getCreatedBy());
            terface.setCreatedTime(entity.getCreatedTime());
            accountInterfaceRepository.saveAndFlush(terface);
            process = process.replace(2, 3, "1");
            if ("HTTPS".equals(terface.getProtocol()) || "https".equals(terface.getProtocol())) {
                //??????redis?????????
                RedisModel redisModel = new RedisModel();
                redisModel.setMd5HmacKey(terface.getAccountPassword());
                redisModel.setBusinessType(entity.getBusinessType());
                redisModel.setSubmitRate(terface.getMaxSubmitSecond());
                redisModel.setSendRate(terface.getMaxSendSecond());
                redisModel.setIps(terface.getIdentifyIp());
                redisModel.setNoCheck(terface.getExecuteCheck());
                //???????????????redis???
                redisTemplate4.opsForValue().set(RedisConstant.HTTP_SERVER_KEY + entity.getAccountId(), redisModel);
            } else {
                redisTemplate4.delete(RedisConstant.HTTP_SERVER_KEY + entity.getAccountId());
            }
            //????????????
            log.info("[EC??????????????????][????????????????????????][{}]??????:{}", "add", JSON.toJSONString(terface));
        }

        //????????????
        /*AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(accountCopyId);
        List<AccountFinanceInfoValidator> list = accountFinanceRepository.findByAccountId(accountFinanceInfoValidator);
        if (!StringUtils.isEmpty(list) && list.size() > 0) {
            List<AccountFinanceInfoValidator> prices = new ArrayList<>();
            for (AccountFinanceInfoValidator accountFinanceInfo : list) {
                AccountFinanceInfoValidator price = new AccountFinanceInfoValidator();
                price.setFlag("1");//?????????????????????
                price.setCarrier(accountFinanceInfo.getCarrier());
                price.setCarrierPrice(accountFinanceInfo.getCarrierPrice());
                prices.add(price);
            }

            //????????????
            accountFinanceInfoValidator.setAccountId(entity.getAccountId());
            accountFinanceInfoValidator.setPayType(list.get(0).getPayType());
            accountFinanceInfoValidator.setChargeType(list.get(0).getChargeType());
            accountFinanceInfoValidator.setFrozenReturnDate(list.get(0).getFrozenReturnDate());
            accountFinanceInfoValidator.setAccountCreditSum(list.get(0).getAccountCreditSum());
            accountFinanceInfoValidator.setCarrierType(list.get(0).getCarrierType());
            accountFinanceInfoValidator.setPrices(prices);
            accountFinanceInfoValidator.setCreatedBy(entity.getCreatedBy());
            accountFinanceRepository.batchSave(accountFinanceInfoValidator);

            //???????????????????????????
            financeAccountRepository.updateAccountCreditSumByAccountId(accountFinanceInfoValidator.getAccountId(), accountFinanceInfoValidator.getAccountCreditSum());
            process = process.replace(1, 2, "1");
            //????????????
            log.info("[EC??????????????????][????????????????????????][{}]??????:{}", "add", JSON.toJSONString(accountFinanceInfoValidator));
        }*/

        //????????????
        /*List<AccountChannelInfo> channelList = accountChannelRepository.findByAccountId(accountCopyId);
        if (!StringUtils.isEmpty(channelList) && channelList.size() > 0) {
            AccountChannelInfoValidator accountChannelInfoValidator = new AccountChannelInfoValidator();
            accountChannelInfoValidator.setAccountId(entity.getAccountId());
            accountChannelInfoValidator.setCreatedBy(entity.getCreatedBy());
            accountChannelRepository.batchChannelCopy(accountChannelInfoValidator, channelList);
            process = process.replace(3, 4, "1");
            //????????????
            log.info("[EC??????????????????][????????????????????????][{}]??????:{}-{}", "add", JSON.toJSONString(accountChannelInfoValidator), JSON.toJSONString(channelList));
        }*/

        copyFiltersInfo(accountCopyId, entity);

        return process.toString();
    }

    @Async
    public void copyFiltersInfo(String accountCopyId, AccountBasicInfo entity) {
        //????????????
        List<ParameterExtendFiltersValue> filtersList = parameterExtendFiltersValueRepository.findParameterExtendFiltersValueByBusinessIdAndBusinessType(accountCopyId, "BUSINESS_ACCOUNT_FILTER");
        if (!StringUtils.isEmpty(filtersList) && filtersList.size() > 0) {
            //??????stream??????list
            List<ParameterExtendFiltersValueValidator> infoList = filtersList.stream()
                    .map(e -> {
                        ParameterExtendFiltersValueValidator info = new ParameterExtendFiltersValueValidator();
                        info.setId(UUID.uuid32());
                        info.setBusinessType(e.getBusinessType());
                        info.setBusinessId(entity.getAccountId());
                        info.setParamKey(e.getParamKey());
                        info.setParamName(e.getParamName());
                        info.setParamValue(e.getParamValue());
                        info.setCreatedBy(entity.getCreatedBy());
                        return info;
                    })
                    .collect(Collectors.toList());

            parameterExtendFiltersValueRepository.batchSave(infoList);
            parameterExtendFiltersValueService.reloadRedisCache(entity.getAccountId(), infoList);
            //????????????
            log.info("[EC??????????????????][????????????????????????][{}]??????:{}-{}", "add", JSON.toJSONString(infoList));
        }
    }

    /**
     * ???????????????????????????
     *
     * @param id
     * @param status
     * @return
     */
    @Transactional
    public ResponseData forbiddenAccountById(String id, String status) {
        String op = status;
        AccountBasicInfo entity = businessAccountRepository.findById(id).get();

        //??????????????????
        if ("0".equals(status)) {
            status = "1";

            /**
             * ?????????????????????????????????redis?????? ?????????
             */
            Optional<AccountInterfaceInfo> data = accountInterfaceRepository.findById(id);
            AccountInterfaceInfo accountInterfaceInfo = data.get();
            //?????????redis???
            if ("HTTPS".equals(accountInterfaceInfo.getProtocol()) || "https".equals(accountInterfaceInfo.getProtocol())) {
                //??????redis?????????
                RedisModel redisModel = new RedisModel();
                redisModel.setMd5HmacKey(accountInterfaceInfo.getAccountPassword());
                redisModel.setBusinessType(entity.getBusinessType());
                redisModel.setSubmitRate(accountInterfaceInfo.getMaxSubmitSecond());
                redisModel.setSendRate(accountInterfaceInfo.getMaxSendSecond());
                redisModel.setIps(accountInterfaceInfo.getIdentifyIp());
                //???????????????redis???
                redisTemplate4.opsForValue().set(RedisConstant.HTTP_SERVER_KEY + entity.getAccountId(), redisModel);
            }


            if (Integer.parseInt(entity.getAccountProcess()) <= 11100) {
                status = "2";
            }
        } else {
            status = "0";
            //??????
            redisTemplate4.delete(RedisConstant.HTTP_SERVER_KEY + entity.getAccountId());
        }

        businessAccountRepository.updateAccountStatusById(id, status);

        //????????????
        log.info("[EC??????????????????][{}]??????:{}", ("1".equals(op) || "2".equals(op)) ? "??????????????????" : "??????????????????", JSON.toJSONString(entity));

        return ResponseDataUtil.buildSuccess();
    }

    /**
     * ?????????????????????????????????
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> findBusinessAccountByEnterpriseId(String enterpriseId) {
        List<AccountBasicInfoValidator> list = businessAccountRepository.findBusinessAccountByEnterpriseId(enterpriseId);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<AccountBasicInfo>> findBusinessAccountByEnterpriseIdAndBusinessType(String enterpriseId, String businessType) {
        List<AccountBasicInfo> list = businessAccountRepository.findBusinessAccountByEnterpriseIdAndBusinessType(enterpriseId, businessType);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ??????????????????
     *
     * @param enterpriseFlag
     * @return
     */
    public ResponseData<String> createAccountId(String enterpriseFlag) {

        String accountId = randomService.getBusinessAccount(enterpriseFlag);

        return ResponseDataUtil.buildSuccess(accountId);
    }

    /**
     * ?????????????????????????????????
     *
     * @param accountBasicInfoValidator
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> findBusinessAccount(AccountBasicInfoValidator accountBasicInfoValidator) {
        List<AccountBasicInfoValidator> list = businessAccountRepository.findBusinessAccount(accountBasicInfoValidator);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ?????????????????????????????????
     *
     * @param messageAccountValidator
     * @return
     */
    public ResponseData<List<MessageAccountValidator>> messageAccountList(MessageAccountValidator messageAccountValidator) {
        List<MessageAccountValidator> list = businessAccountRepository.messageAccountList(messageAccountValidator);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param params
     * @return
     */
    public ResponseData<PageList<MessageAccountValidator>> messageAccountInfoList(PageParams<MessageAccountValidator> params) {
        PageList<MessageAccountValidator> list = businessAccountRepository.messageAccountInfoList(params);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ??????????????????????????????
     *
     * @param statisticSendData
     * @return
     */
    public ResponseData<List<AccountStatisticSendData>> statisticAccountSendNumber(AccountStatisticSendData statisticSendData) {
        List<AccountStatisticSendData> list = businessAccountRepository.statisticAccountSendNumber(statisticSendData);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ?????????????????????
     *
     * @param statisticComplaintData
     * @return
     */
    public ResponseData<List<AccountStatisticComplaintData>> statisticComplaintMonth(AccountStatisticComplaintData statisticComplaintData) {
        List<AccountStatisticComplaintData> list = businessAccountRepository.statisticComplaintMonth(statisticComplaintData);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ????????????????????????
     *
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<AccountInfoQo>> accountAll(PageParams<AccountInfoQo> pageParams) {
        PageList<AccountInfoQo> list = businessAccountRepository.accountAll(pageParams);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ????????????????????????????????????
     *
     * @param pageParams
     * @return
     */
    public PageList<AccountBasicInfoValidator> accountByProtocol(PageParams<AccountBasicInfoValidator> pageParams) {
        return businessAccountRepository.accountByProtocol(pageParams);
    }

    /**
     * ???????????????????????????
     *
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<AccountSendStatisticModel>> queryAccountSendStatistics(PageParams<AccountSendStatisticModel> pageParams) {
        PageList<AccountSendStatisticModel> list = businessAccountRepository.queryAccountSendStatistics(pageParams);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ??????????????????
     *
     * @param accountBasicInfoValidator
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> accountList(AccountBasicInfoValidator accountBasicInfoValidator) {
        List<AccountBasicInfoValidator> list = businessAccountRepository.accountList(accountBasicInfoValidator);
        return ResponseDataUtil.buildSuccess(list);
    }
}
