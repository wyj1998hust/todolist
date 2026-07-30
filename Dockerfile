# --- 构建阶段 --- #
# 使用阿里云镜像站的 Node.js 20 基础镜像
FROM m.daocloud.io/docker.io/library/node:20-alpine AS build

# 设置工作目录
WORKDIR /usr/src/app

# 复制 package.json 和 lock 文件
COPY package*.json ./

# 安装所有依赖 (包括开发依赖)，并处理 React 19 的依赖冲突
RUN npm install --legacy-peer-deps

# 复制所有前端源代码
COPY . .

# 运行构建命令，生成生产环境的静态文件
RUN npm run build

# --- 服务阶段 --- #
# 使用阿里云镜像站的 Nginx 基础镜像
FROM m.daocloud.io/docker.io/library/nginx:stable-alpine

# 将构建阶段生成的 dist 目录下的所有文件复制到 Nginx 的默认网站根目录
COPY --from=build /usr/src/app/dist /usr/share/nginx/html

# （可选）如果你使用了前端路由（例如 React Router），需要配置 Nginx
# 这里我们先注释掉，因为当前应用是单页，但未来扩展时会需要
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 Nginx 默认的 HTTP 端口
EXPOSE 80

# Nginx 镜像会自动启动服务，我们不需要额外的 CMD
