FROM docker.m.daocloud.io/library/nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY release/frontend/ /usr/share/nginx/html/operation/

EXPOSE 80
