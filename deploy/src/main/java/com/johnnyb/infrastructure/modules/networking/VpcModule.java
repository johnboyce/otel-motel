package com.johnnyb.infrastructure.modules.networking;

import com.pulumi.Context;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;
import com.pulumi.aws.ec2.Subnet;
import com.pulumi.aws.ec2.SubnetArgs;
import com.pulumi.aws.ec2.InternetGateway;
import com.pulumi.aws.ec2.InternetGatewayArgs;
import com.pulumi.aws.ec2.Eip;
import com.pulumi.aws.ec2.EipArgs;
import com.pulumi.aws.ec2.NatGateway;
import com.pulumi.aws.ec2.NatGatewayArgs;
import com.pulumi.aws.ec2.RouteTable;
import com.pulumi.aws.ec2.RouteTableArgs;
import com.pulumi.aws.ec2.Route;
import com.pulumi.aws.ec2.RouteArgs;
import com.pulumi.aws.ec2.RouteTableAssociation;
import com.pulumi.aws.ec2.RouteTableAssociationArgs;
import com.pulumi.aws.ec2.VpcEndpoint;
import com.pulumi.aws.ec2.VpcEndpointArgs;
import com.pulumi.core.Output;
import com.pulumi.resources.CustomResourceOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VPC Module - Creates a highly available VPC with public, private, and isolated subnets
 * across multiple availability zones.
 */
public class VpcModule {
    private final String name;
    private final String environment;
    private final Map<String, String> tags;
    
    private Vpc vpc;
    private InternetGateway internetGateway;
    private List<Subnet> publicSubnets = new ArrayList<>();
    private List<Subnet> privateSubnets = new ArrayList<>();
    private List<Subnet> isolatedSubnets = new ArrayList<>();
    private List<NatGateway> natGateways = new ArrayList<>();
    private RouteTable publicRouteTable;
    private List<RouteTable> privateRouteTables = new ArrayList<>();

    /**
     * Creates a new VPC with associated networking resources
     * 
     * @param name Base name for resources
     * @param cidrBlock VPC CIDR block
     * @param availabilityZones Number of AZs to use
     * @param environment Environment name (qa, prod)
     * @param useMultipleNatGateways Whether to use one NAT gateway per AZ (prod) or single (qa)
     */
    public VpcModule(String name, String cidrBlock, int availabilityZones, 
                     String environment, boolean useMultipleNatGateways) {
        this.name = name;
        this.environment = environment;
        this.tags = Map.of(
            "Environment", environment,
            "ManagedBy", "Pulumi",
            "Project", "otel-motel"
        );

        createVpc(cidrBlock);
        createInternetGateway();
        createSubnets(cidrBlock, availabilityZones);
        createNatGateways(useMultipleNatGateways);
        createRouteTables(availabilityZones, useMultipleNatGateways);
        createVpcEndpoints();
    }

    /**
     * Create the VPC
     */
    private void createVpc(String cidrBlock) {
        this.vpc = new Vpc(name + "-vpc", VpcArgs.builder()
            .cidrBlock(cidrBlock)
            .enableDnsHostnames(true)
            .enableDnsSupport(true)
            .tags(Map.of(
                "Name", name + "-vpc",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Project", "otel-motel"
            ))
            .build());
    }

    /**
     * Create Internet Gateway for public subnets
     */
    private void createInternetGateway() {
        this.internetGateway = new InternetGateway(name + "-igw", InternetGatewayArgs.builder()
            .vpcId(vpc.id())
            .tags(Map.of(
                "Name", name + "-igw",
                "Environment", environment,
                "ManagedBy", "Pulumi"
            ))
            .build());
    }

    /**
     * Create public, private, and isolated subnets across multiple AZs
     */
    private void createSubnets(String vpcCidr, int availabilityZones) {
        // Parse CIDR base (e.g., "10.0" from "10.0.0.0/16")
        String[] cidrParts = vpcCidr.split("\\.");
        String cidrBase = cidrParts[0] + "." + cidrParts[1];

        for (int az = 0; az < availabilityZones; az++) {
            String azSuffix = String.valueOf((char) ('a' + az));
            
            // Public subnet (e.g., 10.0.1.0/24, 10.0.2.0/24)
            Subnet publicSubnet = new Subnet(name + "-public-" + azSuffix, SubnetArgs.builder()
                .vpcId(vpc.id())
                .cidrBlock(cidrBase + "." + (1 + az * 3) + ".0/24")
                .availabilityZone(environment.equals("prod") ? 
                    "us-east-1" + azSuffix : "us-east-1a")
                .mapPublicIpOnLaunch(true)
                .tags(Map.of(
                    "Name", name + "-public-" + azSuffix,
                    "Tier", "Public",
                    "Environment", environment
                ))
                .build());
            publicSubnets.add(publicSubnet);

            // Private subnet (e.g., 10.0.2.0/24, 10.0.5.0/24)
            Subnet privateSubnet = new Subnet(name + "-private-" + azSuffix, SubnetArgs.builder()
                .vpcId(vpc.id())
                .cidrBlock(cidrBase + "." + (2 + az * 3) + ".0/24")
                .availabilityZone(environment.equals("prod") ? 
                    "us-east-1" + azSuffix : "us-east-1a")
                .tags(Map.of(
                    "Name", name + "-private-" + azSuffix,
                    "Tier", "Private",
                    "Environment", environment
                ))
                .build());
            privateSubnets.add(privateSubnet);

            // Isolated subnet for databases (e.g., 10.0.3.0/24, 10.0.6.0/24)
            Subnet isolatedSubnet = new Subnet(name + "-isolated-" + azSuffix, SubnetArgs.builder()
                .vpcId(vpc.id())
                .cidrBlock(cidrBase + "." + (3 + az * 3) + ".0/24")
                .availabilityZone(environment.equals("prod") ? 
                    "us-east-1" + azSuffix : "us-east-1a")
                .tags(Map.of(
                    "Name", name + "-isolated-" + azSuffix,
                    "Tier", "Isolated",
                    "Environment", environment
                ))
                .build());
            isolatedSubnets.add(isolatedSubnet);
        }
    }

