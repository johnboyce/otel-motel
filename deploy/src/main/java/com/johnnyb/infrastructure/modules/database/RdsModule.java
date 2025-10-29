package com.johnnyb.infrastructure.modules.database;

import com.pulumi.aws.rds.Instance;
import com.pulumi.aws.rds.InstanceArgs;
import com.pulumi.aws.rds.SubnetGroup;
import com.pulumi.aws.rds.SubnetGroupArgs;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

/**
 * RDS Module - Creates RDS PostgreSQL database for Keycloak
 */
public class RdsModule {
    private final String name;
    private final String environment;
    
    private SubnetGroup dbSubnetGroup;
    private Instance dbInstance;

    /**
     * Create RDS PostgreSQL instance
     * 
     * @param name Base name for resources
     * @param environment Environment name
     * @param subnetIds Subnet IDs for DB subnet group
     * @param securityGroupId Security group ID for RDS
     * @param instanceClass DB instance class
     * @param allocatedStorage Allocated storage in GB
     * @param multiAz Whether to enable Multi-AZ
     * @param backupRetentionDays Backup retention period in days
     */
    public RdsModule(String name, String environment, List<Output<String>> subnetIds,
                     Output<String> securityGroupId, String instanceClass, int allocatedStorage,
                     boolean multiAz, int backupRetentionDays) {
        this.name = name;
        this.environment = environment;
        
        createDbSubnetGroup(subnetIds);
        createDbInstance(securityGroupId, instanceClass, allocatedStorage, 
                        multiAz, backupRetentionDays);
    }

    /**
     * Create DB subnet group
     */
    private void createDbSubnetGroup(List<Output<String>> subnetIds) {
        this.dbSubnetGroup = new SubnetGroup(name + "-db-subnet-group", 
            SubnetGroupArgs.builder()
                .name(name + "-db-subnet-group")
                .subnetIds(subnetIds)
                .tags(Map.of(
                    "Name", name + "-db-subnet-group",
                    "Environment", environment,
                    "ManagedBy", "Pulumi"
                ))
                .build());
    }

    /**
     * Create RDS PostgreSQL instance
     */
    private void createDbInstance(Output<String> securityGroupId, String instanceClass,
                                  int allocatedStorage, boolean multiAz, int backupRetentionDays) {
        this.dbInstance = new Instance(name + "-keycloak-db", InstanceArgs.builder()
            .identifier(name + "-keycloak-db")
            .engine("postgres")
            .engineVersion("16.1")
            .instanceClass(instanceClass)
            .allocatedStorage(allocatedStorage)
            .storageType("gp3")
            .storageEncrypted(true)
            .dbName("keycloak")
            .username("keycloak")
            // In production, use AWS Secrets Manager for password
            .password(Output.format("keycloak-%s-password-change-me", environment))
            .dbSubnetGroupName(dbSubnetGroup.name())
            .vpcSecurityGroupIds(securityGroupId)
            .multiAz(multiAz)
            .publiclyAccessible(false)
            .backupRetentionPeriod(backupRetentionDays)
            .backupWindow("03:00-04:00")
            .maintenanceWindow("sun:04:00-sun:05:00")
            .skipFinalSnapshot(environment.equals("qa"))
            .finalSnapshotIdentifier(environment.equals("prod") ? 
                Output.format("%s-keycloak-db-final-snapshot", name) : null)
            .deletionProtection(environment.equals("prod"))
            .enabledCloudwatchLogsExports("postgresql", "upgrade")
            .performanceInsightsEnabled(environment.equals("prod"))
            .performanceInsightsRetentionPeriod(environment.equals("prod") ? 7 : null)
            .tags(Map.of(
                "Name", name + "-keycloak-db",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Keycloak authentication database"
            ))
            .build());
    }

    // Getters
    public Instance getDbInstance() {
        return dbInstance;
    }

    public Output<String> getDbInstanceEndpoint() {
        return dbInstance.endpoint();
    }

    public Output<String> getDbInstanceAddress() {
        return dbInstance.address();
    }

    public Output<Integer> getDbInstancePort() {
        return dbInstance.port();
    }

    public Output<String> getDbName() {
        return dbInstance.dbName();
    }

    public Output<String> getDbUsername() {
        return dbInstance.username();
    }
}
