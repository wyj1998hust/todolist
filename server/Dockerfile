# 使用阿里云镜像站的 Node.js 20 基础镜像 (解决国内下载 Docker Hub 镜像慢的问题)
FROM m.daocloud.io/docker.io/library/node:20-alpine

# 设置工作目录
WORKDIR /usr/src/app

# 复制 package.json 和 package-lock.json 到工作目录
# 这样做可以利用 Docker 的层缓存机制，只有在依赖变化时才重新安装
COPY package*.json ./

# 安装生产环境所需的依赖
RUN npm install --production

# 复制后端应用代码到工作目录
# .dockerignore 文件会确保 node_modules 等不必要的文件不会被复制进来
COPY . .

# 暴露后端服务运行的端口
EXPOSE 5000

# 定义容器启动时运行的命令
CMD [ "node", "index.js" ]
