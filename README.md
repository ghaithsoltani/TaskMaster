# TaskMaster – Phase 1: Requirements Definition

## Overview

TaskMaster is a task management platform designed for small teams (5–20 members) that need a simple and centralized way to manage tasks, track progress, and improve accountability.

This document defines the business requirements, system requirements, architecture, database design, API contracts, and technology decisions before implementation begins.

---

# 1. Business Problem

## Problem Statement

Many small teams manage work using spreadsheets, email threads, or sticky notes. As projects grow, these methods become difficult to maintain and provide limited visibility into team progress.

TaskMaster provides a centralized platform where teams can:

- Create and manage tasks
- Assign work to team members
- Track task status
- Monitor project progress
- Improve team accountability

## Business Value

The primary business value of TaskMaster is:

- Increased visibility into project work
- Better task ownership
- Improved collaboration
- Reduced communication overhead
- Centralized project tracking

## Target Users

| User Type | Role | Needs |
|------------|------|--------|
| Team Member | Individual Contributor | View assigned tasks, update status, add notes |
| Team Lead | Team Manager | Create tasks, assign tasks, monitor workload |
| Admin | System Administrator | Manage users and system configuration |

---

# 2. Functional Requirements

## FR-01: Task Management

### FR-01.1
Users can create tasks with:

- Title
- Description
- Status
- Priority
- Assignee
- Due Date

### FR-01.2
Users can view all tasks.

### FR-01.3
Users can filter tasks by:

- Status
- Priority
- Assignee

### FR-01.4
Users can update task details.

### FR-01.5
Users can delete tasks.

### FR-01.6
Supported task statuses:

- TODO
- IN_PROGRESS
- DONE

---

## FR-02: User Management

### FR-02.1
Users can register using:

- Username
- Email
- Password

### FR-02.2
Users can log in and receive an authentication token.

### FR-02.3
Users can view their profile information.

### FR-02.4 (Optional)
Role-based access:

- USER
- ADMIN

---

## FR-03: Dashboard

### FR-03.1
Dashboard displays:

- Total tasks
- Tasks by status
- Overdue tasks

### FR-03.2
Tasks are displayed in a sortable table.

---

## FR-04: Search & Filtering

### FR-04.1
Search tasks by:

- Title
- Description

### FR-04.2
Filter tasks by:

- Status
- Priority

---

# 3. Non-Functional Requirements

## NFR-01: Performance

- Task list loads in less than 1 second for up to 1,000 tasks.
- API response time below 200ms for standard operations.

## NFR-02: Availability

- Target uptime: 99.9%
- Graceful handling of temporary database outages.

## NFR-03: Security

- Password hashing using BCrypt.
- Protected API endpoints.
- No sensitive data stored in logs.

## NFR-04: Maintainability

- Layered architecture.
- Separation of concerns.
- Structured logging.
- Clean code principles.

## NFR-05: Scalability

- Stateless backend architecture.
- Horizontal scaling support.
- Externalized configuration.

## NFR-06: Observability

- Prometheus metrics support.
- Health check endpoints.
- Structured logs.

---

# 4. API Design

## Base URL

```http
/api/v1
```

---

## Authentication Endpoints

| Method | Endpoint | Description |
|----------|-----------|------------|
| POST | /auth/register | Register new user |
| POST | /auth/login | Authenticate user |
| GET | /auth/me | Get current user |

### Register Request

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123"
}
```

### Register Response

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

---

### Login Request

```json
{
  "username": "john_doe",
  "password": "Password123"
}
```

### Login Response

```json
{
  "token": "jwt-token"
}
```

---

## Task Endpoints

| Method | Endpoint | Description |
|----------|-----------|------------|
| GET | /tasks | List all tasks |
| GET | /tasks/{id} | Get task by ID |
| POST | /tasks | Create task |
| PUT | /tasks/{id} | Update task |
| DELETE | /tasks/{id} | Delete task |

---

## Task Entity

```json
{
  "id": 1,
  "title": "Implement login page",
  "description": "Create Angular login component",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assignee": "john_doe",
  "dueDate": "2026-09-15",
  "createdAt": "2026-08-29T10:00:00Z",
  "updatedAt": "2026-08-29T14:30:00Z"
}
```

---

## User Entity

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "createdAt": "2026-08-29T10:00:00Z"
}
```

