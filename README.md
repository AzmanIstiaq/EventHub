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
- All documentation is in docs/Milestone3, including test execution evidence, which is under docs/Milestone3/CoverageReport. Run `index.html` in this folder to view.
- Further testing evidence is under Test_Report Sprint 2.md in docs/Milestone3 and in `Test Results — webapp in webapp.html` which must also be run to view.
- Scrum planning is under the 'Planning' tab.

## Records

- Github repository: https://github.com/cosc2299-2025/team-project-group-p04-09
- Github Project Board: https://github.com/orgs/cosc2299-2025/projects/40 (Under Planning Sub-Heading)
- Microsoft Teams Group: https://teams.microsoft.com/l/team/19%3A86kKPFvrRrR25HNSfL55kinBXmcac0b9VkVQjw1AV6o1%40thread.tacv2/conversations?groupId=206cc994-125b-4248-821b-8febbf587784&tenantId=d1323671-cdbe-4417-b4d4-bdb24b51316b
- Microsoft Teams Chat: https://teams.microsoft.com/l/chat/19:71c5c847819f4ef9b4f01913a5e9447b@thread.v2/conversations?context=%7B%22contextType%22%3A%22chat%22%7D

## Table of Contents

- [Requirements](#requirements)
- [Running Locally](#running-the-project)
    - [Running with Docker](#running-with-docker-docker-desktop-must-be-installed)
    - [Profiles](#profiles)
    - [Configuration Files](#configuration-files)
    - [Notes](#notes-1)
- [Using the Website](#using-the-website)
    - [Beginning](#beginning)
    - [Registering-/-Logging-In](#registering--logging-in)
- [Testing](#testing)
- [Other Notes](#other-notes)

## Requirements

- Java 17+, not higher than 24
- Maven 3.8+
- Docker Desktop
- MySQL

---

## Running the Project

### Running With Docker (Docker desktop must be installed)
1.  Ensure Docker Desktop is installed, and running.
2.  Ensure that ports 8080, 8081, and 3306 are free before building.
3. Run the following command in the project root (this should work for all OS):

```bash
 docker-compose up --build
```

4. Visit http://localhost:8080 to access the application.
   The docker profile ensures database connections and other environment variables are configured correctly for containerized execution.

### Profiles

- dev: Local development configuration.
- docker: Configuration for running inside Docker containers.
- default: Fallback if no profile is set.
  Profiles control database connection URLs, credentials, and any other environment-specific settings.

### Configuration Files

- src/main/resources/application.properties – Base configuration.
- src/main/resources/application-dev.properties – Development configuration (used locally).
- src/main/resources/application-docker.properties – Docker-specific configuration (used in container).
  Each profile overrides or extends the base properties as needed.

### Notes

- Always ensure your database is running and accessible before starting the app.
- For local builds, set the correct profile via environment variable.
- Docker profile automatically points to container-compatible database settings.

## Using the Website

### Beginning:
Once the website has been opened, you will be greeted with an index page, showing upcoming events.

These upcoming events can be clicked on, which will take you to a page with more information about the event.

#### Registering / Logging In:

In order to interact properly with the website, you will need to be logged in.

An account of each role has been provided for testing purposes:

- Admin:
  - Username: admin@uni.edu
  - Password: password
- Student/Standard User:
  - Username: alice@uni.edu
  - Password: password 
- Organiser:
    - Username: csit@uni.edu
    - Password: password

Each account role has different permissions, that can be used to test different aspects of the site.

From the start, each user will be associated with a few different events to which they have either created or registered to attend.

Further example accounts are available to see in the database, and in `src/main/java/au/edu/rmit/sept/webapp/WebappApplication.java`.

New accounts can also be created, with the Create Profile button in the top right of the website. New accounts can either be students, or organisers, but not admins.
These new accounts will have no associated events.

## Testing

In order to run tests, mvn must be installed. This can be verified by running `mvn -version` in a terminal.

Navigate to the root directory of the project in a terminal, and run the following command:

`mvn clean test`

On Windows/Powershell, you may need to use `.\mvnw.cmd clean test` or `./mvnw clean test` instead.

This will run all tests, and provide a summary of the results in the terminal.

Otherwise, the tests can also be run be right-clicking the `src/test/java/au/edu/rmit/sept/webapp` folder in an IDE such as IntelliJ, and selecting the option to run all tests.

## Other Notes:
- The current set up retains data between runs, so any changes made to the database will persist.
If you wish to reset the database, you can do so by running the following commands in a terminal (these commands should work for all OS):

```bash
docker-compose down -v
docker-compose up --build
```

- The application can be stopped by pressing `CTRL + C` in the terminal where it is running, and then running `docker-compose down` (this command should work for all OS) to stop and remove the containers.

- If an image fails to upload when interacting with your event as an organiser, choose a smaller image, as there is a size limit on uploads.

- Running the project may take a while, especially the first time.

- The default events are added with times using `LocalDateTime.now().plusDays(X)`, this means on the first run, they will be in the future, but if you stop and restart the application later, they may be in the past.

- Due to the GitHub minutes budget being depleted, the CI/CD and GitHub Actions of this project were not possible to fully test and implement.
