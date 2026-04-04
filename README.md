# Incident Management System

A full-stack incident management platform.

## Architecture

This system follows a **hybrid REST API approach**:

- **Backend**: Spring Boot REST API endpoints (`/api/*`) for data operations
- **Frontend**: Thymeleaf server-side rendered pages + Vanilla JavaScript for dynamic data fetching
- **Storage**: AWS S3 for incident attachments and file management
- **Security**: OAuth2 GitHub authentication with role-based access control (RBAC)
