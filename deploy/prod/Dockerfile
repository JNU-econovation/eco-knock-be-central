FROM eclipse-temurin:25.0.5.1_2-jdk@sha256:ce2ef9d2d67a68de5e196d4de9d743327c44a10b57fe02ec39f572c652cacfd3 AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25.0.5.1_2-jre@sha256:4ed132f6832e9804b123e89a849a7b4502756ae64a4ee08ebab939d9d5d0a9fa

# CVE-2026-114938(Java2Shell) 임시 완화: HotSpot Base64 intrinsic 비활성화.
# 수정 빌드(OpenJDK 25.0.5.1 이상에 대응하는 Temurin)로 올린 뒤 제거한다.
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 18081
EXPOSE 18082

ENTRYPOINT ["java", "-jar", "app.jar"]
