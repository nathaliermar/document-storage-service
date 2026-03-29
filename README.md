# Document Storage Service

Microservice for uploading and managing medical documents.  
Stores binaries in S3 and metadata in PostgreSQL. Exposes Presigned URLs for direct download — the file never passes through the container.

Used as a dependency by [medical-request-platform](https://github.com/nathaliermar/medical-request-platform).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 · Spring Boot 3.x |
| Architecture | Layered (Controller → Service → Repository) |
| Pattern | Strategy Pattern (StorageStrategy) |
| Binary Storage | AWS S3 + Presigned URL |
| Metadata Storage | PostgreSQL (RDS) |
| ORM | Spring Data JPA / Hibernate |
| Infra | ECS Fargate · ECR · IAM Task Role |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Architecture

```
DocumentController
│
└── DocumentService
    │
    ├── StorageStrategy (interface)
    │   └── S3StorageStrategy
    │       └── AmazonS3Client → S3 Bucket
    │
    └── DocumentRepository
        └── PostgreSQL (RDS)
```

**Upload flow:**
1. Client sends `multipart/form-data` to `POST /documents`
2. `DocumentService` delegates binary persistence to `S3StorageStrategy`
3. Metadata (`fileName`, `s3Key`, `contentType`, `ownerId`) is saved to PostgreSQL
4. Response returns the generated `documentId`

**Download flow:**
1. Client requests `GET /documents/{id}/presigned-url`
2. Service generates a Presigned URL with a 15-minute expiration via AWS SDK
3. Client downloads directly from S3 — the file never travels through the container

---

## Endpoints

| Method | Endpoint | Description | Status |
|---|---|---|---|
| `POST` | `/documents` | Upload a document | `201 Created` |
| `GET` | `/documents/{id}` | Fetch metadata | `200 OK` |
| `GET` | `/documents/{id}/presigned-url` | Generate download URL | `200 OK` |

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

## Running Locally

### Prerequisites
- Java 21
- Maven
- Docker (for local PostgreSQL)
- AWS credentials configured (`~/.aws/credentials` or environment variables)

### 1. Start the local database

```bash
docker run --name postgres-storage \
  -e POSTGRES_DB=document_storage \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:15
```

### 2. Configure `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/document_storage
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update

aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET}
    region: ${AWS_REGION:us-east-1}
```

### 3. Run the application

```bash
mvn spring-boot:run
```

---

## Integration with medical-request-platform

Add the dependency to `medical-request-platform`'s `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

Feign Client:

```java
// Java 21 · Spring Boot 3.x
@FeignClient(name = "document-storage-service", url = "${document.storage.service.url}")
public interface DocumentStorageClient {

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    DocumentResponse upload(@RequestPart("file") MultipartFile file,
                            @RequestPart("ownerId") String ownerId);

    @GetMapping("/documents/{id}")
    DocumentResponse findById(@PathVariable UUID id);

    @GetMapping("/documents/{id}/presigned-url")
    PresignedUrlResponse getPresignedUrl(@PathVariable UUID id);
}
```

`application.yml` for `medical-request-platform`:

```yaml
document:
  storage:
    service:
      url: http://document-storage-service:8080
```

---

## Deploy (ECS Fargate)

```bash
# Build and push the image
docker build -t document-storage-service .
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker tag document-storage-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-storage-service:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-storage-service:latest

# Force a new ECS deployment
aws ecs update-service --cluster medical-cluster \
  --service document-storage-service --force-new-deployment
```

The Task IAM Role requires the following permissions: `s3:PutObject`, `s3:GetObject`, `s3:GeneratePresignedUrl`.  
No AWS credentials are hardcoded — everything is handled via IAM Role.

---

## Project Structure

```
src/main/java/
└── com.example.documentstorage/
    ├── controller/
    │   └── DocumentController.java
    ├── service/
    │   ├── DocumentService.java
    │   └── strategy/
    │       ├── StorageStrategy.java        ← interface
    │       └── S3StorageStrategy.java      ← implementation
    ├── repository/
    │   └── DocumentRepository.java
    ├── entity/
    │   └── Document.java
    └── dto/
        ├── DocumentResponse.java
        └── PresignedUrlResponse.java
```
