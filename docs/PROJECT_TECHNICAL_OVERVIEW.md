# Agile Microservice System Technical Overview

## 1. Project Summary

The Agile Microservice System is a backend-focused platform for managing agile delivery workflows. It is organized as a set of Spring Boot microservices connected through an API gateway, service discovery, centralized configuration, Kafka-based asynchronous events, and PostgreSQL persistence.

The system is currently designed to be tested mainly through Postman and direct HTTP calls. There is no frontend application in this repository at the moment.

## 2. Business Scope

The backend supports the main building blocks of agile execution:

- user authentication
- project creation and lifecycle management
- project membership and role assignment
- work item creation and updates
- sprint planning and sprint capacity management
- user notifications triggered by business events

## 3. Backend Microservices

### 3.1 Config Server

Path: `config-server`

Responsibilities:

- centralizes configuration for all services
- stores shared settings and service-specific overrides
- supports default, local, and docker profile variants

Key technology:

- Spring Cloud Config Server

Important files:

- `config-server/src/main/resources/configs/application.properties`
- `config-server/src/main/resources/configs/*-service.properties`
- `config-server/src/main/resources/configs/*-service-docker.properties`

### 3.2 Eureka Server

Path: `eureka-server`

Responsibilities:

- acts as the service registry
- allows runtime discovery between the gateway and internal services

Key technology:

- Spring Cloud Netflix Eureka Server

### 3.3 API Gateway

Path: `api-gateway`

Responsibilities:

- single public entry point to the backend
- routes requests to downstream services
- validates JWT tokens
- forwards authenticated user context using headers such as `X-User-Id`

Key technology:

- Spring Cloud Gateway
- Spring WebFlux

Main routes:

- `/api/auth/** -> auth-service`
- `/api/projects/** -> project-service`
- `/api/workitems/** -> workitem-service`
- `/api/sprints/** -> sprint-service`
- `/api/notifications/** -> notification-service`

### 3.4 Auth Service

Path: `auth-service`

Responsibilities:

- user registration
- user login
- user lookup by ID

Key technology:

- Spring Security
- JWT with `jjwt`
- Spring Data JPA

Current behavior:

- duplicate registration now returns `409 Conflict`
- invalid password returns `401 Unauthorized`
- missing user returns `404 Not Found`

### 3.5 Project Service

Path: `project-service`

Responsibilities:

- create, update, read, and archive projects
- manage project members
- invite members and assign roles
- publish project-related Kafka events

Key technology:

- Spring MVC
- Spring Data JPA
- Spring Kafka
- OpenFeign

### 3.6 Work Item Service

Path: `workitem-service`

Responsibilities:

- create and update backlog/work items
- assign work items to users
- link and unlink work items to sprints
- publish work item Kafka events

Key technology:

- Spring MVC
- Spring Data JPA
- Spring Kafka
- OpenFeign

Important implementation detail:

- the service now exposes a dedicated sprint-assignment endpoint so sprint removal can explicitly clear `sprintId`

### 3.7 Sprint Service

Path: `sprint-service`

Responsibilities:

- create and update sprints
- track sprint capacity
- attach and detach work items from sprints
- compute committed work and assignee load
- publish sprint lifecycle events

Key technology:

- Spring MVC
- Spring Data JPA
- Spring Kafka
- OpenFeign

Compatibility note:

- the API accepts both `capacity` and `plannedCapacity`
- the response exposes both names to remain compatible with existing Postman collections

### 3.8 Notification Service

Path: `notification-service`

Responsibilities:

- consume Kafka events
- persist notifications
- expose notification APIs to clients

Key technology:

- Spring MVC
- Spring Data JPA
- Spring Kafka

Recent fix:

- work item notifications now use real user IDs such as `assigneeId` instead of incorrectly storing `projectId` or `taskId` as the notification owner

## 4. Data Layer

The system uses PostgreSQL as the primary datastore. One PostgreSQL engine runs in Docker, and `init.sql` provisions multiple databases and users:

- `auth_db`
- `project_db`
- `workitem_db`
- `sprint_db`
- `notification_db`

Each business service uses its own schema/database boundary. This is aligned with the microservice principle of isolated persistence ownership.

ORM and persistence stack:

- Spring Data JPA
- Hibernate
- PostgreSQL JDBC driver

Current schema management approach:

- `spring.jpa.hibernate.ddl-auto=update`

