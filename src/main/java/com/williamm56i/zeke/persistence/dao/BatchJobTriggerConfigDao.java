package com.williamm56i.zeke.persistence.dao;

import com.williamm56i.zeke.persistence.vo.BatchJobTriggerConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchJobTriggerConfigDao {

    String selectJobNameByBean(@Param("beanName") String beanName);

    List<BatchJobTriggerConfig> selectEnableJobTrigger();
}
