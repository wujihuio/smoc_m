package com.smoc.cloud.customer.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.qo.Dict;
import com.smoc.cloud.common.auth.qo.DictType;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelBasicInfoValidator;
import com.smoc.cloud.common.smoc.configuate.validator.ChannelPriceValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountFinanceInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.configure.channel.service.ExcelListen;
import com.smoc.cloud.configure.channel.service.ExcelPriceData;
import com.smoc.cloud.customer.service.AccountFinanceService;
import com.smoc.cloud.customer.service.BusinessAccountService;
import com.smoc.cloud.customer.service.EnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务账号财务配置
 **/
@Slf4j
@RestController
@RequestMapping("/account")
public class AccountFinanceController {

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private AccountFinanceService accountFinanceService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    /**
     * 查询配置运营商价格
     *
     * @return
     */
    @RequestMapping(value = "/edit/finance/{accountId}", method = RequestMethod.GET)
    public ModelAndView finance(@PathVariable String accountId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account/account_edit_finance");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(accountId);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询业务账号
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //查询企业数据
        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(data.getData().getEnterpriseId());
        if (!ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())) {
            view.addObject("error", enterpriseData.getCode() + ":" + enterpriseData.getMessage());
        }

        //根据运营商和账号ID查询运营商单价
        AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(data.getData().getAccountId());

        //国际取国家代码
        if("INTL".equals(data.getData().getCarrier())){
            accountFinanceInfoValidator.setCarrier(data.getData().getCountryCode());
            accountFinanceInfoValidator.setCarrierType("2");//国际运营商

        }else{
            accountFinanceInfoValidator.setCarrier(data.getData().getCarrier());
            accountFinanceInfoValidator.setCarrierType("1");
        }
        ResponseData<Map<String, BigDecimal>> map = accountFinanceService.editCarrierPrice(accountFinanceInfoValidator);
        view.addObject("list", map.getData());

