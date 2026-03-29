pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ["/bin/sh"]
    args: ["-c", "sleep 3600"]
    volumeMounts:
    - name: docker-config
      mountPath: /kaniko/.docker
  - name: kubectl
    image: bitnami/kubectl:latest
    command: ["/bin/sh"]
    args: ["-c", "sleep 3600"]
  volumes:
  - name: docker-config
    secret:
      secretName: harbor-docker-config
'''
        }
    }

    stages {
        stage('拉取代码') {
            steps {
                git branch: 'master', 
                    credentialsId: 'gitee-credential', 
                    url: 'https://gitee.com/fwuanyan/demo-nginx-app.git'
            }
        }
        
        stage('构建并推送镜像') {
            steps {
                container('kaniko') {
                    sh """
                    /kaniko/executor \
                      --dockerfile=${WORKSPACE}/Dockerfile \
                      --context=dir://${WORKSPACE} \
                      --destination=192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} \
                      --insecure \
                      --skip-tls-verify
                    """
                }
            }
        }
        
        stage('部署到 Kubernetes') {
            steps {
                container('kubectl') {
                    sh "kubectl set image deployment/demo-nginx nginx=192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} -n mycompany"
                    sh "kubectl rollout status deployment/demo-nginx -n mycompany"
                }
            }
        }
    }
    post {
        success {
            echo "✅ 镜像构建推送+部署成功！镜像标签：${BUILD_NUMBER}"
        }
        failure {
            echo "❌ 构建/部署失败，请检查日志！"
        }
    }
}
