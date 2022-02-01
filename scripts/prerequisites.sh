docker run -it -d -p 53306:3306 \
-e MYSQL_ROOT_PASSWORD=123456 \
-e MYSQL_DATABASE=example \
-e MYSQL_USER=example \
-e MYSQL_PASSWORD=example!@#456 \
--name example-mysql \
mysql:latest