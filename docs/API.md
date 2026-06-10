# Agile Microservice System API Documentation

This document describes the backend APIs currently implemented in the project.
It is written for manual testing with Postman.

## Base URLs

When using the API Gateway:

```text
http://localhost:8080
```

Direct service URLs:

```text
Auth Service      http://localhost:8081
Project Service   http://localhost:8082
Work Item Service http://localhost:8083
Sprint Service    http://localhost:8084
```

Notification Service is registered in Docker Compose, but its business API is not implemented yet.

## Authentication

Public endpoints:

```text
POST /api/auth/register
POST /api/auth/login
```

Protected endpoints through the API Gateway require:

```text
Authorization: Bearer <token>
```

The gateway validates the JWT and forwards these headers to downstream services:

```text
X-User-Id: <logged-in-user-id>
X-User-Role: <role>
```

Current limitation: Auth Service does not currently add a `role` claim to tokens, so `X-User-Role` may be empty.

If calling Project Service directly, add this header manually for project creation:

```text
X-User-Id: <user-uuid>
```

## Common Error Format

Project Service and Sprint Service return errors like:

```json
{
  "timestamp": "2026-06-10T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Project ID is required"
}
```

Work Item Service currently returns:

```json
{
  "timestamp": "2026-06-10T15:30:00",
  "message": "Work item not found",
  "status": 404
}
```

## Enum Values

Project:

```text
AgileMethodology: SCRUM, KANBAN
ProjectStatus: ACTIVE, ARCHIVED
ProjectRole: DEV, QA, SCRUM_MASTER, PRODUCT_OWNER
MemberStatus: INVITED, ACTIVE
```

Work Item:

```text
WorkItemType: USER_STORY, BUG, TECHNICAL_TASK
Priority: LOW, MEDIUM, HIGH
WorkItemStatus: TODO, IN_PROGRESS, DONE
```

Sprint:

```text
SprintStatus: PLANNED, ACTIVE, COMPLETED, CANCELLED
```

## Recommended Postman Flow

1. Register a user.
2. Login and copy the token.
3. Create a project.
4. Invite project members if needed.
5. Create work items for the project.
6. Create a sprint for the project.
7. Add work items to the sprint.
8. Check sprint capacity.

# Auth API

## Register

```text
POST /api/auth/register
```

Request:

```json
{
  "firstName": "Khalil",
  "lastName": "AitNouisse",
  "email": "khalil@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "jwt-token",
  "userId": "user-uuid",
  "email": "khalil@example.com"
}
```

## Login

```text
POST /api/auth/login
```

Request:

```json
{
  "email": "khalil@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "jwt-token",
  "userId": "user-uuid",
  "email": "khalil@example.com"
}
```

## Get User By ID

```text
GET /api/auth/users/{id}
```

Response:

```json
{
  "id": "user-uuid",
  "firstName": "Khalil",
  "lastName": "AitNouisse",
  "email": "khalil@example.com"
}
```

# Project API

## List Projects

```text
GET /api/projects
```

Response:

```json
[
  {
    "id": "project-uuid",
    "name": "Agile Platform",
    "description": "Student MVP",
    "methodology": "SCRUM",
    "status": "ACTIVE",
    "ownerId": "user-uuid",
    "createdAt": "2026-06-10T15:30:00",
    "updatedAt": "2026-06-10T15:30:00"
  }
]
```

## Get Project

```text
GET /api/projects/{projectId}
```

## Create Project

```text
POST /api/projects
```

Headers when calling the service directly:

```text
X-User-Id: <user-uuid>
```

Request:

```json
{
  "name": "Agile Platform",
  "description": "Student MVP",
  "methodology": "SCRUM"
}
```

Response: `201 Created`

```json
{
  "id": "project-uuid",
  "name": "Agile Platform",
  "description": "Student MVP",
  "methodology": "SCRUM",
  "status": "ACTIVE",
  "ownerId": "user-uuid",
  "createdAt": "2026-06-10T15:30:00",
  "updatedAt": "2026-06-10T15:30:00"
}
```

Kafka event published:

```text
project.created
```

## Update Project

