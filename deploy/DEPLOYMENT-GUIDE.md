# OTel-Motel Infrastructure Deployment Guide

This guide walks you through deploying the otel-motel application infrastructure to AWS using Pulumi.

## Quick Start

```bash
cd deploy
./deploy.sh
```

The script will guide you through the deployment process interactively.

## Manual Deployment Steps

If you prefer manual control over each step:

### 1. Prerequisites Check

Ensure you have all required tools installed:

```bash
# Check Java (17+)
java -version

# Check Maven (3.9+)
mvn --version

# Check Pulumi
pulumi version

# Check AWS CLI
aws --version

# Verify AWS credentials
aws sts get-caller-identity
```

### 2. Build Infrastructure Code

```bash
cd deploy
mvn clean package
```

This compiles the Java Pulumi program.

### 3. Initialize Stack

Choose your environment and initialize the Pulumi stack:

**For QA:**
```bash
pulumi stack init qa
pulumi stack select qa
```

**For Production:**
```bash
pulumi stack init prod
pulumi stack select prod
```

### 4. Configure Stack (Optional)

Override default values if needed:

```bash
# Set AWS region (if different from us-east-1)
pulumi config set aws:region us-west-2

# Customize ECS task count
pulumi config set ecs:desiredCount 3

# Set alert email
pulumi config set monitoring:alertEmail alerts@mycompany.com
```

View current configuration:
```bash
pulumi config
```

### 5. Preview Changes

Before deploying, preview what will be created:

```bash
pulumi preview
```

This shows:
- Resources to be created
- Estimated costs
- Any configuration issues

### 6. Deploy Infrastructure

Deploy the infrastructure:

```bash
pulumi up
```

Pulumi will:
1. Show a detailed preview
2. Ask for confirmation
3. Create all resources
4. Display outputs

**Expected deployment time:**
- QA: ~15-20 minutes
- Production: ~20-30 minutes

### 7. Verify Deployment

Check that all resources were created successfully:

```bash
# View all outputs
pulumi stack output

# View specific outputs
pulumi stack output rdsEndpoint
pulumi stack output openSearchEndpoint

# Check AWS Console
aws ec2 describe-vpcs --filters "Name=tag:Project,Values=otel-motel"
aws dynamodb list-tables
aws rds describe-db-instances
```

## Stack Outputs Reference

After deployment, use these outputs for application configuration:

| Output | Description | Use In |
|--------|-------------|--------|
| `rdsEndpoint` | PostgreSQL connection string | Keycloak config |
| `rdsAddress` | PostgreSQL host | Keycloak config |
| `rdsPort` | PostgreSQL port (5432) | Keycloak config |
| `openSearchEndpoint` | OpenSearch endpoint | OTEL Collector config |
| `dynamoDbHotelsTableName` | Hotels table name | Application config |
| `dynamoDbRoomsTableName` | Rooms table name | Application config |
| `dynamoDbCustomersTableName` | Customers table name | Application config |
| `dynamoDbBookingsTableName` | Bookings table name | Application config |
| `ecsTaskRoleArn` | ECS task IAM role | ECS task definition |

Get outputs in JSON format:
```bash
pulumi stack output --json > outputs.json
```

## Application Deployment

After infrastructure is deployed, deploy the application:

### 1. Build Container Image

```bash
cd ..  # Return to project root

# Build application
./mvnw clean package -DskipTests

# Build Docker image
docker build -t otel-motel:latest .

# Tag for ECR (replace with your ECR URL)
docker tag otel-motel:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/otel-motel:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/otel-motel:latest
```

### 2. Update Application Configuration

Use Pulumi outputs to configure the application:

```bash
# Get RDS endpoint
RDS_ENDPOINT=$(pulumi stack output rdsEndpoint --cwd deploy)

# Get DynamoDB table names
HOTELS_TABLE=$(pulumi stack output dynamoDbHotelsTableName --cwd deploy)
ROOMS_TABLE=$(pulumi stack output dynamoDbRoomsTableName --cwd deploy)
CUSTOMERS_TABLE=$(pulumi stack output dynamoDbCustomersTableName --cwd deploy)
BOOKINGS_TABLE=$(pulumi stack output dynamoDbBookingsTableName --cwd deploy)

# Get OpenSearch endpoint
OPENSEARCH_ENDPOINT=$(pulumi stack output openSearchEndpoint --cwd deploy)
```

Update `application.properties` or set as environment variables in ECS task definition.

### 3. Initialize Database

**PostgreSQL (Keycloak):**
```bash
# Connect to RDS
RDS_ADDRESS=$(pulumi stack output rdsAddress --cwd deploy)
psql -h $RDS_ADDRESS -U keycloak -d keycloak

# Run initialization if needed
# (Keycloak will auto-create schema on first start)
```

**DynamoDB (Application Data):**
```bash
# Tables are already created by Pulumi
# Load sample data if needed
aws dynamodb batch-write-item --request-items file://sample-data.json
```

## Updating Infrastructure

To update existing infrastructure:

### 1. Modify Configuration

```bash
cd deploy
pulumi config set ecs:desiredCount 6
```

### 2. Preview Changes

```bash
pulumi preview
```

### 3. Apply Updates

```bash
pulumi up
```

Pulumi will only update changed resources.

## Monitoring and Maintenance

### View Logs

**ECS Container Logs:**
```bash
aws logs tail /ecs/otel-motel-qa --follow
```

**RDS Logs:**
```bash
aws rds describe-db-log-files --db-instance-identifier otel-motel-qa-keycloak-db
```

### Access OpenSearch Dashboards

