FROM mysql:8.0.30
COPY ./_init.sql /docker-entrypoint-initdb.d/_init.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/_init.sql
COPY ./docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
#COPY ./my.cnf /etc/my.cnf
#RUN chown -R mysql:mysql /etc/my.cnf
CMD ["mysqld"]