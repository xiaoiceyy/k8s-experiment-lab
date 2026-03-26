# 04. Prometheus + Grafana + Alertmanager 监控栈

## 已完成内容
- 使用 kube-prometheus-stack 部署完整监控栈
- 接入 node-exporter、kube-state-metrics、metrics-server、ingress-nginx exporter
- Grafana 导入 JSON 仪表盘实时监控集群资源使用
- Alertmanager 配置网易邮箱实现内存使用率告警推送

## 部署命令
helm install prometheus prometheus-community/kube-prometheus-stack --namespace monitoring -f helm-values/prometheus-values.yaml

## Grafana 访问
http://192.168.187.128:32111 (admin / 050919)
已导入仪表盘：315、1860、9611

## Alertmanager 告警
已配置内存使用率 > 80% 触发网易邮箱告警
