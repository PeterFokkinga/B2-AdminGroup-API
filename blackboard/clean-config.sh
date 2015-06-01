#!/bin/sh

if [ ! -d config ] ; then
  echo You need to copy /usr/local/blackboard/config from your Vagrant image first
  exit
fi

if [ ! -d config/internal ] ; then
  echo You did not copy the config directory with its subdirectories, please fix
  exit
fi

cd config
rm -rf apache2 axis2_client collab-server-vm
rm -rf keystores oracle* plugins config-support
rm -rf tomcat* update-tool-vm support
rm -rf license/backups

rm -f *.bb *.template remove* webcas.properties http* bb-sif*
rm -f service-config-[a-sv]*.properties service-config.properties
rm -f bb-tasks.xml bb-tasks-collab.xml *.unix.xml bb-datastores-* auth*
rm -f message-queue-service-config.xml bb-session*

cd internal
rm -rf certs modules renderers tools ws
rm -f copyright.template ehcache-jms.xml ehcache-standalone.xml
rm -f google* images* http* bb-static* bb-auth* auth* legacy* data*
rm -f dwr-open.xml contextMenuOrder.properties license*

cd ../..
echo done.