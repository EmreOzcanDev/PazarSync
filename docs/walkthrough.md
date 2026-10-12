# Walkthrough

Read this in the order a sync happens: the mock produces an order, the importer publishes it, the order service stores it once, and the dashboard shows the result.

## Shared events

`common-events` has no Spring. The records are the contract on the wire: `OrderReceived`, `QuestionReceived`, `StockChanged`, `AnswerApproved`, `SyncFailure`, and `ImporterPoll`. `EventCodec` writes them as JSON with Jackson 3, which is what Spring Boot 4 uses. `EventKeys.of` builds the Kafka key `marketplace:id`. `KafkaTopics` is the list of topic names, including `orders.received.DLT` and `importer.poll`.

## Mock marketplace

`mock-marketplace` is a test double, not a third business service. `MarketplaceController` exposes the calls the importer needs: list orders by a date window, list questions, update price and inventory, and post an answer. The shapes follow Trendyol's seller API without copying its paths.

`OrderGenerator` adds a random order or question on a timer, using the same barcodes the order service seeds (`BARCODE-MUG`, `BARCODE-TEE`, `BARCODE-BAG`). `FaultInterceptor` returns HTTP 500 for about 10% of those GETs, and it can fail the first N calls when `pazarsync.mock.faults.fail-first` is set. `MarketplaceStore` sometimes appends the same order twice so the downstream unique key has something to ignore.

## Importer

`MarketplacePoller` is the scheduled loop. It reads `importer.sync_cursor`, asks the mock for everything since that instant, publishes one event per order or open question, and only then moves the cursor. If the call still fails, the cursor stays put and a `SyncFailure` is published whose retry topic is `importer.poll`.

`Retryer` is the backoff. `OrderMapper` is the pure translation from the HTTP page to `OrderReceived`, which is what `OrderMapperTest` and `RetryerTest` cover without Kafka.

`MarketplacePushListener` does the other direction. `stock.changed` becomes a price-and-inventory call. `answers.approved` becomes the answer post. `importer.poll` runs the poll again, which is what the dashboard's retry button triggers for a failed poll.

## Order service

Flyway `V1__schema.sql` creates the tables: `orders`, `order_lines`, `products`, `stock_movements`, `questions`, `answer_drafts`, `sync_failures`, and `users`. `V2` seeds three products. `CREATE EXTENSION vector` is in that first migration so pgvector exists before Spring AI builds its table.

`OrderIngestionService.ingest` is the idempotent write. It inserts the order with `ON CONFLICT DO NOTHING`. A duplicate returns immediately, so stock is not touched. Otherwise it locks the product row, refuses the order when quantity would go negative (the whole transaction rolls back), writes the line and the stock movement, and after commit publishes `StockChanged`. The low-stock flag flips when quantity crosses the product's threshold.

`IngestListeners` is the Kafka edge. A payload that is not JSON becomes `EventReadException`, which `IngestKafkaConfig` marks as not retryable, so it goes straight to the dead-letter topic. The DLT listener and the `sync.failures` listener both insert `sync_failures`.

`QuestionIngestionService` does the same conflict insert for questions, then `QuestionDraftService` asks the `DraftGenerator` for a draft. Under `ai-stub`, `StubDraftGenerator` and `HashEmbeddingModel` need no API key. Under `gemini`, `GeminiDraftGenerator` uses Spring AI's OpenAI client with Gemini's base URL and `/chat/completions`. Approving a draft writes the question and answer into the vector store and publishes `AnswerApproved` after commit.

`SecurityConfig` is stateless JWT. `POST /api/auth/login` checks a BCrypt password and `JwtService` signs an HS256 token whose `role` claim becomes `ROLE_ADMIN`, `ROLE_OPERATOR`, or `ROLE_VIEWER`. Retry is admin-only. Editing and approving a draft is operator or admin. `UserSeeder` inserts the dev users only when `pazarsync.security.seed-users=true`.

The REST controllers are thin: sales by day, recent orders, low stock, open failures plus retry, and questions. springdoc serves `/swagger-ui.html`.

## Tests

`OrderServiceIntegrationTest` needs Docker. It sends the same `OrderReceived` twice and expects one order and one stock movement, runs ten orders against a stock of five, drops a poison payload and waits for `sync_failures`, checks 401 and 403, and checks that a vector search returns the mug answer rather than the invoice answer. `ImporterMockIntegrationTest` starts the mock with two forced HTTP 500s and asserts the importer still publishes the order.

## Dashboard and deploy

The React app in `dashboard/src` is the desk: login, a sales chart, low stock, failures with retry, and question drafts. Nginx (`dashboard/nginx.conf`) serves the build and proxies `/api` to `order-service`.

`deploy/docker-compose.yml` is the local stack. `deploy/docker-compose.prod.yml` drops the local Postgres, caps Kafka at 384 MB, and caps the JVMs for a 2 GB EC2 instance. `deploy/k8s` is Kustomize for Docker Desktop Kubernetes, with probes on the Actuator health groups and StatefulSets for Postgres and Kafka. `deploy/aws/README.md` is the free-plan checklist.