```bash
# Get dashboard URL
pulumi stack output openSearchDashboardUrl

# Open in browser (requires VPN/bastion for VPC access)
open $(pulumi stack output openSearchDashboardUrl)
```

### Check Resource Status

```bash
# ECS services
aws ecs list-services --cluster otel-motel-qa

# DynamoDB tables
aws dynamodb describe-table --table-name otel-motel-qa-hotels

# RDS instance
aws rds describe-db-instances --db-instance-identifier otel-motel-qa-keycloak-db
```

## Scaling

### Scale ECS Services

```bash
# Update desired count
pulumi config set ecs:desiredCount 8
pulumi up
```

### Scale RDS

```bash
# Update instance class
pulumi config set db:instanceClass db.t3.medium
pulumi up
```

Note: RDS scaling requires downtime.

### Scale OpenSearch

```bash
# Update instance count
pulumi config set opensearch:instanceCount 3
pulumi up
```

## Backup and Recovery

### Database Backups

**RDS (Automated):**
- Automated daily backups (configured in Pulumi)
- Point-in-time recovery enabled
- Retention: 7 days (QA), 30 days (prod)

**Manual Backup:**
```bash
aws rds create-db-snapshot \
  --db-instance-identifier otel-motel-qa-keycloak-db \
  --db-snapshot-identifier otel-motel-manual-backup-$(date +%Y%m%d)
```

**DynamoDB (Point-in-time Recovery):**
- Enabled in production
- 35-day recovery window

### Restore from Backup

**RDS Restore:**
```bash
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier otel-motel-restored \
  --db-snapshot-identifier snapshot-name
```

**DynamoDB Restore:**
```bash
aws dynamodb restore-table-to-point-in-time \
  --source-table-name otel-motel-prod-hotels \
  --target-table-name otel-motel-restored-hotels \
  --restore-date-time "2024-01-01T00:00:00Z"
```

## Destroying Infrastructure

⚠️ **Warning:** This is permanent and irreversible!

### QA Environment

```bash
pulumi stack select qa
pulumi destroy
```

### Production Environment

**Before destroying production:**

1. ✅ Take manual snapshots
   ```bash
   aws rds create-db-snapshot \
     --db-instance-identifier otel-motel-prod-keycloak-db \
     --db-snapshot-identifier final-backup-$(date +%Y%m%d)
   ```

2. ✅ Export DynamoDB tables
   ```bash
   aws dynamodb export-table-to-point-in-time \
     --table-arn <table-arn> \
     --s3-bucket otel-motel-prod-backups \
     --s3-prefix dynamodb-backup/
   ```

3. ✅ Backup S3 buckets
   ```bash
   aws s3 sync s3://otel-motel-prod-assets ./backup-assets/
   aws s3 sync s3://otel-motel-prod-backups ./backup-backups/
   ```

4. ✅ Destroy infrastructure
   ```bash
   pulumi stack select prod
   pulumi destroy
   ```

## Troubleshooting

### Common Issues

**Issue: Pulumi state is locked**
```bash
# Solution: Cancel ongoing update
pulumi cancel

# Or force unlock (use with caution)
pulumi state unlock
```

**Issue: RDS creation fails with subnet group error**
```bash
# Solution: Check subnet configuration
pulumi stack output isolatedSubnetIds

# Ensure subnets are in different AZs
```

**Issue: OpenSearch domain creation times out**
```bash
# Solution: Check service-linked role exists
aws iam get-role --role-name AWSServiceRoleForAmazonElasticsearchService

# Create if missing
aws iam create-service-linked-role --aws-service-name es.amazonaws.com
```

**Issue: DynamoDB table already exists**
```bash
# Solution: Import existing table
pulumi import aws:dynamodb/table:Table hotels otel-motel-qa-hotels

# Or delete and recreate
aws dynamodb delete-table --table-name otel-motel-qa-hotels
pulumi up
```

### Get Help

```bash
# View Pulumi logs
pulumi logs

# Enable verbose logging
pulumi up --logtostderr -v=9

# Check stack history
pulumi stack history

# Export state for debugging
pulumi stack export > state.json
```

## Cost Optimization

### Monitor Costs

```bash
# Use AWS Cost Explorer
aws ce get-cost-and-usage \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY \
  --metrics "UnblendedCost" \
  --filter file://cost-filter.json
```

### Reduce Costs

1. **Right-size instances**: Monitor actual usage and adjust
2. **Use Savings Plans**: For production workloads
3. **Enable auto-scaling**: Scale down during off-hours
4. **Clean up unused resources**: Regularly audit
5. **Use VPC endpoints**: Already configured for S3/DynamoDB

## Security Checklist

Before going to production:

- [ ] Rotate RDS password and store in Secrets Manager
- [ ] Enable AWS WAF on ALB
- [ ] Set up GuardDuty
- [ ] Enable VPC Flow Logs
- [ ] Configure AWS Config rules
- [ ] Set up security group logging
- [ ] Enable MFA on AWS account
- [ ] Review IAM policies for least privilege
- [ ] Set up CloudTrail logging
- [ ] Configure AWS Security Hub

## Next Steps

1. Deploy application containers to ECS
2. Configure DNS and SSL certificates
3. Set up CI/CD pipeline
4. Configure monitoring and alerting
5. Run load tests
6. Create runbooks for common operations

## Additional Resources

- [Pulumi Documentation](https://www.pulumi.com/docs/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [Main Project README](../README.md)
- [Infrastructure README](./README.md)

---

**Need Help?** Open an issue in the repository or consult the Pulumi community.
