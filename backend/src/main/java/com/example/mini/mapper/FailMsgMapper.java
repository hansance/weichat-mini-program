package com.example.mini.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mini.entity.FailMsg;
import org.apache.ibatis.annotations.Mapper;

/**
 * 失败消息 Mapper
 */
@Mapper
public interface FailMsgMapper extends BaseMapper<FailMsg> {
}
