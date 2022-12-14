package com.smoc.cloud.customer.repository;

import com.google.gson.Gson;
import com.smoc.cloud.common.BasePageRepository;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.smoc.customer.qo.CarrierCount;
import com.smoc.cloud.common.smoc.customer.qo.ExportModel;
import com.smoc.cloud.common.smoc.customer.qo.ExportRegisterModel;
import com.smoc.cloud.common.smoc.customer.validator.AccountSignRegisterForFileValidator;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.customer.rowmapper.AccountSignRegisterForFileRowMapper;
import com.smoc.cloud.customer.rowmapper.ExportModelRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AccountSignRegisterForFileRepositoryImpl extends BasePageRepository {

    @Autowired
    public DataSource dataSource;

    /**
     * 分页查询
     *
     * @param pageParams
     * @return
     */
    public PageList<AccountSignRegisterForFileValidator> page(PageParams<AccountSignRegisterForFileValidator> pageParams) {
        //查询条件
        AccountSignRegisterForFileValidator qo = pageParams.getParams();

        //查询sql
        StringBuilder sqlBuffer = new StringBuilder("select");
        sqlBuffer.append("  t.ID");
        sqlBuffer.append(",t.REGISTER_TYPE");
        sqlBuffer.append(", t.REGISTER_SIGN_ID");
        sqlBuffer.append(", t.ACCOUNT");
        sqlBuffer.append(", t.CHANNEL_ID");
        sqlBuffer.append(", t.CHANNEL_NAME");
        sqlBuffer.append(", t.ACCESS_PROVINCE");
        sqlBuffer.append(", t.REGISTER_CARRIER");
        sqlBuffer.append(", t.REGISTER_CODE_NUMBER");
        sqlBuffer.append(", t.REGISTER_EXTEND_NUMBER");
        sqlBuffer.append(", t.REGISTER_SIGN");
        sqlBuffer.append(", t.NUMBER_SEGMENT");
        sqlBuffer.append(", t.REGISTER_EXPIRE_DATE");
        sqlBuffer.append(", t.REGISTER_STATUS");
        sqlBuffer.append(", t.UPDATED_BY");
        sqlBuffer.append(", DATE_FORMAT(t.UPDATED_TIME, '%Y-%m-%d %H:%i:%S')UPDATED_TIME");
        sqlBuffer.append(", DATE_FORMAT(t.CREATED_TIME, '%Y-%m-%d %H:%i:%S')CREATED_TIME");
        sqlBuffer.append("  from account_sign_register_for_file t");
        sqlBuffer.append("  where 1=1");

        List<Object> paramsList = new ArrayList<Object>();

        if (!StringUtils.isEmpty(qo.getAccount())) {
            sqlBuffer.append(" and t.ACCOUNT = ?");
            paramsList.add(qo.getAccount().trim());
        }

        if (!StringUtils.isEmpty(qo.getChannelId())) {
            sqlBuffer.append(" and t.CHANNEL_ID = ?");
            paramsList.add(qo.getChannelId().trim());
        }

        if (!StringUtils.isEmpty(qo.getRegisterCarrier())) {
            sqlBuffer.append(" and t.REGISTER_CARRIER = ?");
            paramsList.add(qo.getRegisterCarrier().trim());
        }

        if (!StringUtils.isEmpty(qo.getChannelName())) {
            sqlBuffer.append(" and t.CHANNEL_NAME like ?");
            paramsList.add("%" + qo.getChannelName().trim() + "%");
        }

        if (!StringUtils.isEmpty(qo.getRegisterCodeNumber())) {
            sqlBuffer.append(" and t.REGISTER_CODE_NUMBER = ?");
            paramsList.add(qo.getRegisterCodeNumber().trim());
        }

        if (!StringUtils.isEmpty(qo.getNumberSegment())) {
            sqlBuffer.append(" and t.NUMBER_SEGMENT like ?");
            paramsList.add("%" + qo.getNumberSegment().trim() + "%");
        }

        if (!StringUtils.isEmpty(qo.getRegisterExtendNumber())) {
            sqlBuffer.append(" and t.REGISTER_EXTEND_NUMBER = ?");
            paramsList.add(qo.getRegisterExtendNumber().trim());
        }

        if (!StringUtils.isEmpty(qo.getRegisterSign())) {
            sqlBuffer.append(" and t.REGISTER_SIGN like ?");
            paramsList.add("%" + qo.getRegisterSign().trim() + "%");
        }

        if (!StringUtils.isEmpty(qo.getRegisterStatus())) {
            sqlBuffer.append(" and t.REGISTER_STATUS = ?");
            paramsList.add(qo.getRegisterStatus().trim());
        }

        sqlBuffer.append(" order by t.REGISTER_STATUS asc,CREATED_TIME desc ");

        //根据参数个数，组织参数值
        Object[] params = new Object[paramsList.size()];
        paramsList.toArray(params);

        PageList<AccountSignRegisterForFileValidator> pageList = this.queryByPageForMySQL(sqlBuffer.toString(), params, pageParams.getCurrentPage(), pageParams.getPageSize(), new AccountSignRegisterForFileRowMapper());
        pageList.getPageParams().setParams(qo);

        return pageList;
    }

    /**
     * 根据运营商，统计未报备得条数
     *
     * @return
     */
    public List<CarrierCount> countByCarrier() {
        String sql = "select REGISTER_CARRIER CARRIER, count(REGISTER_CARRIER) COUNT from account_sign_register_for_file where REGISTER_STATUS='1' group by REGISTER_CARRIER";
        List<CarrierCount> carrierCounts = this.jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<CarrierCount>(CarrierCount.class));
        return carrierCounts;
    }

    /**
     * 查询导出数据
     *
     * @param pageParams
     * @return
     */
    public PageList<ExportModel> export(PageParams<ExportModel> pageParams) {

        //查询条件
        ExportModel qo = pageParams.getParams();
        if (StringUtils.isEmpty(qo.getRegisterCarrier())) {
            return null;
        }

        StringBuffer sqlBuffer = new StringBuffer("select ");
        sqlBuffer.append(" asr.ID");
        sqlBuffer.append(",asr.REGISTER_CODE_NUMBER");
        sqlBuffer.append(",asr.ACCESS_PROVINCE");
        sqlBuffer.append(",asr.ACCESS_CITY");
        sqlBuffer.append(",asr.REGISTER_ENTERPRISE");
        sqlBuffer.append(",asr.NUMBER_SEGMENT");
        sqlBuffer.append(",asr.REGISTER_SIGN");
        sqlBuffer.append(",asr.REGISTER_CARRIER");
        sqlBuffer.append(",sr.APP_NAME");
        sqlBuffer.append(",sr.SERVICE_TYPE");
        sqlBuffer.append(",sr.MAIN_APPLICATION");
        sqlBuffer.append(",esc.ID CERTIFY_ID");
//        sqlBuffer.append(",esc.REGISTER_ENTERPRISE_ID");
        sqlBuffer.append(",esc.REGISTER_ENTERPRISE_NAME");
        sqlBuffer.append(",esc.SOCIAL_CREDIT_CODE");
        sqlBuffer.append(",esc.BUSINESS_LICENSE");
        sqlBuffer.append(",esc.PERSON_LIABLE_NAME");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_TYPE");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_NUMBER");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_URL");
        sqlBuffer.append(",esc.PERSON_HANDLED_NAME");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_NUMBER");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_TYPE");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_URL");
        sqlBuffer.append(",esc.AUTHORIZE_CERTIFICATE");
        sqlBuffer.append(",esc.AUTHORIZE_START_DATE");
        sqlBuffer.append(",esc.AUTHORIZE_EXPIRE_DATE");
        sqlBuffer.append(",esc.POSITION");
        sqlBuffer.append(",esc.OFFICE_PHOTOS");
        sqlBuffer.append(" from account_sign_register_for_file asr,account_sign_register sr, enterprise_sign_certify esc");
        sqlBuffer.append(" where asr.REGISTER_SIGN_ID=sr.ID and sr.ENTERPRISE_ID = esc.ID and asr.REGISTER_STATUS='1' and asr.REGISTER_CARRIER=? order by esc.REGISTER_ENTERPRISE_NAME ");
        log.info("[export sql]:{}", sqlBuffer);

        //根据参数个数，组织参数值
        Object[] params = new Object[1];
        params[0] = qo.getRegisterCarrier();

        PageList<ExportModel> pageList = this.queryByPageForMySQL(sqlBuffer.toString(), params, pageParams.getCurrentPage(), pageParams.getPageSize(), new ExportModelRowMapper());
        pageList.getPageParams().setParams(qo);

        return pageList;
    }

    /**
     *  根据报备订单号查询导出数据
     *
     * @param registerOrderNo
     * @return
     */
    public PageList<ExportModel> query(PageParams pageParams,String registerOrderNo){
        //查询条件
        if (StringUtils.isEmpty(registerOrderNo)) {
            return null;
        }

        StringBuffer sqlBuffer = new StringBuffer("select ");
        sqlBuffer.append(" asr.ID");
        sqlBuffer.append(",asr.REGISTER_CODE_NUMBER");
        sqlBuffer.append(",asr.ACCESS_PROVINCE");
        sqlBuffer.append(",asr.ACCESS_CITY");
        sqlBuffer.append(",asr.REGISTER_ENTERPRISE");
        sqlBuffer.append(",asr.NUMBER_SEGMENT");
        sqlBuffer.append(",asr.REGISTER_SIGN");
        sqlBuffer.append(",asr.REGISTER_CARRIER");
        sqlBuffer.append(",sr.APP_NAME");
        sqlBuffer.append(",sr.SERVICE_TYPE");
        sqlBuffer.append(",sr.MAIN_APPLICATION");
        sqlBuffer.append(",esc.ID CERTIFY_ID");
//        sqlBuffer.append(",esc.REGISTER_ENTERPRISE_ID");
        sqlBuffer.append(",esc.REGISTER_ENTERPRISE_NAME");
        sqlBuffer.append(",esc.SOCIAL_CREDIT_CODE");
        sqlBuffer.append(",esc.BUSINESS_LICENSE");
        sqlBuffer.append(",esc.PERSON_LIABLE_NAME");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_TYPE");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_NUMBER");
        sqlBuffer.append(",esc.PERSON_LIABLE_CERTIFICATE_URL");
        sqlBuffer.append(",esc.PERSON_HANDLED_NAME");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_NUMBER");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_TYPE");
        sqlBuffer.append(",esc.PERSON_HANDLED_CERTIFICATE_URL");
        sqlBuffer.append(",esc.AUTHORIZE_CERTIFICATE");
        sqlBuffer.append(",esc.AUTHORIZE_START_DATE");
        sqlBuffer.append(",esc.AUTHORIZE_EXPIRE_DATE");
        sqlBuffer.append(",esc.POSITION");
        sqlBuffer.append(",esc.OFFICE_PHOTOS");
        sqlBuffer.append(" from account_sign_register_for_file asr,account_sign_register sr, enterprise_sign_certify esc");
        sqlBuffer.append(" where asr.REGISTER_SIGN_ID=sr.ID and sr.ENTERPRISE_ID = esc.ID  and asr.REGISTER_ORDER_NO=? order by esc.REGISTER_ENTERPRISE_NAME ");
        log.info("[export sql]:{}", sqlBuffer);

        //根据参数个数，组织参数值
        Object[] params = new Object[1];
        params[0] = registerOrderNo;

        PageList<ExportModel> pageList = this.queryByPageForMySQL(sqlBuffer.toString(), params, pageParams.getCurrentPage(), pageParams.getPageSize(), new ExportModelRowMapper());
        return pageList;
    }

    /**
     * 为导出数据生成报备订单号，并改变报备数据状态
     *
     * @param exportRegisterModel
     */
    public void register(ExportRegisterModel exportRegisterModel) {
        //log.info("[exportRegisterModel]:{}",new Gson().toJson(exportRegisterModel));
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            //保存报备导出记录
            StringBuffer insertSql = new StringBuffer("insert into account_sign_register_export_record(ID,REGISTER_ORDER_NO,CARRIER,REGISTER_NUMBER,REGISTER_STATUS,CREATED_BY,CREATED_TIME) ");
            insertSql.append("values(?,?,?,?,?,?,now())");
            stmt = conn.prepareStatement(insertSql.toString());
            stmt.setString(1, UUID.uuid32());
            stmt.setString(2, exportRegisterModel.getRegisterOrderNo());
            stmt.setString(3, exportRegisterModel.getCarrier());
            stmt.setInt(4, exportRegisterModel.getIds().size());
            stmt.setString(5, "2");
            stmt.setString(6, exportRegisterModel.getCreatedBy());
            stmt.execute();

            //更新报备状态及报备号
            StringBuffer sql = new StringBuffer();
            sql.append("update account_sign_register_for_file set REGISTER_ORDER_NO=?,REGISTER_STATUS=? where ID=? ");
            stmt = conn.prepareStatement(sql.toString());
            for (String id : exportRegisterModel.getIds()) {
                stmt.setString(1, exportRegisterModel.getRegisterOrderNo());
                stmt.setString(2, "2");
                stmt.setString(3, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != conn) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
                if (null != stmt) {
                    stmt.clearBatch();
                    stmt.clearParameters();
                    stmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
