package com.smoc.cloud.http.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;

/**
 * 序列管理 数据库操作
 */
@Slf4j
public class SequenceRepositoryImpl {

    @Resource
    public JdbcTemplate jdbcTemplate;

    /**
     * 获得序列
     * @param seqName
     * @return
     */
    public Integer findSequence(String seqName){
        String sql = "select system_nextval('"+seqName+"')";
        Integer seq = jdbcTemplate.queryForObject(sql,Integer.class);
        return seq;
    }

}
