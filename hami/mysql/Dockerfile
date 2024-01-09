FROM mysql:8.0.30
COPY ./_init.sql /docker-entrypoint-initdb.d/_init.sql
COPY ./db_hami_community.sql /docker-entrypoint-initdb.d/db_hami_community.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/_init.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/db_hami_community.sql
COPY ./docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
#COPY ./my.cnf /etc/my.cnf
#RUN chown -R mysql:mysql /etc/my.cnf
ENV TZ=Asia/Shanghai
CMD ["mysqld"]