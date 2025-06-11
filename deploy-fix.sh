#!/bin/bash
echo "=== 修复LumiLove后端部署 ==="

# 1. 重新打包
echo "1. 重新打包应用..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 打包失败"
    exit 1
fi

# 2. 停止服务器上的所有Java进程
echo "2. 停止服务器上的Java进程..."
ssh -i ~/.ssh/id_rsa ec2-user@13.239.244.183 "pkill -f java; sleep 3"

# 3. 上传新版本
echo "3. 上传修复版本..."
scp -i ~/.ssh/id_rsa target/LumiLoveBackend-0.0.1-SNAPSHOT.jar ec2-user@13.239.244.183:~/lumilove-backend.jar

if [ $? -ne 0 ]; then
    echo "❌ 上传失败"
    exit 1
fi

# 4. 启动应用
echo "4. 启动应用..."
ssh -i ~/.ssh/id_rsa ec2-user@13.239.244.183 "
    echo '启动应用中...'
    nohup java -jar lumilove-backend.jar > app.log 2>&1 &
    sleep 15
    echo '检查进程状态:'
    ps aux | grep java | grep -v grep || echo '没有找到Java进程'
    echo '检查应用日志:'
    tail -15 app.log
"

# 5. 测试连接
echo "5. 测试HTTPS连接..."
sleep 5

echo "测试注册接口..."
response=$(curl -k -s -w "%{http_code}" -X POST https://13.239.244.183:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"123456"}' \
  -o /tmp/curl_response.txt)

if [ "$response" = "400" ] || [ "$response" = "200" ]; then
    echo "✅ HTTPS连接成功! HTTP状态码: $response"
    echo "响应内容:"
    cat /tmp/curl_response.txt
    echo ""
    echo "🎉 后端部署成功！"
    echo "前端可以使用以下地址:"
    echo "https://13.239.244.183:8443/api/auth/register"
    echo "https://13.239.244.183:8443/api/auth/login"
else
    echo "❌ HTTPS连接失败，HTTP状态码: $response"
    echo "检查服务器日志..."
    ssh -i ~/.ssh/id_rsa ec2-user@13.239.244.183 "tail -20 app.log"
fi

rm -f /tmp/curl_response.txt

echo "=== 修复完成 ===" 