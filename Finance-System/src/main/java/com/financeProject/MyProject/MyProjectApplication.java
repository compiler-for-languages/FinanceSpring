package com.financeProject.MyProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
  Main entry point for the FinanceProject Spring Boot application.

  Purpose:
  - Bootstraps and launches the entire Spring Boot application
  - Serves as the starting point for the financial management system
  - Initializes Spring IoC container and embedded servlet container

  Application Overview:
  - Financial record management system with role-based access control
  - Supports three user roles: VIEWER, ANALYST, and ADMIN
  - Provides REST APIs for transaction management and dashboard analytics
  - Implements JWT-based authentication and authorization

  Key Features Enabled by @SpringBootApplication:
  - Auto-configuration: Automatically configures Spring beans based on dependencies
  - Component scanning: Scans for controllers, services, repositories in package
  - Configuration properties: Enables externalized configuration support


   Default Server Configuration:
    - Embedded Tomcat server runs on port 8080
    - Context path: root (/) unless configured otherwise
    - Application properties can be overridden in application.yml or application.properties

   Startup Process:
    1. SpringApplication.run() triggers application context initialization
    2. Embedded Tomcat server starts on configured port
    3. All beans (controllers, services, repositories) are instantiated
    4. Database connections are established
    5. Application is ready to accept HTTP requests

    Environment Configuration:
    - Development: Uses H2 or local MySQL database
    - Production: Uses configured production database with connection pooling
    - Security: JWT secret and database credentials should use environment variables
 */
 @SpringBootApplication
public class MyProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyProjectApplication.class, args);
	}
}
