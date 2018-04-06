#!/usr/bin/env bash

set -e

export KUBECONFIG=kubeconfig.yaml

# push to gcloud
gcloud docker -- push gcr.io/fdc-test-statistic/fdc-test-statistic:latest

# get credentials and save to $KUBECONFIG file (implicitly if variable is set)
cp kubeconfig.template.yaml $KUBECONFIG
gcloud container clusters get-credentials fdc-test --zone europe-west1-c --project fdc-test-statistic

gcloud container clusters describe fdc-test --zone europe-west1-c 2>/dev/null | grep "username: \|password: "

echo "enter username: "
read KUBE_USER
echo "enter password: "
read KUBE_PASS

# restart pod
echo "scaling service down ..."
kubectl --kubeconfig $KUBECONFIG --password=${KUBE_PASS} --username=${KUBE_USER} scale deployment fdc-test-statistic --replicas=0
echo "scaling service up ..."
kubectl --kubeconfig $KUBECONFIG --password=${KUBE_PASS} --username=${KUBE_USER} scale deployment fdc-test-statistic --replicas=1

kubectl --kubeconfig $KUBECONFIG --password=${KUBE_PASS} --username=${KUBE_USER} get pods
