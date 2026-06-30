# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码
COPY . .

# 设置 API 基础路径（通过环境变量）
ARG VUE_APP_API_BASE_URL=http://localhost:8094
ENV VUE_APP_API_BASE_URL=${VUE_APP_API_BASE_URL}

# 构建生产版本
RUN npm run build

# 生产阶段 - 使用 Nginx 托管静态文件
FROM nginx:alpine

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建好的静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# Nginx 在后台运行
CMD ["nginx", "-g", "daemon off;"]
