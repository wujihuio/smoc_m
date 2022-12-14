package com.smoc.cloud.customer.service;

import com.smoc.cloud.admin.security.remote.client.SystemExtendBusinessParameterFeignClient;
import com.smoc.cloud.common.auth.qo.Dict;
import com.smoc.cloud.common.auth.qo.DictType;
import com.smoc.cloud.common.auth.validator.SystemExtendBusinessParamValidator;
import com.smoc.cloud.common.gateway.utils.AESConstUtil;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.customer.qo.AccountInfoQo;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticComplaintData;
import com.smoc.cloud.common.smoc.customer.qo.AccountStatisticSendData;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountFinanceInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountInterfaceInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseWebAccountInfoValidator;
import com.smoc.cloud.common.smoc.parameter.ParameterExtendFiltersValueValidator;
import com.smoc.cloud.common.smoc.parameter.ParameterExtendSystemParamValueValidator;
import com.smoc.cloud.common.smoc.query.model.AccountSendStatisticModel;
import com.smoc.cloud.common.utils.DES;
import com.smoc.cloud.customer.remote.BusinessAccountFeignClient;
import com.smoc.cloud.identification.model.AccountExcelModel;
import com.smoc.cloud.parameter.remote.ParameterExtendSystemParamValueFeignClient;
import com.smoc.cloud.parameter.service.ParameterExtendFiltersValueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * ????????????????????????
 **/
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BusinessAccountService {

    @Autowired
    private BusinessAccountFeignClient businessAccountFeignClient;

    @Autowired
    private AccountFinanceService accountFinanceService;

    @Autowired
    private AccountInterfaceService accountInterfaceService;

    @Autowired
    private ParameterExtendFiltersValueService parameterExtendFiltersValueService;

    @Autowired
    private SystemExtendBusinessParameterFeignClient systemExtendBusinessParameterFeignClient;

    @Autowired
    private ParameterExtendSystemParamValueFeignClient parameterExtendSystemParamValueFeignClient;

    @Autowired
    private EnterpriseWebService enterpriseWebService;

    /**
     * ????????????
     *
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<AccountBasicInfoValidator>> page(PageParams<AccountBasicInfoValidator> pageParams) {
        try {
            PageList<AccountBasicInfoValidator> pageList = this.businessAccountFeignClient.page(pageParams);
            return ResponseDataUtil.buildSuccess(pageList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ??????id????????????
     *
     * @param id
     * @return
     */
    public ResponseData<AccountBasicInfoValidator> findById(String id) {
        try {
            ResponseData<AccountBasicInfoValidator> data = this.businessAccountFeignClient.findById(id);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ?????????????????????
     * op ????????? ????????????????????????
     */
    public ResponseData save(AccountBasicInfoValidator accountBasicInfoValidator, String op) {

        try {
            ResponseData data = this.businessAccountFeignClient.save(accountBasicInfoValidator, op);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ?????????????????????
     *
     * @param id
     * @param status
     * @return
     */
    public ResponseData forbiddenAccountById(String id, String status) {
        try {
            ResponseData data = this.businessAccountFeignClient.forbiddenAccountById(id, status);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> findBusinessAccountByEnterpriseId(String enterpriseId) {
        try {
            ResponseData<List<AccountBasicInfoValidator>> data = this.businessAccountFeignClient.findBusinessAccountByEnterpriseId(enterpriseId);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param enterpriseId
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> findBusinessAccountByEnterpriseIdAndBusinessType(String enterpriseId,String businessType) {
        try {
            ResponseData<List<AccountBasicInfoValidator>> data = this.businessAccountFeignClient.findBusinessAccountByEnterpriseIdAndBusinessType(enterpriseId,businessType);
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ??????????????????
     *
     * @param enterpriseFlag
     * @return
     */
    public ResponseData<String> createAccountId(String enterpriseFlag) {
        try {
            ResponseData<String> accountId = this.businessAccountFeignClient.createAccountId(enterpriseFlag);
            return accountId;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param statisticSendData
     * @return
     */
    public AccountStatisticSendData statisticAccountSendNumber(AccountStatisticSendData statisticSendData) {
        ResponseData<List<AccountStatisticSendData>> responseData = this.businessAccountFeignClient.statisticAccountSendNumber(statisticSendData);
        List<AccountStatisticSendData> list = responseData.getData();

        //??????
        String[] month = list.stream().map(AccountStatisticSendData::getMonth).toArray(String[]::new);
        //?????????
        BigDecimal[] sendNumber = list.stream().map(AccountStatisticSendData::getSendNumber).toArray(BigDecimal[]::new);
        ;

        AccountStatisticSendData accountStatisticSendData = new AccountStatisticSendData();
        accountStatisticSendData.setMonthArray(month);
        accountStatisticSendData.setSendNumberArray(sendNumber);

        return accountStatisticSendData;
    }

    /**
     * EC???????????????????????????
     *
     * @param statisticComplaintData
     * @return
     */
    public AccountStatisticComplaintData statisticComplaintMonth(AccountStatisticComplaintData statisticComplaintData) {
        ResponseData<List<AccountStatisticComplaintData>> responseData = this.businessAccountFeignClient.statisticComplaintMonth(statisticComplaintData);
        List<AccountStatisticComplaintData> list = responseData.getData();

        //??????
        String[] month = list.stream().map(AccountStatisticComplaintData::getMonth).toArray(String[]::new);
        //?????????
        String[] complaint = list.stream().map(AccountStatisticComplaintData::getComplaint).toArray(String[]::new);
        ;

        AccountStatisticComplaintData accountStatisticComplaintData = new AccountStatisticComplaintData();
        accountStatisticComplaintData.setMonthArray(month);
        accountStatisticComplaintData.setComplaintArray(complaint);

        return accountStatisticComplaintData;
    }

    /**
     * ??????excel?????????????????????
     *
     * @param accountBasicInfoValidator
     * @return
     */
   /* public CopyOnWriteArrayList<AccountExcelModel> excelAccountInfo(AccountBasicInfoValidator accountBasicInfoValidator, HttpServletRequest request) {

        //??????????????????
        AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(accountBasicInfoValidator.getAccountId());
        ResponseData<List<AccountFinanceInfoValidator>> financeList = accountFinanceService.findByAccountId(accountFinanceInfoValidator);

        //??????????????????
        ResponseData<AccountInterfaceInfoValidator> interfaceInfoValidator = accountInterfaceService.findById(accountBasicInfoValidator.getAccountId());

        CopyOnWriteArrayList<AccountExcelModel> list = new CopyOnWriteArrayList<>();

        //??????????????????
        AccountExcelModel excelModel = new AccountExcelModel();
        excelModel.setKey("EC??????");
        excelModel.setValue(accountBasicInfoValidator.getAccountId());
        list.add(excelModel);

        AccountExcelModel excelModel1 = new AccountExcelModel();
        excelModel1.setKey("????????????");
        AccountExcelModel excelModel2 = new AccountExcelModel();
        excelModel2.setKey("????????????");
        if (!StringUtils.isEmpty(financeList.getData()) && financeList.getData().size() > 0) {
            AccountFinanceInfoValidator finance = financeList.getData().get(0);
            excelModel1.setValue(finance.getChargeType().equals("1") ? "????????????" : "????????????");
            excelModel2.setValue(finance.getPayType().equals("1") ? "?????????" : "?????????");
        }
        list.add(excelModel1);
        list.add(excelModel2);

        AccountExcelModel excelModel3 = new AccountExcelModel();
        excelModel3.setKey("?????????");
        excelModel3.setValue("??????????????????" + accountBasicInfoValidator.getExtendCode() + "???????????????????????????" + accountBasicInfoValidator.getRandomExtendCodeLength());
        list.add(excelModel3);

        //????????????
        if (!StringUtils.isEmpty(interfaceInfoValidator.getData())) {
            AccountInterfaceInfoValidator interfaceInfo = interfaceInfoValidator.getData();

            AccountExcelModel excelModel4 = new AccountExcelModel();
            excelModel4.setKey(interfaceInfo.getProtocol() + "???????????????????????????");
            list.add(excelModel4);

            AccountExcelModel excelModel6 = new AccountExcelModel();
            excelModel6.setKey("????????????");
            excelModel6.setValue(DES.decrypt(interfaceInfo.getAccountPassword()));
            list.add(excelModel6);

            AccountExcelModel excelModel5 = new AccountExcelModel();
            excelModel5.setKey("IP??????");
            excelModel5.setValue(StringUtils.isEmpty(interfaceInfo.getIdentifyIp()) ? "?????????" : interfaceInfo.getIdentifyIp());
            list.add(excelModel5);

            AccountExcelModel excelModel7 = new AccountExcelModel();
            excelModel7.setKey("????????????(???/???)");
            excelModel7.setValue("" + interfaceInfo.getMaxSendSecond());
            list.add(excelModel7);

            if ("CMPP".equals(interfaceInfo.getProtocol()) || "SGIP".equals(interfaceInfo.getProtocol()) || "SMGP".equals(interfaceInfo.getProtocol())) {
                AccountExcelModel excelModel8 = new AccountExcelModel();
                excelModel8.setKey("????????????");
                excelModel8.setValue(interfaceInfo.getSrcId());
                list.add(excelModel8);

                AccountExcelModel excelModel9 = new AccountExcelModel();
                excelModel9.setKey("???????????????");
                excelModel9.setValue("" + interfaceInfo.getMaxConnect());
                list.add(excelModel9);
            }

            if (interfaceInfo.getProtocol().contains("HTTP")) {
                AccountExcelModel excelModel10 = new AccountExcelModel();
                excelModel10.setKey("??????????????????(???/???)");
                excelModel10.setValue("" + interfaceInfo.getMaxSubmitSecond());
                list.add(excelModel10);
            }
        }

        //??????????????????
        ResponseData<List<ParameterExtendFiltersValueValidator>> data = this.parameterExtendFiltersValueService.findParameterValue(accountBasicInfoValidator.getAccountId());
        if (!StringUtils.isEmpty(data.getData()) && data.getData().size() > 0) {
            AccountExcelModel excelModel11 = new AccountExcelModel();
            excelModel11.setKey("???????????????????????????");
            list.add(excelModel11);

            List<ParameterExtendFiltersValueValidator> filtersList = data.getData();
            //???????????????
            ServletContext context = request.getServletContext();
            Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");

            for (ParameterExtendFiltersValueValidator filters : filtersList) {
                //??????????????????????????????????????????????????????  ?????????????????????
                if("BLACK_WORD_FILTERING".equals(filters.getParamKey()) || "AUDIT_WORD_FILTERING".equals(filters.getParamKey()) || "BLACK_LIST_LEVEL_FILTERING".equals(filters.getParamKey())){
                    continue;
                }

                String values = getParamValues(filters, dictMap);
                AccountExcelModel excelModel12 = new AccountExcelModel();
                excelModel12.setKey(filters.getParamName());
                excelModel12.setValue(values);
                list.add(excelModel12);
            }
        }

        return list;
    }*/


    /**
     * ????????????????????????
     * @param params
     * @return
     */
    public ResponseData<PageList<AccountInfoQo>> accountAll(PageParams<AccountInfoQo> params) {
        try {
            ResponseData<PageList<AccountInfoQo>> pageList = this.businessAccountFeignClient.accountAll(params);
            return pageList;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    public Map<String, Object> buildExcelMap(AccountBasicInfoValidator accountBasicInfoValidator, HttpServletRequest request) {

        //????????????
        Map<String,String> businessTypeMap = this.businessType(request);
        //?????????
        Map<String,String>  carrierMap = this.carrier(request);

        Map<String, Object> buildMap = new HashMap<>();

        //??????????????????
        AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(accountBasicInfoValidator.getAccountId());
        ResponseData<List<AccountFinanceInfoValidator>> financeList = accountFinanceService.findByAccountId(accountFinanceInfoValidator);

        Map<String, Object> financeMap = new HashMap<>();
        financeMap.put("bussinessType",businessTypeMap.get(accountBasicInfoValidator.getBusinessType()));
        if (!StringUtils.isEmpty(financeList.getData()) && financeList.getData().size() > 0) {
            AccountFinanceInfoValidator finance = financeList.getData().get(0);
            financeMap.put("chargeType",finance.getChargeType().equals("1") ? "?????????????????????" : "??????????????????");
            financeMap.put("payType",finance.getPayType().equals("1") ? "?????????" : "?????????");
            String price = "";
            for(AccountFinanceInfoValidator info: financeList.getData()){
                if(StringUtils.isEmpty(price)){
                    price = carrierMap.get(info.getCarrier())+":"+info.getCarrierPrice();
                }else{
                    price += ","+ carrierMap.get(info.getCarrier())+":"+info.getCarrierPrice();
                }
            }
            financeMap.put("price",price);
        }

        //??????????????????
        ResponseData<AccountInterfaceInfoValidator> interfaceInfoValidator = accountInterfaceService.findById(accountBasicInfoValidator.getAccountId());
        Map<String, Object> interfaceMap = new HashMap<>();
        List<Map<String, Object>> interfaceList = new ArrayList<>();
        if(!StringUtils.isEmpty(interfaceInfoValidator.getData())){
            AccountInterfaceInfoValidator interfaceInfo = interfaceInfoValidator.getData();
            interfaceMap.put("protocol",interfaceInfo.getProtocol());
            interfaceMap.put("accountPassword",DES.decrypt(interfaceInfo.getAccountPassword()));
            interfaceMap.put("maxSendSecond",interfaceInfo.getMaxSendSecond());
            //??????????????????:????????????IP
            if("WEB".equals(interfaceInfo.getProtocol()) || "HTTPS".equals(interfaceInfo.getProtocol())){
                ResponseData<ParameterExtendSystemParamValueValidator> systemParamValue = parameterExtendSystemParamValueFeignClient.findByBusinessTypeAndBusinessIdAndParamKey("SYSTEM_PARAM","SYSTEM","SYSTEM_PARAM_IP_HTTP");
                if(!StringUtils.isEmpty(systemParamValue.getData())){
                    interfaceMap.put("sysIp",systemParamValue.getData().getParamValue());
                }
            }
            if("CMPP".equals(interfaceInfo.getProtocol()) || "SGIP".equals(interfaceInfo.getProtocol()) || "SMGP".equals(interfaceInfo.getProtocol())){
                ResponseData<ParameterExtendSystemParamValueValidator> systemParamValue = parameterExtendSystemParamValueFeignClient.findByBusinessTypeAndBusinessIdAndParamKey("SYSTEM_PARAM","SYSTEM","SYSTEM_PARAM_IP_CMPP");
                if(!StringUtils.isEmpty(systemParamValue.getData())){
                    interfaceMap.put("sysIp",systemParamValue.getData().getParamValue());
                }
            }
            if("SMPP".equals(interfaceInfo.getProtocol())){
                ResponseData<ParameterExtendSystemParamValueValidator> systemParamValue = parameterExtendSystemParamValueFeignClient.findByBusinessTypeAndBusinessIdAndParamKey("SYSTEM_PARAM","SYSTEM","SYSTEM_PARAM_IP_SMPP");
                if(!StringUtils.isEmpty(systemParamValue.getData())){
                    interfaceMap.put("sysIp",systemParamValue.getData().getParamValue());
                }
            }

            if("WEB".equals(interfaceInfo.getProtocol())){
                Map<String, Object> map = new HashMap<>();
                map.put("interKey","??????????????????(???/???)");
                map.put("interValue",interfaceInfo.getMaxSubmitSecond());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("interKey","??????????????????");
                map1.put("interValue",interfaceInfo.getExecuteCheck().equals("1") ? "???" : "???");

                interfaceList.add(map);
                interfaceList.add(map1);
            }

            if("CMPP".equals(interfaceInfo.getProtocol()) || "SGIP".equals(interfaceInfo.getProtocol()) || "SMGP".equals(interfaceInfo.getProtocol())){
                Map<String, Object> map = new HashMap<>();
                map.put("interKey","????????????(????????????)");
                map.put("interValue",interfaceInfo.getSrcId());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("interKey","???????????????");
                map1.put("interValue",interfaceInfo.getMaxConnect());

               /* Map<String, Object> map2 = new HashMap<>();
                map2.put("interKey","??????????????????");
                map2.put("interValue",interfaceInfo.getExecuteCheck().equals("1") ? "???" : "???");

                Map<String, Object> map3 = new HashMap<>();
                map3.put("interKey","??????????????????");
                map3.put("interValue",interfaceInfo.getMatchingCheck().equals("1") ? "???" : "???");*/

                Map<String, Object> map4 = new HashMap<>();
                map4.put("interKey","????????????IP");
                map4.put("interValue",StringUtils.isEmpty(interfaceInfo.getIdentifyIp()) ? "?????????" : interfaceInfo.getIdentifyIp());

                interfaceList.add(map);
                interfaceList.add(map1);
                //interfaceList.add(map2);
                //interfaceList.add(map3);
                interfaceList.add(map4);
            }

            if("HTTPS".equals(interfaceInfo.getProtocol())){
                Map<String, Object> map = new HashMap<>();
                map.put("interKey","??????????????????(???/???)");
                map.put("interValue",interfaceInfo.getMaxSubmitSecond());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("interKey","??????????????????");
                map1.put("interValue",interfaceInfo.getExecuteCheck().equals("1") ? "???" : "???");

                Map<String, Object> map2 = new HashMap<>();
                map2.put("interKey","????????????IP");
                map2.put("interValue",StringUtils.isEmpty(interfaceInfo.getIdentifyIp()) ? "?????????" : interfaceInfo.getIdentifyIp());

                Map<String, Object> map3 = new HashMap<>();
                map3.put("interKey","????????????????????????");
                map3.put("interValue",interfaceInfo.getMoUrl());

                Map<String, Object> map4 = new HashMap<>();
                map4.put("interKey","????????????????????????");
                map4.put("interValue",interfaceInfo.getStatusReportUrl());

                interfaceList.add(map);
                interfaceList.add(map1);
                interfaceList.add(map2);
                interfaceList.add(map3);
                interfaceList.add(map4);
            }
        }

        //web????????????
        List<Map<String, Object>> webList = new ArrayList<>();
        EnterpriseWebAccountInfoValidator enterpriseWebAccountInfoValidator = new EnterpriseWebAccountInfoValidator();
        enterpriseWebAccountInfoValidator.setEnterpriseId(accountBasicInfoValidator.getEnterpriseId());
        ResponseData<List<EnterpriseWebAccountInfoValidator>> webData = this.enterpriseWebService.page(enterpriseWebAccountInfoValidator);
        if(!StringUtils.isEmpty(webData.getData()) && webData.getData().size()>0){
            for(EnterpriseWebAccountInfoValidator info:webData.getData()){
                Map<String, Object> map = new HashMap<>();
                map.put("webLoginName",info.getWebLoginName());
                String webPassword = "";
                if(!StringUtils.isEmpty(info.getAesPassword())){
                    webPassword = AESConstUtil.decrypt(info.getAesPassword());
                }
                map.put("webPassword",webPassword);
                webList.add(map);
            }
        }else{
            Map<String, Object> map = new HashMap<>();
            map.put("webLoginName","???");
            map.put("webPassword","???");
            webList.add(map);
        }

        //????????????????????????
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("transferType",accountBasicInfoValidator.getTransferType().equals("1") ? "???" : "???");
        String value = "";
        if(!"INTL".equals(accountBasicInfoValidator.getCarrier())){
            if(accountBasicInfoValidator.getRandomExtendCodeLength()>0){
                value = "???????????????????????????"+accountBasicInfoValidator.getRandomExtendCodeLength();
            }
            messageMap.put("extendCode",!StringUtils.isEmpty(accountBasicInfoValidator.getExtendCode())? "??????????????????" +accountBasicInfoValidator.getExtendCode() + value: "?????????????????????");
        }
        //??????????????????
        ResponseData<ParameterExtendSystemParamValueValidator> systemParamValue = parameterExtendSystemParamValueFeignClient.findByBusinessTypeAndBusinessIdAndParamKey("SYSTEM_PARAM","SYSTEM","MAX_SMS_TEXT_LENGTH");
        if(!StringUtils.isEmpty(systemParamValue.getData())){
            messageMap.put("messageLength",systemParamValue.getData().getParamValue());
        }

        //??????????????????
        List<Map<String, Object>> messageFiltersList = new ArrayList<>();
        ResponseData<List<ParameterExtendFiltersValueValidator>> data = this.parameterExtendFiltersValueService.findParameterValue(accountBasicInfoValidator.getAccountId());
        if (!StringUtils.isEmpty(data.getData()) && data.getData().size() > 0) {

            List<ParameterExtendFiltersValueValidator> filtersList = data.getData();
            //???????????????
            ServletContext context = request.getServletContext();
            Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");

            for (ParameterExtendFiltersValueValidator filters : filtersList) {
                //??????????????????????????????????????????????????????  ?????????????????????
                if("BLACK_WORD_FILTERING".equals(filters.getParamKey()) || "AUDIT_WORD_FILTERING".equals(filters.getParamKey()) || "BLACK_LIST_LEVEL_FILTERING".equals(filters.getParamKey())){
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                String values = getParamValues(filters, dictMap);
                map.put("filterKey",filters.getParamName());
                map.put("filterValue",values);

                messageFiltersList.add(map);
            }
        }

        buildMap.put("finance",financeMap);
        buildMap.put("interface",interfaceMap);
        buildMap.put("interfaceList",interfaceList);
        buildMap.put("webList",webList);
        buildMap.put("messageMap",messageMap);
        buildMap.put("messageFiltersList",messageFiltersList);

        return buildMap;
    }

    /**
     * ????????????
     */
    private Map<String,String> businessType(HttpServletRequest request) {
        Map<String, DictType> dictMap = (Map<String, DictType>) request.getServletContext().getAttribute("dict");
        //??????????????????
        DictType businessType = dictMap.get("businessType");

        Map<String,String> dictValueMap = new HashMap<>();
        for (Dict dict : businessType.getDict()) {
            dictValueMap.put(dict.getFieldCode(),dict.getFieldName());
        }
        return dictValueMap;
    }

    /**
     * ???????????? ??????????????????
     */
    private Map<String,String> carrier(HttpServletRequest request) {
        Map<String, DictType> dictMap = (Map<String, DictType>) request.getServletContext().getAttribute("dict");
        //?????????
        DictType carrier = dictMap.get("carrier");
        //????????????
        DictType internationalArea = dictMap.get("internationalArea");

        Map<String,String> dictValueMap = new HashMap<>();
        for (Dict dict : carrier.getDict()) {
            dictValueMap.put(dict.getFieldCode(),dict.getFieldName());
        }
        for (Dict dict : internationalArea.getDict()) {
            dictValueMap.put(dict.getFieldCode(),dict.getFieldName());
        }
        return dictValueMap;
    }

    private String getParamValues(ParameterExtendFiltersValueValidator filters, Map<String, DictType> dictMap) {

        String values = "";

        if (StringUtils.isEmpty(filters.getParamKey())) {
            return "";
        }

        //??????????????????????????????????????????????????????????????????FieldName
        SystemExtendBusinessParamValidator validator = new SystemExtendBusinessParamValidator();
        validator.setBusinessType("BUSINESS_ACCOUNT_FILTER");
        validator.setParamKey(filters.getParamKey());
        ResponseData<SystemExtendBusinessParamValidator> systemExtendBusinessParamValidator = systemExtendBusinessParameterFeignClient.findParamByBusinessTypeAndParamKey(validator);

        if (StringUtils.isEmpty(systemExtendBusinessParamValidator.getData())) {
            return "";
        }

        //??????
        if ("text".equals(systemExtendBusinessParamValidator.getData().getShowType())) {
            return filters.getParamValue();
        }

        //???????????????
        DictType dictType = dictMap.get(systemExtendBusinessParamValidator.getData().getDictEnable());
        List<Dict> dictList = dictType.getDict();
        if (StringUtils.isEmpty(dictList) && dictList.size() > 0) {
            return "";
        }

        //??????
        if ("select".equals(systemExtendBusinessParamValidator.getData().getShowType())) {
            for (Dict dict : dictList) {
                if (filters.getParamValue().equals(dict.getFieldCode())) {
                    values = dict.getFieldName();
                    return values;
                }
            }
        }

        //??????
        if ("checkbox".equals(systemExtendBusinessParamValidator.getData().getShowType())) {
            String[] params = filters.getParamValue().split(",");
            if (StringUtils.isEmpty(params) || params.length < 0) {
                return "";
            }

            for (int a = 0; a < params.length; a++) {
                for (int i = 0; i < dictList.size(); i++) {
                    Dict dict = dictList.get(i);
                    if (params[a].equals(dict.getFieldCode())) {
                        if (StringUtils.isEmpty(values)) {
                            values = dict.getFieldName();
                        } else {
                            values += "," + dict.getFieldName();
                        }
                        break;
                    }
                }
            }
        }

        return values;
    }

    /**
     * ????????????????????????????????????
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<AccountBasicInfoValidator>> accountByProtocol(PageParams<AccountBasicInfoValidator> pageParams) {
        try {
            PageList<AccountBasicInfoValidator> pageList = this.businessAccountFeignClient.accountByProtocol(pageParams);
            return ResponseDataUtil.buildSuccess(pageList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ???????????????????????????
     * @param pageParams
     * @return
     */
    public ResponseData<PageList<AccountSendStatisticModel>> queryAccountSendStatistics(PageParams<AccountSendStatisticModel> pageParams) {
        try {
            ResponseData<PageList<AccountSendStatisticModel>> pageList = this.businessAccountFeignClient.queryAccountSendStatistics(pageParams);
            return pageList;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }

    /**
     * ??????????????????
     * @param accountBasicInfoValidator
     * @return
     */
    public ResponseData<List<AccountBasicInfoValidator>> accountList(AccountBasicInfoValidator accountBasicInfoValidator) {
        try {
            ResponseData<List<AccountBasicInfoValidator>> list = this.businessAccountFeignClient.accountList(accountBasicInfoValidator);
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseDataUtil.buildError(e.getMessage());
        }
    }
}
