package com.example.mini.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政服务实体
 */
@Data
@TableName("home_service")
public class HomeService {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 服务名称 */
    private String name;

    /** 服务分类：cleaning/repair/moving */
    private String category;

    /** 服务描述 */
    private String description;

    /** 服务价格 */
    private BigDecimal price;

    /** 服务图片URL */
    private String imageUrl;

    /** 服务时长（分钟） */
    private Integer duration;

    /** 状态：0-下架 1-上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
