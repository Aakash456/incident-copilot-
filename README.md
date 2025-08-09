# Incident Copilot (TiDB Serverless + Agentic RAG)

An agentic incident assistant that ingests runbooks and logs into **TiDB Serverless** with vector embeddings. When an incident or question arises, it performs **hybrid retrieval** (vector + keyword + recency), summarizes the root cause, classifies severity, and **executes actions** (like posting to Slack or creating a GitHub issue).

-----

## Quick Start

### Prerequisites

  - JDK 21+
  - Maven 3.9+
  - TiDB Serverless credentials
  - OpenAI API key (or compatible API)
  - **Optional:** Slack and GitHub tokens for action execution

### Setup

1.  **Create a TiDB Serverless cluster.**

      - Note the host, user, and password.
      - Create a database named `incident_copilot`.

2.  **Configure environment variables.**

      - Copy `.env.example` to `.env` and fill in your credentials, or export them directly in your shell.

    <!-- end list -->

    ```bash
    export TIDB_JDBC_URL='jdbc:mysql://<host>:4000/incident_copilot?sslMode=VERIFY_IDENTITY'
    export TIDB_USER='xxx'
    export TIDB_PASS='xxx'
    export OPENAI_API_KEY='sk-...'
    export OPENAI_BASE='https://api.openai.com/v1'
    export EMBEDDINGS_MODEL='text-embedding-3-large'
    export CHAT_MODEL='gpt-4o-mini'

    # Optional actions
    export SLACK_BOT_TOKEN='xoxb-...'
    export SLACK_CHANNEL_ID='C12345678'
    export GITHUB_TOKEN='ghp_...'
    export GITHUB_REPO='you/incident-copilot'
    ```

3.  **Build and Run the application.**

    ```bash
    mvn -q -DskipTests spring-boot:run
    ```

4.  **Access the OpenAPI UI.**

      - Open your browser and navigate to `http://localhost:8080/swagger-ui/index.html` to interact with the API.

-----

### Sample CURL Commands

You can use these `curl` commands to interact with the running application.

#### Ingest a URL

Ingest a document from a URL to be processed and stored.

```bash
curl -s -X POST http://localhost:8080/ingest/docs \
  -H "Content-Type: application/json" \
  -d '{"url":"https://raw.githubusercontent.com/pingcap/tidb/master/README.md","tags":["db","runbook"]}'
```

#### Ask a Question

Ask a question and receive a root cause summary.

```bash
curl -s -X POST http://localhost:8080/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"p95 latency spiked in prod for payments-api","context":{"service":"payments-api","env":"prod"}}' | jq .
```

#### Apply Actions

Execute actions like posting to Slack or creating a GitHub issue.

```bash
curl -s -X POST http://localhost:8080/actions/apply \
  -H "Content-Type: application/json" \
  -d @sample-actions.json | jq .
```
