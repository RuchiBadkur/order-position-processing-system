# Order Position Processing System

A two-service Java Spring Boot application that reads order updates from a CSV file, validates them, sends valid events to a Position Maintaining Service, and maintains the net position for each symbol.

## Architecture

```text
order_updates.csv
       |
       v
+-------------------------+
| Order Update Service    |
|                         |
| - Read CSV incrementally|
| - Validate rows         |
| - Convert to OrderEvent |
| - Throttle delivery     |
| - Send HTTP events      |
+------------+------------+
             |
             | HTTP POST /events
             v
+-------------------------+
| Position Service        |
|                         |
| - Receive OrderEvent    |
| - Ignore duplicate IDs  |
| - Update positions      |
| - Thread-safe state     |
+------------+------------+
             |
             | GET /position
             v
       Position Map
```
## Services

### Order Update Service

Responsible for:

- Reading the CSV file incrementally, one row at a time
- Validating each row
- Converting valid rows into OrderEvent
- Ignoring duplicate event IDs
- Throttling outgoing events
- Sending valid events to the Position Service
- Logging accepted, rejected, duplicate, successful, and failed events

Default port: 8081

### Position Service

Responsible for:

- Receiving order events over HTTP
- Maintaining net position per symbol in memory
- Applying BUY as positive quantity
- Applying SELL as negative quantity
- Ignoring duplicate event IDs
- Providing current positions through an API

Default port: 8080

## Event Format

An order event contains the following fields:

| Field | Description |
|---|---|
| eventId | Unique logical event identifier |
| symbol | Trading symbol |
| transactionType | BUY or SELL |
| quantity | Positive integer quantity |

Example:

{
"eventId": "evt-001",
"symbol": "TCS",
"transactionType": "BUY",
"quantity": 100
}

## Validation Rules

An event is considered valid only when:

- Event ID is non-empty
- Symbol is non-empty
- Transaction type is exactly BUY or SELL
- Quantity is a positive integer

Invalid rows are logged with the rejection reason and skipped.

Processing continues with subsequent rows even when an invalid row is encountered.

## Communication

The Order Update Service communicates with the Position Service using HTTP.

For each valid order event, the Order Update Service sends a POST request to:

POST http://localhost:8080/events

The event is sent as a JSON payload.

The Position Service receives the event, updates the in-memory position, and keeps track of processed event IDs to prevent duplicate processing.

The Position Service provides the current positions through:

GET http://localhost:8080/position

## Throttling

The Order Update Service limits the rate at which events are sent to the Position Service.

The default maximum rate is 50 events per second.

The rate can be configured using:

order-update.max-events-per-second=50

The rate limiter spaces outgoing requests while preserving the order of events from the CSV file.

## Configuration

The Order Update Service configuration is defined in:

order-update-service/src/main/resources/application.properties

The main configuration properties are:

spring.application.name=order_update_service
server.port=8081
order-update.csv-path=order_updates.csv
position-service.url=http://localhost:8080
order-update.max-events-per-second=50

The following values can be changed without modifying the Java code:

- CSV input file path
- Order Update Service port
- Position Service URL
- Maximum events per second

## Running the Application

The two services should be run as separate processes.

### Start the Position Service

From the project root:

cd position-service

./mvnw spring-boot:run

The Position Service starts on port 8080.

### Start the Order Update Service

Open another terminal and from the project root:

cd order-update-service

./mvnw spring-boot:run

The Order Update Service starts on port 8081 and begins processing the configured CSV file.

The Position Service should be started before the Order Update Service so that it is ready to receive events.

## API

### Get Current Positions

Endpoint:

GET http://localhost:8080/position

Example response:

{
"TCS": 100,
"INFY": -50
}

The response contains every symbol that has been seen in an accepted event, including symbols whose final position is zero.

### Receive Order Event

Endpoint:

POST http://localhost:8080/events

Request body:

{
"eventId": "evt-001",
"symbol": "TCS",
"transactionType": "BUY",
"quantity": 100
}

The Position Service applies the event to the current position for the symbol.

## Duplicate Events

The system tracks processed event IDs in memory.

If an event with the same event ID is received again, the duplicate event is ignored and does not change the position.

The first valid event for an event ID is the one that is processed.

## Delivery Errors

The Order Update Service communicates with the Position Service over HTTP.

If the Position Service is unavailable or an HTTP delivery fails:

- The failure is logged
- The event is not marked as successfully delivered
- Processing continues with subsequent CSV rows

The current implementation does not use a persistent retry queue or message broker.

Therefore, delivery is not guaranteed if the Position Service remains unavailable or the Order Update Service is restarted.

## Thread Safety

