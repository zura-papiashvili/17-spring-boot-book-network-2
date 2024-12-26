# Build stage
FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:17
ARG PROFILE=dev
ARG APP_VERSION=1.0.3
ARG APP_NAME=book-network

WORKDIR /app
COPY --from=build /build/target/book-network-* /app/

EXPOSE 8088

ENV DB_URL=jdbc:postgresql://postgres-sql-bsn:5432/book_social_network
ENV JAR_VERSION=${APP_VERSION}
ENV ACTIVE_PROFILE=${PROFILE}

CMD java -jar book-network-${JAR_VERSION}.jar -Dspring.profiles.active=${ACTIVE_PROFILE} -Dspring.datasource.url=${DB_URL}

