# K8s 实验平台

**唯一负责人**：xiaoiceyy

## 项目概述
使用 **Kubeadm** 在 CentOS 9 / Rocky Linux 9.3 搭建 **1-Master-2-Worker** Kubernetes 集群，并部署 Harbor、Helm、Prometheus、Loki、NFS、Jenkins 等全套组件，实现一站式云原生实验环境。

## 项目结构说明
- k8s-experiment-lab/
- ├── README.md                 ← 主文档（已完成步骤 + 架构图）
- ├── docs/                     ← 详细操作流程（每一步单独 md 文件）
- │    ├── 01-kubeadm-init.md
- │    ├── 02-flannel.md
- │    ├── 03-harbor.md
- │    └── ...
- ├── manifests/                ← 所有自定义 YAML（Ingress、StorageClass 等）
- ├── helm-values/              ← 所有 Helm values.yaml 自定义配置
- ├── scripts/                  ← Groovy Pipeline、备份脚本等
- ├── pipelines/                ← Jenkins Groovy 文件
- └── architecture.png          ← 集群架构图（后面画）


## 集群基础信息
- Kubernetes 版本：**v1.28.0**
- 节点架构：1 Master + 2 Worker
- 容器运行时：containerd
- 网络插件：Flannel
- OS：CentOS 9

## 步骤：
### 1. 基础集群搭建（已完成）
- 在 3 台 CentOS 9 节点安装 containerd + kubeadm + kubectl + kubelet (v1.28.0)
- Master 执行 `kubeadm init --kubernetes-version=v1.28.0`
- 2 个 Worker 执行 `kubeadm join`
- 安装 Flannel CNI 网络插件
- 验证：`kubectl get nodes` 显示全部 **Ready**
- <img width="1571" height="273" alt="image" src="https://github.com/user-attachments/assets/1afa6880-898f-4e57-8f29-cce5f2666fd7" />



## 计划
1. 部署 Harbor 私有镜像仓库 + Jenkins + Groovy Pipeline 实现自动化构建推送
2. 部署 Loki + Grafana（Service 改为 NodePort）
3. 部署 Prometheus + Grafana + Alertmanager 全套监控（含 node-exporter、kube-state-metrics 等）
4. 部署 Ingress-Nginx 实现 7 层入口 + 灰度发布
5. 部署 NFS Provisioner 并创建 StorageClass 实现动态 PV


## 项目结构
- `docs/`          → 每一步详细部署文档
- `manifests/`     → 自定义 YAML 文件
- `helm-values/`   → 所有 Helm values.yaml 自定义配置
- `scripts/`       → 脚本和 Groovy Pipeline
- `pipelines/`     → Jenkins Pipeline 文件




## 如何本地复现
```bash
git clone https://github.com/xiaoiceyy/k8s-experiment-lab.git
cd k8s-experiment-lab
# 参考 docs/ 目录逐步执行


### 已完成步骤
# 下载并安装 Helm v3
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh && ./get_helm.sh

# 验证安装
helm version

#部署 Harbor 私有镜像仓库
# 添加 Harbor Helm 仓库
helm repo add harbor https://helm.goharbor.io
helm repo update

# 部署 Harbor（使用已准备好的 values 文件）
helm install harbor harbor/harbor \
  --namespace harbor --create-namespace \
  -f helm-values/harbor-values.yaml

# 添加 Jenkins Helm 仓库
helm repo add jenkins https://charts.jenkins.io
helm repo update

# 部署 Jenkins（使用已准备好的 values 文件）
helm install jenkins jenkins/jenkins \
  --namespace jenkins --create-namespace \
  -f helm-values/jenkins-values.yaml

#部署 Loki + Grafana 日志系统
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

helm install loki grafana/loki \
  --namespace monitoring --create-namespace \
  -f helm-values/loki-values.yaml


#部署 Prometheus + Grafana + Alertmanager 监控栈
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  -f helm-values/prometheus-values.yaml

# 部署 Ingress-Nginx（7层网关 + 支持灰度）
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx --create-namespace \
  -f helm-values/ingress-nginx-values.yaml

# 部署 NFS 动态 Provisioner 并创建 StorageClass
helm install nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  --namespace nfs-provisioner --create-namespace \
  -f helm-values/nfs-values.yaml


## 6. 在K8s上部署Java网站项目
- 使用 Maven 打包 Spring Boot / Tomcat WAR 项目
- 通过 Harbor 推送镜像 + imagePullSecrets
- 使用 Deployment + ConfigMap + NodePort 部署
- 详细步骤见 [docs/06-java-web-deployment.md](./docs/06-java-web-deployment.md)
