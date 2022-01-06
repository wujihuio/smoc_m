package com.smoc.cloud.customer.service;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.smoc.cloud.auth.service.AuthorityService;
import com.smoc.cloud.common.auth.validator.BaseUserExtendsValidator;
import com.smoc.cloud.common.auth.validator.BaseUserValidator;
import com.smoc.cloud.common.auth.validator.OrgValidator;
import com.smoc.cloud.common.auth.validator.UserValidator;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseBasicInfoValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.customer.entity.EnterpriseBasicInfo;
import com.smoc.cloud.customer.repository.EnterpriseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Optional;

/**
 * 企业接入管理
 **/
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EnterpriseService {

    @Resource
    private EnterpriseRepository enterpriseRepository;

    @Resource
    private AuthorityService authorityService;

    /**
     * 查询列表
     *
     * @param pageParams
     * @return
     */
    public PageList<EnterpriseBasicInfoValidator> page(PageParams<EnterpriseBasicInfoValidator> pageParams) {
        return enterpriseRepository.page(pageParams);
    }

    /**
     * 根据id获取信息
     *
     * @param id
     * @return
     */
    public ResponseData findById(String id) {
        Optional<EnterpriseBasicInfo> data = enterpriseRepository.findById(id);

        if (!data.isPresent()) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_QUERY_ERROR);
        }

        EnterpriseBasicInfo entity = data.get();
        EnterpriseBasicInfoValidator enterpriseBasicInfoValidator = new EnterpriseBasicInfoValidator();
        BeanUtils.copyProperties(entity, enterpriseBasicInfoValidator);

        //查询上级企业的名称
        if(!"0000".equals(entity.getEnterpriseParentId())){
            Optional<EnterpriseBasicInfo> infoData = enterpriseRepository.findById(entity.getEnterpriseParentId());
            if (data.isPresent()) {
                enterpriseBasicInfoValidator.setParentEnterpriseName(infoData.get().getEnterpriseName());
            }
        }

        //转换日期
        enterpriseBasicInfoValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(entity.getCreatedTime()));

        return ResponseDataUtil.buildSuccess(enterpriseBasicInfoValidator);
    }

    /**
     * 保存或修改
     *
     * @param enterpriseBasicInfoValidator
     * @param op                           操作类型 为add、edit
     * @return
     */
    @Transactional
    public ResponseData<EnterpriseBasicInfo> save(EnterpriseBasicInfoValidator enterpriseBasicInfoValidator, String op) {

        Iterable<EnterpriseBasicInfo> data = enterpriseRepository.findByEnterpriseName(enterpriseBasicInfoValidator.getEnterpriseName());

        EnterpriseBasicInfo entity = new EnterpriseBasicInfo();
        BeanUtils.copyProperties(enterpriseBasicInfoValidator, entity);

        //add查重
        if (data != null && data.iterator().hasNext() && "add".equals(op)) {
            return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
        }
        //edit查重
        else if (data != null && data.iterator().hasNext() && "edit".equals(op)) {
            boolean status = false;
            Iterator iter = data.iterator();
            while (iter.hasNext()) {
                EnterpriseBasicInfo info = (EnterpriseBasicInfo) iter.next();
                if (!entity.getEnterpriseId().equals(info.getEnterpriseId())) {
                    status = true;
                    break;
                }
            }
            if (status) {
                return ResponseDataUtil.buildError(ResponseCode.PARAM_CREATE_ERROR);
            }
        }

        //转换日期格式
        entity.setCreatedTime(DateTimeUtils.getDateTimeFormat(enterpriseBasicInfoValidator.getCreatedTime()));

        //op 不为 edit 或 add
        if (!("edit".equals(op) || "add".equals(op))) {
            return ResponseDataUtil.buildError();
        }

        //记录日志
        log.info("[企业接入][企业开户信息][{}]数据:{}", op, JSON.toJSONString(entity));
        enterpriseRepository.saveAndFlush(entity);

        //添加企业开户、生成组织机构、生成用户（包含默认授权）
        if ("add".equals(op)) {
            saveOrg(entity);
        }

        return ResponseDataUtil.buildSuccess();
    }

    private void saveOrg(EnterpriseBasicInfo entity) {
        //系统管理中创建组织机构
        OrgValidator org = new OrgValidator();
        org.setId(entity.getEnterpriseId());
        org.setOrgName(entity.getEnterpriseName());
        org.setOrgCode("0000");
        //0000是一级企业
        if ("0000".equals(entity.getEnterpriseParentId())) {
            org.setParentId("12e309e47f144fc1b5b372c17a12e4d5");
            org.setIsLeaf(0);
        } else {
            org.setParentId(entity.getEnterpriseParentId());
            org.setIsLeaf(1);
        }
        org.setOrgLevel(0);
        org.setOrgType(0);
        org.setActive(1);
        org.setCreateDate(DateTimeUtils.getNowDateTime());
        org.setEditDate(DateTimeUtils.getNowDateTime());
        org.setSort(100);

        //新建组织机构
        ResponseData responseOrg = authorityService.saveOrg(org, "add");

        //记录日志
        log.info("[企业接入][企业开户信息-创建组织机构][{}]数据:{}", "add", JSON.toJSONString(responseOrg));
    }
/*

    private void saveOrgAndUser(EnterpriseBasicInfo entity) {

        //创建用户信息
        UserValidator user = new UserValidator();

        //设置角色
        user.setRoleIds("7da61ba9be8f4f6581c20f5a6d449b85");

        BaseUserValidator baseUser = new BaseUserValidator();
        String userId = UUID.uuid32();
        baseUser.setId(userId);
        baseUser.setUserName(entity.getEnterpriseContactsPhone());
        baseUser.setPassword("");
        baseUser.setPhone(entity.getEnterpriseContactsPhone());
        baseUser.setOrganization(entity.getEnterpriseId());
        baseUser.setCreateDate(DateTimeUtils.getNowDateTime());
        baseUser.setUpdateDate(DateTimeUtils.getNowDateTime());
        baseUser.setActive(1);
        user.setBaseUserValidator(baseUser);

        BaseUserExtendsValidator baseUserExtendsValidator = new BaseUserExtendsValidator();
        baseUserExtendsValidator.setId(userId);
        baseUserExtendsValidator.setRealName(entity.getEnterpriseContacts());
        baseUserExtendsValidator.setCorporation(entity.getEnterpriseName());
        baseUserExtendsValidator.setAdministrator(1);
        baseUserExtendsValidator.setWebChat("smoc-service");
        baseUserExtendsValidator.setType(4);
        user.setBaseUserExtendsValidator(baseUserExtendsValidator);

        //新建人员信息
        ResponseData responseUser = authorityService.saveUser(user, "add");
    }

*/

}
