# 05. Ingress-Nginx + NFS + StorageClass

## 1. 部署 Ingress-Nginx（7层网关 + 灰度支持）
helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress-nginx -f helm-values/ingress-nginx-values.yaml

## 2. 部署 NFS + StorageClass（动态 PV）
helm install nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner --namespace nfs-provisioner -f helm-values/nfs-values.yaml

## 3. 测试
kubectl apply -f manifests/test-nfs-ingress.yaml

访问地址：http://test.k8s.local:30088 （需在本机 hosts 文件添加 192.168.187.128 test.k8s.local）

## 4. 灰度分流示例
后续可通过 nginx.ingress.kubernetes.io/canary: "true" 实现灰度发布。

