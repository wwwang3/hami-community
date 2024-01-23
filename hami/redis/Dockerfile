FROM redis:7.2
VOLUME /data
RUN mkdir "/var/run/redis"
RUN mkdir "/var/log/redis"
RUN chown -R redis:redis /var/run/redis
RUN chown -R redis:redis /var/log/redis
#COPY ./redis.conf /etc/redis/redis.conf
#CMD ["redis-server", "/etc/redis/redis.conf", "--appendonly yes"]


