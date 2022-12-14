package com.smoc.cloud.auth.data.provider.repository;

import com.smoc.cloud.auth.data.provider.entity.BaseRole;
import com.smoc.cloud.auth.data.provider.entity.BaseUserRole;
import com.smoc.cloud.common.smoc.customer.qo.ServiceAuthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * 用户角色数据操作类
 * 2019/3/29 14:29
 */
public interface BaseUserRoleRepository extends CrudRepository<BaseUserRole, String>, JpaRepository<BaseUserRole, String> {

    List<BaseUserRole> findBaseUserRoleByUserId(String userId);

    /**
     * 批量保存用户角色关系
     *
     * @param baseUserRoles
     */
    void batchSave(List<BaseUserRole> baseUserRoles);

    /**
     * 根据用户ID 删除用户角色关系
     *
     * @param userId
     */
    void deleteByUserId(String userId);

    /**
     * 批量删除：根据用户id和角色id
     * @param uId
     * @param list
     */
    void batchDeleteByUserIdAndRoleId(String uId, List<BaseRole> list);

    /**
     * 批量保存web登录权限
     * @param serviceAuthInfo
     */
    void batchSaveWebAuth(ServiceAuthInfo serviceAuthInfo);
}
