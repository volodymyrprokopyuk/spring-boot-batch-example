#!/usr/bin/env bash

set -eu

java -jar build/libs/spring-boot-batch-example-0.1.0.jar \
    importFilePath=data/people.txt \
    exportFilePath=data/people-export.txt \
    multiHumansFilePath=data/multi-humans.txt \
    "$@"