```text
PUT /api/projects/{projectId}
```

Request:

```json
{
  "name": "Agile Platform v2",
  "description": "Updated project description",
  "methodology": "KANBAN",
  "status": "ACTIVE"
}
```

All fields are optional. Only provided fields are updated.

## Archive Project

```text
DELETE /api/projects/{projectId}
```

Response:

```text
204 No Content
```

The project is archived by setting `status` to `ARCHIVED`.

## List Project Members

```text
GET /api/projects/{projectId}/members
```

Response:

```json
[
  {
    "id": "member-uuid",
    "projectId": "project-uuid",
    "email": "dev@example.com",
    "userId": "user-uuid",
    "role": "DEV",
    "status": "INVITED",
    "invitedAt": "2026-06-10T15:30:00"
  }
]
```

## Invite Member

```text
POST /api/projects/{projectId}/members/invitations
```

Request:

```json
{
  "email": "dev@example.com",
  "userId": "user-uuid",
  "role": "DEV"
}
```

Response: `201 Created`

Kafka event published:

```text
member.invited
```

## Assign Member Role

```text
PUT /api/projects/{projectId}/members/{memberId}/role
```

Request:

```json
{
  "role": "SCRUM_MASTER"
}
```

Kafka event published:

```text
role.assigned
```

# Work Item API

## Get Work Items By Project

```text
GET /api/workitems/project/{projectId}
```

## Get Work Items By Sprint

```text
GET /api/workitems/sprint/{sprintId}
```

## Get Work Item

```text
GET /api/workitems/{workItemId}
```

Response:

```json
{
  "id": "work-item-uuid",
  "title": "Build sprint API",
  "description": "Implement sprint capacity endpoints",
  "type": "TECHNICAL_TASK",
  "priority": "HIGH",
  "status": "TODO",
  "estimation": 8,
  "projectId": "project-uuid",
  "assigneeId": "user-uuid",
  "sprintId": null,
  "createdAt": "2026-06-10T15:30:00"
}
```

## Create Work Item

```text
POST /api/workitems
```

Request:

```json
{
  "title": "Build sprint API",
  "description": "Implement sprint capacity endpoints",
  "type": "TECHNICAL_TASK",
  "priority": "HIGH",
  "estimation": 8,
  "projectId": "project-uuid",
  "assigneeId": "user-uuid",
  "sprintId": null
}
```

Response: `201 Created`

Notes:

- `title`, `type`, and `projectId` are required.
- Project existence is validated through Project Service.

Kafka events:

```text
task.created
task.assigned
```

`task.assigned` is published only when `assigneeId` is present.

## Update Work Item

```text
PUT /api/workitems/{workItemId}
```

Request:

```json
{
  "title": "Build sprint API",
  "description": "Implement and test sprint capacity endpoints",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "estimation": 10,
  "assigneeId": "user-uuid",
  "sprintId": "sprint-uuid"
}
```

All fields are optional. Only provided fields are updated.

Kafka events:

```text
task.status-changed
task.assigned
```

## Delete Work Item

```text
DELETE /api/workitems/{workItemId}
```

Response:

```text
204 No Content
```

# Sprint Capacity API

Sprint Service owns sprint planning and capacity data.
It validates project and work item existence through Feign clients.

## List Sprints By Project

```text
GET /api/sprints/project/{projectId}
```

## Get Sprint

```text
GET /api/sprints/{sprintId}
```

Response:

```json
{
  "id": "sprint-uuid",
  "name": "Sprint 1",
  "goal": "Deliver planning MVP",
  "projectId": "project-uuid",
  "startDate": "2026-06-10",
  "endDate": "2026-06-24",
  "plannedCapacity": 40,
  "status": "PLANNED",
  "capacity": {
    "sprintId": "sprint-uuid",
    "projectId": "project-uuid",
    "plannedCapacity": 40,
    "totalEstimatedLoad": 18,
    "remainingCapacity": 22,
    "overloaded": false,
    "workItemCount": 2,
    "assigneeLoads": [
      {
        "assigneeId": "user-uuid",
        "estimatedLoad": 18,
        "workItemCount": 2
      }
    ]
  },
  "workItems": [
    {
      "id": "sprint-work-item-uuid",
      "sprintId": "sprint-uuid",
      "workItemId": "work-item-uuid",
      "title": "Build sprint API",
      "status": "TODO",
      "estimation": 8,
      "assigneeId": "user-uuid",
      "addedAt": "2026-06-10T15:30:00"
    }
  ],
  "createdAt": "2026-06-10T15:30:00",
  "updatedAt": "2026-06-10T15:30:00"
}
```

