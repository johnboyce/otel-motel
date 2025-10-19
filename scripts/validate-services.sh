#!/bin/bash
# Comprehensive validation script for all Docker services
# This script validates each service individually with detailed diagnostics

set -e

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_header() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}========================================${NC}"
}

log_step() {
    echo -e "${BLUE}➜${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

log_info() {
    echo -e "${CYAN}ℹ${NC} $1"
}

# Check if service is running
check_service_running() {
    local service=$1
    if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
        return 0
    else
        return 1
    fi
}

# Check service health status
check_service_health() {
    local service=$1
    local health_status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "no-health-check")
    
    if [ "$health_status" == "healthy" ]; then
        return 0
    else
        return 1
    fi
}

# Get service logs (last 10 lines)
show_service_logs() {
    local service=$1
    local lines=${2:-10}
    echo -e "${YELLOW}Last $lines log lines:${NC}"
    docker logs --tail "$lines" "$service" 2>&1 | sed 's/^/  /'
}

# Validate PostgreSQL
validate_postgres() {
    log_header "Validating PostgreSQL"
    
    log_step "Checking if PostgreSQL container is running..."
    if check_service_running "otel-motel-postgres"; then
        log_success "PostgreSQL container is running"
    else
        log_error "PostgreSQL container is not running"
        return 1
    fi
    
    log_step "Checking PostgreSQL health status..."
    if check_service_health "otel-motel-postgres"; then
        log_success "PostgreSQL is healthy"
    else
        log_warning "PostgreSQL is not healthy yet"
        show_service_logs "otel-motel-postgres" 20
    fi
    
    log_step "Testing PostgreSQL connection..."
    if docker exec otel-motel-postgres pg_isready -U keycloak -d keycloak > /dev/null 2>&1; then
        log_success "PostgreSQL is accepting connections"
    else
        log_error "PostgreSQL is not accepting connections"
        show_service_logs "otel-motel-postgres" 20
        return 1
    fi
    
    log_step "Verifying keycloak database exists..."
    if docker exec otel-motel-postgres psql -U keycloak -d keycloak -c "SELECT 1" > /dev/null 2>&1; then
        log_success "Keycloak database is accessible"
    else
        log_error "Keycloak database is not accessible"
        show_service_logs "otel-motel-postgres" 20
        return 1
    fi
    
    log_step "Checking database owner and privileges..."
    local db_info=$(docker exec otel-motel-postgres psql -U keycloak -d keycloak -tAc "SELECT current_database(), current_user" 2>/dev/null)
    if [ -n "$db_info" ]; then
        log_success "Database connection verified: $db_info"
    else
        log_warning "Could not verify database details"
    fi
    
    log_step "Checking for initialization errors..."
    if docker logs otel-motel-postgres 2>&1 | grep -i "error\|fatal" | grep -v "previous connection" | tail -5; then
        log_warning "Found errors in PostgreSQL logs (see above)"
    else
        log_success "No critical errors in PostgreSQL logs"
    fi
    
    log_success "PostgreSQL validation completed"
    return 0
}

# Validate Keycloak
validate_keycloak() {
    log_header "Validating Keycloak"
    
    log_step "Checking if Keycloak container is running..."
    if check_service_running "otel-motel-keycloak"; then
        log_success "Keycloak container is running"
    else
        log_error "Keycloak container is not running"
        return 1
    fi
    
    log_step "Checking Keycloak health status..."
    if check_service_health "otel-motel-keycloak"; then
        log_success "Keycloak is healthy"
    else
        log_warning "Keycloak is not healthy yet (may still be starting)"
        show_service_logs "otel-motel-keycloak" 20
    fi
    
    log_step "Testing Keycloak database connection..."
    if docker logs otel-motel-keycloak 2>&1 | grep -q "password authentication failed"; then
        log_error "Keycloak has database authentication issues"
        show_service_logs "otel-motel-keycloak" 30
        return 1
    else
        log_success "No database authentication errors detected"
    fi
    
    log_step "Checking if Keycloak is listening on port 8180..."
    if docker exec otel-motel-keycloak sh -c "nc -z 127.0.0.1 8180" 2>/dev/null; then
        log_success "Keycloak is listening on port 8180"
    else
        log_warning "Keycloak is not yet listening on port 8180 (may still be starting)"
    fi
    
    log_step "Testing Keycloak health endpoint..."
    if curl -sf http://localhost:8180/health/ready > /dev/null 2>&1; then
        log_success "Keycloak health endpoint is responding"
    else
        log_warning "Keycloak health endpoint is not ready yet"
    fi
    
    log_step "Checking for startup errors..."
    if docker logs otel-motel-keycloak 2>&1 | grep -i "error\|fatal" | grep -v "WARN" | tail -5; then
        log_warning "Found errors in Keycloak logs (see above)"
    else
        log_success "No critical errors in Keycloak logs"
    fi
    
    log_success "Keycloak validation completed"
    return 0
}

# Validate DynamoDB
validate_dynamodb() {
    log_header "Validating DynamoDB (LocalStack)"
    
    log_step "Checking if DynamoDB container is running..."
    if check_service_running "otel-motel-dynamodb"; then
        log_success "DynamoDB container is running"
    else
        log_error "DynamoDB container is not running"
        return 1
    fi
    
    log_step "Checking DynamoDB health status..."
    if check_service_health "otel-motel-dynamodb"; then
        log_success "DynamoDB is healthy"
    else
        log_warning "DynamoDB is not healthy yet"
        show_service_logs "otel-motel-dynamodb" 15
    fi
    
    log_step "Testing DynamoDB API..."
    if docker exec otel-motel-dynamodb awslocal dynamodb list-tables > /dev/null 2>&1; then
        log_success "DynamoDB API is responding"
    else
        log_error "DynamoDB API is not responding"
        show_service_logs "otel-motel-dynamodb" 15
        return 1
    fi
    
    log_success "DynamoDB validation completed"
    return 0
}

