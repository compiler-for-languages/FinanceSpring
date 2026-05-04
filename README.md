# Finance Data Processing & Dashboard System

A full-stack finance management system with role-based access control, built using Spring Boot (Backend) and React + TypeScript (Frontend).
The system allows Admins, Analysts, and Viewers to interact with financial records securely and efficiently.

---

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* JPA / Hibernate
* MySQL

### Frontend

* React
* TypeScript
* Vite
* Tailwind CSS
* Zustand (State Management)
* TanStack Query (API Handling)

---

## System Overview

The application follows a layered architecture:

```
Frontend (React)
        ↓
REST APIs (Spring Boot Controllers)
        ↓
Service Layer (Business Logic + Role Checks)
        ↓
Repository Layer (JPA)
        ↓
Database (MySQL)
```

Security is enforced using JWT Authentication and Role-Based Access Control.

---

## User Roles & Permissions

### Admin

* Create users (Viewer / Analyst only)
* Create financial records for users
* Update and delete records
* Access all data
* Manage system completely

### Analyst

* View all financial records
* Access dashboard analytics
* View trends, summaries, and insights
* Cannot modify data

### Viewer

* View only their own records
* View personal dashboard
* No access to others’ data
* No modification permissions

---

## Features

### User Management

* Register users with roles
* Role-based restrictions enforced at backend
* Secure login using JWT

### Financial Records

Each record contains:

* Amount
* Type (INCOME / EXPENSE)
* Category
* Date
* Description
* User ID

Operations:

* Create Record (Admin)
* View Records (Role-based)
* Update Record (Admin)
* Delete Record (Admin)
* Filter by date, category, type

### Dashboard APIs

Provides aggregated insights:

* Total Income
* Total Expenses
* Net Balance
* Category-wise Analysis
* Monthly Trends
* Recent Activity

### Access Control

* Viewer → only own data
* Analyst → read all data
* Admin → full control

Enforced using:

* JWT Filter
* Role validation in service layer

### Additional Features

* Pagination support
* Filtering APIs
* DTO-based response structure
* Modular service architecture
* Separate AdminService

---

## Project Structure

```
Finance-System/
├── src/main/java/com/financeProject/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── security/
│   ├── dto/
│   └── entity/

frontend/
├── src/
├── components/
├── pages/
├── store/
├── api/
└── styles/
```

---

## Authentication Flow

1. User logs in
2. JWT token is generated
3. Token is sent in header:
   Authorization: Bearer <token>
4. Backend validates token
5. Role-based access is enforced

---

## API Testing

* Postman
* Swagger UI: /swagger-ui/index.html

---

## Setup Instructions

### Backend

```
cd Finance-System
mvn spring-boot:run
```

### Frontend

```
cd frontend
npm install
npm run dev
```

---

## Key Highlights

* Clean layered architecture
* Secure JWT-based authentication
* Strict role-based access control
* Full-stack integration
* Dashboard analytics
* Modular and scalable design

---

## Author

Developed by: SKG and SSV
Role: Java Backend Developer Intern

---

## Notes

GitHub language statistics show only Java and TypeScript.
Libraries like React, Zustand, and TanStack Query are used within TypeScript and can be verified in package.json.

---

## Future Enhancements

* Soft delete implementation
* Unit and integration testing
* Role-based UI improvements
* Deployment using cloud platforms

---
