# K8s 实验平台

**唯一负责人**：xiaoiceyy

## 项目概述
使用 **Kubeadm** 在 CentOS 9 / Rocky Linux 9.3 搭建 **1-Master-2-Worker** Kubernetes 集群，并部署 Harbor、Helm、Prometheus、Loki、NFS、Jenkins 等全套组件，实现一站式云原生实验环境。

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

