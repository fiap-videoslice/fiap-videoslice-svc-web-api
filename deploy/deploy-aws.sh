#!/bin/bash

# Realiza o deploy da aplicação no cluster EKS
# Este script simula os passos realizados no workflow de CD

EKS_CLUSTER_NAME=app-cluster
DB_INSTANCE_IDENTIFIER=videoslice-db
COGNITO_USER_POOL=videoslice-logins
DATABASE_NAME=postgres
DATABASE_USER=videoslice
DB_PW=Slice!123456

SMTP_SERVER=smtp.gmail.com
SMTP_PORT=587
SMTP_TLS=true
SMTP_USER=fiapvideoslice@gmail.com
SMTP_PASSWORD="vqgs mpjb vjnf hyam"
SMTP_MAILFROM=fiapvideoslice@gmail.com

VIDEO_PROCESS_REQUEST_QUEUE_NAME=videoslice_job_requests
VIDEO_PROCESS_RESPONSE_QUEUE_NAME=videoslice_job_status
VIDEO_PROCESS_REQUEST_BUCKET_NAME=videoslice-job-requests
VIDEO_PROCESS_RESPONSE_BUCKET_NAME=videoslice-job-results


APP_IMAGE=gomesrodris/fiap-videoslice-svc-web-api:0.0.5

TMP_OUTPUTS=/tmp/deploy/tmp_outputs.vars

baseDir=$(dirname $0)

cd $baseDir || exit 1
mkdir /tmp/deploy


./utils/check-cluster-status.sh ${EKS_CLUSTER_NAME}
if [ $? -ne 0 ]
then
  echo "Error checking AWS resource. See error messages"
  exit 1
fi

./utils/get-database-url.sh ${DB_INSTANCE_IDENTIFIER} > $TMP_OUTPUTS
if [ $? -ne 0 ]
then
  echo "Error checking AWS resource. See error messages"
  exit 1
fi
source $TMP_OUTPUTS

./utils/get-user-pool-info.sh ${COGNITO_USER_POOL} > $TMP_OUTPUTS
if [ $? -ne 0 ]
then
  echo "Error checking AWS resource. See error messages"
  exit 1
fi
source $TMP_OUTPUTS

cat ./k8s/db/db-configs-cloud-template.yml \
   | sed "s/{DB_URL}/${DB_URL}/" \
   | sed "s/{DB_NAME}/${DATABASE_NAME}/" \
   | sed "s/{DB_USER}/${DATABASE_USER}/" \
   | sed "s/{DB_PASS}/${DB_PW}/" > /tmp/deploy/db-configs.yml

if [ $? -ne 0 ]
then
  echo "Error preparing configmap. See error messages"
  exit 1
fi

cat ./k8s/app/aws-configs-template.yml \
   | sed "s/{ACCESS_KEY_ID}/$(echo -n ${aws_access_key_id} | tr -d '\n')/" \
   | sed "s|{SECRET_ACCESS_KEY}|$(echo -n ${aws_secret_access_key} | tr -d '\n')|" \
   | sed "s|{SESSION_TOKEN}|$(echo -n ${aws_session_token} | tr -d '\n')|" \
   | sed "s/{REGION}/us-east-1/" \
   | sed "s/{USER_POOL_ID}/${USER_POOL_ID}/" \
   | sed "s/{USER_POOL_CLIENT_ID}/${USER_POOL_CLIENT_ID}/" \
   | sed "s/{USER_POOL_CLIENT_SECRET}/${USER_POOL_CLIENT_SECRET}/" > /tmp/deploy/aws-configs.yml

if [ $? -ne 0 ]
then
  echo "Error preparing configmap. See error messages"
  exit 1
fi

cat ./k8s/integration/archburgers-integration-config-template.yml \
   | sed "s/{QUEUE_REQUESTS}/$(echo -n ${VIDEO_PROCESS_REQUEST_QUEUE_NAME} | tr -d '\n')/" \
   | sed "s/{QUEUE_RESPONSE}/$(echo -n ${VIDEO_PROCESS_RESPONSE_QUEUE_NAME} | tr -d '\n')/" \
   | sed "s/{BUCKET_INPUT}/$(echo -n ${VIDEO_PROCESS_REQUEST_BUCKET_NAME} | tr -d '\n')/" \
   | sed "s/{BUCKET_RESULT}/$(echo -n ${VIDEO_PROCESS_RESPONSE_BUCKET_NAME} | tr -d '\n')/"  > /tmp/deploy/archburgers-integration-config-template.yml

if [ $? -ne 0 ]
then
  echo "Error preparing configmap. See error messages"
  exit 1
fi

cat ./k8s/integration/email-notification-config-template.yml \
   | sed "s/{SMTP_SERVER}/${SMTP_SERVER}/" \
   | sed "s/{SMTP_PORT}/${SMTP_PORT}/" \
   | sed "s/{SMTP_TLS}/${SMTP_TLS}/" \
   | sed "s/{SMTP_USER}/${SMTP_USER}/" \
   | sed "s/{SMTP_PASSWORD}/${SMTP_PASSWORD}/" \
   | sed "s/{SMTP_MAILFROM}/${SMTP_MAILFROM}/" > /tmp/deploy/email-notification-config.yml

if [ $? -ne 0 ]
then
  echo "Error preparing configmap. See error messages"
  exit 1
fi

cat ./k8s/app/app-deployment.yml \
  | sed "s|image: .*architect-burgers.*$|image: $APP_IMAGE|" > /tmp/deploy/app-deployment.yml

if [ $? -ne 0 ]
then
  echo "Error preparing app-deployment. See error messages"
  exit 1
fi

aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME}

kubectl apply -f /tmp/deploy/db-configs.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f /tmp/deploy/email-notification-config.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f /tmp/deploy/aws-configs.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f /tmp/deploy/archburgers-integration-config-template.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f ./k8s/app/app-service-loadbalancer.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f /tmp/deploy/app-deployment.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi

kubectl apply -f ./k8s/app/app-hpa.yml
if [ $? -ne 0 ]
then
  echo "Error running kubectl step. See error messages"
  exit 1
fi
