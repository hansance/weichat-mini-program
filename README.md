# 微信小程序示例项目（SpringBoot + 小程序前后端分离）

## 项目说明

一个基于 Spring Boot 后端 + 微信小程序前端的家政服务预约示例，演示前后端分离的完整流程。

### 功能列表
- ✅ 微信小程序登录（获取openId）
- ✅ 首页服务列表（分类筛选）
- ✅ 服务详情展示
- ✅ 在线预约下单
- ✅ 模拟支付（开发环境）
- ✅ 我的订单列表
- ✅ 取消订单
- ✅ 订单超时自动取消（RabbitMQ 延迟消息）

## 项目结构

```
wechat-mini-example/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/example/mini/
│       ├── MiniApplication.java          # 启动类
│       ├── common/
│       │   ├── Result.java               # 统一响应
│       │   └── JwtUtil.java              # JWT工具类
│       ├── config/
│       │   ├── CorsConfig.java           # 跨域配置
│       │   ├── MybatisPlusConfig.java    # 分页插件
│       │   ├── AuthInterceptor.java      # JWT认证拦截器
│       │   └── WebConfig.java            # 拦截器注册
│       ├── controller/
│       │   ├── ServiceController.java    # 服务接口
│       │   ├── OrderController.java      # 订单接口
│       │   ├── PayController.java        # 支付接口（模拟）
│       │   └── LoginController.java      # 登录接口
│       ├── entity/
│       │   ├── HomeService.java          # 服务实体
│       │   └── Order.java                # 订单实体
│       ├── mapper/
│       │   ├── HomeServiceMapper.java
│       │   └── OrderMapper.java
│       └── service/
│           ├── HomeServiceService.java
│           ├── OrderService.java
│           └── WechatService.java        # 微信登录服务
├── miniprogram/                # 微信小程序前端
│   ├── app.js / app.json / app.wxss
│   └── pages/
│       ├── index/              # 首页-服务列表
│       ├── detail/             # 服务详情+下单
│       └── order/              # 我的订单
└── sql/
    └── init.sql                # 数据库初始化脚本
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动后端

```bash
cd backend

# 修改 src/main/resources/application.yml 中的数据库连接信息
# 修改微信小程序的 app-id 和 app-secret

mvn spring-boot:run
```

后端启动后访问 http://localhost:8080

### 3. 启动小程序

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 用微信开发者工具导入 `miniprogram/` 目录
3. 填入你的小程序 AppID（或使用测试号）
4. 在开发者工具的「详情 > 本地设置」中勾选「不校验合法域名」

---

## macOS 本地环境搭建（从零开始）

以下记录了在 macOS 上从零搭建开发环境时遇到的问题及解决方案。

### 前提条件

- macOS + Homebrew 已安装

### 步骤 1：安装 Java 11

```bash
brew install openjdk@11
sudo ln -sfn $(brew --prefix openjdk@11)/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
```

> ⚠️ 如果系统同时安装了多个 Java 版本（如 openjdk 26），需要在编译时显式指定 Java 11：
>
> ```bash
> export JAVA_HOME=$(brew --prefix openjdk@11)/libexec/openjdk.jdk/Contents/Home
> ```
>
> 否则 Lombok 会因为不兼容高版本 Java 而报错：
> `Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

### 步骤 2：安装 Maven

```bash
brew install maven
```

### 步骤 3：安装 MariaDB（MySQL 替代）

> ⚠️ `brew install mysql` 在某些 macOS + Xcode 版本下会编译失败（非 Tier 1 配置）。MariaDB 是 MySQL 的兼容替代品，安装更顺畅。

```bash
brew install mariadb
```

#### 初始化数据目录

MariaDB 安装后可能缺少数据目录，需手动初始化（注意使用完整路径，不要用 `~`）：

```bash
mkdir -p /Users/$(whoami)/.homebrew/var/mysql
mariadb-install-db --datadir=/Users/$(whoami)/.homebrew/var/mysql
```

#### 启动 MariaDB

```bash
brew services start mariadb
```

验证是否运行：

```bash
brew services info mariadb
# 应看到 Running: ✔
```

#### 连接注意事项

MariaDB 默认使用 unix socket 认证，root 用户可能无法直接通过 `mysql -u root` 连接。使用 MariaDB 自带的客户端：

```bash
# 使用 mariadb 客户端（自动识别正确的 socket 路径）
mariadb < sql/init.sql
```

