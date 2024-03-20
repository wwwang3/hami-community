FROM nginx:1.25.4
ENV TZ=Asia/Shanghai
RUN mkdir -p /data/nginx/log /data/nginx/html
#RUN chmod -R 644 /data/nginx
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone
EXPOSE 80
EXPOSE 443