---

## HTTP Status Codes

| Code | Description |
|--------|-------------|
| 200 | Success |
| 201 | Resource Created |
| 204 | Resource Deleted |
| 400 | Validation Error |
| 401 | Unauthorized |
| 404 | Resource Not Found |
| 500 | Internal Server Error |

---

# 5. Database Design

## Database

**PostgreSQL 15+**

### Why PostgreSQL?

- ACID compliance
- Excellent Spring Boot support
- JSON support
- Industry-standard relational database
- Strong performance and reliability

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Tasks Table

```sql
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'TODO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    assignee VARCHAR(50),
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Entity Relationship

```text
users (1) ----------- (N) tasks

users.id  ---------> tasks.user_id
```

---

# 6. System Architecture

```text
Client Browser
      |
      v
Angular Frontend
      |
      v
Spring Boot REST API
      |
      v
Service Layer
      |
      v
Repository Layer
      |
      v
PostgreSQL Database
```

---

## Layer Responsibilities

| Layer | Responsibility |
|---------|----------------|
| Controller | Handle HTTP Requests |
| Service | Business Logic |
| Repository | Database Access |
| Entity | Database Mapping |
| DTO | Data Transfer |

---

# 7. Technology Stack

| Technology | Purpose |
|------------|---------|
| Angular | Frontend Framework |
| Spring Boot | Backend Framework |
| Spring Data JPA | ORM Layer |
| Spring Security | Authentication & Authorization |
| PostgreSQL | Relational Database |
| Maven | Build Tool |
| Lombok | Boilerplate Reduction |
| Docker | Containerization |
| Git | Version Control |

---

# 8. Backend Project Structure

```text
taskmaster/
│
├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── security/
│   └── resources/
│
├── database/
│
└── docs/
```

---

# 9. Development Prerequisites

| Tool | Version |
|--------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Angular CLI | 16+ |
| PostgreSQL | 15+ |
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Git | 2.40+ |

---

# Success Criteria

TaskMaster Phase 1 is considered complete when:

- Business requirements are documented.
- Functional requirements are approved.
- Non-functional requirements are defined.
- API contracts are finalized.
- Database schema is designed.
- Architecture is documented.
- Technology stack is validated.
- Development environment requirements are identified.

---

# Next Phase

**Phase 2 – Backend Development**

Goals:

1. Create Spring Boot project.
2. Configure PostgreSQL connection.
3. Implement User Management.
4. Implement Authentication (JWT).
5. Implement Task CRUD APIs.
6. Add validation and exception handling.
7. Write unit and integration tests.

---

**Project:** TaskMaster  
**Version:** 1.0.0  
**Phase:** Requirements Definition  
**Status:** Approved for Development

# TaskMaster — Project 1: Beginner DevOps

A simple task management system built to learn core DevOps practices.

## Business Problem

Small teams need a lightweight tool to track tasks across projects.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 16+, TypeScript, Angular Material |
| Backend | Spring Boot 3.2, Java 17 |
| Database | PostgreSQL 15 |
| Build | Maven, npm |
| Testing | JUnit 5, Jasmine, Karma |

## Architecture


## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### Backend
```bash
cd backend
mvn spring-boot:run
# API at http://localhost:8080/api/v1

cd frontend
npm install
ng serve --proxy-config src/proxy.conf.json
# App at http://localhost:4200

docker run -d \
  --name taskmaster-postgres \
  -e POSTGRES_DB=taskmaster \
  -e POSTGRES_USER=taskmaster_user \
  -e POSTGRES_PASSWORD=taskmaster_pass \
  -p 5432:5432 \
  postgres:15

taskmaster/
├── backend/          # Spring Boot application
├── frontend/         # Angular application
├── database/         # SQL migrations
├── docker/           # Docker configurations
├── jenkins/          # CI/CD pipelines
├── terraform/        # Infrastructure as Code
├── ansible/          # Configuration management
├── kubernetes/       # K8s manifests
├── monitoring/       # Prometheus & Grafana
└── docs/             # Documentation