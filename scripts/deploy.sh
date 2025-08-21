#!/usr/bin/env bash

set -e  # 에러 발생 시 스크립트 중단

REPOSITORY=/home/ubuntu/app
LOG_FILE=$REPOSITORY/deploy.log
PORT=8080

# 로그 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

log "=== 배포 시작 ==="

# 환경 변수 로드
log "> 환경 변수 로드"
source ~/.bashrc

# 현재 구동 중인 애플리케이션 pid 확인
log "> 현재 구동 중인 애플리케이션 pid 확인"
CURRENT_PID=$(sudo lsof -t -i:$PORT 2>/dev/null || echo "")

if [ -z "$CURRENT_PID" ]; then
    log "현재 구동 중인 애플리케이션이 없습니다."
else
    log "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"
    log "> 기존 애플리케이션 종료"
    kill -9 $CURRENT_PID || log "애플리케이션 종료 실패 (이미 종료됨)"
    sleep 5
fi

# 새 애플리케이션 배포
log "> 새 애플리케이션 배포"

# JAR 파일 찾기
JAR_NAME=$(ls -tr $REPOSITORY/*SNAPSHOT.jar 2>/dev/null | tail -n 1)

if [ -z "$JAR_NAME" ]; then
    log "ERROR: JAR 파일을 찾을 수 없습니다!"
    exit 1
fi

log "> JAR NAME: $JAR_NAME"

# 실행권한 추가
log "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

# 애플리케이션 실행
log "> $JAR_NAME 실행 (prod 프로파일 활성화)"
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Seoul \
    -Xms512m \
    -Xmx1024m \
    $JAR_NAME >> $REPOSITORY/nohup.out 2>&1 &

# 새로운 PID 확인
sleep 3
NEW_PID=$(sudo lsof -t -i:$PORT 2>/dev/null || echo "")

if [ -n "$NEW_PID" ]; then
    log "✅ 배포 성공! 새로운 PID: $NEW_PID"
else
    log "❌ 배포 실패! 애플리케이션이 시작되지 않았습니다."
    log "로그 확인: tail -f $REPOSITORY/nohup.out"
    exit 1
fi

log "=== 배포 완료 ==="