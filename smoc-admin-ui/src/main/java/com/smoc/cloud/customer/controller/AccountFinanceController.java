package com.smoc.cloud.customer.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.validator.AccountBasicInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountFinanceInfoValidator;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.AccountFinanceService;
import com.smoc.cloud.customer.service.BusinessAccountService;
import com.smoc.cloud.customer.service.EnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
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
        accountFinanceInfoValidator.setCarrierType("1");
        accountFinanceInfoValidator.setAccountCreditSum(new BigDecimal("0"));
        accountFinanceInfoValidator.setAccountId(data.getData().getAccountId());
        accountFinanceInfoValidator.setCarrier(data.getData().getCarrier());
        ResponseData<Map<String, BigDecimal>> map = accountFinanceService.editCarrierPrice(accountFinanceInfoValidator);
        view.addObject("list", map.getData());

        //查询账号配置的运营商价格
        ResponseData<List<AccountFinanceInfoValidator>> list = accountFinanceService.findByAccountId(accountFinanceInfoValidator);
        if (!StringUtils.isEmpty(list.getData())) {
            view.addObject("op", "edit");
            accountFinanceInfoValidator = list.getData().get(0);
        } else {
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
        if("2".equals(accountFinanceInfoValidator.getPayType()) && accountFinanceInfoValidator.getAccountCreditSum().compareTo(BigDecimal.ZERO) <=0){
            view.addObject("error", "如果选择后付费，授信额度必须大于0");
            return view;
        }

        //查询业务账号
        ResponseData<AccountBasicInfoValidator> data = businessAccountService.findById(accountFinanceInfoValidator.getAccountId());
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        String carrier = data.getData().getCarrier();

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
            systemUserLogService.logsAsync("ACCOUNT_FINANCE", accountFinanceInfoValidator.getAccountId(), "add".equals(op) ? user.getRealName() : user.getRealName(), op, "add".equals(op) ? "添加账号财务信息" : "修改账号财务信息", JSON.toJSONString(accountFinanceInfoValidator));
        }

        //记录日志
        log.info("[EC业务账号管理][业务账号财务信息][{}][{}]数据:{}", op, user.getUserName(), JSON.toJSONString(accountFinanceInfoValidator));

        view.setView(new RedirectView("/account/edit/finance/"+accountFinanceInfoValidator.getAccountId(), true, false));
        return view;
    }
}