@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM Canal v1.1.8 升级脚本 - Windows版本
REM 用于升级 Canal 从 v1.1.7 到 v1.1.8

echo ==================================================
echo Canal v1.1.8 升级脚本 - Windows版本
echo ==================================================

REM 检查 Docker 和 Docker Compose
echo 检查 Docker 环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo 错误：Docker 未安装或未运行
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo 错误：Docker Compose 未安装
        pause
        exit /b 1
    )
)

REM 停止现有的 Canal 服务
echo 停止现有的 Canal 服务...
docker-compose stop canal-admin canal-server 2>nul
docker-compose rm -f canal-admin canal-server 2>nul

REM 清理旧的 Canal 镜像
echo 清理旧的 Canal 镜像...
docker image rm canal/canal-admin:v1.1.7 2>nul
docker image rm canal/canal-server:v1.1.7 2>nul

REM 拉取新版本镜像
echo 拉取 Canal v1.1.8 镜像...
echo 拉取 Canal Admin v1.1.8...
docker pull canal/canal-admin:v1.1.8
if errorlevel 1 (
    echo 错误：拉取 Canal Admin v1.1.8 镜像失败
    pause
    exit /b 1
)

echo 拉取 Canal Server v1.1.8...
docker pull canal/canal-server:v1.1.8
if errorlevel 1 (
    echo 错误：拉取 Canal Server v1.1.8 镜像失败
    pause
    exit /b 1
)

REM 备份现有配置
echo 备份现有配置...
if exist "docker\canal\conf" (
    set timestamp=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
    set timestamp=!timestamp: =0!
    xcopy /E /I "docker\canal\conf" "docker\canal\conf.backup.!timestamp!" >nul 2>&1
    echo 配置已备份到 docker\canal\conf.backup.!timestamp!
)

REM 创建必要的目录
echo 创建必要的目录...
if not exist "docker\canal\conf\nextera-order" mkdir "docker\canal\conf\nextera-order"

REM 启动依赖服务
echo 启动依赖服务...
docker-compose up -d mysql redis rocketmq-nameserver rocketmq-broker

REM 等待依赖服务启动
echo 等待依赖服务启动（30秒）...
timeout /t 30 /nobreak >nul

REM 启动 Canal Admin
echo 启动 Canal Admin...
docker-compose up -d canal-admin

REM 等待 Canal Admin 启动
echo 等待 Canal Admin 启动（20秒）...
timeout /t 20 /nobreak >nul

REM 启动 Canal Server
echo 启动 Canal Server...
docker-compose up -d canal-server

REM 等待服务启动
echo 等待 Canal Server 启动（30秒）...
timeout /t 30 /nobreak >nul

REM 检查服务状态
echo 检查服务状态...
echo ==================================================
docker-compose ps | findstr canal
echo ==================================================

REM 检查 Canal Admin 健康状态
echo 检查 Canal Admin 健康状态...
set /a count=0
:check_loop
set /a count+=1
curl -s -f http://localhost:8089/api/v1/health >nul 2>&1
if errorlevel 1 (
    if !count! LSS 10 (
        echo 等待 Canal Admin 启动... (!count!/10)
        timeout /t 5 /nobreak >nul
        goto check_loop
    ) else (
        echo 警告：Canal Admin 可能未完全启动
    )
) else (
    echo Canal Admin 启动成功！
)

REM 检查 Canal Server 日志
echo 检查 Canal Server 日志...
echo 最近的 Canal Server 日志：
docker-compose logs --tail=20 canal-server

REM 显示访问信息
echo.
echo ==================================================
echo Canal v1.1.8 升级完成！
echo ==================================================
echo Canal Admin 访问地址：http://localhost:8089
echo 默认用户名：admin
echo 默认密码：admin
echo.
echo Canal Server 端口：11111
echo Canal Metrics 端口：11112
echo.
echo 请检查日志确认服务正常运行：
echo   docker-compose logs canal-admin
echo   docker-compose logs canal-server
echo ==================================================

REM 提示用户检查配置
echo.
echo 注意事项：
echo 1. 请登录 Canal Admin 控制台确认实例配置
echo 2. 确认数据库连接正常
echo 3. 确认 RocketMQ 连接正常
echo 4. 监控 Canal 日志确保数据同步正常

echo.
echo 升级完成！按任意键退出...
pause >nul 