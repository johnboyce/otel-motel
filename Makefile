.PHONY: help build clean test run dev docker-up docker-down docker-logs \
        elk-setup elk-health schema-export bruno-generate docs all

# Default target
.DEFAULT_GOAL := help

# Colors for output
CYAN := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

# Variables
MAVEN := ./mvnw
DOCKER_COMPOSE := docker compose
PROJECT_NAME := otel-motel
ELASTICSEARCH_HOST := http://localhost:9200
KIBANA_HOST := http://localhost:5601

##@ General

help: ## Display this help message
	@echo ""
	@echo "$(CYAN)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(CYAN)  $(PROJECT_NAME) - Hotel Booking GraphQL Server$(NC)"
	@echo "$(CYAN)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make $(CYAN)<target>$(NC)\n\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  $(CYAN)%-20s$(NC) %s\n", $$1, $$2 } \
		/^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) }' $(MAKEFILE_LIST)
	@echo ""

all: clean build test ## Clean, build, and test the project

##@ Build & Development

build: ## Build the project
	@echo "$(GREEN)Building $(PROJECT_NAME)...$(NC)"
	$(MAVEN) clean package -DskipTests
	@echo "$(GREEN)✓ Build completed$(NC)"

build-native: ## Build native executable (requires GraalVM)
	@echo "$(GREEN)Building native executable...$(NC)"
	$(MAVEN) package -Dnative -DskipTests
	@echo "$(GREEN)✓ Native build completed$(NC)"

clean: ## Clean build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	$(MAVEN) clean
	@echo "$(GREEN)✓ Clean completed$(NC)"

compile: ## Compile sources without packaging
	@echo "$(GREEN)Compiling sources...$(NC)"
	$(MAVEN) compile
	@echo "$(GREEN)✓ Compilation completed$(NC)"

test: ## Run tests
	@echo "$(GREEN)Running tests...$(NC)"
	$(MAVEN) test
	@echo "$(GREEN)✓ Tests completed$(NC)"

verify: ## Run tests and integration tests
	@echo "$(GREEN)Running verification...$(NC)"
	$(MAVEN) verify
	@echo "$(GREEN)✓ Verification completed$(NC)"

##@ Running the Application

dev: ## Run application in development mode with hot reload
	@echo "$(GREEN)Starting application in dev mode...$(NC)"
	@echo "$(CYAN)GraphQL UI: http://localhost:8080/q/graphql-ui$(NC)"
	@echo "$(CYAN)Dev UI: http://localhost:8080/q/dev$(NC)"
	$(MAVEN) quarkus:dev

run: build ## Build and run the application
	@echo "$(GREEN)Starting application...$(NC)"
	java -jar target/quarkus-app/quarkus-run.jar

run-native: build-native ## Build and run native executable
	@echo "$(GREEN)Starting native application...$(NC)"
	./target/$(PROJECT_NAME)-1.0-SNAPSHOT-runner

##@ Docker & Infrastructure

docker-up: ## Start all Docker services (PostgreSQL, ELK, OTEL Collector)
	@echo "$(GREEN)Starting Docker services...$(NC)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)✓ Services started$(NC)"
	@echo ""
	@echo "$(CYAN)Services:$(NC)"
	@echo "  PostgreSQL:     localhost:5432"
	@echo "  Elasticsearch:  $(ELASTICSEARCH_HOST)"
	@echo "  Kibana:         $(KIBANA_HOST)"
	@echo "  OTEL Collector: localhost:4318 (HTTP) / localhost:4317 (gRPC)"
	@echo "  Logstash:       localhost:5044"
	@echo ""
	@echo "$(YELLOW)Run 'make elk-setup' to initialize Elasticsearch indices$(NC)"

docker-down: ## Stop all Docker services
	@echo "$(YELLOW)Stopping Docker services...$(NC)"
	$(DOCKER_COMPOSE) down
	@echo "$(GREEN)✓ Services stopped$(NC)"

docker-down-volumes: ## Stop Docker services and remove volumes
	@echo "$(RED)Stopping Docker services and removing volumes...$(NC)"
	$(DOCKER_COMPOSE) down -v
	@echo "$(GREEN)✓ Services stopped and volumes removed$(NC)"

