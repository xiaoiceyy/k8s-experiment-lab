
# 03. Loki + Grafana 部署

## Helm 安装

helm install loki grafana/loki-stack \

  --namespace monitoring --create-namespace \

  -f helm-values/loki-values.yaml

## Grafana 访问

http://节点IP:32000

用户名: admin

密码: 050919

## LogQL 示例

{job="promtail"} |= "error"

