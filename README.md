## Incident Management System  

##### A full-stack incident management platform for housing-related issues.

---
### Architecture
This system follows a **REST API + Static Frontend** architecture:
- **Backend**: Spring Boot REST API endpoints (`/api/*`) for all data operations
- **Frontend**: Static HTML/CSS/JavaScript files served from `src/main/resources/static/`
- **Authentication**: OAuth2 GitHub login with role-based access control
- **File Storage**: MinIO (S3-compatible) for incident attachments
- **Database**: PostgreSQL with Flyway migration management

---

### Tech Stack
#### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Main programming language |
| Spring Boot | 4.0.4 | Application framework |
| Spring Security | - | Authentication & Authorization |
| Spring Data JPA | - | Database ORM |
| Spring OAuth2 Client | - | GitHub login integration |
| PostgreSQL | 17.9 | Relational database |
| Flyway | - | Database migration management |
| MinIO | - | S3-compatible file storage |
| Hibernate | 7.2.7 | JPA implementation |
| Maven | 3.9.11 | Build tool |

#### Frontend
| Technology | Purpose |
|------------|---------|
| HTML5 | Structure |
| CSS3 | Styling (custom, no frameworks) |
| Vanilla JavaScript | Dynamic content, API calls |
| Fetch API | REST API communication |

#### Testing
| Technology | Purpose |
|------------|---------|
| JUnit 5 | Unit and integration tests |
| Mockito | Mocking framework |
| Spring Boot Test | Test utilities |
| JaCoCo | Code coverage |

---
### Features
#### Authentication & Authorization
- **GitHub OAuth2 login** - Secure authentication via GitHub
- **Role-based access control (RBAC)** - Four distinct user roles:
    - `PENDING` - Awaiting admin approval, no system access
    - `RESIDENT` - Can create and view own incidents, add comments
    - `HANDLER` - Can manage assigned incidents, change status, resolve/close
    - `ADMIN` - Full system access, user management, audit logs

### Incident Management
- Create incidents with subject, description, category, and attachments
- Upload multiple files (images, PDFs, documents) to MinIO
- Update incident status: `OPEN → IN_PROGRESS → RESOLVED → CLOSED`
- Assign incidents to handlers (admin only)
- Filter and search incidents by status, subject, or description

### Comments & Activity
- Add comments to incidents
- Automatic activity logging for all incident actions
- Real-time notifications for status changes and assignments

### Admin Panel
- **User Management** - View, approve, edit roles, activate/deactivate, delete users
- **Incident Management** - View all incidents, assign handlers, update status
- **Audit Log** - Complete history of all system actions with search
- **Statistics Dashboard** - System overview with user and incident metrics

### Developer Mode
- **User Switcher** - Instantly switch between test users without re-authentication
- **Test Users** - Preconfigured users for all roles (RESIDENT, HANDLER, ADMIN)
- **Dev-only endpoints** - Access user data and switch accounts
---
### Required Software
- **Java 25**
- **Maven 3.9+**
- **PostgreSQL 17+**
- **Docker** (optional, for MinIO)

### Environment Variables
Create the following environment variables or add them to IntelliJ run configuration:

| Variable | Description | Example |
|----------|-------------|---------|
| `GITHUB_CLIENT_ID` | GitHub OAuth App Client ID | `Ov23li...` |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth App Client Secret | `abcdef...` |
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/team6_db` |
| `DB_USERNAME` | Database username | `team6` |
| `DB_PASSWORD` | Database password | `your_password` |

### Setting up GitHub OAuth App
1. Go to GitHub Settings → Developer settings → OAuth Apps
2. Click "New OAuth App"
3. Fill in:
    - **Application name**: `Incident Management System`
    - **Homepage URL**: `http://localhost:8080`
    - **Authorization callback URL**: `http://localhost:8080/login/oauth2/code/github`
4. Copy the **Client ID** and **Client Secret**

### IntelliJ Run Configuration (Recommended)
1. Run → Edit Configurations → Spring Boot → team6-backend
2. Modify options → Environment variables
3. Add:  
GITHUB_CLIENT_ID=[your_client_id];  
GITHUB_CLIENT_SECRET=[your_secret];  
DB_USERNAME=team6;  
DB_PASSWORD=team6-backend;  
DB_URL=jdbc:postgresql://localhost:5432/team6_db
---
### Setup & Installation

1. Clone the repository
```bash
git clone https://github.com/ithsjava25/team6-backend.git
cd team6-backend
```
2. Start PostgreSQL with Docker
3. Start MinIO (File Storage) with Docker


MinIO Console: http://localhost:9001  
Access Key: minioadmin  
Secret Key: minioadmin  
Create a bucket named documents  

