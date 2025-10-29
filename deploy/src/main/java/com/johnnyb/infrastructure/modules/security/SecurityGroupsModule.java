package com.johnnyb.infrastructure.modules.security;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupEgressArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

/**
 * Security Groups Module - Creates security groups for all infrastructure components
 */
public class SecurityGroupsModule {
    private final String name;
    private final String environment;
    
    private SecurityGroup albSecurityGroup;
    private SecurityGroup appSecurityGroup;
    private SecurityGroup dbSecurityGroup;
    private SecurityGroup openSearchSecurityGroup;

    /**
     * Create security groups for all components
     * 
     * @param name Base name for resources
     * @param vpcId VPC ID
     * @param environment Environment name
     */
    public SecurityGroupsModule(String name, Output<String> vpcId, String environment) {
        this.name = name;
        this.environment = environment;
        
        createAlbSecurityGroup(vpcId);
        createAppSecurityGroup(vpcId);
        createDatabaseSecurityGroup(vpcId);
        createOpenSearchSecurityGroup(vpcId);
    }

    /**
     * Create security group for Application Load Balancer
     */
    private void createAlbSecurityGroup(Output<String> vpcId) {
        this.albSecurityGroup = new SecurityGroup(name + "-alb-sg", SecurityGroupArgs.builder()
            .name(name + "-alb-sg")
            .description("Security group for Application Load Balancer")
            .vpcId(vpcId)
            .ingress(
                SecurityGroupIngressArgs.builder()
                    .description("HTTP from internet")
                    .fromPort(80)
                    .toPort(80)
                    .protocol("tcp")
                    .cidrBlocks("0.0.0.0/0")
                    .build(),
                SecurityGroupIngressArgs.builder()
                    .description("HTTPS from internet")
                    .fromPort(443)
                    .toPort(443)
                    .protocol("tcp")
                    .cidrBlocks("0.0.0.0/0")
                    .build()
            )
            .egress(
                SecurityGroupEgressArgs.builder()
                    .description("All outbound traffic")
                    .fromPort(0)
                    .toPort(0)
                    .protocol("-1")
                    .cidrBlocks("0.0.0.0/0")
                    .build()
            )
            .tags(Map.of(
                "Name", name + "-alb-sg",
                "Environment", environment,
                "ManagedBy", "Pulumi"
            ))
            .build());
    }

    /**
     * Create security group for ECS tasks (application)
     */
    private void createAppSecurityGroup(Output<String> vpcId) {
        this.appSecurityGroup = new SecurityGroup(name + "-app-sg", SecurityGroupArgs.builder()
            .name(name + "-app-sg")
            .description("Security group for ECS application tasks")
            .vpcId(vpcId)
            .ingress(
                SecurityGroupIngressArgs.builder()
                    .description("HTTP from ALB")
                    .fromPort(8080)
                    .toPort(8080)
                    .protocol("tcp")
                    .securityGroups(albSecurityGroup.id())
                    .build(),
                SecurityGroupIngressArgs.builder()
                    .description("Keycloak from ALB")
                    .fromPort(8180)
                    .toPort(8180)
                    .protocol("tcp")
                    .securityGroups(albSecurityGroup.id())
                    .build(),
                SecurityGroupIngressArgs.builder()
                    .description("OpenTelemetry HTTP")
                    .fromPort(4318)
                    .toPort(4318)
                    .protocol("tcp")
                    .securityGroups(albSecurityGroup.id())
                    .build(),
                SecurityGroupIngressArgs.builder()
                    .description("OpenTelemetry gRPC")
                    .fromPort(4317)
                    .toPort(4317)
                    .protocol("tcp")
                    .securityGroups(albSecurityGroup.id())
                    .build(),
                SecurityGroupIngressArgs.builder()
                    .description("Kibana from ALB")
                    .fromPort(5601)
                    .toPort(5601)
                    .protocol("tcp")
                    .securityGroups(albSecurityGroup.id())
                    .build()
            )
            .egress(
                SecurityGroupEgressArgs.builder()
                    .description("All outbound traffic")
                    .fromPort(0)
                    .toPort(0)
                    .protocol("-1")
                    .cidrBlocks("0.0.0.0/0")
                    .build()
            )
            .tags(Map.of(
                "Name", name + "-app-sg",
                "Environment", environment,
                "ManagedBy", "Pulumi"
            ))
            .build());
    }

    /**
     * Create security group for RDS PostgreSQL database
     */
    private void createDatabaseSecurityGroup(Output<String> vpcId) {
        this.dbSecurityGroup = new SecurityGroup(name + "-db-sg", SecurityGroupArgs.builder()
            .name(name + "-db-sg")
            .description("Security group for RDS PostgreSQL database")
            .vpcId(vpcId)
            .ingress(
                SecurityGroupIngressArgs.builder()
                    .description("PostgreSQL from application")
                    .fromPort(5432)
                    .toPort(5432)
                    .protocol("tcp")
                    .securityGroups(appSecurityGroup.id())
                    .build()
            )
            .egress(
                SecurityGroupEgressArgs.builder()
                    .description("All outbound traffic")
                    .fromPort(0)
                    .toPort(0)
                    .protocol("-1")
                    .cidrBlocks("0.0.0.0/0")
                    .build()
            )
            .tags(Map.of(
                "Name", name + "-db-sg",
                "Environment", environment,
                "ManagedBy", "Pulumi"
            ))
            .build());
    }

    /**
     * Create security group for OpenSearch cluster
     */
    private void createOpenSearchSecurityGroup(Output<String> vpcId) {
        this.openSearchSecurityGroup = new SecurityGroup(name + "-opensearch-sg", 
            SecurityGroupArgs.builder()
                .name(name + "-opensearch-sg")
                .description("Security group for OpenSearch cluster")
                .vpcId(vpcId)
                .ingress(
                    SecurityGroupIngressArgs.builder()
                        .description("HTTPS from application")
                        .fromPort(443)
                        .toPort(443)
                        .protocol("tcp")
                        .securityGroups(appSecurityGroup.id())
                        .build()
                )
                .egress(
                    SecurityGroupEgressArgs.builder()
                        .description("All outbound traffic")
                        .fromPort(0)
                        .toPort(0)
                        .protocol("-1")
                        .cidrBlocks("0.0.0.0/0")
                        .build()
                )
                .tags(Map.of(
                    "Name", name + "-opensearch-sg",
                    "Environment", environment,
                    "ManagedBy", "Pulumi"
                ))
                .build());
    }

    // Getters
    public SecurityGroup getAlbSecurityGroup() {
        return albSecurityGroup;
    }

    public SecurityGroup getAppSecurityGroup() {
        return appSecurityGroup;
    }

    public SecurityGroup getDbSecurityGroup() {
        return dbSecurityGroup;
    }

    public SecurityGroup getOpenSearchSecurityGroup() {
        return openSearchSecurityGroup;
    }

    public Output<String> getAlbSecurityGroupId() {
        return albSecurityGroup.id();
    }

    public Output<String> getAppSecurityGroupId() {
        return appSecurityGroup.id();
    }

    public Output<String> getDbSecurityGroupId() {
        return dbSecurityGroup.id();
    }

    public Output<String> getOpenSearchSecurityGroupId() {
        return openSearchSecurityGroup.id();
    }
}
