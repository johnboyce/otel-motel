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

echo -e "${CYAN}Waiting for all Docker services to be healthy...${NC}"
echo ""

# Services to check (those with health checks defined in docker-compose.yml)
SERVICES=("otel-motel-postgres" "otel-motel-dynamodb" "otel-motel-elasticsearch" "otel-motel-kibana" "otel-motel-keycloak")

start_time=$(date +%s)

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

# Wait for all services
all_healthy=false
while [ "$all_healthy" = false ]; do
    current_time=$(date +%s)
    elapsed=$((current_time - start_time))
    
    if [ $elapsed -gt $MAX_WAIT ]; then
        echo -e "${RED}Timeout waiting for services to be healthy after ${MAX_WAIT} seconds${NC}"
        exit 1
    fi
    
    all_healthy=true
    for service in "${SERVICES[@]}"; do
        if check_service_health "$service"; then
            echo -e "  ${GREEN}✓${NC} $service is healthy"
        else
            echo -e "  ${YELLOW}⏳${NC} $service is not ready yet..."
            all_healthy=false
        fi
    done
    
    if [ "$all_healthy" = false ]; then
        echo ""
        echo -e "${CYAN}Waiting ${CHECK_INTERVAL} seconds before next check... (${elapsed}s elapsed)${NC}"
        echo ""
        sleep $CHECK_INTERVAL
    fi
done

echo ""
echo -e "${GREEN}✅ All services are healthy!${NC}"
echo ""
