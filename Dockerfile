FROM fedora:41
LABEL org.opencontainers.image.source=https://github.com/opendonationasssistant/oda-recipient-service
WORKDIR /app
COPY target/oda-recipient-service /app

CMD ["./oda-recipient-service"]
