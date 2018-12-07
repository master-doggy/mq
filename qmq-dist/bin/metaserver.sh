#!/usr/bin/env bash
set -euo pipefail

QMQ_BIN="${BASH_SOURCE-$0}"
QMQ_BIN="$(dirname "$QMQ_BIN")"
QMQ_BIN_DIR="$(cd "$QMQ_BIN"; pwd)"
QMQ_META_MAIN="qunar.tc.qmq.meta.startup.Bootstrap"

. "$QMQ_BIN_DIR/base.sh"
. "$QMQ_BIN_DIR/metaserver-env.sh"

if [[ "$JAVA_HOME" != "" ]]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

JAVA_OPTS="$JAVA_OPTS -DQMQ_LOG_DIR=$QMQ_LOG_DIR"
QMQ_PID_FILE="$QMQ_PID_DIR/metaserver.pid"
QMQ_DAEMON_OUT="$QMQ_LOG_DIR/metaserver.out"

CMD=${1:-}
case ${CMD} in
start)
    echo  -n "Starting qmq meta server ... "
    if [[ -f "$QMQ_PID_FILE" ]]; then
      if kill -0 `cat "$QMQ_PID_FILE"` > /dev/null 2>&1; then
         echo already running as process `cat "$QMQ_PID_FILE"`.
         exit 0
      fi
    fi
    nohup "$JAVA" -cp "$CLASSPATH" ${JAVA_OPTS} ${QMQ_META_MAIN} > "$QMQ_DAEMON_OUT" 2>&1 < /dev/null &
    if [[ $? -eq 0 ]]
    then
      /bin/echo -n $! > "$QMQ_PID_FILE"
      if [[ $? -eq 0 ]];
      then
        sleep 1
        echo STARTED
      else
        echo FAILED TO WRITE PID
        exit 1
      fi
    else
      echo SERVER DID NOT START
      exit 1
    fi
    ;;
start-foreground)
    ZOO_CMD=(exec "$JAVA")
    "${ZOO_CMD[@]}" -cp "$CLASSPATH" ${JAVA_OPTS} ${QMQ_META_MAIN}
    ;;
stop)
    echo -n "Stopping qmq meta server ... "
    if [[ ! -f "$QMQ_PID_FILE" ]]
    then
      echo "no meta server to stop (could not find file $QMQ_PID_FILE)"
    else
      kill -9 $(cat "$QMQ_PID_FILE")
      rm "$QMQ_PID_FILE"
      echo STOPPED
    fi
    exit 0
    ;;
*)
    echo "Usage: $0 {start|start-foreground|stop}" >&2
esac