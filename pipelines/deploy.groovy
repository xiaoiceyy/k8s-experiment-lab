pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    command: ["sleep"]
    args: ["3600"]
    securityContext:
      privileged: true
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-socket
  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
'''
            defaultContainer 'jnlp'
        }
    }

    stages {
        stage('安装依赖工具') {
            steps {
                sh '''
                    apt-get update
                    apt-get install -y docker.io kubectl
                '''
            }
        }

        stage('拉取 Gitee 代码') {
            steps {
                git branch: 'main', 
                    credentialsId: 'gitee-credential', 
                    url: 'https://gitee.com/fwuanyan/demo-nginx-app.git'
            }
        }
        
        stage('构建 Docker 镜像') {
            steps {
                sh "docker build -t 192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} ."
            }
        }
        
        stage('推送镜像到 Harbor') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'harbor-credential', usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
                    sh "docker login 192.168.187.128:30080 -u ${HARBOR_USER} -p ${HARBOR_PASS}"
                    sh "docker push 192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER}"
                }
            }
        }
        
        stage('部署到 Kubernetes') {
            steps {
                sh "kubectl set image deployment/demo-nginx demo-nginx=192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} -n default"
            }
        }
    }
    
    post {
        success {
            echo '✅ 官方Pod创建成功！全流程完成！'
        }
        failure {
            echo '❌ 执行失败'
        }
    }
}