> ⚠️ 不要用 `mysql -u root`，可能会报 `Access denied` 或找不到 socket (`/tmp/mysql.sock`)。

#### 设置 root 密码（供后端连接）

```bash
mariadb -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123'; FLUSH PRIVILEGES;"
```

### 步骤 4：初始化数据库

```bash
mariadb < sql/init.sql
```

### 步骤 5：启动后端

```bash
export JAVA_HOME=$(brew --prefix openjdk@11)/libexec/openjdk.jdk/Contents/Home
cd backend
mvn clean spring-boot:run
```

### 步骤 6：微信开发者工具配置

1. 导入项目时选择**项目根目录**（包含 `project.config.json` 的目录）
2. `project.config.json` 中必须包含 `"miniprogramRoot": "miniprogram/"`，否则会报 `app.json is not found in the project root directory`
3. 在「详情 → 本地设置」中：
   - 勾选「不校验合法域名」
   - 建议使用**稳定版基础库**（非 canary），canary 版本可能出现 `Cannot read property 'enableUpdateWxAppCode' of undefined` 错误

### 常见错误速查

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `app.json is not found in the project root directory` | `project.config.json` 缺少 `miniprogramRoot` | 添加 `"miniprogramRoot": "miniprogram/"` |
| `net::ERR_CONNECTION_REFUSED` | 后端未启动 | 启动后端：`mvn spring-boot:run` |
| `Cannot read property 'enableUpdateWxAppCode' of undefined` | 基础库版本不兼容 | 切换到稳定版基础库 |
| `ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag` | Lombok 与高版本 Java 不兼容 | `export JAVA_HOME` 指向 Java 11 |
| `Can't connect to local server through socket '/tmp/mysql.sock'` | 使用了 mysql 客户端连 MariaDB | 改用 `mariadb` 命令 |
| `Access denied for user 'root'@'localhost'` | MariaDB socket 认证限制 | 使用 `mariadb`（无 `-u root`）或设置密码后用 `mariadb -u root -proot123` |
| MariaDB `Running: ✘` | 数据目录未初始化 | 运行 `mariadb-install-db --datadir=<完整路径>` |
| `brew install mysql` 编译失败 | macOS 非 Tier 1 配置 | 改用 `brew install mariadb` |
| `http://localhost:8080 不在以下 request 合法域名列表中` | 使用真实 AppID 后域名校验默认开启 | 详情 → 本地设置 → 勾选「不校验合法域名」 |
| `Illegal character in query...the code is a mock one` | 游客模式下 wx.login 返回假 code，后端直接拼进 URL | 使用真实 AppID 导入项目，或改用 mock 登录 |
| 导入项目时 AppID 无法修改 | 项目已被缓存 | 关闭项目 → 从项目列表删除 → 重新导入；或退出工具后修改 `project.config.json` 再重开 |

## API 接口文档

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/login | 微信登录，传入code | 无 |
| GET  | /api/service/list?category= | 获取服务列表 | 无 |
| GET  | /api/service/page?pageNum=&pageSize=&category= | 分页查询服务 | 无 |
| GET  | /api/service/detail/{id} | 服务详情 | 无 |
| POST | /api/order/create | 创建订单 | Bearer Token |
| GET  | /api/order/my | 我的订单列表 | Bearer Token |
| POST | /api/order/cancel/{id} | 取消订单 | Bearer Token |
| POST | /api/pay/mock/{orderId} | 模拟支付（开发环境） | Bearer Token |

> 需要认证的接口必须在请求头中携带 `Authorization: Bearer <token>`，token 由登录接口返回。

## 调试指南

### 一、后端调试

#### 1. 启动前检查

```bash
cd backend

# 确认 MySQL 正在运行
mysql -u root -p -e "SELECT 1"

# 初始化数据库
mysql -u root -p < ../sql/init.sql
```

#### 2. 修改配置

编辑 `src/main/resources/application.yml`：
- 修改数据库连接地址、用户名、密码
- 填入真实的微信小程序 `app-id` 和 `app-secret`（开发阶段可暂不配置，使用 mock 登录）

#### 3. 启动后端

```bash
mvn spring-boot:run
```

#### 4. 用 curl 测试接口

