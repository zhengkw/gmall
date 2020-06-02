#!/bin/bash
case $1 in "start")
for host in hadoop102 hadoop103 hadoop104
do
	echo "==========启动$host ES========"
	ssh $host "nohup /opt/module/elasticsearch-6.3.1/bin/elasticsearch 1>/opt/module/elasticsearch-6.3.1/start.log 2>/opt/module/elasticsearch-6.3.1/error.log &"
done
xcall jps
;;
"stop")
for host in hadoop102 hadoop103 hadoop104
do
	echo "==========关闭$host ES========"
	ssh $host "jps | awk '/Elasticsearch/ {print \$1}' | xargs kill -9"
done
xcall jps
;;
*)
echo "请输入start命令或stop"
;;
esac