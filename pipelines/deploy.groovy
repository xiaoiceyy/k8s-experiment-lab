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
    securityContext:
      privileged: true
      runAsUser: 0
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-socket

  - name: tools
    image: docker:24.0.7-cli
    command: ["sleep"]
    args: ["3600"]
    securityContext:
      privileged: true
      runAsUser: 0
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-socket

  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
'''
            defaultContainer 'tools'
        }
    }

    stages {
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
                sh "apk add --no-cache kubectl"
                sh "kubectl set image deployment/demo-nginx demo-nginx=192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} -n default"
            }
        }
    }
    
    post {
        success {
            echo '✅ 全流程成功！'
        }
        failure {
            echo '❌ 执行失败'
        }
    }
}