```bash
# 测试服务列表
curl http://localhost:8080/api/service/list

# 测试分类筛选
curl http://localhost:8080/api/service/list?category=cleaning

# 测试服务详情
curl http://localhost:8080/api/service/detail/1

# 测试创建订单（需要 Bearer Token）
curl -X POST http://localhost:8080/api/order/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"serviceId":1,"contactName":"张三","contactPhone":"13800138000","address":"北京市朝阳区xxx","appointmentTime":"2026-08-01T10:00:00"}'

# 测试模拟支付（将订单状态从"待支付"改为"待服务"）
curl -X POST "http://localhost:8080/api/pay/mock/1" \
  -H "Authorization: Bearer <token>"

# 测试查询订单
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/order/my
```

> `<token>` 由登录接口 `/api/auth/login` 返回。开发阶段可使用 mock 登录获取（见「开发阶段跳过微信登录」章节）。

#### 5. 后端常见问题

| 问题 | 排查方法 |
|------|----------|
| 连不上数据库 | 检查 MySQL 是否启动，用户名密码是否正确 |
| 表不存在 | 确认已执行 `sql/init.sql` |
| 端口被占用 | `lsof -i :8080`，kill 进程或修改端口 |
| 启动报错找不到类 | 确认执行了 `mvn clean compile` |

---

### 二、小程序前端调试

#### 1. 安装微信开发者工具

下载地址：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html

#### 2. 导入项目

- 打开微信开发者工具 → 「导入项目」
- 目录选择 `miniprogram/` 文件夹
- AppID：填你在微信公众平台注册的小程序 AppID，或点「使用测试号」

#### 3. 关键设置（开发阶段必须）

在开发者工具中：「详情」→「本地设置」→ 勾选：
- ✅ **不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**

> 不勾选此项将无法请求 `http://localhost:8080`

#### 4. 调试面板说明

| 面板 | 用途 |
|------|------|
| Console | 查看 JS 错误和 `console.log` 输出 |
| Network | 查看网络请求，确认 URL、状态码、响应数据 |
| AppData | 实时查看页面 `data` 的值，排查数据绑定问题 |
| Storage | 查看 `openId`/`token` 是否正确存储 |

#### 5. 开发阶段跳过微信登录

如果没有真实的微信 AppID，可在 `app.js` 中临时 mock 登录：

```javascript
// 替换 login() 方法为：
login() {
  // 开发调试模式：直接使用模拟 openId
  const mockOpenId = 'dev_test_user_001'
  this.globalData.openId = mockOpenId
  wx.setStorageSync('openId', mockOpenId)
  wx.setStorageSync('token', 'mock-token')
  console.log('开发模式：使用模拟 openId =', mockOpenId)
}
```

---

### 三、前后端联调 Checklist

| # | 检查项 | 验证方法 |
|---|--------|----------|
| 1 | 后端已启动？ | 浏览器访问 http://localhost:8080/api/service/list 有数据返回 |
| 2 | 数据库有数据？ | 接口返回的 `data` 数组非空 |
| 3 | 小程序能请求到后端？ | Network 面板看请求状态为 200 |
| 4 | 勾选了不校验域名？ | 否则 localhost 请求会被拦截报错 |
| 5 | openId 已存储？ | Storage 面板确认 `openId` 有值 |
| 6 | 跨域正常？ | 后端 `CorsConfig` 已配置 `/api/**`，无需额外处理 |

---

### 四、真机调试

小程序真机无法直接访问 `localhost`，需要将后端暴露到公网：

#### 方案 A：部署到服务器

将后端打包部署到有公网 IP 的服务器，修改 `app.js` 中的 `baseUrl`。

#### 方案 B：内网穿透（推荐开发阶段使用）

```bash
# 使用 ngrok
ngrok http 8080
# 获得类似 https://abc123.ngrok.io 的公网地址

# 或使用 natapp（国内更快）
natapp -authtoken=your_token
```

获得公网地址后，修改 `app.js`：

```javascript
globalData: {
  baseUrl: 'https://abc123.ngrok.io',  // 替换为你的公网地址
  openId: '',
  token: ''
}
```

然后在微信开发者工具中点击「预览」或「真机调试」，用手机微信扫码即可。

---

### 五、生产部署注意事项

