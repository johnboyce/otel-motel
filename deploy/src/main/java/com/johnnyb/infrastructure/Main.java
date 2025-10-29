package com.johnnyb.infrastructure;

import com.johnnyb.infrastructure.config.StackConfig;
import com.johnnyb.infrastructure.modules.database.DynamoDbModule;
import com.johnnyb.infrastructure.modules.database.RdsModule;
import com.johnnyb.infrastructure.modules.networking.VpcModule;
import com.johnnyb.infrastructure.modules.security.IamRolesModule;
import com.johnnyb.infrastructure.modules.security.SecurityGroupsModule;
import com.johnnyb.infrastructure.modules.storage.OpenSearchModule;
import com.johnnyb.infrastructure.modules.storage.S3Module;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;

import java.util.List;

/**
 * Main Pulumi program for otel-motel AWS infrastructure
 * 
 * This creates a complete, highly available infrastructure including:
 * - VPC with public, private, and isolated subnets across multiple AZs
 * - Security groups for all components
 * - DynamoDB tables for application data
 * - RDS PostgreSQL for Keycloak authentication
 * - OpenSearch for logs, traces, and metrics
 * - S3 buckets for assets and backups
 * - IAM roles for ECS tasks
 * 
 * The infrastructure is designed to be:
 * - Highly available (multi-AZ where appropriate)
 * - Secure (encryption at rest and in transit, VPC isolation)
 * - Cost-optimized (VPC endpoints, appropriate instance sizing)
 * - Observable (CloudWatch logs, OpenSearch dashboards)
 */
public class Main {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {
            // Load configuration
            StackConfig config = new StackConfig();
            String environment = config.getEnvironment();
            String resourceBaseName = config.getResourceName("");
            
            ctx.log().info("Deploying otel-motel infrastructure for environment: " + environment);

            // 1. Create VPC and networking
            ctx.log().info("Creating VPC and networking resources...");
            VpcModule vpc = new VpcModule(
                resourceBaseName,
                config.getVpcCidr(),
                config.getAvailabilityZones(),
                environment,
                config.isProduction() // Use multiple NAT gateways in prod
            );

            // 2. Create security groups
            ctx.log().info("Creating security groups...");
            SecurityGroupsModule securityGroups = new SecurityGroupsModule(
                resourceBaseName,
                vpc.getVpcId(),
                environment
            );

            // 3. Create DynamoDB tables
            ctx.log().info("Creating DynamoDB tables...");
            DynamoDbModule dynamodb = new DynamoDbModule(
                resourceBaseName,
                environment,
                config.getDynamoDbBillingMode()
            );

            // 4. Create RDS PostgreSQL for Keycloak
            ctx.log().info("Creating RDS PostgreSQL database...");
            RdsModule rds = new RdsModule(
                resourceBaseName,
                environment,
                vpc.getIsolatedSubnetIds(),
                securityGroups.getDbSecurityGroupId(),
                config.getDbInstanceClass(),
                config.getDbAllocatedStorage(),
                config.getDbMultiAz(),
                config.getDbBackupRetentionDays()
            );

            // 5. Create OpenSearch cluster
            ctx.log().info("Creating OpenSearch cluster...");
            OpenSearchModule openSearch = new OpenSearchModule(
                resourceBaseName,
                environment,
                vpc.getPrivateSubnetIds(),
                securityGroups.getOpenSearchSecurityGroupId(),
                config.getOpenSearchInstanceType(),
                config.getOpenSearchInstanceCount(),
                config.getOpenSearchVolumeSize()
            );

            // 6. Create S3 buckets
            ctx.log().info("Creating S3 buckets...");
            S3Module s3 = new S3Module(resourceBaseName, environment);

            // 7. Create IAM roles
            ctx.log().info("Creating IAM roles...");
            IamRolesModule iam = new IamRolesModule(
                resourceBaseName,
                environment,
                List.of(
                    dynamodb.getHotelsTableArn(),
                    dynamodb.getRoomsTableArn(),
                    dynamodb.getCustomersTableArn(),
                    dynamodb.getBookingsTableArn()
                ),
                List.of(
                    s3.getAssetsBucketArn(),
                    s3.getBackupsBucketArn()
                )
            );

            // Export outputs
            ctx.export("environment", Output.of(environment));
            ctx.export("region", Output.of(config.getAwsRegion()));
            
            // VPC outputs
            ctx.export("vpcId", vpc.getVpcId());
            ctx.export("publicSubnetIds", Output.all(vpc.getPublicSubnetIds()));
            ctx.export("privateSubnetIds", Output.all(vpc.getPrivateSubnetIds()));
            ctx.export("isolatedSubnetIds", Output.all(vpc.getIsolatedSubnetIds()));
            
            // Security group outputs
            ctx.export("albSecurityGroupId", securityGroups.getAlbSecurityGroupId());
            ctx.export("appSecurityGroupId", securityGroups.getAppSecurityGroupId());
            ctx.export("dbSecurityGroupId", securityGroups.getDbSecurityGroupId());
            ctx.export("openSearchSecurityGroupId", securityGroups.getOpenSearchSecurityGroupId());
            
            // DynamoDB outputs
            ctx.export("dynamoDbHotelsTableName", dynamodb.getHotelsTableName());
            ctx.export("dynamoDbRoomsTableName", dynamodb.getRoomsTableName());
            ctx.export("dynamoDbCustomersTableName", dynamodb.getCustomersTableName());
            ctx.export("dynamoDbBookingsTableName", dynamodb.getBookingsTableName());
            
            // RDS outputs
            ctx.export("rdsEndpoint", rds.getDbInstanceEndpoint());
            ctx.export("rdsAddress", rds.getDbInstanceAddress());
            ctx.export("rdsPort", rds.getDbInstancePort());
            ctx.export("rdsDbName", rds.getDbName());
            ctx.export("rdsUsername", rds.getDbUsername());
            
            // OpenSearch outputs
            ctx.export("openSearchEndpoint", openSearch.getOpenSearchDomainEndpoint());
            ctx.export("openSearchDashboardUrl", openSearch.getOpenSearchDashboardUrl());
            
            // S3 outputs
            ctx.export("s3AssetsBucketName", s3.getAssetsBucketName());
            ctx.export("s3BackupsBucketName", s3.getBackupsBucketName());
            
            // IAM outputs
            ctx.export("ecsTaskExecutionRoleArn", iam.getEcsTaskExecutionRoleArn());
            ctx.export("ecsTaskRoleArn", iam.getEcsTaskRoleArn());

            ctx.log().info("Infrastructure deployment complete!");
        });
    }
}
