# Digital Loan Origination & Credit Decisioning System

<p align="center">
  <img src="images/architecture.png" alt="Digital Loan Origination and Credit Decisioning System architecture" width="100%">
</p>

<p align="center">
  <strong>A distributed lending backend built with Java, Spring Boot, Spring Cloud Gateway, Apache Kafka, PostgreSQL, Redis, Docker, and Razorpay.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-Spring%20Boot-orange?logo=springboot" alt="Java and Spring Boot">
  <img src="https://img.shields.io/badge/Architecture-Microservices-blue" alt="Microservices architecture">
  <img src="https://img.shields.io/badge/Event%20Streaming-Apache%20Kafka-black?logo=apachekafka" alt="Apache Kafka">
  <img src="https://img.shields.io/badge/Database-Neon%20PostgreSQL-4169E1?logo=postgresql" alt="Neon PostgreSQL">
  <img src="https://img.shields.io/badge/Containers-Docker-2496ED?logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/Payments-Razorpay%20Test%20Mode-2B84EA" alt="Razorpay Test Mode">
</p>

<p align="center">
  <a href="#overview">Overview</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#end-to-end-workflow">Workflow</a> ·
  <a href="#screenshots">Screenshots</a> ·
  <a href="#run-locally">Run locally</a>
</p>

---

## Overview

The **Digital Loan Origination & Credit Decisioning System** is a backend-focused lending platform that models important stages of a digital loan lifecycle: customer registration and authentication, loan application, rule-based credit decisioning, document handling, payment processing, repayment scheduling, notifications, and audit events.

The application is divided into independently deployable Spring Boot services. A Spring Cloud API Gateway provides a common entry point, REST/OpenFeign supports synchronous service-to-service calls, and Apache Kafka supports asynchronous event-driven workflows. External integrations include Neon PostgreSQL, Redis, Cloudinary, Razorpay Test Mode, and Gmail SMTP.

The repository also includes a small HTML/CSS/JavaScript checkout page used to exercise the repayment payment flow. It is a test interface for the backend—not a complete customer-facing lending frontend.

### What this project demonstrates

- Designing a multi-service Spring Boot application around business capabilities.
- Routing requests through a centralized API Gateway.
- Applying JWT-based authentication and refresh-token handling.
- Evaluating loan applications with configurable credit-score and debt-to-income (DTI) rules.
- Managing documents and external file storage.
- Integrating a payment provider in test mode and reconciling payment outcomes.
- Generating repayment schedules and tracking installment state.
- Using Kafka to decouple event producers and consumers.
- Sending email notifications and recording audit events.
- Running backend services and Kafka with Docker Compose.

## Key features

| Capability | What it does |
|---|---|
| Customer and authentication | Customer registration, email/password login, JWT access tokens, and refresh tokens |
| Loan origination | Creates loan applications, calculates EMI and interest, and tracks loan status |
| Credit decisioning | Applies configured credit-score and DTI rules; returns `APPROVED`, `REVIEW`, or `REJECTED` with decision information |
| Document management | Associates document metadata with customers and loans; stores uploaded files using Cloudinary |
| Payment processing | Creates Razorpay Test Mode orders, verifies or reconciles payments, and updates payment records |
| Repayment management | Generates installment schedules and tracks due dates, paid amounts, remaining balances, and statuses |
| Notifications | Consumes configured business events and sends relevant email notifications through Gmail SMTP |
| Audit logging | Consumes configured audit events and stores audit records |
| API Gateway | Routes API requests and provides gateway-level configuration, including CORS and request correlation |
| Local orchestration | Builds and runs the services and Kafka through Docker Compose |

## Technology stack