Important observation:

- there is currently no Flyway or Liquibase migration strategy in the repository
- schema migrations are therefore not yet production-grade

## 5. Messaging and Eventing

Kafka is used for asynchronous integration between services.

Current topics include:

- `project.created`
- `member.invited`
- `role.assigned`
- `task.created`
- `task.assigned`
- `task.status-changed`

Main producer services:

- `project-service`
- `workitem-service`
- `sprint-service`

Main consumer service:

- `notification-service`

Why Kafka is used here:

- decouples notification logic from synchronous business transactions
- reduces direct runtime coupling between services
- supports future expansion into audit, analytics, and workflow automation

## 6. Security Model

Current security is intentionally lightweight.

Implemented today:

- JWT token creation in `auth-service`
- token validation in `api-gateway`
- authenticated user propagation with `X-User-Id`

Not fully implemented yet:

- role-based authorization in JWT claims
- fine-grained permission enforcement across services
- refresh token flow
- token revocation

## 7. DevOps Architecture

The repository includes a Docker Compose based local platform.

Main infrastructure containers:

- PostgreSQL
- Zookeeper
- Kafka
- Config Server
- Eureka Server
- API Gateway
- Auth Service
- Project Service
- Work Item Service
- Sprint Service
- Notification Service
- Prometheus
- Grafana

DevOps characteristics:

- containerized local environment
- healthchecks on service startup
- service dependency sequencing using `depends_on`
- centralized runtime configuration through config server
- local observability stack with metrics and dashboards

## 8. Monitoring and Observability

Monitoring is implemented with:

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana

### 8.1 Metrics Exposure

All services inherit:

- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

Metrics endpoint:

- `/actuator/prometheus`

Exposed management endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

### 8.2 Prometheus

Path:

- `monitoring/prometheus/prometheus.yml`

Responsibilities:

- scrapes service metrics every 15 seconds
- monitors gateway, discovery, configuration, and all business services

### 8.3 Grafana

Provisioning paths:

- `monitoring/grafana/provisioning/datasources`
- `monitoring/grafana/provisioning/dashboards`

Implemented dashboard:

- `Agile Microservice Overview`

Current dashboard focus:

- service availability
- healthy service count
- heap memory usage
- HTTP request rate

## 9. Technologies Used

Core backend:

- Java 17
- Spring Boot 4.0.6
- Spring Cloud 2025.1.1

Service platform:

- Spring Cloud Config
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud OpenFeign

Persistence:

- Spring Data JPA
- Hibernate
- PostgreSQL

Security:

- Spring Security
- JJWT

Messaging:

- Apache Kafka
- Zookeeper

Testing:

- JUnit 5
- Spring Boot Test
- H2 for test runtime contexts

Observability:

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana

DevOps:

- Docker
- Docker Compose

## 10. CI/CD Status

Current repository state:

- there is no CI/CD pipeline file currently committed
- there is no GitHub Actions, GitLab CI, Jenkinsfile, or similar automation in the repo

What is already in place that supports future CI/CD:

- reproducible Maven build
- automated `mvn test`
- Docker image build per service
- fully containerized local integration environment

Recommended future CI/CD pipeline:

1. checkout source
2. run `mvn test`
3. run `mvn package`
4. build Docker images
5. run smoke checks with Docker Compose
6. publish images to a registry
7. deploy to staging or production environment

## 11. Current Strengths

- clear microservice separation by business responsibility
- infrastructure components already integrated
- event-driven notification model
- centralized configuration and discovery
- Docker-based local deployment
- working metrics and dashboard stack

## 12. Current Gaps and Improvement Opportunities

- no formal CI/CD pipeline committed yet
- no database migration tooling such as Flyway or Liquibase
- JWT model is still minimal
- no distributed transaction pattern such as outbox or saga
- no frontend application in the repository
- observability is metrics-focused; centralized logs and tracing are not implemented

## 13. Summary

This project is a strong backend microservice foundation for agile work management. It already contains the essential runtime building blocks expected in a modern distributed system: gateway, discovery, configuration, messaging, persistence isolation, metrics, dashboards, and Docker-based orchestration.

The platform is most mature as a backend/demo and local deployment environment. The next steps for production readiness would be CI/CD automation, migration tooling, stronger authorization, tracing/log aggregation, and transactional reliability patterns across service boundaries.