        //查询账号配置的运营商价格
        ResponseData<List<AccountFinanceInfoValidator>> list = accountFinanceService.findByAccountId(accountFinanceInfoValidator);
        if (!StringUtils.isEmpty(list.getData())) {
            view.addObject("op", "edit");
            accountFinanceInfoValidator = list.getData().get(0);
            if(!StringUtils.isEmpty(accountFinanceInfoValidator.getAccountCreditSum())){
                accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal(accountFinanceInfoValidator.getAccountCreditSum().stripTrailingZeros().toPlainString()));
            }
        } else {
            accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal("0"));
            accountFinanceInfoValidator.setFrozenReturnDate("1");//返还时间
            accountFinanceInfoValidator.setPayType("1");//付费方式
            accountFinanceInfoValidator.setChargeType("2");//计费方式
            view.addObject("op", "add");
        }

        view.addObject("accountFinanceInfoValidator", accountFinanceInfoValidator);
        view.addObject("accountBasicInfoValidator", data.getData());
        view.addObject("enterpriseBasicInfoValidator", enterpriseData.getData());
        return view;
    }

    /**
     * 保存财务信息
     * @param accountFinanceInfoValidator
     * @param result
     * @param op
     * @param request
     * @return
     */
    @RequestMapping(value = "/finance/save/{op}", method = RequestMethod.POST)
    public ModelAndView priceSave(@ModelAttribute @Validated AccountFinanceInfoValidator accountFinanceInfoValidator, BindingResult result, @PathVariable String op, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account/account_edit_finance");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //完成参数规则验证
        if (result.hasErrors()) {
            view.addObject("accountFinanceInfoValidator", accountFinanceInfoValidator);
            view.addObject("op", op);
            return view;
        }

        //如果是后付费，授信额度必须大于0
        /*if("2".equals(accountFinanceInfoValidator.getPayType()) && accountFinanceInfoValidator.getAccountCreditSum().compareTo(BigDecimal.ZERO) <=0){
            view.addObject("error", "如果选择后付费，授信额度必须大于0");
            return view;
        }*/
        if(StringUtils.isEmpty(accountFinanceInfoValidator.getAccountCreditSum())){
            accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal(0));
        }

        //查询业务账号
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountFinanceInfoValidator.getAccountId());
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //国际得区分字段查
        String carrier = "";
        if("INTL".equals(data.getData().getCarrier())){
            carrier = data.getData().getCountryCode();
        }else{
            carrier = data.getData().getCarrier();
        }

        //封装运营商价格数据
        List<AccountFinanceInfoValidator> list = new ArrayList<>();
        String[] carriers = carrier.split(",");
        for (int i = 0; i < carriers.length; i++) {
            AccountFinanceInfoValidator info = new AccountFinanceInfoValidator();
            info.setCarrier(carriers[i]);
            info.setCarrierPrice(new BigDecimal(request.getParameter(carriers[i])));
            list.add(info);
        }

        //保存数据
        accountFinanceInfoValidator.setPrices(list);
        accountFinanceInfoValidator.setCreatedBy(user.getRealName());
        ResponseData pricedata = accountFinanceService.save(accountFinanceInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(pricedata.getCode())) {
            view.addObject("error", pricedata.getCode() + ":" + pricedata.getMessage());
            return view;
        }

        //保存操作记录
        if (ResponseCode.SUCCESS.getCode().equals(pricedata.getCode())) {
            systemUserLogService.logsAsync("BUSINESS_ACCOUNT", accountFinanceInfoValidator.getAccountId(), "add".equals(op) ? user.getRealName() : user.getRealName(), op, "add".equals(op) ? "添加账号财务信息" : "修改账号财务信息", JSON.toJSONString(accountFinanceInfoValidator));
        }

        //记录日志
        log.info("[EC业务账号管理][业务账号财务信息][{}][{}]数据:{}", op, user.getUserName(), JSON.toJSONString(accountFinanceInfoValidator));

        view.setView(new RedirectView("/account/edit/finance/"+accountFinanceInfoValidator.getAccountId(), true, false));
        return view;
    }

    /**
     * 查看已配置单价
     * @param accountId
     * @param request
     * @return
     */
    @RequestMapping(value = "/finance/carrierPrice/{accountId}", produces = "text/html;charset=utf-8", method = RequestMethod.GET)
    public String carrierPrice(@PathVariable String accountId, HttpServletRequest request) {


        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(accountId);
        if (!MpmValidatorUtil.validate(validator)) {
            return MpmValidatorUtil.validateMessage(validator);
        }

        //查询业务账号
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            return "业务账号不存在";
        }

        //根据运营商和账号ID查询运营商单价
        AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(data.getData().getAccountId());
        accountFinanceInfoValidator.setCarrier(data.getData().getCarrier());
        ResponseData<List<AccountFinanceInfoValidator>> respList = accountFinanceService.findByAccountId(accountFinanceInfoValidator);

        List<AccountFinanceInfoValidator> list = respList.getData();
        if (StringUtils.isEmpty(list) || list.size() <= 0) {
            return "无配置资费";
        }

        //取字典数据
        ServletContext context = request.getServletContext();
        Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");
        DictType dictType  = dictMap.get("carrier");
        if("INTL".equals(data.getData().getCarrier())){
            dictType  = dictMap.get("internationalArea");
        }

        List<Dict> dictList = dictType.getDict();

        //封装已配置的运营商单价
        StringBuilder carrierPrices = new StringBuilder();
        for (int a=0;a<list.size();a++) {
            AccountFinanceInfoValidator info = list.get(a);
            String name = "";
            for (int i =0;i<dictList.size();i++) {
                Dict dict = dictList.get(i);
                if (info.getCarrier().equals(dict.getFieldCode())) {
                    name += dict.getFieldName();
                    break;
                }
            }
            carrierPrices.append(name+"："+info.getCarrierPrice()+"；");
            if (a != list.size()-1) {
                carrierPrices.append("@");
            }
        }

        return carrierPrices.toString();
    }

    /**
     * 进入国际价格导入页
     *
     * @return
     */
    @RequestMapping(value = "/finance/toImport/{accountId}", method = RequestMethod.GET)
    public ModelAndView importAccountPrice(@PathVariable String accountId) {

        ModelAndView view = new ModelAndView("customer/account/international/account_international_import_price");
        //查询账号是否存在
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountId);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        String supportAreaCodes = data.getData().getCountryCode();
        //如果区域为空直接返回
        if (StringUtils.isEmpty(supportAreaCodes)) {
            view.addObject("error", "未配置业务区域");
            return view;
        }

        //查询企业数据
        ResponseData<EnterpriseBasicInfoValidator> enterpriseData = enterpriseService.findById(data.getData().getEnterpriseId());
        if (!ResponseCode.SUCCESS.getCode().equals(enterpriseData.getCode())) {
            view.addObject("error", enterpriseData.getCode() + ":" + enterpriseData.getMessage());
        }

        //根据运营商和账号ID查询运营商单价
        AccountFinanceInfoValidator accountFinanceInfoValidator = new AccountFinanceInfoValidator();
        accountFinanceInfoValidator.setAccountId(data.getData().getAccountId());

        //国际取国家代码
        if("INTL".equals(data.getData().getCarrier())){
            accountFinanceInfoValidator.setCarrier(data.getData().getCountryCode());
            accountFinanceInfoValidator.setCarrierType("2");//国际运营商
        }else{
            accountFinanceInfoValidator.setCarrier(data.getData().getCarrier());
            accountFinanceInfoValidator.setCarrierType("1");
        }

        //查询账号配置的运营商价格
        ResponseData<List<AccountFinanceInfoValidator>> list = accountFinanceService.findByAccountId(accountFinanceInfoValidator);
        if (!StringUtils.isEmpty(list.getData())) {
            accountFinanceInfoValidator = list.getData().get(0);
            if(!StringUtils.isEmpty(accountFinanceInfoValidator.getAccountCreditSum())){
                accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal(accountFinanceInfoValidator.getAccountCreditSum().stripTrailingZeros().toPlainString()));
            }
        } else {
            accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal("0"));
            accountFinanceInfoValidator.setFrozenReturnDate("1");//返还时间
            accountFinanceInfoValidator.setPayType("1");//付费方式
            accountFinanceInfoValidator.setChargeType("2");//计费方式
        }

        view.addObject("accountFinanceInfoValidator", accountFinanceInfoValidator);
        view.addObject("enterpriseBasicInfoValidator", enterpriseData.getData());
        view.addObject("accountBasicInfoValidator", data.getData());
        return view;
    }

    /**
     * 国际价格导入
     *
     * @return
     */
    @RequestMapping(value = "/finance/import/save", method = RequestMethod.POST)
    public ModelAndView saveAccountPrice(@ModelAttribute AccountFinanceInfoValidator accountFinanceInfoValidator, @RequestPart("countriesFile") MultipartFile countriesFile, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("customer/account/international/account_international_import_price");


        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //查询通道是否存在
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountFinanceInfoValidator.getAccountId());
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //通道配置的国际
        String supportAreaCodes = data.getData().getCountryCode();
        //如果区域为空直接返回
        if (StringUtils.isEmpty(supportAreaCodes)) {
            view.addObject("error", "未配置业务区域");
            return view;
        }

        //读取国际短信 国家短信
        ServletContext context = request.getServletContext();
        Map<String, DictType> dictMap = (Map<String, DictType>) context.getAttribute("dict");
        Map<String, String> countries = new HashMap<>();
        String[] areas = supportAreaCodes.split(",");
        for (String area : areas) {
            countries.put(area, area);
        }

        ExcelListen excelListen = new ExcelListen();
        try {
            InputStream inputStream = countriesFile.getInputStream();
            String fileType = countriesFile.getOriginalFilename().substring(countriesFile.getOriginalFilename().lastIndexOf("."));
            if (!((".xlsx".equals(fileType) || ".xls".equals(fileType)))) {
                view.addObject("error", "文件类型错误！");
                return view;
            }

            EasyExcel.read(inputStream, ExcelPriceData.class, excelListen).sheet().doRead();

        } catch (Exception e) {
            e.printStackTrace();
            view.addObject("error", "文件导入错误！");
            return view;
        }

        if (null == excelListen || excelListen.result.size() < 1) {
            view.addObject("error", "导入数据为空！");
            return view;
        }

        //封装区域价格数据
        List<AccountFinanceInfoValidator> list = new ArrayList<>();
        for (ExcelPriceData excelPriceData : excelListen.result) {

            AccountFinanceInfoValidator info = new AccountFinanceInfoValidator();
            //excel中国家编码
            String countryNo = excelPriceData.getCountriesNo();
            if (StringUtils.isEmpty(countryNo)) {
                continue;
            }
            //通道配置的国家编码
            String countriesCode = countries.get(countryNo);
            if (StringUtils.isEmpty(countriesCode)) {
                continue;
            }
            info.setCarrier(countriesCode);

            String price = excelPriceData.getAccountPrice();
            if (StringUtils.isEmpty(price)) {
                continue;
            }
            //校验价格为数字
            String reg = "^[0-9]+(.[0-9]+)?$";
            if (!price.matches(reg)) {
                continue;
            }
            info.setCarrierPrice(new BigDecimal(price));

            list.add(info);
        }

        log.info("[list]:{}",new Gson().toJson(list));
        //保存数据
        accountFinanceInfoValidator.setPrices(list);
        accountFinanceInfoValidator.setCreatedBy(user.getRealName());
        ResponseData priceData = accountFinanceService.save(accountFinanceInfoValidator, "edit");
        if (!ResponseCode.SUCCESS.getCode().equals(priceData.getCode())) {
            view.addObject("error", priceData.getCode() + ":" + priceData.getMessage());
            return view;
        }

        //保存操作记录
        if (ResponseCode.SUCCESS.getCode().equals(priceData.getCode())) {
            systemUserLogService.logsAsync("BUSINESS_ACCOUNT", accountFinanceInfoValidator.getAccountId(), user.getRealName(), "edit", "修改区域计价配置", JSON.toJSONString(accountFinanceInfoValidator));
        }

        //记录日志
        log.info("[业务账号财务信息][区域计价导入][{}][{}]数据:{}", "edit", user.getUserName(), JSON.toJSONString(accountFinanceInfoValidator));

        view.setView(new RedirectView("/account/edit/finance/"+accountFinanceInfoValidator.getAccountId(), true, false));
        return view;
    }
}
