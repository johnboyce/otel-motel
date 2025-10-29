package com.johnnyb.infrastructure.config;

import com.pulumi.Context;

/**
 * Stack configuration wrapper for easy access to configuration values
 * across all modules.
 */
public class StackConfig {
    private final Context ctx;
    private final String environment;
    private final String projectName = "otel-motel";

    public StackConfig(Context ctx) {
        this.ctx = ctx;
        this.environment = ctx.config().require("environment");
    }

    // Project Configuration
    public String getProjectName() {
        return projectName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppVersion() {
        return ctx.config().get("app:version").orElse("1.0.0");
    }

    // AWS Configuration
    public String getAwsRegion() {
        return ctx.config("aws").require("region");
    }

    // VPC Configuration
    public String getVpcCidr() {
        return ctx.config().get("vpc:cidr").orElse("10.0.0.0/16");
    }

    public int getAvailabilityZones() {
        return Integer.parseInt(ctx.config().get("vpc:availabilityZones").orElse("2"));
    }

    // Database Configuration
    public String getDbInstanceClass() {
        return ctx.config().get("db:instanceClass").orElse("db.t3.micro");
    }

    public int getDbAllocatedStorage() {
        return Integer.parseInt(ctx.config().get("db:allocatedStorage").orElse("20"));
    }

    public boolean getDbMultiAz() {
        return Boolean.parseBoolean(ctx.config().get("db:multiAz").orElse("false"));
    }

    public int getDbBackupRetentionDays() {
        return Integer.parseInt(ctx.config().get("db:backupRetentionDays").orElse("7"));
    }

    // ECS Configuration
    public int getEcsCpu() {
        return Integer.parseInt(ctx.config().get("ecs:cpu").orElse("1024"));
    }

    public int getEcsMemory() {
        return Integer.parseInt(ctx.config().get("ecs:memory").orElse("2048"));
    }

    public int getEcsDesiredCount() {
        return Integer.parseInt(ctx.config().get("ecs:desiredCount").orElse("2"));
    }

    public int getEcsMinCapacity() {
        return Integer.parseInt(ctx.config().get("ecs:minCapacity").orElse("2"));
    }

    public int getEcsMaxCapacity() {
        return Integer.parseInt(ctx.config().get("ecs:maxCapacity").orElse("4"));
    }

    // OpenSearch Configuration
    public String getOpenSearchInstanceType() {
        return ctx.config().get("opensearch:instanceType").orElse("t3.small.search");
    }

    public int getOpenSearchInstanceCount() {
        return Integer.parseInt(ctx.config().get("opensearch:instanceCount").orElse("1"));
    }

    public int getOpenSearchVolumeSize() {
        return Integer.parseInt(ctx.config().get("opensearch:volumeSize").orElse("20"));
    }

    // DynamoDB Configuration
    public String getDynamoDbBillingMode() {
        return ctx.config().get("dynamodb:billingMode").orElse("PAY_PER_REQUEST");
    }

    // Logging Configuration
    public int getLoggingRetentionDays() {
        return Integer.parseInt(ctx.config().get("logging:retentionDays").orElse("7"));
    }

    // Monitoring Configuration
    public boolean getMonitoringEnabled() {
        return Boolean.parseBoolean(ctx.config().get("monitoring:enabled").orElse("true"));
    }

    public String getMonitoringAlertEmail() {
        return ctx.config().get("monitoring:alertEmail").orElse("alerts@example.com");
    }

    /**
     * Get resource name with environment prefix
     */
    public String getResourceName(String resourceType) {
        return String.format("%s-%s-%s", projectName, environment, resourceType);
    }

    /**
     * Check if this is a production environment
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(environment);
    }
}