## Create Sprint

```text
POST /api/sprints
```

Request:

```json
{
  "name": "Sprint 1",
  "goal": "Deliver planning MVP",
  "projectId": "project-uuid",
  "startDate": "2026-06-10",
  "endDate": "2026-06-24",
  "plannedCapacity": 40
}
```

Response: `201 Created`

Required fields:

```text
name
projectId
startDate
endDate
plannedCapacity
```

Validation:

- `endDate` must be after or equal to `startDate`.
- `plannedCapacity` cannot be negative.
- `projectId` must exist in Project Service.

Kafka event published:

```text
sprint.created
```

## Update Sprint

```text
PUT /api/sprints/{sprintId}
```

Request:

```json
{
  "name": "Sprint 1 Updated",
  "goal": "Complete sprint planning",
  "startDate": "2026-06-10",
  "endDate": "2026-06-24",
  "plannedCapacity": 35,
  "status": "ACTIVE"
}
```

All fields are optional. Only provided fields are updated.

Kafka event:

```text
capacity.overloaded
```

This event is published if the update makes the sprint overloaded.

## Cancel Sprint

```text
DELETE /api/sprints/{sprintId}
```

Response:

```text
204 No Content
```

The sprint is not physically deleted. Its status becomes `CANCELLED`.

## List Sprint Work Items

```text
GET /api/sprints/{sprintId}/workitems
```

Response:

```json
[
  {
    "id": "sprint-work-item-uuid",
    "sprintId": "sprint-uuid",
    "workItemId": "work-item-uuid",
    "title": "Build sprint API",
    "status": "TODO",
    "estimation": 8,
    "assigneeId": "user-uuid",
    "addedAt": "2026-06-10T15:30:00"
  }
]
```

## Add Work Item To Sprint

```text
POST /api/sprints/{sprintId}/workitems
```

Request:

```json
{
  "workItemId": "work-item-uuid"
}
```

Response: `201 Created`

Validation:

- Sprint must exist.
- Sprint must not be `COMPLETED` or `CANCELLED`.
- Work item must exist in Work Item Service.
- Work item must belong to the same project as the sprint.
- Work item cannot already be in the sprint.

Kafka events:

```text
sprint.work-item-added
capacity.overloaded
```

`capacity.overloaded` is published only if the sprint load becomes greater than planned capacity.

## Remove Work Item From Sprint

```text
DELETE /api/sprints/{sprintId}/workitems/{workItemId}
```

Response:

```text
204 No Content
```

## Get Sprint Capacity

```text
GET /api/sprints/{sprintId}/capacity
```

Response:

```json
{
  "sprintId": "sprint-uuid",
  "projectId": "project-uuid",
  "plannedCapacity": 40,
  "totalEstimatedLoad": 42,
  "remainingCapacity": -2,
  "overloaded": true,
  "workItemCount": 5,
  "assigneeLoads": [
    {
      "assigneeId": "user-uuid",
      "estimatedLoad": 20,
      "workItemCount": 2
    }
  ]
}
```

# Infrastructure Endpoints

Health checks:

```text
GET /actuator/health
```

Service ports:

```text
Config Server        8888
Eureka Server        8761
API Gateway          8080
Auth Service         8081
Project Service      8082
Work Item Service    8083
Sprint Service       8084
Notification Service 8085
```

Eureka dashboard:

```text
http://localhost:8761
```

# Pending APIs

Notification Service is not implemented yet. Planned endpoints:

```text
GET /api/notifications
GET /api/notifications/user/{userId}
PUT /api/notifications/{notificationId}/read
```

These should be added after Kafka consumers and notification persistence are implemented.
