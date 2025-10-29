package com.johnnyb.infrastructure.modules.storage;

import com.pulumi.aws.opensearch.Domain;
import com.pulumi.aws.opensearch.DomainArgs;
import com.pulumi.aws.opensearch.inputs.DomainClusterConfigArgs;
import com.pulumi.aws.opensearch.inputs.DomainEbsOptionsArgs;
import com.pulumi.aws.opensearch.inputs.DomainEncryptAtRestArgs;
import com.pulumi.aws.opensearch.inputs.DomainNodeToNodeEncryptionArgs;
import com.pulumi.aws.opensearch.inputs.DomainVpcOptionsArgs;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

/**
 * OpenSearch Module - Creates AWS OpenSearch cluster for logs, traces, and metrics
 */
public class OpenSearchModule {
    private final String name;
    private final String environment;
    
    private Domain openSearchDomain;

    /**
     * Create OpenSearch domain
     * 
     * @param name Base name for resources
     * @param environment Environment name
     * @param subnetIds Subnet IDs for OpenSearch
     * @param securityGroupId Security group ID for OpenSearch
     * @param instanceType OpenSearch instance type
     * @param instanceCount Number of instances
     * @param volumeSize EBS volume size in GB
     */
    public OpenSearchModule(String name, String environment, List<Output<String>> subnetIds,
                           Output<String> securityGroupId, String instanceType, 
                           int instanceCount, int volumeSize) {
        this.name = name;
        this.environment = environment;
        
        createOpenSearchDomain(subnetIds, securityGroupId, instanceType, 
                              instanceCount, volumeSize);
    }

    /**
     * Create OpenSearch domain
     */
    private void createOpenSearchDomain(List<Output<String>> subnetIds, 
                                       Output<String> securityGroupId,
                                       String instanceType, int instanceCount, 
                                       int volumeSize) {
        this.openSearchDomain = new Domain(name + "-opensearch", DomainArgs.builder()
            .domainName(name + "-opensearch")
            .engineVersion("OpenSearch_2.11")
            .clusterConfig(DomainClusterConfigArgs.builder()
                .instanceType(instanceType)
                .instanceCount(instanceCount)
                .dedicatedMasterEnabled(instanceCount >= 3)
                .dedicatedMasterType(instanceCount >= 3 ? "t3.small.search" : null)
                .dedicatedMasterCount(instanceCount >= 3 ? 3 : null)
                .zoneAwarenessEnabled(instanceCount > 1)
                .zoneAwarenessConfig(builder -> 
                    builder.availabilityZoneCount(instanceCount > 1 ? 2 : 1))
                .build())
            .ebsOptions(DomainEbsOptionsArgs.builder()
                .ebsEnabled(true)
                .volumeSize(volumeSize)
                .volumeType("gp3")
                .build())
            .encryptAtRest(DomainEncryptAtRestArgs.builder()
                .enabled(true)
                .build())
            .nodeToNodeEncryption(DomainNodeToNodeEncryptionArgs.builder()
                .enabled(true)
                .build())
            .vpcOptions(DomainVpcOptionsArgs.builder()
                .subnetIds(instanceCount > 1 ? 
                    Output.all(subnetIds.get(0), subnetIds.get(1))
                        .applyValue(ids -> List.of(ids.get(0), ids.get(1))) :
                    subnetIds.get(0).applyValue(List::of))
                .securityGroupIds(securityGroupId)
                .build())
            .accessPolicies(Output.format("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {
                        "AWS": "*"
                      },
                      "Action": "es:*",
                      "Resource": "arn:aws:es:%s:*:domain/%s/*"
                    }
                  ]
                }
                """, System.getenv("AWS_REGION"), name + "-opensearch"))
            .advancedOptions(Map.of(
                "rest.action.multi.allow_explicit_index", "true",
                "override_main_response_version", "false"
            ))
            .tags(Map.of(
                "Name", name + "-opensearch",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Logs, traces, and metrics storage"
            ))
            .build());
    }

    // Getters
    public Domain getOpenSearchDomain() {
        return openSearchDomain;
    }

    public Output<String> getOpenSearchDomainEndpoint() {
        return openSearchDomain.endpoint();
    }

    public Output<String> getOpenSearchDomainArn() {
        return openSearchDomain.arn();
    }

    public Output<String> getOpenSearchDashboardUrl() {
        return openSearchDomain.endpoint()
            .apply(endpoint -> "https://" + endpoint + "/_dashboards");
    }
}
