#!/bin/bash

# OTel-Motel Infrastructure Deployment Script
# This script helps deploy the infrastructure to AWS using Pulumi

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_info() {
    echo -e "${BLUE}ℹ ${NC}$1"
}

print_success() {
    echo -e "${GREEN}✓ ${NC}$1"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${NC}$1"
}

print_error() {
    echo -e "${RED}✗ ${NC}$1"
}

# Print header
echo "════════════════════════════════════════════════════════════════"
echo "  OTel-Motel Infrastructure Deployment"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Check prerequisites
print_info "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17 or later."
    exit 1
fi
print_success "Java found: $(java -version 2>&1 | head -n 1)"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven 3.9 or later."
    exit 1
fi
print_success "Maven found: $(mvn --version | head -n 1)"

# Check Pulumi
if ! command -v pulumi &> /dev/null; then
    print_error "Pulumi is not installed."
    print_info "Install with: curl -fsSL https://get.pulumi.com | sh"
    exit 1
fi
print_success "Pulumi found: $(pulumi version)"

# Check AWS CLI
if ! command -v aws &> /dev/null; then
    print_error "AWS CLI is not installed."
    print_info "Install with: pip install awscli"
    exit 1
fi
print_success "AWS CLI found: $(aws --version)"

echo ""

# Check AWS credentials
print_info "Checking AWS credentials..."
if ! aws sts get-caller-identity &> /dev/null; then
    print_error "AWS credentials not configured."
    print_info "Run: aws configure"
    exit 1
fi
print_success "AWS credentials configured"

echo ""

# Prompt for stack selection
print_info "Select deployment stack:"
echo "  1) qa   - QA environment (cost-optimized)"
echo "  2) prod - Production environment (high availability)"
read -p "Enter choice [1-2]: " stack_choice

case $stack_choice in
    1)
        STACK="qa"
        ;;
    2)
        STACK="prod"
        print_warning "You are about to deploy to PRODUCTION!"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" != "yes" ]; then
            print_info "Deployment cancelled."
            exit 0
        fi
        ;;
    *)
        print_error "Invalid choice"
        exit 1
        ;;
esac

echo ""
print_info "Deploying to stack: $STACK"

# Navigate to deploy directory
cd "$(dirname "$0")"

# Build the project
print_info "Building infrastructure code..."
mvn clean package -q
print_success "Build complete"

echo ""

# Initialize or select stack
print_info "Initializing Pulumi stack: $STACK"
if pulumi stack select $STACK 2>/dev/null; then
    print_success "Stack '$STACK' selected"
else
    print_info "Creating new stack: $STACK"
    pulumi stack init $STACK
    print_success "Stack '$STACK' created"
fi

echo ""

# Preview changes
print_info "Previewing infrastructure changes..."
echo ""
pulumi preview

echo ""
print_warning "Review the changes above carefully!"
read -p "Do you want to proceed with deployment? (yes/no): " deploy_confirm

if [ "$deploy_confirm" != "yes" ]; then
    print_info "Deployment cancelled."
    exit 0
fi

echo ""

# Deploy infrastructure
print_info "Deploying infrastructure..."
pulumi up --yes

echo ""
echo "════════════════════════════════════════════════════════════════"
print_success "Infrastructure deployment complete!"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Show outputs
print_info "Key outputs:"
pulumi stack output --json | python3 -m json.tool

echo ""
print_info "To view all outputs: pulumi stack output"
print_info "To destroy: pulumi destroy"
echo ""
