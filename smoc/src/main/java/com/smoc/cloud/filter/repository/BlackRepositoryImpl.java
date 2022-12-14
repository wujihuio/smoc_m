package com.smoc.cloud.filter.repository;

import com.smoc.cloud.common.BasePageRepository;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.smoc.filter.ExcelModel;
import com.smoc.cloud.common.smoc.filter.FilterBlackListValidator;
import com.smoc.cloud.common.smoc.filter.FilterWhiteListValidator;
import com.smoc.cloud.common.smoc.message.MessageComplaintInfoValidator;
import com.smoc.cloud.common.smoc.message.model.ComplaintExcelModel;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.filter.rowmapper.BlackRowMapper;
import com.smoc.cloud.filter.rowmapper.ExcelModelRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单管理 数据库操作
 */
@Slf4j
public class BlackRepositoryImpl extends BasePageRepository {

    @Resource
    public JdbcTemplate jdbcTemplate;

    public PageList<FilterBlackListValidator> page(PageParams<FilterBlackListValidator> pageParams) {
        //组织查询条件
        FilterBlackListValidator filterBlackListValidator = pageParams.getParams();

        //查询sql
        StringBuffer sqlBuffer = new StringBuffer("select t.ID,t.GROUP_ID,t.NAME,t.MOBILE,t.STATUS,str_to_date(t.CREATED_TIME,'%Y-%m-%d %H:%i:%S')CREATED_TIME,t.CREATED_BY " +
                " from filter_black_list t where t.GROUP_ID=? ");

        List<Object> paramsList = new ArrayList<Object>();
        if (null != filterBlackListValidator) {
            paramsList.add(filterBlackListValidator.getGroupId().trim());

            if (!StringUtils.isEmpty(filterBlackListValidator.getEnterpriseId())) {
                sqlBuffer.append(" and t.ENTERPRISE_ID = ? ");
                paramsList.add(filterBlackListValidator.getEnterpriseId().trim());
            }

            if (!StringUtils.isEmpty(filterBlackListValidator.getName())) {
                sqlBuffer.append(" and t.NAME like ? ");
                paramsList.add("%" + filterBlackListValidator.getName().trim() + "%");
            }
            if (!StringUtils.isEmpty(filterBlackListValidator.getMobile())) {
                sqlBuffer.append(" and t.MOBILE like ? ");
                paramsList.add("%" + filterBlackListValidator.getMobile().trim() + "%");
            }
        }
        sqlBuffer.append(" order by t.CREATED_TIME desc,t.ID ");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        PageList<FilterBlackListValidator> pageList = this.queryByPageForMySQL(sqlBuffer.toString(), params, pageParams.getCurrentPage(), pageParams.getPageSize(), new BlackRowMapper());
        return pageList;
    }

