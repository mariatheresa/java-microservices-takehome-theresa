# Java Microservices Take-home — README

This repository contains a Spring Boot project implementing logical microservices (order, payment,
notification) with an in-memory event bus for communication.

## How to run the services locally

This project is a single Maven-based Spring Boot application that contains three logical
microservices: Order, Payment, and Notification. To run locally you can use the included Maven
wrapper.

Prerequisites:

- Java 21+ (or the JDK version required by the project)
- macOS (instructions assume zsh), or any OS with Java/Maven installed

From the repository root run:

```bash
# Run the application with the Maven wrapper
./mvnw spring-boot:run
```

This starts the Spring Boot application; the server port is configured in
`src/main/resources/application.properties` (default 8080 unless changed).

To build a runnable jar and run it directly:

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

To run tests:

```bash
./mvnw test
```

If you prefer running from an IDE (IntelliJ IDEA):

- Import the project as a Maven project
- Run the `GicJavaApplication` main class (located in
  `sg.com.gic.orderprocessingsystem.GicJavaApplication`)

---

## Architecture overview

High-level components (logical services within the single Spring Boot app):

- Order Service (package: `order`) — handles order creation and state
- Payment Service (package: `payment`) — processes payments for orders
- Notification Service (package: `notification`) — sends notifications (email/SMS/console) about
  state changes
- Event Bus (package: `eventBus`) — lightweight in-memory event-publishing mechanism used for
  asynchronous communication between services
- Config (package: `config`) — application configuration, beans, and wiring

The project is organized as a single Spring Boot application (one process) with separate packages
representing service boundaries. Communication between services is performed by publishing domain
events onto the event bus rather than direct synchronous calls.

Rationale: this structure makes it easy to run and test locally. It follows domain separation while
avoiding the operational overhead of multiple JVM processes for a take-home exercise.

---

## Event flow description

Typical end-to-end flow when an order is created:

1. A client (or test) creates an order via the Order Service API/controller.
2. Order Service creates an `OrderCreatedEvent` (domain event) and publishes it to the Event Bus.
3. Payment Service subscribes to order-related events and receives `OrderCreatedEvent`. It attempts
   to process payment for the order.
    - On success, Payment Service publishes a `PaymentCompletedEvent`.
    - On failure, Payment Service publishes a `PaymentFailedEvent` (or a similar error event).
4. Notification Service subscribes to payment and order events. When it receives
   `PaymentCompletedEvent` or `PaymentFailedEvent`, it sends an appropriate notification (for
   example, an email or console log).
5. Order Service may subscribe to payment events to update the order status (e.g., mark order as
   PAID or PAYMENT_FAILED).

Event types are simple POJOs (domain event classes) and the Event Bus delivers them to all
registered listeners in a decoupled fashion. The current implementation is in-memory and typically
asynchronous or dispatched on an executor depending on configuration.

---

## Design decisions

- In-process event bus: chosen for simplicity and to make the solution runnable without external
  infrastructure. It keeps event-driven design while reducing friction for testing and evaluation.

- Domain events: the services communicate using domain event objects (explicit types) rather than
  passing primitive messages. This improves type safety and readability.

- Package-level service separation: logical separation of services by package within a single JVM.
  This gives modularity for the codebase while keeping startup simple.

- Tests: unit and integration tests should exercise service logic and event interactions. The
  `src/test` show multiple tests and reports present in the repo.

---

## Assumptions

- This is a take-home example and the services are implemented in a single runnable application for
  convenience
- The Event Bus is in-memory and does not provide persistence or cross-process communication
- Notifications are simplified (console output or stubbed) rather than integrating with third-party
  email/SMS providers
- No multi-tenant or production-grade security/config was added unless present in the repo already

---

## Limitations and future improvements

Short-term improvements (low risk):

- Extract each logical service to its own Spring Boot module or separate microservice to more
  closely model a distributed system
- Add configuration profiles for running multiple service instances on different ports for
  integration testing
- Add more comprehensive integration tests (e2e) to cover failures and retry scenarios

Medium-term / production improvements:

- Replace the in-process event bus with a durable message broker (Kafka, RabbitMQ, or similar) to
  support cross-process async communication and replayability
- Implement idempotency keys and persistent outbox pattern to make event publication resilient
- Add observability: distributed tracing (Jaeger/OpenTelemetry), structured logging, and metrics (
  Prometheus)
- Add API gateway / routing, service discovery (Consul/Eureka) and circuit breakers for fault
  tolerance
- Consider separate databases per service and data migration/versioning strategies

---

## Where to look in this repo

- `src/main/java/sg.com.gic.orderprocessingsystem/order` — order  logic
- `src/main/java/sg.com.gic.orderprocessingsystem/payment` — payment logic
- `src/main/java/sg.com.gic.orderprocessingsystem/notification` — notification logic
- `src/main/java/sg.com.gic.orderprocessingsystem/eventBus` — event bus implementation and event types
- `src/main/java/sg.com.gic.orderprocessingsystem/config` — config files
- `src/main/java/sg.com.gic.orderprocessingsystem/exception` — exception logic
- `src/main/resources/application.properties` — application configuration

---


