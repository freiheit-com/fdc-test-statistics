#!/usr/bin/env bash
VERSION="0.10.2"

set -e

# build service
LEIN_SNAPSHOTS_IN_RELEASE=1 lein uberjar

# pull ui submodule
# TODO

# build docker image
docker build --build-arg VERSION="${VERSION}" \
    -t "gcr.io/fdc-test-statistic/fdc-test-statistic:${VERSION}" \
    -t "gcr.io/fdc-test-statistic/fdc-test-statistic:latest" \
    .

# for (TODO integration) testing
# docker run -i -p 3001:3001 --name fdc-test-statistic ${VERSION}