| Area | Technology |
|---|---|
| Language | Java |
| Application framework | Spring Boot |
| Gateway | Spring Cloud Gateway |
| Security | Spring Security, JWT, refresh tokens |
| Service communication | REST APIs, OpenFeign |
| Event streaming | Apache Kafka |
| Relational database | Neon PostgreSQL |
| Caching / session-related functionality | Redis |
| Document storage | Cloudinary |
| Payment integration | Razorpay Test Mode |
| Email delivery | Gmail SMTP |
| Build | Maven |
| Containers | Docker, Docker Compose |
| API testing | Postman |
| Payment test interface | HTML, CSS, JavaScript |

## Architecture

The Gateway is the entry point for client requests. It routes each request to the service responsible for that business capability. Services use REST/OpenFeign when an immediate response is required and Kafka for configured asynchronous event workflows.

### Service map

| Component | Port | Responsibility |
|---|---:|---|
| API Gateway | `8080` | Entry point and API routing |
| Customer Service | `8081` | Customer profiles, registration, and email/password authentication |
| Loan Service | `8082` | Loan applications, EMI calculations, and loan status |
| Credit Decision Service | `8083` | Credit-score / DTI evaluation and decision reasons |
| Document Service | `8084` | Document metadata, upload/retrieval, and Cloudinary integration |
| Payment Service | `8085` | Payment orders, verification, reconciliation, and payment status |
| Repayment Service | `8086` | Repayment schedules and installment state |
| Notification Service | `8087` | Event consumption and email notifications |
| Audit Service | `8088` | Audit event consumption and record storage |
| Kafka (container network) | `9094` | Internal event communication |
| Kafka (host access) | `29092` | Host-side Kafka access, as configured in Compose |

> The service ports above describe the intended local/Docker configuration. Inter-service URLs and port mappings must stay consistent with `backend/docker-compose.yml` and each service's `application.yaml`.

### Communication patterns

**Synchronous — REST/OpenFeign**

Used where a service needs an immediate result, such as validating a customer, retrieving loan information, or coordinating payment and repayment operations.

**Asynchronous — Apache Kafka**

Configured business events allow producers and consumers to communicate without requiring every downstream action to run in the original HTTP request. The Notification and Audit services consume their respective event flows.

### External integrations

- **Neon PostgreSQL:** cloud-hosted relational database.
- **Redis:** configured caching and session-related functionality.
- **Cloudinary:** uploaded document storage.
- **Razorpay Test Mode:** payment-order and checkout testing.
- **Gmail SMTP:** email delivery.

## End-to-end workflow

### 1. Register and authenticate a customer

A customer is created through the customer APIs and can sign in with email and password. The authentication flow returns tokens for authenticated requests. Protected API requests can then be sent through the Gateway.

<p align="center">
  <img src="images/customer-creation.png" alt="Customer registration API response" width="85%">
</p>
<p align="center"><em>Customer creation through the REST API.</em></p>

### 2. Submit a loan application

The client submits the customer ID and application details, such as loan amount, tenure, monthly income, credit score, and purpose. The Loan Service calculates the EMI and interest information and coordinates the credit-decision flow.

<p align="center">
  <img src="images/loan-creation.png" alt="Loan creation response showing EMI, interest rate, and decision status" width="85%">
</p>
<p align="center"><em>Loan API response with calculated loan details and the resulting decision.</em></p>

### 3. Evaluate credit eligibility

The Credit Decision Service applies the rules configured in the project. The result is represented by one of these statuses:

- `APPROVED` — the application meets the configured approval criteria.
- `REVIEW` — the configured rules indicate that further assessment is needed.
- `REJECTED` — the application does not meet the configured criteria.

This is a rule-based demonstration, not a connection to a commercial credit bureau or a production lending policy engine.

### 4. Upload and retrieve loan documents

The Document Service maintains document metadata and its association with customers and loans. Cloudinary stores the uploaded files, while the application exposes APIs for retrieving document information.

<p align="center">
  <img src="images/document-retrieval.png" alt="Documents retrieved by customer ID through the API" width="90%">
</p>
<p align="center"><em>Retrieving customer-associated document records through the API.</em></p>

