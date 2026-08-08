-- ============================================
-- 微信小程序示例项目 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS wechat_mini_demo DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wechat_mini_demo;

-- -------------------------------------------
-- 家政服务表
-- -------------------------------------------
DROP TABLE IF EXISTS home_service;
CREATE TABLE home_service (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '服务名称',
  category VARCHAR(50) NOT NULL COMMENT '分类: cleaning/repair/moving',
  description TEXT COMMENT '服务描述',
  price DECIMAL(10,2) NOT NULL COMMENT '服务价格',
  image_url VARCHAR(500) COMMENT '服务图片URL',
  duration INT DEFAULT 60 COMMENT '服务时长(分钟)',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务表';

-- -------------------------------------------
-- 订单表
-- -------------------------------------------
DROP TABLE IF EXISTS service_order;
CREATE TABLE service_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  open_id VARCHAR(100) NOT NULL COMMENT '用户openid',
  service_id BIGINT NOT NULL COMMENT '服务ID',
  service_name VARCHAR(100) COMMENT '服务名称',
  amount DECIMAL(10,2) COMMENT '订单金额',
  contact_name VARCHAR(50) COMMENT '联系人姓名',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  address VARCHAR(500) COMMENT '服务地址',
  appointment_time DATETIME COMMENT '预约时间',
  status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待支付 1-待服务 2-服务中 3-已完成 4-已取消',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_open_id (open_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -------------------------------------------
-- 插入示例数据
-- -------------------------------------------
INSERT INTO home_service (name, category, description, price, image_url, duration) VALUES
('日常保洁', 'cleaning', '专业保洁人员上门，全屋清洁消毒，包含厨房、卫生间深度清洁。适合日常维护。', 168.00, 'https://picsum.photos/400/300?random=1', 120),
('深度保洁', 'cleaning', '全屋360度无死角清洁，含玻璃擦拭、家具除尘、地板打蜡。适合新居入住或季度大扫除。', 388.00, 'https://picsum.photos/400/300?random=2', 240),
('开荒保洁', 'cleaning', '新房装修后全方位保洁，清除建筑垃圾、灰尘，让新家焕然一新。', 588.00, 'https://picsum.photos/400/300?random=3', 360),
('空调清洗', 'repair', '空调拆机深度清洗，高温杀菌除螨，还原清新空气，延长使用寿命。', 128.00, 'https://picsum.photos/400/300?random=4', 60),
('管道疏通', 'repair', '专业设备疏通厨房/卫生间管道堵塞，快速解决排水问题。', 98.00, 'https://picsum.photos/400/300?random=5', 45),
('热水器维修', 'repair', '各品牌电热水器/燃气热水器故障检测维修，上门服务。', 158.00, 'https://picsum.photos/400/300?random=6', 90),
('小型搬家', 'moving', '适合单身/情侣搬家，含2名搬运师傅+一辆小货车，市区内免费。', 299.00, 'https://picsum.photos/400/300?random=7', 180),
('家庭搬家', 'moving', '适合家庭搬迁，含4名搬运师傅+大货车，提供物品打包与拆装服务。', 699.00, 'https://picsum.photos/400/300?random=8', 360);

-- -------------------------------------------
-- 消息发送失败记录表
-- -------------------------------------------
DROP TABLE IF EXISTS fail_msg;
CREATE TABLE fail_msg (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  msg_id VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
  exchange VARCHAR(200) NOT NULL COMMENT '交换机',
  routing_key VARCHAR(200) NOT NULL COMMENT '路由键',
  msg_body TEXT NOT NULL COMMENT '消息体(JSON)',
  error_msg VARCHAR(500) COMMENT '错误信息',
  retry_count INT DEFAULT 0 COMMENT '已重试次数',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-待重发 1-重发成功 2-重发失败',
  next_retry_time DATETIME COMMENT '下次重试时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status_retry (status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息发送失败记录表';