docker-logs: ## Show logs from all Docker services
	$(DOCKER_COMPOSE) logs -f

docker-logs-elk: ## Show logs from ELK services only
	$(DOCKER_COMPOSE) logs -f elasticsearch logstash kibana

docker-logs-otel: ## Show logs from OTEL Collector
	$(DOCKER_COMPOSE) logs -f otel-collector

docker-ps: ## Show status of Docker services
	$(DOCKER_COMPOSE) ps

docker-restart: docker-down docker-up ## Restart all Docker services

##@ ELK Stack

elk-setup: ## Initialize Elasticsearch indices and mappings
	@echo "$(GREEN)Setting up Elasticsearch indices...$(NC)"
	@sleep 5  # Wait for Elasticsearch to be ready
	./elk/elasticsearch/setup-indices.sh
	@echo "$(GREEN)✓ ELK setup completed$(NC)"

elk-health: ## Check health of ELK services
	@echo "$(CYAN)Checking Elasticsearch health...$(NC)"
	@curl -s $(ELASTICSEARCH_HOST)/_cluster/health?pretty || echo "$(RED)✗ Elasticsearch not available$(NC)"
	@echo ""
	@echo "$(CYAN)Checking Kibana health...$(NC)"
	@curl -s $(KIBANA_HOST)/api/status || echo "$(RED)✗ Kibana not available$(NC)"

elk-indices: ## List Elasticsearch indices
	@echo "$(CYAN)Elasticsearch indices:$(NC)"
	@curl -s $(ELASTICSEARCH_HOST)/_cat/indices?v

elk-logs: ## View recent logs from Elasticsearch
	@echo "$(CYAN)Recent logs from Elasticsearch:$(NC)"
	@curl -s $(ELASTICSEARCH_HOST)/otel-motel-logs-*/_search?size=10&sort=@timestamp:desc&pretty

elk-delete-indices: ## Delete all otel-motel indices (WARNING: destructive)
	@echo "$(RED)Deleting all otel-motel indices...$(NC)"
	@curl -X DELETE $(ELASTICSEARCH_HOST)/otel-motel-*
	@echo ""
	@echo "$(GREEN)✓ Indices deleted$(NC)"

##@ GraphQL Schema

schema-export: ## Export GraphQL schema
	@echo "$(GREEN)Exporting GraphQL schema...$(NC)"
	@mkdir -p docs/graphql
	@curl -s http://localhost:8080/graphql/schema.graphql > docs/graphql/schema.graphql || echo "$(RED)Server must be running$(NC)"
	@echo "$(GREEN)✓ Schema exported to docs/graphql/schema.graphql$(NC)"

schema-doc: ## Generate GraphQL schema documentation
	@echo "$(GREEN)Generating GraphQL documentation...$(NC)"
	@echo "$(YELLOW)Install graphql-markdown: npm install -g graphql-markdown$(NC)"
	@graphql-markdown docs/graphql/schema.graphql > docs/graphql/API.md 2>/dev/null || echo "$(YELLOW)graphql-markdown not found$(NC)"

##@ Bruno API Collection

bruno-generate: schema-export ## Generate Bruno API collection (requires schema export)
	@echo "$(GREEN)Generating Bruno collection...$(NC)"
	@mkdir -p bruno
	@echo "$(YELLOW)Manual step: Create Bruno collection from schema$(NC)"
	@echo "$(CYAN)Bruno collection should be created in: ./bruno/$(NC)"

bruno-test: ## Run Bruno collection tests
	@echo "$(GREEN)Running Bruno tests...$(NC)"
	@echo "$(YELLOW)Note: Install Bruno CLI: npm install -g @usebruno/cli$(NC)"
	@bru run bruno/ || echo "$(YELLOW)Bruno CLI not installed$(NC)"

##@ Documentation

docs: ## Open documentation in browser
	@echo "$(CYAN)Opening documentation...$(NC)"
	@open README.md || xdg-open README.md || echo "$(YELLOW)Please open README.md manually$(NC)"

