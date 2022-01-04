FROM openjdk:17
EXPOSE 8080
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.behl.glumon.RateLimitingApiSpringBootApplication"]
LABEL maintainer="Hardik Singh Behl" email="behl.hardiksingh@gmail.com"