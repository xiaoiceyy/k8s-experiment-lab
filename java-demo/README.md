# java-demo

# **在K8s上部署Java网站项目**
### **第一步：制作镜像**

像java，C，GO都是编译型语言，使用前需要编译。
解释型语言：php,python

```
# 安装环境
yum install java-1.8.0-openjdk maven -y
# 代码编译
mvn clean package
# 解压构建文件
unzip target/*.war -d target/ROOT
```

```
#修改maven源为阿里源
vi /etc/maven/settings.xml
查找mirror插入

<mirror>
<id>aliyun-maven</id>
<name>阿里云公共仓库</name>
<url>https://maven.aliyun.com/repository/public</url>
<mirrorOf>central</mirrorOf>
</mirror>

```


解压编译好的war包

```
unzip target/*.war -d target/ROOT

```

制作新镜像并推入镜像仓

```
docker build -t java-demo-web:v1 .
docker tag java-demo-web:v1 192.168.31.71/java-web-demo/java-demo-web:v1
docker push 192.168.31.71/java-web-demo/java-demo-web:v1

1.注：需要将harbor设置为信任仓库
{
  "insecure-registries": ["192.168.31.71"],
  "registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com","http://docker.1ms.run"],
  "exec-opts": ["native.cgroupdriver=systemd"]
}

2、将镜像仓库认证凭据保存在K8s Secret中
kubectl create secret docker-registry registry-auth --docker-username=admin --docker-password=Harbor12345 --docker-server=192.168.31.71

3、在yaml中使用这个认证凭据
      imagePullSecrets:
      - name: registry-auth

```

## **第二步：使用工作负载资源部署镜像**

```
#生成部署yaml
kubectl create deployment java-web-demo --image=192.168.31.71/java-web-demo/java-demo-web:v1 --replicas=2 --dry-run=client -o yaml > deployment.yaml

#创建service
kubectl expose deployment java-web-demo --port=80 --target-port=8080 --dry-run=client -o yaml > service.yaml
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
```

重启java服务生效

```
kubectl rollout restart deployment java-web-demo
```