<p align="center">
  <img src="images/cloudinary-documents.png" alt="Loan platform documents in the Cloudinary media library" width="90%">
</p>
<p align="center"><em>Uploaded document assets in Cloudinary.</em></p>

### 5. Generate a repayment schedule

The Repayment Service generates installments for a loan and tracks details such as installment number, due date, installment amount, paid amount, remaining amount, and payment status.

<p align="center">
  <img src="images/repayment-schedule.png" alt="Generated repayment installment schedule" width="100%">
</p>
<p align="center"><em>Generated installments before repayment.</em></p>

### 6. Create and complete a test payment

The checkout test page collects the loan ID, installment number, amount, and idempotency key. The backend creates a payment order, the Razorpay checkout UI is opened, and the configured verification/reconciliation flow processes the result. Successful payment outcomes are integrated with repayment processing.

<p align="center">
  <img src="images/payment-checkout.png" alt="Digital Loan System repayment checkout form" width="80%">
</p>
<p align="center"><em>Checkout test page used to initiate a repayment.</em></p>

<p align="center">
  <img src="images/payment-success.png" alt="Razorpay Test Mode payment success screen" width="90%">
</p>
<p align="center"><em>Successful Razorpay Test Mode checkout.</em></p>

<p align="center">
  <img src="images/razorpay-dashboard.png" alt="Razorpay dashboard showing the test transaction" width="90%">
</p>
<p align="center"><em>The corresponding transaction shown in the Razorpay dashboard.</em></p>

> **Testing note:** Razorpay is configured in Test Mode. A checkout success screen alone does not prove that every backend state was updated; for an end-to-end test, also confirm the payment record and corresponding repayment state.

### 7. Deliver notifications

Configured Kafka events are consumed by the Notification Service. Where required by the workflow, the service retrieves customer or loan information and sends the relevant email through Gmail SMTP.

<p align="center">
  <img src="images/payment-email.png" alt="Payment notification email" width="90%">
</p>
<p align="center"><em>Example payment-related email notification.</em></p>

### 8. Record audit events

The Audit Service consumes configured audit events and persists audit records. This creates a foundation for traceability and operational investigation of important application activities.

## API testing

Use Postman or another HTTP client to call the Gateway.

**Base URL**

```text
http://localhost:8080
```

Representative endpoints:

| Operation | Method | Endpoint |
|---|---|---|
| Customer registration | `POST` | `/api/customers` |
| Login | `POST` | `/api/auth/login` |
| Create a loan | `POST` | `/api/loans` |
| Retrieve customer documents | `GET` | `/api/documents/customer/{customerId}` |
| Generate a repayment schedule | `POST` | `/api/repayments/schedule/{loanId}` |

These are representative routes from the testing workflow; check the current controllers and Gateway configuration for the exact request bodies, authorization requirements, and available endpoints.

