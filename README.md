English version | [中文版](README_cn.md) 

<img src="imgs/logo.png" width="600px">

[![LICENSE](https://img.shields.io/github/license/Gao-Ge-Ryan/enroll-pro)](/LICENSE)
[![Releases](https://img.shields.io/github/v/release/Gao-Ge-Ryan/enroll-pro)](https://github.com/Gao-Ge-Ryan/enroll-pro/releases/latest)
[![docker pulls](https://img.shields.io/docker/pulls/gaogegaogle/gao-ge-ryan)](https://hub.docker.com/repositories/gaogegaogle)
[![website](https://img.shields.io/badge/website-blue)](https://www.enrollpro.top)
[![Contact Me](https://img.shields.io/badge/ContactMe-QQ：3300755918-blue)](https://www.enrollpro.top)


## Enroll Pro: Customizable Registration System

## Introduction

A customizable registration system suitable for exam registration systems, event registration systems, or any other form of registration scenarios. This system can be used for production or enterprise-level projects. The project and code have undergone rigorous testing and support private deployments.

### System Architecture

- Backend: Java 8, SpringBoot 2.5.15, Mybatis, Gradle, Minio.
- Frontend: Vue 3.
- Database: Mysql 8.0.21, Redis (latest).
- Requires zero changes to existing programs.
- Deployment: Docker, Docker-compose

## Quick Start

### Prerequisites

Before running the system, ensure the following prerequisites are met:

- Operating System: Centos, Ubuntu, Debian
- Disk Space: > 40GB
- RAM: > 4GB
- Required Software: Docker, Docker-compose

### Start the System

Run the following command in the root directory under deploy:

```
docker-compose up -d
```

Update the Nginx configuration file:

```
cd /etc/nginx/conf.d
```

Build the backend Docker image:

```
docker build -t gaogegaogle/gao-ge-ryan:latest .
```
Set up a scheduled backup for the database:
```
crontab -e
```
```
59 23 * * * /opt/register-backend/bin/mysqlbak.sh >> /opt/register-backend/backups/mysqlbak/cron.log 2>&1
```
```
crontab -l
```

### Web Interface Demo

[User Portal](https://www.enrollpro.top) （Registration page, the interface for users to register.）

[Enterprise Portal](https://enterprise.enrollpro.top) （Enterprise management interface for enterprises to manage and review their registration information.）

[Admin Portal](https://admin.enrollpro.top) （System administration interface for managing the system, user management, role permissions, log monitoring, etc.）

You can register for a test account or use the pre-configured super administrator account (Account: 123@qq.com , Password: Hh123@qqcom).

## Meeting & Contact

Enroll Pro is dedicated to fostering a friendly community environment and offering multiple ways for users and developers to interact.

If you have any questions, feel free to contact us via the following channels:

- General Contact:
  - [QQ: 3300755918]()
- Email:
  - [3300755918@qq.com]()
  - [444238219@qq.com]()

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=star-history/star-history,Gao-Ge-Ryan/enroll-pro&type=date&legend=top-left)](https://www.star-history.com/#star-history/star-history&Gao-Ge-Ryan/enroll-pro&type=date&legend=top-left)
