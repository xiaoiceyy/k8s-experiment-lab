# 02. Harbor 部署 & 配置

## Helm 安装
helm install harbor harbor/harbor \
  --namespace harbor --create-namespace \
  -f helm-values/harbor-values.yaml

## 访问地址
http://192.168.187.128:30080
用户名：admin
密码：050919（已自定义并修改）

## 配置步骤
1. 创建项目：mycompany (Public)
2. 创建机器人账号：jenkins-robot（权限：Push/Pull）
3. 测试推送镜像：
   docker login 192.168.187.128:30080 -u robot$jenkins-robot -p token
   docker tag nginx:alpine 192.168.187.128:30080/mycompany/nginx:alpine
   docker push 192.168.187.128:30080/mycompany/nginx:alpine

## 离线镜像包
已准备 harbor-offline-installer-v2.14.3.tgz 并导入所有节点

