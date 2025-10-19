-- Initialize Keycloak database
-- This script is automatically executed when the PostgreSQL container starts for the first time

-- The PostgreSQL Docker image creates the database and user from environment variables,
-- but we explicitly ensure they exist here for reliability

-- Create keycloak user if it doesn't exist
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
    CREATE USER keycloak WITH PASSWORD 'keycloak';
    RAISE NOTICE 'User keycloak created';
  ELSE
    RAISE NOTICE 'User keycloak already exists';
  END IF;
END
$$;

-- Create keycloak database if it doesn't exist (should already exist from POSTGRES_DB)
SELECT 'CREATE DATABASE keycloak OWNER keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Grant all privileges to keycloak user on keycloak database
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to the keycloak database
\c keycloak

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;

-- Set timezone for consistency
SET timezone = 'UTC';

-- Optional: Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Log successful initialization
SELECT 'Keycloak database initialized successfully!' AS status;
