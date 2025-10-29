# OTel-Motel AWS Infrastructure with Pulumi (Java)

This directory contains the Pulumi infrastructure-as-code (IaC) for deploying the otel-motel application to AWS. The infrastructure is written in **Java** to match the project's technology stack.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
- [Stacks](#stacks)
- [Modules](#modules)
- [Deployment](#deployment)
- [Outputs](#outputs)
- [Cost Considerations](#cost-considerations)
- [Security](#security)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This infrastructure setup provides a complete, production-ready AWS environment for the otel-motel hotel booking system with:

- **High Availability**: Multi-AZ deployment for critical components
- **Security**: VPC isolation, encryption at rest and in transit, least-privilege IAM
- **Observability**: OpenSearch for logs/traces/metrics, CloudWatch integration
- **Scalability**: Auto-scaling ECS services, serverless DynamoDB
- **Cost Optimization**: VPC endpoints, right-sized resources per environment

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Internet                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  Application Load     │
              │     Balancer          │
              └──────────┬───────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │  ECS    │    │  ECS    │    │  ECS    │
    │  Task   │    │  Task   │    │  Task   │
    │ (App)   │    │(Keycloak│    │ (OTEL)  │
    └────┬────┘    └────┬────┘    └────┬────┘
         │              │              │
         ├──────────────┴──────────────┤
         │                             │
    ┌────▼────┐                   ┌───▼────┐
    │DynamoDB │                   │  RDS   │
    │ Tables  │                   │Postgres│
    └─────────┘                   └────────┘
         │                             │
         └──────────────┬──────────────┘
                        │
                   ┌────▼────────┐
                   │ OpenSearch  │
                   │  (Logs/     │
                   │  Traces)    │
                   └─────────────┘
```

### Components

1. **VPC & Networking**
   - Multi-AZ VPC with public, private, and isolated subnets
   - NAT Gateways for private subnet internet access
   - VPC Endpoints for S3 and DynamoDB (cost optimization)

2. **Compute (ECS Fargate)**
   - Application container (Quarkus GraphQL API)
   - Keycloak authentication service
   - OpenTelemetry Collector

3. **Databases**
   - DynamoDB: Hotels, Rooms, Customers, Bookings (with GSIs)
   - RDS PostgreSQL: Keycloak user store

4. **Observability**
   - AWS OpenSearch: Logs, traces, and metrics
   - CloudWatch: Container logs and metrics
   - X-Ray: Distributed tracing

5. **Storage**
   - S3: Application assets and backups

## 📦 Prerequisites

### Required Software

- **Java 17+** (to match the main application)
- **Maven 3.9+**
- **Pulumi CLI** 3.x or later
- **AWS CLI** configured with appropriate credentials
- **Docker** (for building container images)

### AWS Account Setup

1. AWS account with appropriate permissions
2. AWS CLI configured:
   ```bash
   aws configure
   ```
3. Pulumi account (free for individuals):
   ```bash
   pulumi login
   ```

### Installation

#### Install Pulumi (macOS)
```bash
brew install pulumi/tap/pulumi
```

#### Install Pulumi (Linux)
```bash
curl -fsSL https://get.pulumi.com | sh
```

#### Install Pulumi (Windows)
```powershell
choco install pulumi
```

## 📁 Project Structure

```
deploy/
├── pom.xml                           # Maven project configuration
├── Pulumi.yaml                       # Pulumi project configuration
├── Pulumi.qa.yaml                    # QA stack configuration
├── Pulumi.prod.yaml                  # Production stack configuration
├── README.md                         # This file
└── src/main/java/com/johnnyb/infrastructure/
    ├── Main.java                     # Main Pulumi program (orchestrates all modules)
    ├── config/
    │   └── StackConfig.java          # Configuration wrapper
    └── modules/
        ├── networking/
        │   └── VpcModule.java        # VPC, subnets, NAT gateways
        ├── security/
        │   ├── SecurityGroupsModule.java  # Security groups
        │   └── IamRolesModule.java   # IAM roles and policies
        ├── database/
        │   ├── DynamoDbModule.java   # DynamoDB tables
        │   └── RdsModule.java        # RDS PostgreSQL
        └── storage/
            ├── OpenSearchModule.java # OpenSearch domain
            └── S3Module.java         # S3 buckets
```

## ⚙️ Configuration

Configuration is managed through stack-specific YAML files and environment variables.

### Stack Configuration Files

- **Pulumi.qa.yaml**: QA environment (cost-optimized, single-AZ where possible)
- **Pulumi.prod.yaml**: Production environment (multi-AZ, higher capacity)

### Configuration Parameters

| Parameter | Description | QA Default | Prod Default |
|-----------|-------------|------------|--------------|
| `environment` | Environment name | qa | prod |
| `aws:region` | AWS region | us-east-1 | us-east-1 |
| `vpc:cidr` | VPC CIDR block | 10.0.0.0/16 | 10.1.0.0/16 |
| `vpc:availabilityZones` | Number of AZs | 2 | 3 |
| `db:instanceClass` | RDS instance type | db.t3.micro | db.t3.small |
| `db:multiAz` | RDS Multi-AZ | false | true |
| `ecs:desiredCount` | ECS task count | 2 | 4 |
| `opensearch:instanceCount` | OpenSearch nodes | 1 | 3 |

## 🚀 Getting Started

### 1. Build the Infrastructure Code

```bash
cd deploy
mvn clean package
```

### 2. Initialize Pulumi Stack

For QA:
```bash
pulumi stack init qa
pulumi stack select qa
```

For Production:
```bash
pulumi stack init prod
pulumi stack select prod
```

### 3. Configure AWS Credentials

```bash
pulumi config set aws:region us-east-1
```

### 4. Preview Changes

```bash
pulumi preview
```

### 5. Deploy Infrastructure

```bash
pulumi up
```

Pulumi will show you a preview of changes and ask for confirmation before proceeding.

## 📚 Stacks

### QA Stack

**Purpose**: Development and testing environment

**Characteristics**:
- Cost-optimized configuration
- Single NAT Gateway
- Smaller instance sizes
- Shorter backup retention
- Single-AZ where appropriate

**Deploy**:
```bash
pulumi stack select qa
pulumi up
```

### Production Stack

**Purpose**: Production workload

**Characteristics**:
- High availability (Multi-AZ)
- Multiple NAT Gateways (one per AZ)
- Larger instance sizes
- Extended backup retention (30 days)
- Deletion protection enabled
- Performance Insights enabled

**Deploy**:
```bash
pulumi stack select prod
pulumi up
```

## 🧩 Modules

### Networking Module (`VpcModule.java`)

Creates VPC infrastructure:
- VPC with configurable CIDR
- Public subnets (for ALB)
- Private subnets (for ECS tasks)
- Isolated subnets (for databases)
- Internet Gateway
- NAT Gateways
- Route tables
- VPC Endpoints (S3, DynamoDB)

### Security Module

#### SecurityGroupsModule.java
- ALB security group (ports 80, 443)
- Application security group (ports 8080, 8180, 4317, 4318, 5601)
- Database security group (port 5432)
- OpenSearch security group (port 443)

#### IamRolesModule.java
- ECS Task Execution Role (for ECS service)
- ECS Task Role (for application code)
- Policies for DynamoDB, S3, CloudWatch, X-Ray

### Database Module

#### DynamoDbModule.java
Creates four DynamoDB tables:
- `hotels` - Hotel information (GSI: CityIndex)
- `rooms` - Room inventory (GSI: HotelIndex)
- `customers` - Customer data (GSI: EmailIndex)
- `bookings` - Reservations (GSI: CustomerIndex, RoomIndex)

#### RdsModule.java
- PostgreSQL 16.1 instance for Keycloak
- Encrypted storage
- Automated backups
- Performance Insights (prod only)
- CloudWatch log export

### Storage Module

#### OpenSearchModule.java
- OpenSearch 2.11 domain
- EBS-backed storage
- Encryption at rest and in transit
- VPC-based access
- Dedicated master nodes (prod only)

#### S3Module.java
- Assets bucket (application files)
- Backups bucket (database backups)
- Versioning enabled
- Public access blocked

## 📤 Outputs

After deployment, Pulumi exports useful outputs:

```bash
# View all outputs
pulumi stack output

# View specific output
pulumi stack output rdsEndpoint
```

### Key Outputs

| Output | Description |
|--------|-------------|
| `vpcId` | VPC ID |
| `publicSubnetIds` | Public subnet IDs |
| `privateSubnetIds` | Private subnet IDs |
| `rdsEndpoint` | RDS connection endpoint |
| `rdsAddress` | RDS host address |
| `openSearchEndpoint` | OpenSearch endpoint |
| `openSearchDashboardUrl` | OpenSearch Dashboards URL |
| `dynamoDbHotelsTableName` | Hotels table name |
| `s3AssetsBucketName` | Assets bucket name |
| `ecsTaskRoleArn` | ECS task IAM role ARN |

## 💰 Cost Considerations

### Estimated Monthly Costs

#### QA Environment (~$150-200/month)
- RDS db.t3.micro: ~$15
- OpenSearch t3.small.search (1 node): ~$30
- NAT Gateway: ~$32
- ECS Fargate (2 tasks): ~$50
- DynamoDB (pay-per-request): ~$10
- Data transfer: ~$10-20

#### Production Environment (~$500-700/month)
- RDS db.t3.small (Multi-AZ): ~$60
- OpenSearch t3.medium.search (3 nodes): ~$180
- NAT Gateways (3): ~$96
- ECS Fargate (4 tasks): ~$100
- DynamoDB (pay-per-request): ~$30
- Data transfer: ~$30-50

### Cost Optimization Tips

1. **Use VPC Endpoints**: Already included for S3 and DynamoDB (saves data transfer costs)
2. **Right-size instances**: Adjust instance types based on actual usage
3. **Use Reserved Instances**: For production RDS and OpenSearch
4. **Enable Auto-scaling**: Scale ECS tasks based on demand
5. **Set up S3 lifecycle policies**: Auto-delete old backups
6. **Monitor unused resources**: Use AWS Cost Explorer

## 🔒 Security

### Security Best Practices Implemented

✅ **Network Security**
- Private subnets for application and database tiers
- Security groups with least-privilege rules
- VPC endpoints to avoid internet traffic

✅ **Data Security**
- Encryption at rest for all data stores (RDS, DynamoDB, OpenSearch, S3)
- Encryption in transit (TLS/HTTPS)
- RDS automated backups with 7-30 day retention

✅ **Access Control**
- IAM roles with least-privilege policies
- No hardcoded credentials
- Separate execution and task roles for ECS

✅ **Production Safeguards**
- RDS deletion protection (prod only)
- Multi-AZ for high availability
- Automated backups

### Security Recommendations

⚠️ **Before Production**:
1. Replace default RDS password with AWS Secrets Manager
2. Enable AWS WAF on ALB
3. Set up AWS GuardDuty for threat detection
4. Enable VPC Flow Logs
5. Configure AWS Config for compliance
6. Set up security group logging
7. Enable MFA for AWS root account

## 🐛 Troubleshooting

### Common Issues

#### Issue: Pulumi preview fails with authentication error
```bash
# Solution: Configure AWS credentials
aws configure
pulumi config set aws:region us-east-1
```

#### Issue: Stack is stuck in updating state
```bash
# Solution: Cancel the update and refresh state
pulumi cancel
pulumi refresh
```

#### Issue: Resource already exists error
```bash
# Solution: Import existing resource or delete manually
pulumi import aws:s3/bucket:Bucket my-bucket my-bucket-name
# Or delete via AWS Console
```

#### Issue: OpenSearch domain creation fails
```bash
# Common cause: Service-linked role missing
aws iam create-service-linked-role --aws-service-name es.amazonaws.com
```

### Debug Mode

Enable detailed logging:
```bash
pulumi up --logtostderr -v=9
```

### State Management

Export stack state:
```bash
pulumi stack export --file stack-backup.json
```

Import stack state:
```bash
pulumi stack import --file stack-backup.json
```

## 🔄 Updates and Maintenance

### Update Stack Configuration

```bash
# Update configuration value
pulumi config set ecs:desiredCount 4

# Apply changes
pulumi up
```

### Destroy Stack

⚠️ **Warning**: This will delete all resources!

```bash
pulumi destroy
```

For production, consider:
1. Taking manual snapshots of databases
2. Backing up S3 buckets
3. Exporting OpenSearch dashboards

## 📖 Additional Resources

- [Pulumi Java Documentation](https://www.pulumi.com/docs/languages-sdks/java/)
- [Pulumi AWS Provider](https://www.pulumi.com/registry/packages/aws/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [Main Application README](../README.md)

## 🤝 Contributing

When adding new infrastructure:

1. Create a new module in appropriate directory
2. Follow existing code structure and naming conventions
3. Add comprehensive JavaDoc comments
4. Update this README with new resources
5. Test in QA stack before deploying to production
6. Document any new configuration parameters

## 📝 License

This infrastructure code is part of the otel-motel project.

---

**Questions or Issues?** Open an issue in the main repository.