### Login request example

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "your-test-user@example.com",
  "password": "your-test-password"
}
```

Use your own test credentials. Never commit passwords, JWTs, refresh tokens, API keys, or populated environment files.

## Run locally

### Prerequisites

- Java version compatible with the Maven project configuration
- Maven
- Docker and Docker Compose
- Git
- A Neon PostgreSQL database and credentials
- Service credentials for the integrations you intend to test: Redis, Cloudinary, Razorpay Test Mode, and Gmail SMTP

### 1. Clone the repository

```bash
git clone https://github.com/naveendp2805/digital-loan-credit-decisioning-system.git
cd digital-loan-credit-decisioning-system
```

### 2. Configure environment variables

The services use service-specific configuration and `.env` files. Inspect each service's `application.yaml`, its environment-variable references, and `backend/docker-compose.yml`. Configure your own database connection, JWT secret, Kafka settings, and any integration credentials needed by the services you plan to run.

Example variable names only:

```dotenv
DB_URL=your_neon_jdbc_url
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
JWT_SECRET=replace_with_a_secure_secret
KAFKA_BOOTSTRAP_SERVERS=kafka:9094
```

These are illustrative placeholders, not a complete `.env` file. Required names can vary by service.

**Do not commit populated `.env` files or real credentials.** Keep secrets local and use safe example templates if documenting environment variables.

### 3. Validate and start the backend

```bash
cd backend
docker compose config -q
docker compose up -d --build
```

Check container status:

```bash
docker compose ps
```

View Gateway logs:

```bash
docker compose logs --tail=100 api-gateway
```

View recent logs from all services:

```bash
docker compose logs --tail=50
```

### 4. Check the Gateway

If Actuator health is enabled and exposed in the current configuration:

```bash
curl -i http://localhost:8080/actuator/health
```

A healthy Actuator response typically reports `UP`. If this endpoint is not exposed, inspect the service logs or use the health endpoint configured for your application.

### 5. Test the workflow

1. Authenticate using the login API.
2. Send authorized requests through `http://localhost:8080`.
3. Create a customer and submit a loan application using valid test data.
4. Retrieve or upload documents.
5. Generate the repayment schedule.
6. Launch the checkout test page and use Razorpay Test Mode credentials/payment methods.
7. Verify the resulting payment and repayment state, then check the configured notification and audit flows.

The checkout interface is located in `frontend/razorpay-checkout/`. Use `run-razorpay-checkout.sh` and inspect the script before executing it to understand how it starts the local page.

## Repository structure

```text
digital-loan-credit-decisioning-system/
├── backend/
│   ├── api-gateway/
│   ├── customer-service/
│   ├── loan-service/
│   ├── credit-decision-service/
│   ├── document-service/
│   ├── payment-service/
│   ├── repayment-service/
│   ├── notification-service/
│   ├── audit-service/
│   └── docker-compose.yml
├── frontend/
│   └── razorpay-checkout/
│       ├── checkout.html
│       └── run-razorpay-checkout.sh
├── images/
│   ├── architecture.png
│   ├── login.png
│   ├── customer-creation.png
│   ├── loan-creation.png
│   ├── document-retrieval.png
│   ├── cloudinary-documents.png
│   ├── repayment-schedule.png
│   ├── payment-checkout.png
│   ├── payment-success.png
│   ├── razorpay-dashboard.png
│   └── payment-email.png
├── .gitignore
└── README.md
```

## Engineering highlights

- **Service decomposition:** separate Spring Boot applications for distinct lending capabilities.
- **Gateway pattern:** a single entry point for API consumers.
- **Token-based security:** Spring Security with JWT access tokens and refresh-token handling.
- **Distributed communication:** synchronous REST/OpenFeign calls plus asynchronous Kafka events.
- **Financial domain logic:** EMI calculation, configurable credit rules, repayment schedules, and payment-state handling.
- **Third-party integrations:** cloud database, document storage, payment processing, caching, and email.
- **Containerized development:** Docker Compose orchestration for the backend and Kafka.
- **Traceability:** correlation IDs and event-driven audit logging.

## Potential next improvements

These are possible hardening steps rather than claims that all are already implemented:

- Expand unit, integration, and end-to-end test coverage.
- Add versioned database migrations and schema-management practices.
- Further harden payment verification, idempotency, retries, and reconciliation.
- Review Kafka retry and dead-letter handling and improve consumer observability.
- Add centralized metrics, distributed tracing, and structured log aggregation.
- Strengthen production secrets management, rate limiting, and security controls.
- Add CI/CD and deploy to a cloud environment or Kubernetes.
- Build a complete customer-facing frontend and test the full journey through the Gateway.

## Author

**Naveen**

- GitHub: [@naveendp2805](https://github.com/naveendp2805)
- Repository: [Digital Loan Origination & Credit Decisioning System](https://github.com/naveendp2805/digital-loan-credit-decisioning-system)

---

<p align="center">
  <em>Built to explore backend engineering, microservices, credit decisioning, event-driven architecture, and payment integration with Java and Spring Boot.</em>
</p>
