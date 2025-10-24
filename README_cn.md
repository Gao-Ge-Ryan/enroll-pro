[English version](README.md) | 中文版 

<img src="imgs/logo.png" width="600px">

[![LICENSE](https://img.shields.io/github/license/Gao-Ge-Ryan/enroll-pro)](/LICENSE)
[![Releases](https://img.shields.io/github/v/release/Gao-Ge-Ryan/enroll-pro)](https://github.com/Gao-Ge-Ryan/enroll-pro/releases/latest)
[![docker pulls](https://img.shields.io/docker/pulls/gaogegaogle/gao-ge-ryan)](https://hub.docker.com/repositories/gaogegaogle)
[![website](https://img.shields.io/badge/官网-blue)](https://www.enrollpro.top)
[![Contact Me](https://img.shields.io/badge/联系我们-QQ：3300755918-blue)](https://www.enrollpro.top)

## Enroll Pro: 可定制化报名系统

## 简介

可定制化的报名系统，适用于考试报名系统，活动报名系统等任何报名形式的场景，可用于生产、企业级别的项目。 项目和代码经过严格审核测试，支持私有化部署。

## 系统技术架构

- 后端: Java:8、SpringBoot:2.5.15、Mybatis、Gradle、Minio
- 前端：Vue3
- 数据库：Mysql:8.0.21、Redis:latest
- 部署: Docker、Docker-compose

## 快速开始

### 前置条件

运行系统服务器前置条件如下：

- 操作系统 = Centos、Ubuntu、Debian
- 系统硬盘 > 40G
- 运行内存 > 4G
- 部署依赖软件 = docker、docker-compose

### 启动运行

首先，进入根目录下deploy下执行以下命令：

```
docker-compose up -d
```

更新nginx配置文件,

```
cd /etc/nginx/conf.d
```

构建后端镜像，

```
docker build -t gaogegaogle/gao-ge-ryan:latest .
```
数据库定时备份
```
crontab -e
```
```
59 23 * * * /opt/register-backend/bin/mysqlbak.sh >> /opt/register-backend/backups/mysqlbak/cron.log 2>&1
```
```
crontab -l
```

### Web 界面演示环境

[用户端](https://www.enrollpro.top) （报名界面，用户使用的门户界面。）

[企业端](https://enterprise.enrollpro.top) （企业管理界面，用于企业管理自己企业发布的报名信息，报名信息审核维护等。）

[管理端](https://admin.enrollpro.top) （系统管理界面，用于维护系统，用户管理、权限角色、日志监控等。）

测试账号可自行注册，也可使用预置的超级管理员账号（账号：123@qq.com 密码：Hh123@qqcom）。

## 会议与联系方式

Enroll Pro 社区致力于营造友好的环境，提供多种方式与其他用户和开发者互动。

如果你有任何问题，请随时通过以下渠道与我们联系：

- 常规联系：
  - [QQ: 3300755918]()
- 电子邮件：
  - [3300755918@qq.com]()
  - [444238219@qq.com]()

## Star 趋势

[![Star History Chart](https://api.star-history.com/svg?repos=star-history/star-history,Gao-Ge-Ryan/enroll-pro&type=date&legend=top-left)](https://www.star-history.com/#star-history/star-history&Gao-Ge-Ryan/enroll-pro&type=date&legend=top-left)