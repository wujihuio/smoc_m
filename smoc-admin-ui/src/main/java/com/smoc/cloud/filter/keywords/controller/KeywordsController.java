package com.smoc.cloud.filter.keywords.controller;

import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.filter.ExcelModel;
import com.smoc.cloud.common.smoc.filter.FilterKeyWordsInfoValidator;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.filter.keywords.service.KeywordsService;
import com.smoc.cloud.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 关键词管理
 **/
@Slf4j
@Controller
@RequestMapping("/filter/keywords")
public class KeywordsController {

    @Autowired
    private KeywordsService keywordsService;

    @Autowired
    private SystemUserLogService systemUserLogService;

    /**
     * 关键字管理列表
     *
     * @return
     */
    @RequestMapping(value = "/{keywordsType}/{businessId}/list", method = RequestMethod.GET)
    public ModelAndView list(@PathVariable String keywordsType, @PathVariable String businessId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/keyword_list");

        //初始化数据
        PageParams<FilterKeyWordsInfoValidator> params = new PageParams<FilterKeyWordsInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);
        params.setParams(filterKeyWordsInfoValidator);

        //查询
        ResponseData<PageList<FilterKeyWordsInfoValidator>> data = keywordsService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * 关键字管理列表查询
     *
     * @return
     */
    @RequestMapping(value = "/{keywordsType}/page", method = RequestMethod.POST)
    public ModelAndView page(@PathVariable String keywordsType, @ModelAttribute FilterKeyWordsInfoValidator filterKeyWordsInfoValidator, PageParams pageParams, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/keyword_list");

        //分页查询
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
        pageParams.setParams(filterKeyWordsInfoValidator);

        ResponseData<PageList<FilterKeyWordsInfoValidator>> data = keywordsService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }


        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * 添加关键字
     *
     * @return
     */
    @RequestMapping(value = "/{keywordsType}/{businessId}/add", method = RequestMethod.GET)
    public ModelAndView add(@PathVariable String keywordsType, @PathVariable String businessId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/keyword_edit");

        view.addObject("op", "add");

        //初始化参数
        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setId(UUID.uuid32());
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        return view;

    }

    /**
     * 编辑关键字
     *
     * @return
     */
    @RequestMapping(value = "/{keywordsType}/edit/{id}", method = RequestMethod.GET)
    public ModelAndView sysEdit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/keyword_edit");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询数据
        ResponseData<FilterKeyWordsInfoValidator> data = keywordsService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("filterKeyWordsInfoValidator", data.getData());
        view.addObject("op", "edit");

