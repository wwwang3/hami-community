FROM mysql:8.0.30
COPY _init.sql /docker-entrypoint-initdb.d/_init.sql
COPY db_hami_community.sql /docker-entrypoint-initdb.d/db_hami_community.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/_init.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/db_hami_community.sql
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chown mysql:mysql /var/run/mysqld
RUN chmod 755 /var/run/mysqld
