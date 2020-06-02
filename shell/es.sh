#!/bin/bash
es_home=/zhengkw/module/elasticsearch-6.3.1
log_home=/zhengkw/elklogs
kibana_home=/zhengkw/module/kibana
case $1 in
"start")
for host in hadoop102 hadoop103 hadoop104
do
	echo "==========启动$host ES========"
	ssh $host "nohup $es_home/bin/elasticsearch 1>$log_home/start.log 2>$log_home/error.log &"
    echo "==========启动$host kibana========"
        nohup $kibana_home/bin/kibana 1> $log_home/kibanastart.log 2>$log_home/kibanastop.log &

done
xcall jps
;;
"stop")
for host in hadoop102 hadoop103 hadoop104
do
	echo "==========关闭$host ES========"
	ssh $host "jps | awk '/Elasticsearch/ {print \$1}' | xargs kill -9"
done
	echo "==========关闭hadoop102 Kibana========"
ps -ef | grep node | grep -v grep|awk '{print $2}'|xargs kill -9

xcall jps
;;
*)
echo "请输入start命令或stop"
;;
esac