    /**
     * Create NAT Gateways for private subnet internet access
     */
    private void createNatGateways(boolean useMultipleNatGateways) {
        int natGatewayCount = useMultipleNatGateways ? publicSubnets.size() : 1;
        
        for (int i = 0; i < natGatewayCount; i++) {
            String suffix = useMultipleNatGateways ? String.valueOf((char) ('a' + i)) : "";
            
            // Allocate Elastic IP for NAT Gateway
            Eip eip = new Eip(name + "-nat-eip" + suffix, EipArgs.builder()
                .domain("vpc")
                .tags(Map.of(
                    "Name", name + "-nat-eip" + suffix,
                    "Environment", environment
                ))
                .build());

            // Create NAT Gateway in public subnet
            NatGateway natGateway = new NatGateway(name + "-nat" + suffix, NatGatewayArgs.builder()
                .allocationId(eip.id())
                .subnetId(publicSubnets.get(i).id())
                .tags(Map.of(
                    "Name", name + "-nat" + suffix,
                    "Environment", environment
                ))
                .build());
            natGateways.add(natGateway);
        }
    }

    /**
     * Create route tables and associate with subnets
     */
    private void createRouteTables(int availabilityZones, boolean useMultipleNatGateways) {
        // Public route table (shared across all public subnets)
        publicRouteTable = new RouteTable(name + "-public-rt", RouteTableArgs.builder()
            .vpcId(vpc.id())
            .tags(Map.of(
                "Name", name + "-public-rt",
                "Environment", environment
            ))
            .build());

        // Route to Internet Gateway
        new Route(name + "-public-route", RouteArgs.builder()
            .routeTableId(publicRouteTable.id())
            .destinationCidrBlock("0.0.0.0/0")
            .gatewayId(internetGateway.id())
            .build());

        // Associate public route table with public subnets
        for (int i = 0; i < publicSubnets.size(); i++) {
            new RouteTableAssociation(name + "-public-rta-" + i, 
                RouteTableAssociationArgs.builder()
                    .subnetId(publicSubnets.get(i).id())
                    .routeTableId(publicRouteTable.id())
                    .build());
        }

        // Private route tables (one per AZ if using multiple NAT gateways, else shared)
        for (int i = 0; i < availabilityZones; i++) {
            String suffix = String.valueOf((char) ('a' + i));
            
            RouteTable privateRouteTable = new RouteTable(name + "-private-rt-" + suffix, 
                RouteTableArgs.builder()
                    .vpcId(vpc.id())
                    .tags(Map.of(
                        "Name", name + "-private-rt-" + suffix,
                        "Environment", environment
                    ))
                    .build());
            privateRouteTables.add(privateRouteTable);

            // Route to NAT Gateway
            int natGatewayIndex = useMultipleNatGateways ? i : 0;
            new Route(name + "-private-route-" + suffix, RouteArgs.builder()
                .routeTableId(privateRouteTable.id())
                .destinationCidrBlock("0.0.0.0/0")
                .natGatewayId(natGateways.get(natGatewayIndex).id())
                .build());

            // Associate with private subnet
            new RouteTableAssociation(name + "-private-rta-" + suffix,
                RouteTableAssociationArgs.builder()
                    .subnetId(privateSubnets.get(i).id())
                    .routeTableId(privateRouteTable.id())
                    .build());
        }
    }

    /**
     * Create VPC Endpoints for AWS services (reduces costs and improves security)
     */
    private void createVpcEndpoints() {
        // S3 Gateway Endpoint (no cost)
        List<Output<String>> privateRouteTableIds = privateRouteTables.stream()
            .map(RouteTable::id)
            .toList();

        new VpcEndpoint(name + "-s3-endpoint", VpcEndpointArgs.builder()
            .vpcId(vpc.id())
            .serviceName(Output.format("com.amazonaws.%s.s3", System.getenv("AWS_REGION")))
            .vpcEndpointType("Gateway")
            .routeTableIds(privateRouteTableIds)
            .tags(Map.of(
                "Name", name + "-s3-endpoint",
                "Environment", environment
            ))
            .build());

        // DynamoDB Gateway Endpoint (no cost)
        new VpcEndpoint(name + "-dynamodb-endpoint", VpcEndpointArgs.builder()
            .vpcId(vpc.id())
            .serviceName(Output.format("com.amazonaws.%s.dynamodb", System.getenv("AWS_REGION")))
            .vpcEndpointType("Gateway")
            .routeTableIds(privateRouteTableIds)
            .tags(Map.of(
                "Name", name + "-dynamodb-endpoint",
                "Environment", environment
            ))
            .build());
    }

    // Getters for outputs
    public Output<String> getVpcId() {
        return vpc.id();
    }

    public List<Output<String>> getPublicSubnetIds() {
        return publicSubnets.stream().map(Subnet::id).toList();
    }

    public List<Output<String>> getPrivateSubnetIds() {
        return privateSubnets.stream().map(Subnet::id).toList();
    }

    public List<Output<String>> getIsolatedSubnetIds() {
        return isolatedSubnets.stream().map(Subnet::id).toList();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