        return view;

    }

    /**
     * 保存关键字
     *
     * @param op
     * @param request
     * @return
     */
    @RequestMapping(value = "/batchSave/{keywordsType}/{businessId}/{op}", method = RequestMethod.POST)
    public ModelAndView batchSave(@PathVariable String keywordsType,@PathVariable String businessId,@PathVariable String op,String keyWordsType, HttpServletRequest request) {
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");
        ModelAndView view = new ModelAndView("filter/keywords/keyword_edit_batch");

        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        List<FilterKeyWordsInfoValidator> filterKeyWords = new ArrayList<>();

        //初始化其他变量
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
            filterKeyWordsInfoValidator.setBusinessId(businessId);
            filterKeyWordsInfoValidator.setCreatedTime(new Date());
            filterKeyWordsInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {

        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //封装数据
        for (int i = 1; i <= 10; i++) {
            FilterKeyWordsInfoValidator validator = new FilterKeyWordsInfoValidator();
            String keyWords = request.getParameter("keyWords" + i);
            String keyDesc = request.getParameter("keyDesc" + i);
            if (!StringUtils.isEmpty(keyWordsType) && !StringUtils.isEmpty(keyWords)) {
                validator.setKeyWordsType(keyWordsType);
                validator.setKeyWords(keyWords);
                validator.setKeyDesc(keyDesc);
                filterKeyWords.add(validator);
            }
        }

        filterKeyWordsInfoValidator.setFilterKeyWordsList(filterKeyWords);

        //保存操作
        if (!StringUtils.isEmpty(filterKeyWords) && filterKeyWords.size() > 0) {

            //记录日志
            log.info("[关键词库管理][{}][{}]数据:{}", op, user.getUserName(), JSON.toJSONString(filterKeyWordsInfoValidator));

            ResponseData data = keywordsService.batchSave(filterKeyWordsInfoValidator, op);
            if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
                view.addObject("error", data.getCode() + ":" + data.getMessage());
                return view;
            }

            //保存操作记录
            if (ResponseCode.SUCCESS.getCode().equals(data.getCode()) && !"SYSTEM".equals(keywordsType)) {
                systemUserLogService.logsAsync(keywordsType, businessId, filterKeyWordsInfoValidator.getCreatedBy() , op, "添加关键词" , JSON.toJSONString(filterKeyWordsInfoValidator));
            }
        }

        if(!"SYSTEM".equals(keywordsType)){
            view.setView(new RedirectView("/filter/keywords/list/"+keywordsType+"/"+businessId, true, false));
            return view;
        }

        view.setView(new RedirectView("/filter/keywords/"+keywordsType+"/"+businessId+"/list", true, false));
        return view;
    }

    /**
     * 保存关键字
     *
     * @param op
     * @param request
     * @return
     */
    @RequestMapping(value = "/save/{keywordsType}/{businessId}/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute @Validated FilterKeyWordsInfoValidator filterKeyWordsInfoValidator, BindingResult result, @PathVariable String businessId,@PathVariable String op,@PathVariable String keywordsType, HttpServletRequest request) {
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");
        ModelAndView view = new ModelAndView("filter/keywords/keyword_edit");

        if(!"SYSTEM".equals(keywordsType)){
            view.setViewName("filter/keywords/account/keyword_edit_common");
        }

        //完成参数规则验证
        if (result.hasErrors()) {
            view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
            view.addObject("op", op);
            return view;
        }

        if("SUPER_WHITE".equals(filterKeyWordsInfoValidator.getKeyWordsType())){
            filterKeyWordsInfoValidator.setWaskKeyWords("");
        }

        //初始化其他变量
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
            filterKeyWordsInfoValidator.setBusinessId(businessId);
            filterKeyWordsInfoValidator.setCreatedTime(new Date());
            filterKeyWordsInfoValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            ResponseData<FilterKeyWordsInfoValidator> data = keywordsService.findById(filterKeyWordsInfoValidator.getId());
            filterKeyWordsInfoValidator.setCreatedTime(data.getData().getCreatedTime());
            filterKeyWordsInfoValidator.setUpdatedBy(user.getRealName());
            filterKeyWordsInfoValidator.setUpdatedTime(new Date());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }


        //保存操作
        ResponseData data = keywordsService.save(filterKeyWordsInfoValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //保存操作记录
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode()) && !"SYSTEM".equals(keywordsType)) {
            String flag = "add".equals(op)?"添加":"修改";
            systemUserLogService.logsAsync(keywordsType, businessId, filterKeyWordsInfoValidator.getCreatedBy() , op , flag+ (filterKeyWordsInfoValidator.getKeyWordsType().equals("CHECK")? "审核词":"敏感词") , JSON.toJSONString(filterKeyWordsInfoValidator));
        }

        //记录日志
        log.info("[{}][{}][{}]数据:{}", filterKeyWordsInfoValidator.getKeyWordsType().equals("CHECK")? "审核词管理":"敏感词管理",op, user.getUserName(), JSON.toJSONString(filterKeyWordsInfoValidator));

        if(!"SYSTEM".equals(keywordsType)){
            view.setView(new RedirectView("/filter/keywords/list/"+filterKeyWordsInfoValidator.getKeyWordsBusinessType()+"/"+filterKeyWordsInfoValidator.getBusinessId()+"/"+filterKeyWordsInfoValidator.getKeyWordsType(), true, false));
            return view;
        }

        view.setView(new RedirectView("/filter/keywords/"+keywordsType+"/"+businessId+"/list", true, false));
        return view;
    }

    /**
     * 删除关键字
     *
     * @return
     */
    @RequestMapping(value = "/deleteById/{id}", method = RequestMethod.GET)
    public ModelAndView deleteById(@PathVariable String id, HttpServletRequest request) {
        SecurityUser user = (SecurityUser)request.getSession().getAttribute("user");
        ModelAndView view = new ModelAndView("filter/keywords/keyword_edit");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询数据
        ResponseData<FilterKeyWordsInfoValidator> keyData = keywordsService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(keyData.getCode())) {
            view.addObject("error", keyData.getCode() + ":" + keyData.getMessage());
            return view;
        }

        //记录日志
        log.info("[{}][delete][{}]数据::{}",keyData.getData().getKeyWordsType().equals("CHECK")? "审核词管理":"敏感词管理", user.getUserName(), JSON.toJSONString(keyData.getData()));

        //删除操作
        ResponseData data = keywordsService.deleteById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        if(!"SYSTEM".equals(keyData.getData().getKeyWordsBusinessType())){
            view.setView(new RedirectView("/filter/keywords/list/"+keyData.getData().getKeyWordsBusinessType()+"/"+keyData.getData().getBusinessId()+"/"+keyData.getData().getKeyWordsType(), true, false));
            return view;
        }

        view.setView(new RedirectView("/filter/keywords/"+keyData.getData().getKeyWordsBusinessType()+"/"+keyData.getData().getBusinessId()+"/list", true, false));
        return view;

    }

    @RequestMapping(value = "/{keywordsType}/{businessId}/upFilesView", method = RequestMethod.GET)
    public ModelAndView upFilesView(@PathVariable String keywordsType,@PathVariable String businessId,HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/keyword_upfiles_view");

        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);

        return view;
    }

    /**
     *  关键字导入
     * @param request
     * @return
     */
    @RequestMapping(value = "/upFiles", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute FilterKeyWordsInfoValidator filterKeyWordsInfoValidator, BindingResult result, HttpServletRequest request) {
        SecurityUser user = (SecurityUser)request.getSession().getAttribute("user");

        ModelAndView view = new ModelAndView("filter/keywords/keyword_upfiles_view");

        if(!"SYSTEM".equals(filterKeyWordsInfoValidator.getKeyWordsBusinessType())){
            view.setViewName("filter/keywords/account/keyword_upfiles_common");
        }

        if(StringUtils.isEmpty(filterKeyWordsInfoValidator.getKeyWordsType())){
            FieldError err = new FieldError("关键词类型", "keyWordsType", "关键词类型不能为空");
            result.addError(err);
        }
        //完成参数规则验证
        if (result.hasErrors()) {
            view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
            return view;
        }

        /**
         * 获取文件信息
         */
        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = mRequest.getFile("file");
        if (file != null && file.getSize() > 0) {

            List<ExcelModel> list = FileUtils.readFile(file,"2");

            //批量保存
            if(!StringUtils.isEmpty(list) && list.size()>0){
                FilterKeyWordsInfoValidator validator = new FilterKeyWordsInfoValidator();
                validator.setKeyWordsBusinessType(filterKeyWordsInfoValidator.getKeyWordsBusinessType());
                validator.setBusinessId(filterKeyWordsInfoValidator.getBusinessId());
                validator.setKeyWordsType(filterKeyWordsInfoValidator.getKeyWordsType());
                validator.setExccelList(list);
                validator.setCreatedBy(user.getRealName());
                ResponseData data  = keywordsService.expBatchSave(validator);
                if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
                    view.addObject("error", data.getCode() + ":" + data.getMessage());
                    return view;
                }

                //保存操作记录
                if (ResponseCode.SUCCESS.getCode().equals(data.getCode()) && !"SYSTEM".equals(filterKeyWordsInfoValidator.getKeyWordsBusinessType())) {
                    systemUserLogService.logsAsync(filterKeyWordsInfoValidator.getKeyWordsBusinessType(), filterKeyWordsInfoValidator.getBusinessId(), filterKeyWordsInfoValidator.getCreatedBy() , "add", filterKeyWordsInfoValidator.getKeyWordsType().equals("CHECK")? "导入审核词":"导入敏感词" , JSON.toJSONString(validator));
                }
            }

            log.info("[{}][导入][{}]数据::{}",filterKeyWordsInfoValidator.getKeyWordsType().equals("CHECK")? "审核词管理":"敏感词管理", user.getUserName(), list.size());
        }

        if(!"SYSTEM".equals(filterKeyWordsInfoValidator.getKeyWordsBusinessType())){
            view.setView(new RedirectView("/filter/keywords/list/"+filterKeyWordsInfoValidator.getKeyWordsBusinessType()+"/"+filterKeyWordsInfoValidator.getBusinessId()+"/"+filterKeyWordsInfoValidator.getKeyWordsType(), true, false));
            return view;
        }

        view.setView(new RedirectView("/filter/keywords/"+filterKeyWordsInfoValidator.getKeyWordsBusinessType()+"/"+filterKeyWordsInfoValidator.getBusinessId()+"/list", true, false));

        return view;
    }

    /**
     * 关键字管理列表
     *
     * @return
     */
    @RequestMapping(value = "/center/{keywordsBussinessType}/{businessId}", method = RequestMethod.GET)
    public ModelAndView common(@PathVariable String keywordsBussinessType, @PathVariable String businessId, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_center");

        //初始化数据
        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsBussinessType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);

        return view;

    }

    /**
     * 关键字管理列表
     *
     * @return
     */
    @RequestMapping(value = "/list/{keywordsBussinessType}/{businessId}/{keywordsType}", method = RequestMethod.GET)
    public ModelAndView commonList(@PathVariable String keywordsBussinessType, @PathVariable String businessId, @PathVariable String keywordsType,HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_list_common");

        if("WHITE".equals(keywordsType)){
            view = new ModelAndView("filter/keywords/white/keyword_white_common");
        }

        //初始化数据
        PageParams<FilterKeyWordsInfoValidator> params = new PageParams<FilterKeyWordsInfoValidator>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsBussinessType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);
        filterKeyWordsInfoValidator.setKeyWordsType(keywordsType);
        params.setParams(filterKeyWordsInfoValidator);

        //查询
        ResponseData<PageList<FilterKeyWordsInfoValidator>> data = keywordsService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        //view.addObject("businessId", businessId);
       // view.addObject("keywordsType", keywordsType);

        return view;

    }

    @RequestMapping(value = "/commonPage", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute FilterKeyWordsInfoValidator filterKeyWordsInfoValidator, PageParams pageParams, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_list_common");

        if(filterKeyWordsInfoValidator.getKeyWordsType().contains("WHITE")){
            view = new ModelAndView("filter/keywords/white/keyword_white_common");
        }

        //分页查询
        pageParams.setParams(filterKeyWordsInfoValidator);

        ResponseData<PageList<FilterKeyWordsInfoValidator>> data = keywordsService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }
        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * 添加关键字
     *
     * @return
     */
    @RequestMapping(value = "/add/{keywordsBussinessType}/{businessId}/{keywordsType}", method = RequestMethod.GET)
    public ModelAndView commonadd(@PathVariable String keywordsBussinessType, @PathVariable String businessId,@PathVariable String keywordsType, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_edit_common");

        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setId(UUID.uuid32());
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keywordsBussinessType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);
        filterKeyWordsInfoValidator.setKeyWordsType(keywordsType);

        view.addObject("op", "add");
        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);

        return view;

    }

    /**
     * 编辑关键字
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_edit_common");

        //完成参数规则验证
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //查询数据
        ResponseData<FilterKeyWordsInfoValidator> data = keywordsService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("filterKeyWordsInfoValidator", data.getData());
        view.addObject("op", "edit");
        return view;

    }

    @RequestMapping(value = "/upFilesCommon/{keyWordsBusinessType}/{businessId}/{keyWordsType}", method = RequestMethod.GET)
    public ModelAndView upFilesCommon(@PathVariable String keyWordsBusinessType, @PathVariable String businessId, @PathVariable String keyWordsType, HttpServletRequest request) {

        ModelAndView view = new ModelAndView("filter/keywords/account/keyword_upfiles_common");

        FilterKeyWordsInfoValidator filterKeyWordsInfoValidator = new FilterKeyWordsInfoValidator();
        filterKeyWordsInfoValidator.setKeyWordsBusinessType(keyWordsBusinessType);
        filterKeyWordsInfoValidator.setBusinessId(businessId);
        filterKeyWordsInfoValidator.setKeyWordsType(keyWordsType);

        view.addObject("filterKeyWordsInfoValidator", filterKeyWordsInfoValidator);
        return view;
    }

}