4. Configure application.yml
5. Run the application

#### Normal mode  
mvn spring-boot:run  

#### Development mode (with user switcher)  
mvn spring-boot:run '-Dspring-boot.run.profiles=dev'  

6. Access the application  
Application: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html (Admin only)   
MinIO Console: http://localhost:9001  

---
### Database

#### Flyway Migrations

Database schema is managed with Flyway.
Migration files are located in:
`src/main/resources/db/migration/`

| Migration                               | Description                                               |
| --------------------------------------- | --------------------------------------------------------- |
| V1__create_app_user_table.sql           | User table with roles (PENDING, RESIDENT, HANDLER, ADMIN) |
| V3__create_incident_table.sql           | Incident table with status and assignments                |
| V4__create_comment_table.sql            | Comments on incidents                                     |
| V5__insert_test_users.sql               | Test users for development                                |
| V6__insert_test_incidents.sql           | Test incidents                                            |
| V7__create_activity_log_table.sql       | Activity log for incident actions                         |
| V8__create_document_table.sql           | File attachments storage                                  |
| V9__create_notification_table.sql       | User notifications                                        |
| V10__change_timestamp_to_timestampz.sql | Convert timestamps to timezone-aware                      |
| V11__create_audit_log_table.sql         | System-wide audit log                                     |

---

### Test Users

| GitHub Login   | Name            | Role     | Active |
| -------------- | --------------- | -------- | ------ |
| test_resident  | Test Resident   | RESIDENT | ✓      |
| test_handler   | Test Handler    | HANDLER  | ✓      |
| test_pending   | Test Pending    | PENDING  | ✓      |
| test_resident2 | Test Resident 2 | RESIDENT | ✓      |
| test_inactive  | Test Inactive   | RESIDENT | ✗      |
| test_admin     | Test Admin      | ADMIN    | ✓      |


### Development Mode

Dev mode provides a user switcher that allows developers to instantly switch between different user accounts without logging in/out.
Perfect for testing roles and permissions.

---

#### Enable Dev Mode