The Position Service uses a ReadWriteLock to protect the in-memory position state and processed event IDs.

Write operations are protected by the write lock when an order event updates the position.

Read operations are protected by the read lock when the current positions are requested through the API.

The API returns a copy of the position map so callers cannot directly modify the internal state.

## Persistence

The Position Service stores positions and processed event IDs in memory.

No database or external persistence mechanism is used.

When the Position Service is restarted, all positions and processed event IDs are cleared.

Persistent storage is outside the scope of this assessment.

## Testing

Tests are included for both services.

Run the tests from the respective service directory:

./mvnw test

The tests cover:

- Valid BUY events
- Valid SELL events
- Multiple symbols
- Positive, negative, and zero positions
- Duplicate event IDs
- Invalid transaction types
- Zero quantities
- Negative quantities
- Non-integer quantities
- Blank event IDs
- Blank symbols
- Malformed CSV rows
- Continuing after invalid rows
- Rate limiting
- Position API behavior
- Position Service delivery failures

## Project Structure

The project is divided into two independently runnable Spring Boot services.

### Root Directory
```text
order-position-processing-system/
├── order-update-service/
├── position-service/
├── README.md
└── .gitignore
```

### Order Update Service
```text
order-update-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ruchi/
│   │   │           └── order_update_service/
│   │   │               ├── client/
│   │   │               │   └── PositionServiceClient.java
│   │   │               │
│   │   │               ├── config/
│   │   │               │   ├── PositionServiceProperties.java
│   │   │               │   └── ThrottleProperties.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── OrderEvent.java
│   │   │               │   └── RawOrderRow.java
│   │   │               │
│   │   │               ├── processor/
│   │   │               │   └── OrderUpdateProcessor.java
│   │   │               │
│   │   │               ├── reader/
│   │   │               │   └── OrderCsvReader.java
│   │   │               │
│   │   │               ├── runner/
│   │   │               │   └── OrderUpdateServiceRunner.java
│   │   │               │
│   │   │               ├── throttle/
│   │   │               │   └── RateLimiter.java
│   │   │               │
│   │   │               ├── validation/
│   │   │               │   ├── OrderEventValidator.java
│   │   │               │   └── ValidationResult.java
│   │   │               │
│   │   │               └── OrderUpdateServiceApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── ruchi/
│                   └── order_update_service/
│                       ├── processor/
│                       ├── reader/
│                       ├── throttle/
│                       └── validation/
│
├── order_updates.csv
└── pom.xml
```

### Position Service
```text
position-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ruchi/
│   │   │           └── position_service/
│   │   │               ├── controller/
│   │   │               │   └── PositionController.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   └── OrderEvent.java
│   │   │               │
│   │   │               ├── store/
│   │   │               │   └── PositionStore.java
│   │   │               │
│   │   │               └── PositionServiceApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── ruchi/
│                   └── position_service/
│                       ├── controller/
│                       └── store/
│
└── pom.xml
```

## Design Decisions

### Incremental CSV Processing

The CSV file is processed one row at a time instead of loading the complete file into memory.

This keeps memory usage independent of the total number of rows in the input file.

### HTTP Communication

HTTP was selected for communication between the two services because it is simple to implement, easy to test, and does not require additional infrastructure such as a message broker.

### In-Memory Position Store

The Position Service maintains positions and processed event IDs in memory because persistence is outside the scope of this assessment.

### Duplicate Event Handling

Event IDs are tracked to ensure that the same logical event does not modify a position more than once.

### Thread Safety

A ReadWriteLock is used to protect the shared position state and processed event IDs from concurrent access.

### Configurable Throttling

The maximum event rate is configurable through application properties, with a default limit of 50 events per second.

### Error Handling

Invalid CSV rows are rejected individually and do not stop processing of subsequent rows.

HTTP delivery failures are logged and processing continues with later events.

## Limitations

- Position data is stored only in memory and is lost when the Position Service restarts.
- Processed event IDs are also stored only in memory.
- There is no persistent event store or database.
- There is no durable retry queue or message broker.
- If the Position Service is unavailable, failed HTTP deliveries are logged and processing continues, but guaranteed delivery is not provided.
- The CSV parser assumes standard comma-separated fields and does not support advanced CSV quoting or embedded commas.
- The application is designed to process a finite CSV input file rather than continuously consume a live stream.

## AI Usage Disclosure

AI assistance was used during development for:

- Understanding and breaking down the assessment requirements
- Discussing implementation approaches
- Debugging and resolving development issues
- Identifying test cases and edge cases
- Improving documentation

The final implementation was reviewed and understood during development.

The application was verified using automated tests and manual API testing.
