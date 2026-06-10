# Agile Microservice System

Spring Boot microservice platform for agile project delivery. The system currently includes service discovery, centralized configuration, an API gateway, PostgreSQL-backed business services, Kafka-based eventing, and a monitoring stack with Prometheus and Grafana.

## Services

- `config-server`: central configuration source for all services.
- `eureka-server`: service registry and discovery server.
- `api-gateway`: single entry point for routed APIs and JWT validation.
- `auth-service`: user registration, login, and user lookup.
- `project-service`: project lifecycle and project member management.
- `workitem-service`: backlog items, sprint assignment, and task events.
- `sprint-service`: sprint planning, sprint capacity tracking, and sprint/work item linking.
- `notification-service`: Kafka consumers and user notification storage.
- `postgres`: shared PostgreSQL engine with one database per service.
- `kafka` and `zookeeper`: event streaming backbone.
- `prometheus`: metrics scraping and storage.
- `grafana`: dashboards for operational monitoring.

## Core Architecture

- `Spring Cloud Config` centralizes shared and service-specific configuration in `config-server/src/main/resources/configs`.
- `Netflix Eureka` enables runtime service discovery between gateway and services.
- `Spring Cloud Gateway` routes:
  - `/api/auth/**`
  - `/api/projects/**`
  - `/api/workitems/**`
  - `/api/sprints/**`
  - `/api/notifications/**`
- `Kafka` carries project and work item events to the notification service.
- `Actuator + Micrometer + Prometheus` expose metrics at `/actuator/prometheus`.

## Run The System

Prerequisites:

- Java `17`
- Maven `3.9+`
- Docker Desktop / Docker Engine

Build the jars:

```bash
mvn clean package
```

Start the full stack:

```bash
docker compose up --build -d
```

Check containers:

```bash
docker compose ps
```

Main URLs:

- API Gateway: [http://localhost:8080](http://localhost:8080)
- Eureka: [http://localhost:8761](http://localhost:8761)
- Config Server: [http://localhost:8888](http://localhost:8888)
- Prometheus: [http://localhost:9090](http://localhost:9090)
- Grafana: [http://localhost:3000](http://localhost:3000)

Grafana default login:

- Username: `admin`
- Password: `admin`

## Monitoring

Prometheus scrapes:

- `config-server:8888/actuator/prometheus`
- `eureka-server:8761/actuator/prometheus`
- `api-gateway:8080/actuator/prometheus`
- `auth-service:8081/actuator/prometheus`
- `project-service:8082/actuator/prometheus`
- `workitem-service:8083/actuator/prometheus`
- `sprint-service:8084/actuator/prometheus`
- `notification-service:8085/actuator/prometheus`

Grafana is provisioned automatically with:

- a Prometheus datasource
- an `Agile Microservice Overview` dashboard

## Testing

Run the automated test suite:

```bash
mvn test
```

Useful health endpoints:

- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- [http://localhost:8761/actuator/health](http://localhost:8761/actuator/health)

## Sprint API Notes

The sprint service accepts both:

- `capacity`
- `plannedCapacity`

Example create payload:

```json
{
  "name": "Sprint 1",
  "goal": "Deliver backend MVP",
  "projectId": "YOUR_PROJECT_ID",
  "startDate": "2026-06-10",
  "endDate": "2026-06-24",
  "plannedCapacity": 40
}
```

## Documentation

Deeper technical documentation is available in [docs/PROJECT_TECHNICAL_OVERVIEW.md](/Users/a/Desktop/IT/PROJECTS/agile-microservice-system/docs/PROJECT_TECHNICAL_OVERVIEW.md).
