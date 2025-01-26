#!/bin/bash
#
# Cria a imagem docker localmente usando build container

repoprefix=gomesrodris
basename=fiap-videoslice-svc-web-api

cd $(dirname $0) || exit 1
cd ..

version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [ "$version" == "" ]
then
  echo "Could not get project version"
  exit 1
fi

mvn clean install dependency:copy-dependencies
#mvn -DskipTests -DskipITs clean install dependency:copy-dependencies

if [ $? -ne 0 ]
then
  echo "Build error"
  exit 2
fi

if [ -d ./docker/temp_libs ]
then
  rm ./docker/temp_libs/*
else
  mkdir ./docker/temp_libs || exit 1
fi

cp ./modules/web-app/target/*webapi-*.jar ./docker/temp_libs/ && cp ./modules/web-app/target/lib/* ./docker/temp_libs/

cd docker || exit 1

docker build . -t $repoprefix/$basename:$version

if [ $? -ne 0 ]
then
  echo "Image build error"
  exit 2
fi

echo "::: Built $repoprefix/$basename:$version"

docker push docker.io/$repoprefix/$basename:$version

