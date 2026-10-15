FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build

COPY pom.xml checkstyle.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted


FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

USER spring
ENV TZ=UTC
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]