# Validate Elasticsearch
validate_elasticsearch() {
    log_header "Validating Elasticsearch"
    
    log_step "Checking if Elasticsearch container is running..."
    if check_service_running "otel-motel-elasticsearch"; then
        log_success "Elasticsearch container is running"
    else
        log_error "Elasticsearch container is not running"
        return 1
    fi
    
    log_step "Checking Elasticsearch health status..."
    if check_service_health "otel-motel-elasticsearch"; then
        log_success "Elasticsearch is healthy"
    else
        log_warning "Elasticsearch is not healthy yet"
        show_service_logs "otel-motel-elasticsearch" 15
    fi
    
    log_step "Testing Elasticsearch cluster health..."
    local cluster_health=$(curl -sf http://localhost:9200/_cluster/health 2>/dev/null | grep -o '"status":"[^"]*"' || echo "unavailable")
    if [ "$cluster_health" != "unavailable" ]; then
        log_success "Elasticsearch cluster health: $cluster_health"
    else
        log_error "Elasticsearch cluster is not responding"
        return 1
    fi
    
    log_success "Elasticsearch validation completed"
    return 0
}

# Validate Kibana
validate_kibana() {
    log_header "Validating Kibana"
    
    log_step "Checking if Kibana container is running..."
    if check_service_running "otel-motel-kibana"; then
        log_success "Kibana container is running"
    else
        log_error "Kibana container is not running"
        return 1
    fi
    
    log_step "Checking Kibana health status..."
    if check_service_health "otel-motel-kibana"; then
        log_success "Kibana is healthy"
    else
        log_warning "Kibana is not healthy yet"
        show_service_logs "otel-motel-kibana" 15
    fi
    
    log_step "Testing Kibana API..."
    if curl -sf http://localhost:5601/api/status > /dev/null 2>&1; then
        log_success "Kibana API is responding"
    else
        log_warning "Kibana API is not ready yet"
    fi
    
    log_success "Kibana validation completed"
    return 0
}

# Validate OTEL Collector
validate_otel_collector() {
    log_header "Validating OTEL Collector"
    
    log_step "Checking if OTEL Collector container is running..."
    if check_service_running "otel-motel-collector"; then
        log_success "OTEL Collector container is running"
    else
        log_error "OTEL Collector container is not running"
        return 1
    fi
    
    log_step "Checking OTEL Collector health status..."
    if check_service_health "otel-motel-collector"; then
        log_success "OTEL Collector is healthy"
    else
        log_warning "OTEL Collector is not healthy yet"
        show_service_logs "otel-motel-collector" 15
    fi
    
    log_step "Testing OTEL Collector health endpoint..."
    if curl -sf http://localhost:13133/ > /dev/null 2>&1; then
        log_success "OTEL Collector health endpoint is responding"
    else
        log_warning "OTEL Collector health endpoint is not ready yet"
    fi
    
    log_success "OTEL Collector validation completed"
    return 0
}

# Summary function
show_summary() {
    log_header "Validation Summary"
    
    echo -e "${CYAN}Service Status:${NC}"
    
    for service in "otel-motel-postgres" "otel-motel-keycloak" "otel-motel-dynamodb" \
                   "otel-motel-elasticsearch" "otel-motel-kibana" "otel-motel-collector"; do
        if check_service_running "$service"; then
            if check_service_health "$service"; then
                echo -e "  ${GREEN}✓${NC} $service: Running and Healthy"
            else
                echo -e "  ${YELLOW}⚠${NC} $service: Running but not healthy"
            fi
        else
            echo -e "  ${RED}✗${NC} $service: Not running"
        fi
    done
    
    echo ""
    echo -e "${CYAN}Service URLs:${NC}"
    echo -e "  PostgreSQL:     ${GREEN}localhost:5432${NC} (keycloak/keycloak)"
    echo -e "  Keycloak:       ${GREEN}http://localhost:8180${NC} (admin/admin)"
    echo -e "  DynamoDB:       ${GREEN}http://localhost:4566${NC}"
    echo -e "  Elasticsearch:  ${GREEN}http://localhost:9200${NC}"
    echo -e "  Kibana:         ${GREEN}http://localhost:5601${NC}"
    echo -e "  OTEL Collector: ${GREEN}http://localhost:4318${NC} (HTTP) / ${GREEN}http://localhost:4317${NC} (gRPC)"
    echo ""
}

# Main execution
main() {
    log_header "Docker Services Validation Script"
    log_info "This script validates all Docker services with detailed diagnostics"
    echo ""
    
    # Check if docker is running
    if ! docker ps > /dev/null 2>&1; then
        log_error "Docker is not running or not accessible"
        exit 1
    fi
    
    # Validate each service
    validate_postgres || true
    validate_dynamodb || true
    validate_elasticsearch || true
    validate_kibana || true
    validate_otel_collector || true
    validate_keycloak || true
    
    # Show summary
    show_summary
    
    log_header "Validation Complete"
    log_info "Check the results above for any issues"
    echo ""
}

# Run main function
main "$@"
