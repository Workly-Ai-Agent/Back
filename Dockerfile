# 1. Java 21 기반 가벼운 이미지 사용
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 2. 빌드된 jar 파일을 컨테이너 내부로 복사
# (Gradle 빌드 시 생성되는SNAPSHOT.jar 파일을 app.jar로 이름을 바꿉니다)
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]