FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Trust any corporate/proxy root CA dropped in certs/ (see certs/README.md).
# Needed on networks that TLS-intercept outbound HTTPS (e.g. Palo Alto),
# since the JVM's own cacerts doesn't know about that root - even though
# the OS/browser does, from the Windows trust store.
COPY certs/ /usr/local/share/ca-certificates/corp/
RUN for cert in /usr/local/share/ca-certificates/corp/*.crt; do \
      [ -f "$cert" ] || continue; \
      keytool -importcert -noprompt -trustcacerts \
        -alias "$(basename "$cert" .crt)" \
        -file "$cert" \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit; \
    done

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
