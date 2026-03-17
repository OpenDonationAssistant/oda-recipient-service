# ODA Recipient Service
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OpenDonationAssistant/oda-recipient-service)

## Running with Docker

The Docker image is available from GitHub Container Registry:

```bash
docker pull ghcr.io/opendonationassistant/oda-recipient-service:latest
```

### Required Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `RABBITMQ_HOST` | RabbitMQ server hostname | `localhost` |
| `INFINISPAN_HOST` | Infinispan server hostname | `127.0.0.1` |
| `INFINISPAN_PORT` | Infinispan server port | `11222` |
| `INFINISPAN_USER` | Infinispan username | `admin` |
| `INFINISPAN_PASSWORD` | Infinispan password | `password` |
| `JDBC_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost/postgres?currentSchema=recipient` |
| `JDBC_USER` | PostgreSQL username | `postgres` |
| `JDBC_PASSWORD` | PostgreSQL password | `postgres` |

### Running the Container

```bash
docker run -d \
  --name oda-recipient-service \
  -e RABBITMQ_HOST=<rabbitmq-host> \
  -e INFINISPAN_HOST=<infinispan-host> \
  -e INFINISPAN_PORT=11222 \
  -e INFINISPAN_USER=admin \
  -e INFINISPAN_PASSWORD=<infinispan-password> \
  -e JDBC_URL=jdbc:postgresql://<postgres-host>:5432/postgres?currentSchema=recipient \
  -e JDBC_USER=postgres \
  -e JDBC_PASSWORD=<postgres-password> \
  ghcr.io/opendonationassistant/oda-recipient-service:latest
```

### Docker Compose Example

```yaml
version: '3.8'
services:
  oda-recipient-service:
    image: ghcr.io/opendonationassistant/oda-recipient-service:latest
    environment:
      - RABBITMQ_HOST=rabbitmq
      - INFINISPAN_HOST=infinispan
      - INFINISPAN_PORT=11222
      - INFINISPAN_USER=admin
      - INFINISPAN_PASSWORD=password
      - JDBC_URL=jdbc:postgresql://postgres:5432/postgres?currentSchema=recipient
      - JDBC_USER=postgres
      - JDBC_PASSWORD=postgres
    depends_on:
      - rabbitmq
      - postgres
      - infinispan
```

