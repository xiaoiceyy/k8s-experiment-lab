# 在K8s上部署Java网站项目
#下载java项目
git clone https://gitee.com/xinghaik8s/java-demo.git
# 安装环境
yum install java-1.8.0-openjdk maven -y
# 代码编译
mvn clean package
# 解压构建文件
unzip target/*.war -d target/ROOT

#修改maven源为阿里源
vi /etc/maven/settings.xml
查找mirror插入

<mirror>
<id>aliyun-maven</id>
<name>阿里云公共仓库</name>
<url>https://maven.aliyun.com/repository/public</url>
<mirrorOf>central</mirrorOf>
</mirror>

# 构建镜像
docker build -t java-demo:v1 .

# 打标签（使用你的 Harbor）
docker tag java-demo:v1 192.168.187.128/java-demo/java-demo:v1

# 推送镜像
docker push 192.168.187.128/java-demo/java-demo:v1

1.注：需要将harbor设置为信任仓库
{
  "insecure-registries": ["192.168.187.128：30080"],
  "registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com","http://docker.1ms.run"],
  "exec-opts": ["native.cgroupdriver=systemd"]
}

2、将镜像仓库认证凭据保存在K8s Secret中
kubectl create secret docker-registry registry-auth --docker-username=admin --docker-password=Harbor12345 --docker-server=192.168.187.128

3、在yaml中使用这个认证凭据
      imagePullSecrets:
      - name: registry-auth

第二步：使用工作负载资源部署镜像

#生成部署yaml
kubectl create deployment java-demo --image=192.168.187.128:30080/java-demo/java-demo:v1 --replicas=2 --dry-run=client -o yaml > deployment.yaml


#创建service
kubectl expose deployment java-demo --port=80 --target-port=8080 --dry-run=client -o yaml > service.yaml
为了方便从外部访问，可以把type改为NodePort

#创建configmap

apiVersion: v1
kind: ConfigMap
metadata:
  name: java-demo-config
data:
  application.yml: |
    server:
      port: 8080
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/test?characterEncoding=utf-8
        username: root
        password: "123456"
        driver-class-name: com.mysql.jdbc.Driver
      freemarker:
        allow-request-override: false
        cache: true
        check-template-location: true
        charset: UTF-8
        content-type: text/html; charset=utf-8
        expose-request-attributes: false
        expose-session-attributes: false
        expose-spring-macro-helpers: false
        suffix: .ftl
        template-loader-path: classpath:/templates/
        
   #在deployment上增加挂在卷

        volumeMounts:
        - name: config
          mountPath: /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/application.yml
          subPath: application.yml
      volumes:
      - name: config
        configMap:
          name: java-demo-config
          items:
          - key: application.yml
            path: application.yml



#重启java服务生效

kubectl rollout restart deployment java-demo

#初始化数据库：

kubectl cp user.sql java-demo-db-xxxxxxxxxx:/


#测试负载均衡,在node节点

1. 安装 EPEL 仓库：
yum -y install epel-release
2. 再次尝试安装 Nginx：
yum -y install nginx

设置负载均衡
upstream ingress-controller {
    server 192.168.187.128:32295;
    server 192.168.187.139:32295;
}

server {
    listen 8888;
    server_name _;

    location / {
        proxy_pass http://ingress-controller;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

