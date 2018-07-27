#!/usr/bin/env bash

set -eu

java -jar build/libs/spring-boot-batch-example-0.1.0.jar \
    spring.batch.job.names=initialJob \
    importFilePath=data/people.txt \
    exportFilePath=data/people-export.txt \
    importHumansFilePath=data/humans.txt \
    exportMalesFilePath=data/males-export.txt \
    exportFemalesFilePath=data/females-export.txt \
    "$@"