1. 将 `baseUrl` 改为正式域名（必须 HTTPS）
2. 在微信公众平台 → 开发管理 → 服务器域名中添加你的域名
3. 去掉「不校验合法域名」的勾选
4. 配置真实的微信 `app-id` 和 `app-secret`
5. 修改 `jwt.secret` 为高强度随机字符串（至少32字符），不要使用默认值
6. 登录接口已返回 JWT Token，确保 token 过期时间合理（默认24小时）

---

### 六、获取真实 AppID（非游客模式）

游客模式下 `wx.login` 返回的是模拟数据，无法获取真实的用户 `openId`。要实现真实登录，需注册小程序并获取 AppID。

#### 1. 注册小程序账号

1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 点击右上角「立即注册」→ 选择 **小程序**
3. 填写邮箱、密码，完成邮箱验证
4. 选择主体类型：
   - **个人**：身份证 + 手机号即可
   - **企业**：需要营业执照（支付功能必须企业主体）

#### 2. 获取 AppID 和 AppSecret

注册完成后登录微信公众平台：
- 左侧菜单 → **开发管理** → **开发设置**
- 页面顶部即可看到 **AppID(小程序ID)** 和 **AppSecret(小程序密钥)**

#### 3. 配置到项目中

修改 `project.config.json`：
```json
"appid": "wx1234567890abcdef"
```

修改后端 `backend/src/main/resources/application.yml`：
```yaml
wechat:
  mini:
    app-id: wx1234567890abcdef
    app-secret: your-actual-app-secret
```

#### 4. 恢复真实登录逻辑

将 `miniprogram/app.js` 中的 `login()` 方法恢复为真实登录：

```javascript
login() {
  wx.login({
    success: (res) => {
      if (res.code) {
        wx.request({
          url: `${this.globalData.baseUrl}/api/auth/login`,
          method: 'POST',
          data: { code: res.code },
          success: (loginRes) => {
            if (loginRes.data.code === 200) {
              this.globalData.openId = loginRes.data.data.openId
              this.globalData.token = loginRes.data.data.token
              wx.setStorageSync('openId', loginRes.data.data.openId)
              wx.setStorageSync('token', loginRes.data.data.token)
            }
          }
        })
      }
    }
  })
}
```

#### 5. 登录流程说明

```
小程序端                      后端                          微信服务器
   │                          │                               │
   │── wx.login() ──────────► │                               │
   │◄─ 返回 code ───────────  │                               │
   │                          │                               │
   │── POST /api/auth/login ─►│                               │
   │   { code: "xxx" }        │── GET code2session ──────────►│
   │                          │◄─ 返回 openId, session_key ── │
   │                          │                               │
   │◄─ { openId, token } ──── │                               │
```

---

### 七、接入微信支付

微信支付仅支持**企业主体**的小程序，个人主体无法使用。

#### 前提条件

