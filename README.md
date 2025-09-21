# RMIT COSC2299 SEPT Major Project

# Group Information

## Group-P04-09

## Members

- Jannik Ernst (s4002696)
- Benjamin Hatfield (s3980868)
- Okasha Varoos (s3990054)
- Abdulahi abdulahi (s3948224)
- Nghi Le Hoang Vinh (s3978216)
- Azman Istiaq Asfi (s4076788)

## Notes:
- All documentation is in docs/Milestone2, including test execution evidence, which is under docs/Milestone2/CoverageReport.
- Scrum planning is under the 'Planning' tab.

## Records

- Github repository: https://github.com/cosc2299-2025/team-project-group-p04-09
- Github Project Board: https://github.com/orgs/cosc2299-2025/projects/40 (Under Planning Sub-Heading)
- Microsoft Teams Group: https://teams.microsoft.com/l/team/19%3A86kKPFvrRrR25HNSfL55kinBXmcac0b9VkVQjw1AV6o1%40thread.tacv2/conversations?groupId=206cc994-125b-4248-821b-8febbf587784&tenantId=d1323671-cdbe-4417-b4d4-bdb24b51316b
- Microsoft Teams Chat: https://teams.microsoft.com/l/chat/19:71c5c847819f4ef9b4f01913a5e9447b@thread.v2/conversations?context=%7B%22contextType%22%3A%22chat%22%7D

## Table of Contents

- [Requirements](#requirements)
- [Running Locally](#running-locally)
  - [Using Terminal / CMD](#using-terminal--cmd)
  - [Using IntelliJ](#using-intellij)
- [Running with Docker](#running-with-docker)
- [Profiles](#profiles)
- [Configuration Files](#configuration-files)

---

## Requirements

- Java 17+
- Maven 3.8+
- Docker (optional for containerized setup)
- MySQL or compatible database

---

## Running Locally

### Using Terminal / CMD

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd team-project-group-p04-09
   ```
2. Create properties files:

- Find src/main/resources folder.
- create new files from the three example property files located in the folder.
- To do this copy the file and paste into the resources folder removing the .example from the filename.
- You can edit the property values as needed but if done with all three files the application should run correctly.

3. Set the active Spring profile and run the application:
   ### Linux / macOS:
   ```bash
   SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
   ```
   ### Windows PowerShell:
   ```bash
   $env:SPRING_PROFILES_ACTIVE="dev"; ./mvnw spring-boot:run
   ```
   ### Using IntelliJ:
   1. Open the project in IntelliJ.
   2. Go to Run → Edit Configurations….
   3. Select your Spring Boot run configuration (WebappApplication).
   4. In Environment variables, add:
   ```bash
   SPRING_PROFILES_ACTIVE=dev
   ```
   5. Apply and run. Changing the profile here allows quick switching between dev, docker, or other profiles.

## Running With Docker (Docker desktop must be installed)

1.  Run the following command in the project root:

```bash
 docker-compose up --build
```

2. Visit http://localhost:8080 to access the application.
   The docker profile ensures database connections and other environment variables are configured correctly for containerized execution.

## Profiles

- dev: Local development configuration.
- docker: Configuration for running inside Docker containers.
- default: Fallback if no profile is set.
  Profiles control database connection URLs, credentials, and any other environment-specific settings.

## Configuration Files

- src/main/resources/application.properties – Base configuration.
- src/main/resources/application-dev.properties – Development configuration (used locally).
- src/main/resources/application-docker.properties – Docker-specific configuration (used in container).
  Each profile overrides or extends the base properties as needed.

## Notes

- Always ensure your database is running and accessible before starting the app.
- For local builds, set the correct profile via environment variable.
- Docker profile automatically points to container-compatible database settings.

```

```
