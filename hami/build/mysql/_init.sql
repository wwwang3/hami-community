drop user if exists 'canal'@'%';
create user 'canal'@'%' identified with mysql_native_password by '123456';
grant all privileges on *.* to 'canal'@'%';
flush privileges;