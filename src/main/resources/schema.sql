CREATE TABLE IF NOT EXISTS documents (
  doc_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255),
  source_url TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doc_chunks (
  chunk_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_id BIGINT NOT NULL,
  ord INT NOT NULL,
  content TEXT NOT NULL,
  embedding VECTOR(1536),
  tokens INT,
  service VARCHAR(64),
  env VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (doc_id) REFERENCES documents(doc_id)
);

CREATE TABLE IF NOT EXISTS log_entries (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ts TIMESTAMP NOT NULL,
  service VARCHAR(64),
  env VARCHAR(32),
  level VARCHAR(8),
  message TEXT,
  hash BINARY(16),
  KEY (service, env, ts)
);

CREATE TABLE IF NOT EXISTS incidents (
  incident_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(16) DEFAULT 'OPEN',
  severity VARCHAR(8) DEFAULT 'SEV3',
  title VARCHAR(255),
  summary_md TEXT,
  suspected_cause VARCHAR(255),
  service VARCHAR(64),
  env VARCHAR(32),
  confidence DECIMAL(5,2)
);

CREATE TABLE IF NOT EXISTS incident_evidence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  incident_id BIGINT NOT NULL,
  chunk_id BIGINT,
  log_id BIGINT,
  score DECIMAL(6,3),
  note VARCHAR(255),
  FOREIGN KEY (incident_id) REFERENCES incidents(incident_id)
);

CREATE TABLE IF NOT EXISTS actions (
  action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  incident_id BIGINT,
  kind VARCHAR(16),
  payload_json JSON,
  status VARCHAR(16) DEFAULT 'PENDING',
  external_ref VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
