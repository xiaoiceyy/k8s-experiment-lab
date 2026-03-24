pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: tools
    image: imranq2/docker-kubectl:latest
    command: ["sleep"]
    args: ["infinity"]
    securityContext:
      privileged: true
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-socket
    - mountPath: /root/.kube
      name: kube-config
  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
  - name: kube-config
    hostPath:
      path: /root/.kube
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
                    sh "docker logout 192.168.187.128:30080"
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
            echo '✅ 流水线全流程执行成功！'
        }
        failure {
            echo '❌ 执行失败，请查看日志'
        }
    }
}
