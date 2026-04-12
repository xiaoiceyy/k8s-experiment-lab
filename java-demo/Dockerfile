FROM registry.cn-hangzhou.aliyuncs.com/xinghaik8s/tomcat:jdk8
LABEL author="xinghai"
RUN rm -rf /usr/local/tomcat/webapps/*
ADD target/ROOT /usr/local/tomcat/webapps/ROOT
