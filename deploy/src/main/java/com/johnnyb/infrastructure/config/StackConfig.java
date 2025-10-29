package com.johnnyb.infrastructure.config;

import com.pulumi.Config;

/**
 * Stack configuration wrapper for easy access to configuration values
 * across all modules.
 */
public class StackConfig {
    private final Config config;
    private final Config awsConfig;
    private final String environment;
    private final String projectName = "otel-motel";

    public StackConfig() {
        this.config = new Config();
        this.awsConfig = new Config("aws");
        this.environment = config.require("environment");
    }

    // Project Configuration
    public String getProjectName() {
        return projectName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppVersion() {
        return config.get("app:version").orElse("1.0.0");
    }

    // AWS Configuration
    public String getAwsRegion() {
        return awsConfig.require("region");
    }

    // VPC Configuration
    public String getVpcCidr() {
        return config.get("vpc:cidr").orElse("10.0.0.0/16");
    }

    public int getAvailabilityZones() {
        return Integer.parseInt(config.get("vpc:availabilityZones").orElse("2"));
    }

    // Database Configuration
    public String getDbInstanceClass() {
        return config.get("db:instanceClass").orElse("db.t3.micro");
    }

    public int getDbAllocatedStorage() {
        return Integer.parseInt(config.get("db:allocatedStorage").orElse("20"));
    }

    public boolean getDbMultiAz() {
        return Boolean.parseBoolean(config.get("db:multiAz").orElse("false"));
    }

    public int getDbBackupRetentionDays() {
        return Integer.parseInt(config.get("db:backupRetentionDays").orElse("7"));
    }

    // ECS Configuration
    public int getEcsCpu() {
        return Integer.parseInt(config.get("ecs:cpu").orElse("1024"));
    }

    public int getEcsMemory() {
        return Integer.parseInt(config.get("ecs:memory").orElse("2048"));
    }

    public int getEcsDesiredCount() {
        return Integer.parseInt(config.get("ecs:desiredCount").orElse("2"));
    }

    public int getEcsMinCapacity() {
        return Integer.parseInt(config.get("ecs:minCapacity").orElse("2"));
    }

    public int getEcsMaxCapacity() {
        return Integer.parseInt(config.get("ecs:maxCapacity").orElse("4"));
    }

    // OpenSearch Configuration
    public String getOpenSearchInstanceType() {
        return config.get("opensearch:instanceType").orElse("t3.small.search");
    }

    public int getOpenSearchInstanceCount() {
        return Integer.parseInt(config.get("opensearch:instanceCount").orElse("1"));
    }

    public int getOpenSearchVolumeSize() {
        return Integer.parseInt(config.get("opensearch:volumeSize").orElse("20"));
    }

    // DynamoDB Configuration
    public String getDynamoDbBillingMode() {
        return config.get("dynamodb:billingMode").orElse("PAY_PER_REQUEST");
    }

    // Logging Configuration
    public int getLoggingRetentionDays() {
        return Integer.parseInt(config.get("logging:retentionDays").orElse("7"));
    }

    // Monitoring Configuration
    public boolean getMonitoringEnabled() {
        return Boolean.parseBoolean(config.get("monitoring:enabled").orElse("true"));
    }

    public String getMonitoringAlertEmail() {
        return config.get("monitoring:alertEmail").orElse("alerts@example.com");
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
