#!/bin/bash

# Docker 容器名或 ID
container_name="mysql"

# MySQL 登录信息
dbuser="root"
dbpasswd="Mysql123"
dbname="style_register style_register_slave"  # 多个库用空格分开

# 时间和路径
backtime=$(date +%Y%m%d)
logpath='/opt/register-backend/backups/mysqlbak'
datapath='/opt/register-backend/backups/mysqlbak'

# 创建目录
mkdir -p "$logpath"
mkdir -p "$datapath"

# 日志开始
echo "################## ${backtime} #############################"
echo "开始备份"

echo "" >> ${logpath}/mysqlback.log
echo "-------------------------------------------------" >> ${logpath}/mysqlback.log
echo "备份时间为 ${backtime}, 开始备份数据库: ${dbname}" >> ${logpath}/mysqlback.log

# 遍历所有数据库
for db in $dbname; do
    dump_file="${logpath}/${db}_${backtime}.sql"
    tar_file="${datapath}/${db}_${backtime}.tar.gz"

    echo "正在备份数据库：$db"

    # 在容器中执行 mysqldump
    docker exec -i ${container_name} sh -c "mysqldump -u${dbuser} -p${dbpasswd} --databases ${db}" > "$dump_file" 2>> ${logpath}/mysqlback.log

    if [ $? -eq 0 ]; then
        tar -zcf "$tar_file" -C "$logpath" "$(basename "$dump_file")" > /dev/null
        rm -f "$dump_file"
        # 清理7天前的备份
        find $datapath -name "*.tar.gz" -type f -mtime +7 -exec rm -f {} \;
        echo "数据库 ${db} 备份成功!!" >> ${logpath}/mysqlback.log
    else
        echo "数据库 ${db} 备份失败!!" >> ${logpath}/mysqlback.log
    fi
done

echo "完成备份"
echo "################## ${backtime} #############################"