| 条件 | 说明 |
|------|------|
| 企业主体小程序 | 个人小程序不支持支付 |
| 微信商户号 | 在 [微信商户平台](https://pay.weixin.qq.com/) 注册 |
| 小程序绑定商户号 | 在商户平台「产品中心」→「AppID 授权管理」中绑定 |
| API 密钥 & 证书 | 在商户平台「账户中心」→「API 安全」中设置 |

#### 1. 申请微信商户号

1. 访问 [微信商户平台](https://pay.weixin.qq.com/)
2. 点击「成为商家」，提交企业资质
3. 审核通过后获得：
   - **商户号 (mch_id)**
   - **API 密钥 (api_key)**
   - **API 证书 (apiclient_cert.pem / apiclient_key.pem)**

#### 2. 后端集成微信支付

在 `pom.xml` 中添加微信支付 SDK：

```xml
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-java</artifactId>
    <version>0.2.12</version>
</dependency>
```

在 `application.yml` 中添加商户配置：

```yaml
wechat:
  pay:
    mch-id: "1234567890"
    api-key: "your-api-v3-key"
    cert-path: "/path/to/apiclient_cert.pem"
    key-path: "/path/to/apiclient_key.pem"
    notify-url: "https://yourdomain.com/api/pay/notify"
```

后端支付核心流程（伪代码）：

```java
// 1. 创建订单后调用微信统一下单接口
public Map<String, String> createPayment(Order order) {
    // 调用微信 JSAPI 下单
    JsapiServiceExtension service = new JsapiServiceExtension.Builder().build();
    PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(
        new PrepayRequest()
            .setAppid("your-app-id")
            .setMchid("your-mch-id")
            .setDescription(order.getServiceName())
            .setOutTradeNo(order.getOrderNo())
            .setNotifyUrl("https://yourdomain.com/api/pay/notify")
            .setAmount(new Amount().setTotal(order.getAmount().intValue()))  // 单位：分
            .setPayer(new Payer().setOpenid(order.getOpenId()))
    );
    // 返回前端调起支付所需参数
    return response;
}

// 2. 支付结果回调通知
@PostMapping("/api/pay/notify")
public String payNotify(@RequestBody String body, HttpServletRequest request) {
    // 验签 → 解密 → 更新订单状态为已支付
    order.setStatus(1);  // 待服务
    orderMapper.updateById(order);
    return "SUCCESS";
}
```

#### 3. 小程序端调起支付

```javascript
// 下单后调用后端获取支付参数，然后调起微信支付
wx.request({
  url: `${app.globalData.baseUrl}/api/pay/create`,
  method: 'POST',
  data: { orderId: orderId },
  success: (res) => {
    const payParams = res.data.data
    wx.requestPayment({
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.package,       // 格式：prepay_id=xxx
      signType: payParams.signType,     // RSA
      paySign: payParams.paySign,
      success: () => {
        wx.showToast({ title: '支付成功' })
        // 刷新订单状态
      },
      fail: () => {
        wx.showToast({ title: '支付取消', icon: 'none' })
      }
    })
  }
})
```

#### 4. 支付流程图

```
小程序端              后端                    微信支付服务器
   │                  │                          │
   │─ 创建订单 ──────►│                          │
   │◄─ 返回 orderId ─ │                          │
   │                  │                          │
   │─ 请求支付参数 ──►│                          │
   │                  │── JSAPI 统一下单 ───────►│
   │                  │◄─ 返回 prepay_id ─────── │
   │◄─ 返回支付参数 ── │                          │
   │                  │                          │
   │─ wx.requestPayment() ─────────────────────►│
   │◄─ 支付结果 ─────────────────────────────── │
   │                  │                          │
   │                  │◄── 支付成功回调通知 ───── │
   │                  │── 更新订单状态           │
   │                  │── 返回 SUCCESS ─────────►│
```

#### 5. 开发阶段模拟支付

项目已内置模拟支付接口（`PayController.java`），无需商户号即可测试完整订单流程：

```java
// PayController.java — 仅限开发环境使用！生产环境必须替换为真实微信支付
@PostMapping("/api/pay/mock/{orderId}")
public Result<String> mockPay(@PathVariable Long orderId, @RequestParam String openId) {
    // 直接将订单状态改为已支付
    Order order = orderMapper.selectById(orderId);
    if (order != null && order.getOpenId().equals(openId)) {
        order.setStatus(1);  // 待服务
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("模拟支付成功");
    }
    return Result.error("订单不存在或无权操作");
}
```

小程序端已集成「去支付」按钮（`pages/order/order.js` 中的 `payOrder` 方法），点击后调用模拟支付接口。

curl 测试：
```bash
curl -X POST "http://localhost:8080/api/pay/mock/1?openId=dev_test_user_001"
```

#### 6. 从模拟支付切换到真实支付

当你具备企业主体 + 商户号后，按以下步骤切换：

1. 在 `pom.xml` 中添加 `wechatpay-java` 依赖（见上方步骤 2）
2. 在 `application.yml` 中配置商户号、API 密钥、证书路径
3. 将 `PayController.java` 中的 `mockPay` 方法替换为真实的 `createPayment` + `payNotify`
4. 将小程序 `order.js` 中的 `payOrder` 方法改为调用 `/api/pay/create/{orderId}`，并用返回参数调用 `wx.requestPayment()`
5. 确保回调地址 (`notify-url`) 为公网 HTTPS 地址

---

### 八、RabbitMQ 延迟消息与消息重试

项目集成了 RabbitMQ 实现订单超时自动取消和可靠消息发送。

#### 架构设计

```
用户下单 → 写入DB → 发送消息到 order.delay.queue（TTL=30分钟）
                         │
                         │ 30分钟后消息过期（进入死信队列）
                         ▼
                    order.cancel.queue（死信队列）
                         │
                         ▼
                  OrderTimeoutConsumer 消费
                    → 检查订单是否仍为"待支付"
                    → 是 → 自动取消订单
                    → 否 → 跳过（已支付/已取消）
```

#### 1. 安装 RabbitMQ

```bash
brew install rabbitmq
brew services start rabbitmq
```

验证运行状态：
```bash
brew services info rabbitmq
# 应看到 Running: ✔
```

管理界面：http://localhost:15672（用户名/密码: `guest`/`guest`）

#### 2. 核心组件说明

| 文件 | 说明 |
|------|------|
| `config/RabbitMqConfig.java` | 交换机、队列、绑定声明（TTL + 死信队列） |
| `mq/RabbitClient.java` | 消息发送客户端（重试3次 + 失败持久化） |
| `mq/OrderTimeoutMessage.java` | 订单超时消息体 |
| `mq/OrderTimeoutConsumer.java` | 死信队列消费者，自动取消超时订单 |
| `entity/FailMsg.java` | 失败消息实体 |
| `mapper/FailMsgMapper.java` | 失败消息 Mapper |

#### 3. 延迟队列方案（TTL + 死信队列）

```
                    order.delay.exchange
                          │
                          │ routing_key: order.delay
                          ▼
              ┌─────────────────────────┐
              │   order.delay.queue     │
              │   TTL = 30分钟           │
              │   DLX = order.dlx.exchange│
              └───────────┬─────────────┘
                          │ 消息过期后转发
                          ▼
                    order.dlx.exchange
                          │
                          │ routing_key: order.cancel
                          ▼
              ┌─────────────────────────┐
              │   order.cancel.queue    │
              │   (死信队列)             │
              │   消费者: OrderTimeoutConsumer
              └─────────────────────────┘
```

#### 4. 消息发送重试机制

```
发送消息 ──失败──► 重试第1次（等待3秒）
                    │
                  失败──► 重试第2次（等待4.5秒）
                           │
                         失败──► 重试第3次（等待6.75秒）
                                  │
                                失败──► 持久化到 fail_msg 表
                                        （后续可通过定时任务重发）
```

关键特性：
- **@Retryable**：最多重试3次，指数退避（3s × 1.5倍递增）
- **@Recover**：重试全部失败后，消息写入 `fail_msg` 数据库表
- **Publisher Confirm**：确认消息到达交换机
- **Publisher Returns**：消息无法路由时回调记录

#### 5. 配置说明

`application.yml` 中 RabbitMQ 相关配置：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated    # 发送确认
    publisher-returns: true               # 路由失败回调
    listener:
      simple:
        acknowledge-mode: auto            # 自动确认
        retry:
          enabled: true                   # 消费失败重试
          max-attempts: 3
          initial-interval: 3000
          multiplier: 1.5
```

#### 6. 数据库表

需要执行 `sql/init.sql` 创建 `fail_msg` 表：

```sql
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
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status_retry (status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息发送失败记录表';
```

#### 7. 修改超时时间

默认订单超时时间为 30 分钟，如需修改，编辑 `RabbitMqConfig.java`：

```java
/** 订单超时时间（毫秒），默认30分钟 */
public static final int ORDER_TIMEOUT_MS = 30 * 60 * 1000;
```

开发测试时可改为较短时间（如 1 分钟 = 60000）方便验证。

#### 8. 验证流程

```bash
# 1. 确认 RabbitMQ 运行中
brew services info rabbitmq

# 2. 创建订单
curl -X POST http://localhost:8080/api/order/create \
  -H "Content-Type: application/json" \
  -d '{"openId":"dev_test_user_001","serviceId":1,"contactName":"张三","contactPhone":"13800138000","address":"北京市朝阳区xxx","appointmentTime":"2026-08-01T10:00:00"}'

# 3. 查看 RabbitMQ 管理界面 (http://localhost:15672)
#    - Queues 标签页应看到 order.delay.queue 中有一条消息
#    - 30分钟后消息自动转到 order.cancel.queue 并被消费

# 4. 30分钟后查看订单状态（应变为 status=4 已取消）
curl http://localhost:8080/api/order/my?openId=dev_test_user_001
```

#### 9. 生产环境注意事项

| 项目 | 建议 |
|------|------|
| RabbitMQ 部署 | 使用云服务（如腾讯云 TDMQ、阿里云消息队列）或集群部署 |
| 消息持久化 | 已配置 `MessageDeliveryMode.PERSISTENT`，消息不会因重启丢失 |
| 失败消息重发 | 建议添加定时任务（如 XXL-JOB）扫描 `fail_msg` 表中待重发消息 |
| 监控告警 | 监控队列积压数、消费延迟，及时告警 |
| 死信队列 | 建议为消费异常消息单独设置 error queue 人工排查 |

---

### 九、微信云开发/云托管部署

使用微信云开发或云托管可免去服务器运维，按量付费，初期成本极低。

#### 两种模式对比

| 模式 | 说明 | 适合场景 | 代码改动 |
|------|------|----------|----------|
| **云开发（CloudBase）** | 无服务器，小程序直接调用云函数/云数据库 | 小型项目、快速原型 | 需重写后端为云函数 |
| **云托管（CloudRun）** | 托管你的后端容器（Spring Boot），自动扩缩容 | 已有后端代码、正式上线 | 仅需加 Dockerfile |

---

#### 方案 A：云开发（零服务器）

##### 开通步骤

1. 打开微信开发者工具 → 点击工具栏 **「云开发」** 按钮
2. 开通云开发环境（选择按量付费）
3. 获得云环境 ID（如 `cloud1-xxx`）

##### 配置小程序

`app.json` 中声明：
```json
{
  "cloud": true
}
```

`app.js` 中初始化：
```javascript
App({
  onLaunch() {
    wx.cloud.init({
      env: 'your-env-id',
      traceUser: true
    })
  }
})
```

##### 创建云函数

在项目根目录创建 `cloudfunctions/` 目录：

```
project-root/
├── miniprogram/
├── cloudfunctions/
│   ├── getServiceList/
│   │   ├── index.js
│   │   └── package.json
│   └── createOrder/
│       ├── index.js
│       └── package.json
```

`cloudfunctions/getServiceList/index.js`：
```javascript
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event) => {
  const { category } = event
  let query = db.collection('home_service').where({ status: 1 })
  if (category) {
    query = query.where({ category })
  }
  const res = await query.orderBy('create_time', 'desc').get()
  return { code: 200, data: res.data }
}
```

`cloudfunctions/createOrder/index.js`：
```javascript
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event) => {
  const wxContext = cloud.getWXContext()
  const openId = wxContext.OPENID  // 自动获取，无需登录接口

  const orderNo = Date.now().toString() + Math.random().toString(36).substr(2, 6)
  const serviceRes = await db.collection('home_service').doc(event.serviceId).get()
  const service = serviceRes.data

  const order = {
    orderNo,
    openId,
    serviceId: event.serviceId,
    serviceName: service.name,
    amount: service.price,
    contactName: event.contactName,
    contactPhone: event.contactPhone,
    address: event.address,
    appointmentTime: event.appointmentTime,
    status: 0,
    createTime: db.serverDate(),
    updateTime: db.serverDate()
  }

  await db.collection('service_order').add({ data: order })
  return { code: 200, data: order }
}
```

##### 小程序端调用

```javascript
// 获取服务列表
const res = await wx.cloud.callFunction({
  name: 'getServiceList',
  data: { category: 'cleaning' }
})

// 创建订单（openId 自动获取）
const res = await wx.cloud.callFunction({
  name: 'createOrder',
  data: {
    serviceId: 'xxx',
    contactName: '张三',
    contactPhone: '13800138000',
    address: '北京市朝阳区'
  }
})
```

##### 云开发免费额度

- 每月 5 万次云函数调用
- 2GB 数据库存储
- 5GB 文件存储
- 超出部分按量付费

---

#### 方案 B：云托管（推荐，复用现有后端）

将现有的 Spring Boot 后端打包为 Docker 容器，部署到微信云托管。

##### 开通步骤

1. 登录 [微信云托管控制台](https://cloud.weixin.qq.com/)
2. 用小程序管理员微信扫码
3. 创建环境 → 选择「按量付费」
4. 创建服务

##### 1. 创建 Dockerfile

`backend/Dockerfile`：
```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app
COPY target/wechat-mini-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

##### 2. 创建云托管配置

`backend/container.config.json`：
```json
{
  "containerPort": 8080,
  "dockerfilePath": "Dockerfile",
  "buildDir": "",
  "minNum": 0,
  "maxNum": 5,
  "cpu": 0.5,
  "mem": 1,
  "policyType": "cpu",
  "policyThreshold": 60
}
```

- `minNum: 0` — 无请求时缩容到 0 个实例（零费用）
- `maxNum: 5` — 高峰时最多 5 个实例

##### 3. 配置云数据库

在云托管控制台 →「附加资源」→ 开通 MySQL，获取内网地址后修改配置：

`application-prod.yml`：
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://<内网地址>:3306/wechat_mini_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: <云托管分配的密码>
  rabbitmq:
    host: <云 RabbitMQ 内网地址>
    port: 5672
    username: admin
    password: <密码>
```

##### 4. 部署方式

**方式 A：代码仓库自动部署（推荐）**
1. 将代码推送到 GitHub / Gitee
2. 在云托管控制台绑定仓库
3. 每次 push 自动构建部署

**方式 B：本地打包上传**
```bash
export JAVA_HOME=$(brew --prefix openjdk@11)/libexec/openjdk.jdk/Contents/Home
cd backend
mvn clean package -DskipTests
# 在云托管控制台上传 jar 或 Docker 镜像
```

##### 5. 小程序端调用云托管

```javascript
// 方式1：通过 wx.cloud.callContainer（免鉴权，自动注入 openId）
const res = await wx.cloud.callContainer({
  config: { env: 'your-env-id' },
  path: '/api/service/list',
  method: 'GET',
  header: { 'X-WX-SERVICE': 'your-service-name' }
})

// 方式2：直接 HTTPS 请求（需在微信公众平台配置域名）
wx.request({
  url: 'https://your-service.ap-shanghai.app.tcloudbase.com/api/service/list',
  success: (res) => { ... }
})
```

##### 云托管优势

- ✅ 直接复用现有 Spring Boot 代码
- ✅ 免购买/运维服务器
- ✅ 自动扩缩容（缩到 0 省钱）
- ✅ 内网访问 MySQL/Redis
- ✅ 自动 HTTPS，免域名备案
- ✅ `callContainer` 方式自动注入用户 openId

---

#### 费用对比

| 项目 | 自建服务器 | 云开发 | 云托管 |
|------|-----------|--------|--------|
| 月费用（低流量） | ~150-200元 | 免费额度内 0 元 | ~10-30元 |
| 月费用（中流量） | ~300-500元 | ~50-100元 | ~50-200元 |
| 运维成本 | 需自己管理 | 零运维 | 零运维 |
| 适合阶段 | 已上线稳定 | MVP/原型 | 正式上线 |
| 代码改动 | 无 | 需重写为云函数 | 几乎无（加Dockerfile） |

---

#### 推荐演进路径

```
本地开发（当前）→ 云托管上线 → 按需扩容
     │
     └─ 或快速验证 → 云开发（零成本原型）→ 后续迁移到云托管
```

---

### 十、Docker 本地构建与运行

项目已包含 `backend/Dockerfile`，可本地构建 Docker 镜像运行。

#### Dockerfile 说明

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/wechat-mini-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Duser.timezone=Asia/Shanghai", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

- `UseContainerSupport` — JVM 自动识别容器内存限制
- `MaxRAMPercentage=75%` — 最多使用容器 75% 内存
- 时区设置为 `Asia/Shanghai`

#### 构建与运行

```bash
# 1. 打包 jar
export JAVA_HOME=$(brew --prefix openjdk@11)/libexec/openjdk.jdk/Contents/Home
cd backend
mvn clean package -DskipTests

# 2. 构建 Docker 镜像
docker build -t wechat-mini-backend .

# 3. 运行 (docker run -d -p 8080:8080 --name mini-backend wechat-mini-backend)（连接本机 MySQL/MariaDB 和 RabbitMQ）
docker run -d \
  --name mini-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/wechat_mini_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  wechat-mini-backend

# 4. 查看日志
docker logs -f mini-backend

# 5. 停止/删除
docker stop mini-backend && docker rm mini-backend
```

> `host.docker.internal` 是 Docker Desktop for Mac 中访问宿主机的地址。

#### Docker Compose（一键启动全部服务）

创建 `docker-compose.yml`（项目根目录）：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: wechat_mini_demo
    ports:
      - "3306:3306"
    volumes:
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - rabbitmq
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/wechat_mini_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123
      SPRING_RABBITMQ_HOST: rabbitmq
```

一键启动：
```bash
# 先打包 jar
cd backend && mvn clean package -DskipTests && cd ..

# 启动所有服务
docker-compose up -d

# 查看状态
docker-compose ps

# 停止
docker-compose down
```
