drop user if exists 'test'@'%';
create user 'test'@'%' identified with mysql_native_password by '123456';
# grant replication slave ON *.* to 'test'@'%';
grant all privileges on *.* to 'test'@'%';
drop user if exists 'canal'@'%';
create user 'canal'@'%' identified with mysql_native_password by '123456';
grant all privileges on *.* to 'canal'@'%';
flush privileges;