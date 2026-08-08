FROM openjdk:11-jre-slim

WORKDIR /app

# 复制构建产物
COPY target/wechat-mini-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动参数：时区设置 + JVM 优化
ENTRYPOINT ["java", \
  "-Duser.timezone=Asia/Shanghai", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
