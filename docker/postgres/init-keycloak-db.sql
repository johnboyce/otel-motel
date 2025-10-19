-- Initialize Keycloak database
-- This script is automatically executed when the PostgreSQL container starts for the first time
-- Note: The PostgreSQL Docker image creates the database and user from environment variables
-- (POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD), so they should already exist.
-- This script runs additional setup and validation.

-- Log script execution
\echo '=================================================='
\echo 'Starting Keycloak database initialization script'
\echo '=================================================='

-- Ensure we're running as the postgres superuser
-- (this script runs automatically during container initialization)

-- Verify the keycloak user exists (created by env vars)
DO $$
BEGIN
  IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
    RAISE NOTICE 'User "keycloak" exists - OK';
  ELSE
    RAISE EXCEPTION 'User "keycloak" does not exist! This should have been created from POSTGRES_USER environment variable.';
  END IF;
END
$$;

-- Verify the keycloak database exists (created by env vars)
DO $$
BEGIN
  IF EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak') THEN
    RAISE NOTICE 'Database "keycloak" exists - OK';
  ELSE
    RAISE EXCEPTION 'Database "keycloak" does not exist! This should have been created from POSTGRES_DB environment variable.';
  END IF;
END
$$;

-- Ensure keycloak user owns the keycloak database
ALTER DATABASE keycloak OWNER TO keycloak;

-- Grant all privileges to keycloak user on keycloak database
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

\echo 'Connecting to keycloak database...'

-- Connect to the keycloak database
\c keycloak

\echo 'Connected to keycloak database'

-- Grant schema privileges to keycloak user
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO keycloak;

-- Set default privileges for future objects created by any user
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO keycloak;

-- Set timezone for consistency
SET timezone = 'UTC';

-- Create extensions that Keycloak might need
-- (uuid-ossp is commonly used for UUID generation)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\echo '=================================================='
\echo 'Keycloak database initialization completed!'
\echo '=================================================='
\echo ''
\echo 'Database: keycloak'
\echo 'Owner: keycloak'
\echo 'Schema: public (with full privileges)'
\echo 'Extensions: uuid-ossp'
\echo ''

-- Validation: Show database and user info
SELECT 
  d.datname as database,
  pg_catalog.pg_get_userbyid(d.datdba) as owner,
  pg_catalog.pg_encoding_to_char(d.encoding) as encoding,
  d.datcollate as collate,
  d.datctype as ctype
FROM pg_catalog.pg_database d
WHERE d.datname = 'keycloak';

\echo ''
\echo 'Initialization script completed successfully!'
\echo '=================================================='
