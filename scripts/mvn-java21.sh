#!/bin/zsh

set -euo pipefail

JAVA_HOME_21=$(/usr/libexec/java_home -v 21)
export JAVA_HOME="$JAVA_HOME_21"
export PATH="$JAVA_HOME/bin:$PATH"

exec mvn -Dmaven.repo.local=.m2/repository "$@"
