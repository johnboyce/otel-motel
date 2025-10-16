-- Initialize Keycloak database
-- This script is automatically executed when the PostgreSQL container starts for the first time

-- Create keycloak database (if not exists)
-- Note: The database is created by POSTGRES_DB env var, this is just documentation

-- Create keycloak user (if not exists)
-- Note: The user is created by POSTGRES_USER env var, this is just documentation

-- Grant privileges to keycloak user on keycloak database
-- Note: This is handled automatically by the postgres image

-- The keycloak user will have full privileges on the keycloak database
-- This allows Keycloak to create and manage its own schema

\c keycloak

-- Set timezone for consistency
SET timezone = 'UTC';

-- Optional: Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Log successful initialization
SELECT 'Keycloak database initialized successfully!' AS status;
