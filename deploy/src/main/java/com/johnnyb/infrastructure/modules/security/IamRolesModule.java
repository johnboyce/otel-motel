package com.johnnyb.infrastructure.modules.security;

import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.iam.RolePolicy;
import com.pulumi.aws.iam.RolePolicyArgs;
import com.pulumi.aws.iam.RolePolicyAttachment;
import com.pulumi.aws.iam.RolePolicyAttachmentArgs;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

/**
 * IAM Roles Module - Creates IAM roles and policies for ECS tasks and services
 */
public class IamRolesModule {
    private final String name;
    private final String environment;
    
    private Role ecsTaskExecutionRole;
    private Role ecsTaskRole;

    /**
     * Create IAM roles
     * 
     * @param name Base name for resources
     * @param environment Environment name
     * @param dynamoDbTableArns List of DynamoDB table ARNs
     * @param s3BucketArns List of S3 bucket ARNs
     */
    public IamRolesModule(String name, String environment, 
                         List<Output<String>> dynamoDbTableArns,
                         List<Output<String>> s3BucketArns) {
        this.name = name;
        this.environment = environment;
        
        createEcsTaskExecutionRole();
        createEcsTaskRole(dynamoDbTableArns, s3BucketArns);
    }

    /**
     * Create ECS Task Execution Role (used by ECS to pull images and write logs)
     */
    private void createEcsTaskExecutionRole() {
        this.ecsTaskExecutionRole = new Role(name + "-ecs-task-execution-role", 
            RoleArgs.builder()
                .name(name + "-ecs-task-execution-role")
                .assumeRolePolicy("""
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": {
                            "Service": "ecs-tasks.amazonaws.com"
                          },
                          "Action": "sts:AssumeRole"
                        }
                      ]
                    }
                    """)
                .tags(Map.of(
                    "Name", name + "-ecs-task-execution-role",
                    "Environment", environment,
                    "ManagedBy", "Pulumi"
                ))
                .build());

        // Attach AWS managed policy for ECS task execution
        new RolePolicyAttachment(name + "-ecs-task-execution-policy-attachment",
            RolePolicyAttachmentArgs.builder()
                .role(ecsTaskExecutionRole.name())
                .policyArn("arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy")
                .build());
    }

    /**
     * Create ECS Task Role (used by application code running in the container)
     */
    private void createEcsTaskRole(List<Output<String>> dynamoDbTableArns, 
                                   List<Output<String>> s3BucketArns) {
        this.ecsTaskRole = new Role(name + "-ecs-task-role", RoleArgs.builder()
            .name(name + "-ecs-task-role")
            .assumeRolePolicy("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {
                        "Service": "ecs-tasks.amazonaws.com"
                      },
                      "Action": "sts:AssumeRole"
                    }
                  ]
                }
                """)
            .tags(Map.of(
                "Name", name + "-ecs-task-role",
                "Environment", environment,
                "ManagedBy", "Pulumi"
            ))
            .build());

        // Create policy for DynamoDB access
        Output<String> dynamoDbPolicy = Output.all(dynamoDbTableArns)
            .apply(arns -> String.format("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Action": [
                        "dynamodb:GetItem",
                        "dynamodb:PutItem",
                        "dynamodb:UpdateItem",
                        "dynamodb:DeleteItem",
                        "dynamodb:Query",
                        "dynamodb:Scan",
                        "dynamodb:BatchGetItem",
                        "dynamodb:BatchWriteItem"
                      ],
                      "Resource": [%s]
                    }
                  ]
                }
                """, String.join(",", arns.stream()
                    .map(arn -> "\"" + arn + "\"")
                    .toList())));

        new RolePolicy(name + "-dynamodb-policy", RolePolicyArgs.builder()
            .role(ecsTaskRole.id())
            .policy(dynamoDbPolicy)
            .build());

        // Create policy for S3 access
        Output<String> s3Policy = Output.all(s3BucketArns)
            .apply(arns -> String.format("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Action": [
                        "s3:GetObject",
                        "s3:PutObject",
                        "s3:DeleteObject",
                        "s3:ListBucket"
                      ],
                      "Resource": [%s]
                    }
                  ]
                }
                """, String.join(",", arns.stream()
                    .flatMap(arn -> List.of("\"" + arn + "\"", 
                                           "\"" + arn + "/*\"").stream())
                    .toList())));

        new RolePolicy(name + "-s3-policy", RolePolicyArgs.builder()
            .role(ecsTaskRole.id())
            .policy(s3Policy)
            .build());

        // Attach CloudWatch Logs policy
        new RolePolicyAttachment(name + "-cloudwatch-logs-policy-attachment",
            RolePolicyAttachmentArgs.builder()
                .role(ecsTaskRole.name())
                .policyArn("arn:aws:iam::aws:policy/CloudWatchLogsFullAccess")
                .build());

        // Attach X-Ray policy for distributed tracing
        new RolePolicyAttachment(name + "-xray-policy-attachment",
            RolePolicyAttachmentArgs.builder()
                .role(ecsTaskRole.name())
                .policyArn("arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess")
                .build());
    }

    // Getters
    public Role getEcsTaskExecutionRole() {
        return ecsTaskExecutionRole;
    }

    public Role getEcsTaskRole() {
        return ecsTaskRole;
    }

    public Output<String> getEcsTaskExecutionRoleArn() {
        return ecsTaskExecutionRole.arn();
    }

    public Output<String> getEcsTaskRoleArn() {
        return ecsTaskRole.arn();
    }
}