```bash id="devcmd1"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

#### IntelliJ Setup

* Go to: `Run → Edit Configurations`
* Set **Active profiles**: `dev`

Or add VM option:

```bash id="devcmd2"
-Dspring-boot.run.profiles=dev
```

---

#### Features

* User switcher dropdown
* Instant switching between roles:

    * RESIDENT
    * HANDLER
    * ADMIN
    * PENDING

---

#### Dev Endpoints

```text id="devendpoints"
GET  /dev/enabled                Check if dev mode is active
GET  /dev/users                  HTML page with all users
GET  /dev/all-users              JSON list of all users
GET  /dev/switch-user?githubLogin=X  Switch to user X
GET  /dev/test                   Test endpoint
```

---

#### Dev UI

When dev mode is active:

* A **"Developer Mode"** button appears on the index page
* A **user switcher dropdown** appears in the dashboard
* You can switch roles instantly without re-authentication
---

##### API Documentation
```text
Public Endpoints (no authentication)
Method    Endpoint              Description
GET       /                     Landing page
GET       /index.html           Login page
GET       /dev/**               Dev mode endpoints

User Endpoints
Method    Endpoint                        Role            Description
GET       /api/users/me                  Authenticated   Get current user info
GET       /api/users/{userId}            Authenticated   Get user by ID
GET       /api/users/role/{role}         Authenticated   Get users by role

Incident Endpoints
Method    Endpoint                              Role                    Description
POST      /api/incidents                        RESIDENT/ADMIN          Create incident
GET       /api/incidents/my                     RESIDENT                Get my incidents
GET       /api/incidents/assigned               HANDLER                 Get assigned incidents
GET       /api/incidents/all                    ADMIN                   Get all incidents
GET       /api/incidents/{id}                   RESIDENT/HANDLER/ADMIN  Get incident by ID
PATCH     /api/incidents/{id}/status            ADMIN/HANDLER           Update status
PATCH     /api/incidents/{id}/close             ADMIN/HANDLER           Close incident
PATCH     /api/incidents/{id}/resolve           ADMIN/HANDLER           Resolve incident
PATCH     /api/incidents/{id}/assign            ADMIN                   Assign to handler
PATCH     /api/incidents/{id}/unassign          ADMIN                   Unassign incident
GET       /api/incidents/handlers               ADMIN                   Get all handlers

Admin Endpoints
Method    Endpoint                              Role    Description
GET       /api/admin/users                      ADMIN   Get all users
GET       /api/admin/users/pending              ADMIN   Get pending users
GET       /api/admin/users/{userId}             ADMIN   Get user by ID
POST      /api/admin/users/{userId}/approve     ADMIN   Approve user
PATCH     /api/admin/users/{userId}/role        ADMIN   Update role
PATCH     /api/admin/users/{userId}/status      ADMIN   Activate/deactivate user
DELETE    /api/admin/users/{userId}             ADMIN   Delete user
GET       /api/admin/stats                      ADMIN   System statistics
GET       /api/admin/audit                      ADMIN   Audit logs
GET       /api/admin/audit/user/{userId}        ADMIN   Audit logs by user

Comment Endpoints
Method    Endpoint                              Role            Description
GET       /comments/incident/{incidentId}       Authenticated   Get comments
POST      /comments                            Authenticated   Add comment

Document Endpoints
Method    Endpoint                              Role            Description
GET       /documents/{fileKey}                  Authenticated   Download file
POST      /documents/upload/{incidentId}        Authenticated   Upload files
DELETE    /documents/{documentId}               Authenticated   Delete file

Notification Endpoints
Method    Endpoint                              Role            Description
GET       /notifications/user                   Authenticated   Get notifications
GET       /notifications/user/unread-count      Authenticated   Get unread count
PATCH     /notifications/{id}/read              Authenticated   Mark as read

Activity Endpoints
Method    Endpoint                              Role            Description
GET       /activity/incident/{incidentId}       Authenticated   Get activity log
```
---
#### Project Structure
```text
team6-backend/
├── src/
│ ├── main/
│ │ ├── java/org/example/team6backend/
│ │ │ ├── activity/ # Activity log module
│ │ │ ├── admin/ # Admin controller
│ │ │ ├── auditlog/ # Audit log module
│ │ │ ├── comment/ # Comment module
│ │ │ ├── config/ # Spring configuration
│ │ │ ├── dev/ # Dev mode controller
│ │ │ ├── document/ # Document/MinIO module
│ │ │ ├── exception/ # Global exception handler
│ │ │ ├── incident/ # Incident module (core)
│ │ │ ├── notification/ # Notification module
│ │ │ ├── security/ # OAuth2 security config
│ │ │ ├── user/ # User module
│ │ │ └── Team6BackendApplication.java
│ │ ├── resources/
│ │ │ ├── static/ # Frontend files
│ │ │ │ ├── index.html
│ │ │ │ ├── dashboard.html
│ │ │ │ ├── incidents.html
│ │ │ │ ├── viewincident.html
│ │ │ │ ├── createincident.html
│ │ │ │ ├── profile.html
│ │ │ │ ├── admin.html
│ │ │ │ ├── images/
│ │ │ │ └── css/ (embedded)
│ │ │ ├── db/migration/ # Flyway migrations (V1-V11)
│ │ │ ├── application.yml
│ │ │ └── application-dev.yml
│ │ └── test/ # Unit and integration tests
│ └── pom.xml
├── docker-compose.yml # Docker Compose configuration
├── .gitignore
└── README.md
```
---
```text
Action                     PENDING   RESIDENT   HANDLER              ADMIN
---------------------------------------------------------------------------
View own profile           ✓         ✓          ✓                    ✓
Create incident            ✗         ✓          ✗                    ✓
View own incidents         ✗         ✓          ✗                    ✓
View assigned incidents    ✗         ✗          ✓                    ✓
View all incidents         ✗         ✗          ✗                    ✓
Add comments               ✗         ✓          ✓                    ✓
Change incident status     ✗         ✗          ✓ (assigned only)    ✓
Close/Resolve incident     ✗         ✗          ✓ (assigned only)    ✓
Assign incidents           ✗         ✗          ✗                    ✓
View audit logs            ✗         ✗          ✗                    ✓
Manage users               ✗         ✗          ✗                    ✓
Delete incidents           ✗         ✗          ✗                    ✓
```


---

### Security
```text
OAuth2 Flow
-----------
User clicks "Sign in with GitHub"
Redirected to GitHub authorization page
User authorizes the application
GitHub redirects back with authorization code
Backend exchanges code for access token
Backend fetches user info from GitHub API
User is created/updated in database
Session is created with role-based authorities
```

---
#### Role Hierarchy
```text
ADMIN
│
├── HANDLER
│     │
│     └── RESIDENT
│           │
│           └── PENDING
│
└── (all permissions)  
Security Rules (PreAuthorize)
java
@PreAuthorize("hasRole('RESIDENT')")           // Resident only
@PreAuthorize("hasRole('HANDLER')")            // Handler only
@PreAuthorize("hasRole('ADMIN')")              // Admin only
@PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')") // Admin or Handler
```
---
#### License  
This project is developed for educational purposes as part of the ITHS Java course.