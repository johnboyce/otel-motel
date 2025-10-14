package com.johnnyb.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@ApplicationScoped
public class DynamoDbConfig {

    @ConfigProperty(name = "quarkus.dynamodb.endpoint-override", defaultValue = "http://localhost:4566")
    String endpoint;

    @ConfigProperty(name = "quarkus.dynamodb.aws.region", defaultValue = "us-east-1")
    String region;

    @ConfigProperty(name = "quarkus.dynamodb.aws.credentials.static-provider.access-key-id", defaultValue = "test")
    String accessKeyId;

    @ConfigProperty(name = "quarkus.dynamodb.aws.credentials.static-provider.secret-access-key", defaultValue = "test")
    String secretAccessKey;

    @Produces
    @ApplicationScoped
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .httpClient(software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient.builder().build())
            .build();
    }

    @Produces
    @ApplicationScoped
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }
}
