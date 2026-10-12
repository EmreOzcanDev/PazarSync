# AWS free-plan runbook

Do these steps yourself. The application code stays in this repo; this file is the checklist for a single small EC2 instance in front of RDS. Set a billing alert before you create anything that can leave the free plan.

## 1. Billing alert

1. Open Billing → Budgets → Create budget.
2. Use a monthly cost budget of 1 USD (or whatever ceiling you want).
3. Email yourself at 80% and at 100%.

## 2. RDS PostgreSQL

1. Create a PostgreSQL instance on the smallest free-plan-eligible class (for example `db.t3.micro` or `db.t4g.micro` where the free plan still lists it).
2. Use PostgreSQL 16 so it matches the local `pgvector/pgvector:pg16` image.
3. Storage: the free-plan minimum, 20 GB gp2/gp3 if that is what the wizard offers.
4. Public access: no. Put it in the same VPC as the EC2 instance.
5. Initial database name: `pazarsync`. Master user: `pazarsync`.
6. Note the endpoint. You will use it as `SPRING_DATASOURCE_URL`.

pgvector is not installed on stock RDS until you allow the extension and create it:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE DATABASE importer;
```

Connect to the `pazarsync` database for the extension. `CREATE DATABASE importer` is a second database on the same instance for the importer cursor. RDS parameter group must allow `vector` if the console asks you to add it to `shared_preload_libraries` is not required for pgvector; the extension install is enough on current RDS PostgreSQL. If `CREATE EXTENSION` says the extension is not available, add `pgvector` in the option/parameter group the console shows for that engine version, then reboot the instance and retry.

Grant the master user access to `importer` (it owns both databases if it created them).

## 3. EC2

Use a free-plan-eligible type with at least 2 GB RAM (`t3.small` or `t4g.small` if it is still in the free plan; `t3.micro` has 1 GB and is too small for Kafka plus two JVMs).

Security group:

- Inbound 22 from your IP only, so you can SSH.
- Inbound 80 from your IP, or from the world if you want the dashboard public. Leave 8081 closed; Nginx on the dashboard container proxies `/api`.
- The instance's group must be allowed to reach RDS on 5432. Add the EC2 security group as the source of an inbound rule on the RDS security group.
- Do not open 9092 or 5432 to the internet.

User data (paste as the instance user-data script):

```bash
#!/bin/bash
set -eux
dnf update -y || yum update -y
dnf install -y docker git || yum install -y docker git
systemctl enable --now docker
usermod -aG docker ec2-user
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v2.40.0/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```

On a Graviton instance, download `docker-compose-linux-aarch64` instead.

## 4. Start the stack

SSH in, clone the repo, and create `deploy/.env`:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/pazarsync
IMPORTER_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/importer
SPRING_DATASOURCE_USERNAME=pazarsync
SPRING_DATASOURCE_PASSWORD=the-rds-password
JWT_SECRET=replace-with-32-bytes-minimum!!
GEMINI_API_KEY=
SPRING_PROFILES_ACTIVE=ai-stub
```

Use `SPRING_PROFILES_ACTIVE=gemini` only after `GEMINI_API_KEY` is set. The stub profile drafts replies without calling Google.

```bash
cd deploy
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

Memory caps in that file: Kafka heap 384 MB (`KAFKA_HEAP_OPTS`), and the JVMs use `-XX:MaxRAMPercentage` so they stay inside the container limit instead of reading the whole host.

## 5. Teardown

Do this when you are finished, or the free plan hours keep counting.

1. `docker compose -f docker-compose.prod.yml down`
2. Terminate the EC2 instance. Do not only stop it if you want the hours to stop.
3. Delete the RDS instance and turn off automated backups / final snapshot if you do not need the data.
4. Delete unattached EBS volumes and elastic IPs.
5. Confirm the budget page shows no running resources.
