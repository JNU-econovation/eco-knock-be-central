FROM eclipse-temurin:25.0.5_8-jdk@sha256:884a2f8692cc1e2c7e21f95914adf0ae0eafee9a7eb663a12169cc44468256ec AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25.0.5_8-jre@sha256:945247f9d092703e0c6be92243ef3c51640229e23ab34d353c8dcdc73e6cd4e6

# CVE-2026-114938(Java2Shell) 임시 완화: HotSpot Base64 intrinsic 비활성화.
# 수정 빌드(OpenJDK 25.0.5.1 이상에 대응하는 Temurin)로 올린 뒤 제거한다.
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 18081
EXPOSE 18082

ENTRYPOINT ["java", "-jar", "app.jar"]
