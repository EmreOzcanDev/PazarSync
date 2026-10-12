# PazarSync

Two Spring Boot services sync marketplace orders, stock, and customer questions through Kafka. A React dashboard shows sales, low stock, and failed syncs, and an operator approves AI reply drafts.

This repository is built in milestones. See the modules below once they land, and `docs/walkthrough.md` for the interview walkthrough.

```text
mock-marketplace -> order-importer -> Kafka -> order-service -> PostgreSQL
```
