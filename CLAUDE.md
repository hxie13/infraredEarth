# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build (from Back-End directory)
cd Back-End && mvn clean package

# Run
java -jar Back-End/target/infrared-0.0.1-SNAPSHOT.jar

# Tests are currently skipped (skipTests=true in pom.xml)
```

Server starts on port 8080 with context path `/infrared`.

## Project Structure

This is a **Spring Boot 3.5.3 + MyBatis** geospatial data platform for infrared earth observation. Single Maven module under `Back-End/`.

```
Back-End/src/main/java/cn/ac/sitp/infrared/
├── config/          # DataSource (Druid+MyBatis), CORS, WebMvc interceptors
├── controller/      # REST endpoints under /rest/*
├── datasource/
│   ├── dao/         # POJOs (AxrrAccount, NC, NaturalDisaster, Job, etc.)
│   ├── mapper/      # MyBatis mapper interfaces
│   └── enumeration/ # Enums (LogActionEnum)
├── security/        # Custom session-based auth (SessionAuthInterceptor)
├── service/         # Business logic interfaces + impl/ subdirectory
├── util/            # Helpers (Util, AESLoginUtil, HttpUtils, DateUtil)
└── web/request/     # Request DTOs (LoginRequest, PaginationRequest, etc.)
```

MyBatis XML mappers: `Back-End/src/main/resources/mapper/*.xml`

## Architecture Notes

- **Database**: PostgreSQL with PostGIS extensions. Connection pool via Druid. Schema in `infrared_earth.sql`.
- **ORM**: MyBatis with XML-based SQL mappings (not JPA). Mapper interfaces scanned from `cn.ac.sitp.infrared.datasource.mapper`.
- **Authentication**: Custom session-based auth using `SessionAuthInterceptor` + `SessionAccountHelper`. No Spring Security or Shiro. Sessions stored in HttpSession with key `"infrared.currentAccount"`. Login uses MD5 password hashing with AES-encrypted transmission.
- **Response format**: All REST responses use `Util.getSuccessResponse(contents)` / `Util.getFailResponse()` / `Util.getLoggedOutResponse()` returning `{"status": "Success|Failure|Logged_out", ...}`.
- **Pagination**: PageHelper plugin via `PageHelper.startPage(currPage, pageSize)` before mapper calls.
- **Frontend**: Static HTML/JS files in `resources/static/` using Vue.js + Element UI + Cesium.js for 3D earth visualization.
- **CORS**: Configured in `GlobalCorsConfig`, allowed origins from `app.cors.allowed-origin-patterns` property.

## Key Configuration

Environment-overridable database config in `application.properties`:
- `spring.datasource.infrareddb.url` (default: `jdbc:postgresql://localhost:5432/infrared_earth`)
- `spring.datasource.infrareddb.username` / `password`

Application properties:
- `app.login.maxfailurecount` (5) - failed attempts before lock
- `app.login.lockminutes` (5) - lock duration
- `app.login.expiredday` (180) - password expiration days
- `app.storage.data-root` - root path for file storage

## REST API Endpoints

All under `/infrared/rest/`:
- `/account` - login, logout, password update
- `/job` - job management, algorithm listing
- `/nc` - NetCDF data queries, file downloads, dataset creation
- `/naturaldisaster` - natural disaster data queries
- `/log` - audit logs (requires authentication)

## Java Version

JDK 21 with `-parameters` compiler flag enabled.
