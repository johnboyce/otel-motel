#!/bin/bash
# Wait for all Docker services to be healthy
# This script checks the health status of all services defined in docker-compose.yml

set -e

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

MAX_WAIT=300  # Maximum wait time in seconds (5 minutes)
CHECK_INTERVAL=5  # Check every 5 seconds

echo -e "${CYAN}=================================================${NC}"
echo -e "${CYAN}Waiting for all Docker services to be healthy...${NC}"
echo -e "${CYAN}=================================================${NC}"
echo ""

# Services to check (those with health checks defined in docker-compose.yml)
SERVICES=("otel-motel-postgres" "otel-motel-dynamodb" "otel-motel-elasticsearch" "otel-motel-kibana" "otel-motel-otel-collector" "otel-motel-keycloak")

start_time=$(date +%s)

# Function to check if a service is running
check_service_running() {
    local service=$1
    docker ps --format '{{.Names}}' | grep -q "^${service}$"
}

# Function to check if a service is healthy
check_service_health() {
    local service=$1
    local health_status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "no-health-check")
    
    if [ "$health_status" == "healthy" ]; then
        return 0
    else
        return 1
    fi
}

# Function to get service status message
get_service_status() {
    local service=$1
    
    if ! check_service_running "$service"; then
        echo "not running"
        return 1
    fi
    
    local health_status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "no-health-check")
    
    case "$health_status" in
        "healthy")
            echo "healthy"
            return 0
            ;;
        "starting")
            echo "starting"
            return 1
            ;;
        "unhealthy")
            echo "unhealthy"
            return 1
            ;;
        "no-health-check")
            echo "running (no health check)"
            return 0
            ;;
        *)
            echo "unknown"
            return 1
            ;;
    esac
}

# Wait for all services
all_healthy=false
iteration=0
while [ "$all_healthy" = false ]; do
    current_time=$(date +%s)
    elapsed=$((current_time - start_time))
    
    if [ $elapsed -gt $MAX_WAIT ]; then
        echo -e "${RED}=================================================${NC}"
        echo -e "${RED}Timeout waiting for services after ${MAX_WAIT} seconds${NC}"
        echo -e "${RED}=================================================${NC}"
        echo ""
        echo -e "${YELLOW}Current service status:${NC}"
        for service in "${SERVICES[@]}"; do
            status=$(get_service_status "$service" || echo "error")
            echo -e "  ${RED}✗${NC} $service: $status"
        done
        echo ""
        echo -e "${YELLOW}Suggestion: Check service logs with 'docker compose logs <service-name>'${NC}"
        exit 1
    fi
    
    all_healthy=true
    iteration=$((iteration + 1))
    
    echo -e "${CYAN}Check #$iteration (${elapsed}s elapsed):${NC}"
    
    for service in "${SERVICES[@]}"; do
        status=$(get_service_status "$service")
        status_result=$?
        
        if [ $status_result -eq 0 ]; then
            echo -e "  ${GREEN}✓${NC} $service: $status"
        else
            echo -e "  ${YELLOW}⏳${NC} $service: $status"
            all_healthy=false
            
            # Show recent logs for services that are unhealthy
            if [ "$status" == "unhealthy" ]; then
                echo -e "     ${YELLOW}Recent logs:${NC}"
                docker logs --tail 5 "$service" 2>&1 | sed 's/^/     /'
            fi
        fi
    done
    
    if [ "$all_healthy" = false ]; then
        echo ""
        echo -e "${CYAN}Waiting ${CHECK_INTERVAL} seconds before next check...${NC}"
        echo ""
        sleep $CHECK_INTERVAL
    fi
done

echo ""
echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}✅ All services are healthy!${NC}"
echo -e "${GREEN}=================================================${NC}"
echo ""
echo -e "${CYAN}Service details:${NC}"
docker compose ps
echo ""