    public void bathSave(FilterBlackListValidator filterBlackListValidator) {

        //每batchSize 分批执行一次
        int batchSize = 60000;
        Connection connection = null;
        PreparedStatement statement = null;
        List<ExcelModel> list = filterBlackListValidator.getExcelModelList();
        final String sql = "insert into filter_black_list(ID,ENTERPRISE_ID,GROUP_ID,NAME,MOBILE,IS_SYNC,STATUS,CREATED_BY,CREATED_TIME) values(?,?,?,?,?,?,?,?,now()) ";
        log.info(sql);
        log.info("[系统黑名单导入开始]数据：{}- 共{}条", System.currentTimeMillis(), list.size());
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            int i = 0;
            for (ExcelModel entry : list) {
                statement.setString(1, UUID.uuid32());
                statement.setString(2, filterBlackListValidator.getEnterpriseId());
                statement.setString(3, filterBlackListValidator.getGroupId());
                statement.setString(4, entry.getColumn2());
                statement.setString(5, entry.getColumn1());
                statement.setString(6, filterBlackListValidator.getIsSync());
                statement.setString(7, filterBlackListValidator.getStatus());
                statement.setString(8, filterBlackListValidator.getCreatedBy());
                statement.addBatch();
                i++;
                if (i % batchSize == 0) {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        log.info("[系统黑名单导入结束]数据：{}", System.currentTimeMillis());

    }

    public List<ExcelModel> excelModel(PageParams<FilterBlackListValidator> pageParams) {
        //组织查询条件
        FilterBlackListValidator filterBlackListValidator = pageParams.getParams();

        //查询sql
        StringBuffer sqlBuffer = new StringBuffer("select t.NAME,t.MOBILE from filter_black_list t where t.GROUP_ID=? ");

        List<Object> paramsList = new ArrayList<Object>();
        if (null != filterBlackListValidator) {
            paramsList.add(filterBlackListValidator.getGroupId().trim());
        }
        if (!StringUtils.isEmpty(filterBlackListValidator.getEnterpriseId())) {
            sqlBuffer.append(" and t.ENTERPRISE_ID = ? ");
            paramsList.add(filterBlackListValidator.getEnterpriseId().trim());
        }

        sqlBuffer.append(" order by t.CREATED_TIME desc,t.ID ");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        PageList<ExcelModel> pageList = this.queryByPageForMySQL(sqlBuffer.toString(), params, pageParams.getCurrentPage(), pageParams.getPageSize(), new ExcelModelRowMapper());
        return pageList.getList();
    }

    public void complaintBathSave(MessageComplaintInfoValidator messageComplaintInfoValidator, String groupComplaintId) {

        //每batchSize 分批执行一次
        int batchSize = 60000;
        Connection connection = null;
        PreparedStatement statement = null;

        List<ComplaintExcelModel> list = messageComplaintInfoValidator.getComplaintList();

        final String sql = "insert into filter_black_list(ID,ENTERPRISE_ID,GROUP_ID,NAME,MOBILE,IS_SYNC,STATUS,CREATED_BY,CREATED_TIME) values(?,?,?,?,?,?,?,?,now()) ";
        log.info(sql);
        log.info("[系统投诉黑名单导入开始]数据：{}- 共{}条", System.currentTimeMillis(), list.size());
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            int i = 0;
            for (ComplaintExcelModel entry : list) {
                statement.setString(1, UUID.uuid32());
                statement.setString(2, "SYSTEM");
                statement.setString(3, groupComplaintId);
                statement.setString(4, "");
                statement.setString(5, entry.getReportNumber());
                statement.setString(6, "0");
                statement.setString(7, "1");
                statement.setString(8, messageComplaintInfoValidator.getCreatedBy());
                statement.addBatch();
                i++;
                if (i % batchSize == 0) {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        log.info("[系统投诉黑名单导入结束]数据：{}", System.currentTimeMillis());

    }

    /**
     * 查询系统黑名单
     *
     * @return
     */
    public List<String> findSystemBlackList() {
        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select t.MOBILE from filter_black_list t  where t.ENTERPRISE_ID='SYSTEM' and IS_SYNC ='0' ");
        List<String> result = this.jdbcTemplate.queryForList(sqlBuffer.toString(), String.class);

        return result;

    }

    /**
     * 查询行业黑名单
     *
     * @return
     */
    public List<FilterBlackListValidator> findIndustryBlackList(){
        //查询sql
        //查询sql
        StringBuffer sqlBuffer = new StringBuffer("select t.ID,t.GROUP_ID,t.NAME,t.MOBILE,t.STATUS,str_to_date(t.CREATED_TIME,'%Y-%m-%d %H:%i:%S')CREATED_TIME,t.CREATED_BY " +
                " from filter_black_list t where t.ENTERPRISE_ID='INDUSTRY' and IS_SYNC ='0' ");
        List<FilterBlackListValidator> result = this.jdbcTemplate.query(sqlBuffer.toString(), new BlackRowMapper());

        return result;
    }

    /**
     * 更新系统黑名单状态
     * @param list
     */
    public void bathUpdate(List<String> list) {

        //每batchSize 分批执行一次
        Connection connection = null;
        PreparedStatement statement = null;
        final String sql = "update filter_black_list set IS_SYNC ='1' where MOBILE=? ";
        log.info(sql);
        log.info("[更新系统黑名单状态]数据：{}- 共{}条", System.currentTimeMillis(), list.size());
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            for (String mobile : list) {
                statement.setString(1, mobile);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        log.info("[更新系统黑名单状态]数据：{}", System.currentTimeMillis());

    }

    /**
     * 更新系统黑名单状态
     * @param list
     */
    public void bathUpdateIndustry(List<FilterBlackListValidator> list) {

        //每batchSize 分批执行一次
        Connection connection = null;
        PreparedStatement statement = null;
        final String sql = "update filter_black_list set IS_SYNC ='1' where MOBILE=? ";
        log.info(sql);
        log.info("[更新行业黑名单状态]数据：{}- 共{}条", System.currentTimeMillis(), list.size());
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            for (FilterBlackListValidator validator : list) {
                statement.setString(1, validator.getMobile());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        log.info("[更新行业黑名单状态]数据：{}", System.currentTimeMillis());

    }
}
