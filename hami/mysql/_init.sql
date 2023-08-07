create user 'test'@'%' identified with mysql_native_password by '123456';
# grant replication slave ON *.* to 'test'@'%';
grant all privileges on *.* to 'test'@'%';
flush privileges;