FROM tomcat:9.0.65-jdk17-corretto

ARG DB_URL
ARG DB_USER
ARG DB_PASS
ARG BASE_URL

# Modify tomcat configuration to set max header size for requests to
# 65536 bytes (64 KiB). This because Azure headers are so large.
RUN sed -i 's/port="8080"/port="8080" maxHttpHeaderSize="65536"/g' /usr/local/tomcat/conf/server.xml

# Postgresql JDBC driver needs this to connect over TLS.
ADD ./rds-combined-ca-bundle.pem /root/.postgresql/root.crt

# Copy unpacked webappd dir into image, at webapps/ROOT so that it is available at '/'.
ADD ./target/webapp /usr/local/tomcat/webapps/ROOT

ENV JAVA_OPTS="-Dtr.db.url=\"${DB_URL}\" \
               -Dtr.db.user=${DB_USER} \
               -Dtr.db.pass=${DB_PASS} \
               -Dtr.base.url=${BASE_URL}"