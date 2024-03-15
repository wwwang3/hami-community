FROM azul/zulu-openjdk-debian:17-jre-headless-latest
ARG VERSION=0.0.1
RUN mkdir "/root/hami"
WORKDIR /application
COPY ./hami-api-${VERSION}.jar ./app.jar
RUN chmod 744 ./app.jar
ENV JVM_OPTS="-Xms256m -Xmx512m"
ENV TZ=Asia/Shanghai
# word-dir
ENV HAMI_WORK_DIR=/root/hami
RUN RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar /root/app.jar ${@}"]