docs-serve: ## Serve documentation (requires Python)
	@echo "$(GREEN)Serving documentation at http://localhost:8000$(NC)"
	@cd docs && python3 -m http.server 8000 || echo "$(RED)Python 3 required$(NC)"

##@ Database

db-console: ## Connect to PostgreSQL database
	@echo "$(GREEN)Connecting to database...$(NC)"
	@docker exec -it otel-motel-postgres psql -U otelmotel -d otelmotel

db-reset: ## Reset database (drops and recreates)
	@echo "$(RED)Resetting database...$(NC)"
	@docker exec otel-motel-postgres psql -U otelmotel -c "DROP DATABASE IF EXISTS otelmotel;"
	@docker exec otel-motel-postgres psql -U otelmotel -c "CREATE DATABASE otelmotel;"
	@echo "$(GREEN)✓ Database reset$(NC)"

##@ Monitoring & Observability

otel-metrics: ## View OTEL Collector metrics
	@echo "$(CYAN)OTEL Collector metrics:$(NC)"
	@curl -s http://localhost:8888/metrics

kibana-open: ## Open Kibana in browser
	@echo "$(CYAN)Opening Kibana...$(NC)"
	@open $(KIBANA_HOST) || xdg-open $(KIBANA_HOST) || echo "$(YELLOW)Open $(KIBANA_HOST) manually$(NC)"

graphql-ui: ## Open GraphQL UI in browser
	@echo "$(CYAN)Opening GraphQL UI...$(NC)"
	@open http://localhost:8080/q/graphql-ui || xdg-open http://localhost:8080/q/graphql-ui || echo "$(YELLOW)Open http://localhost:8080/q/graphql-ui manually$(NC)"

##@ Complete Workflows

setup: docker-up elk-setup ## Complete setup (Docker + ELK initialization)
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  ✓ Setup Complete!$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(CYAN)Next steps:$(NC)"
	@echo "  1. Run: make dev"
	@echo "  2. Open GraphQL UI: http://localhost:8080/q/graphql-ui"
	@echo "  3. Open Kibana: $(KIBANA_HOST)"
	@echo ""

start: setup dev ## Complete startup (setup + run in dev mode)

stop: docker-down ## Stop everything

restart: stop start ## Complete restart

##@ Utilities

install-tools: ## Install required development tools (macOS)
	@echo "$(GREEN)Installing development tools for macOS...$(NC)"
	@which brew > /dev/null || (echo "$(RED)Homebrew not found. Install from https://brew.sh$(NC)" && exit 1)
	@echo "$(CYAN)Updating Homebrew...$(NC)"
	@brew update
	@echo "$(CYAN)Installing tools...$(NC)"
	@brew install docker docker-compose curl jq
	@echo "$(GREEN)✓ Tools installed$(NC)"

check-deps: ## Check if required dependencies are installed
	@echo "$(CYAN)Checking dependencies...$(NC)"
	@which java > /dev/null && echo "$(GREEN)✓ Java$(NC)" || echo "$(RED)✗ Java$(NC)"
	@which docker > /dev/null && echo "$(GREEN)✓ Docker$(NC)" || echo "$(RED)✗ Docker$(NC)"
	@which curl > /dev/null && echo "$(GREEN)✓ curl$(NC)" || echo "$(RED)✗ curl$(NC)"
	@$(MAVEN) --version > /dev/null 2>&1 && echo "$(GREEN)✓ Maven$(NC)" || echo "$(RED)✗ Maven$(NC)"

lint: ## Run code linting (requires spotless)
	@echo "$(GREEN)Running linter...$(NC)"
	$(MAVEN) spotless:check || echo "$(YELLOW)Spotless not configured$(NC)"

format: ## Format code (requires spotless)
	@echo "$(GREEN)Formatting code...$(NC)"
	$(MAVEN) spotless:apply || echo "$(YELLOW)Spotless not configured$(NC)"

version: ## Show project version
	@echo "$(CYAN)$(PROJECT_NAME) version:$(NC)"
	@$(MAVEN) help:evaluate -Dexpression=project.version -q -DforceStdout
