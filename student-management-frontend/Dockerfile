# 构建阶段
FROM node:20-alpine as builder

WORKDIR /app

# 复制 package.json 和 package-lock.json（如果存在）
COPY package.json package-lock.json* ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY . .

# 构建项目
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制 Nginx 配置
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 5173

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
