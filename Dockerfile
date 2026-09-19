# 1. 빌드 단계
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

# Windows에서 생성된 CRLF 줄바꿈으로 인한 Linux 실행 오류 방지
RUN sed -i 's/\r$//' gradlew \
	&& chmod +x gradlew \
	&& ./gradlew bootJar --no-daemon \
	&& cp "$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" app.jar

# 2. 실행 단계
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 빌드 단계에서 생성한 실행 가능한 JAR 복사
COPY --from=builder /app/app.jar app.jar

EXPOSE 8080

# 3. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]