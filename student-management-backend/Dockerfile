FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /build

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 使用 Maven 构建应用
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# 最终镜像
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 复制 JAR 文件
COPY --from=builder /build/target/student-management-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
