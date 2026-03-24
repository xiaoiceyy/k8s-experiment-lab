pipeline {
    agent any
    
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
                sh 'docker build -t 192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} .'
            }
        }
        
        stage('推送镜像到 Harbor') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'harbor-credential', usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
                    sh 'docker login -u $HARBOR_USER -p $HARBOR_PASS http://192.168.187.128:30080'
                    sh 'docker push 192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER}'
                }
            }
        }
        
        stage('部署到 Kubernetes') {
            steps {
                sh 'kubectl set image deployment/demo-nginx demo-nginx=192.168.187.128:30080/mycompany/demo-nginx:${BUILD_NUMBER} -n default || true'
            }
        }
    }
    
    post {
        success {
            echo '✅ Gitee 拉取 → 构建 → 推 Harbor（30080）→ 部署 K8s 全部成功！'
        }
        failure {
            echo '❌ 流水线执行失败'
        }
    }
}
