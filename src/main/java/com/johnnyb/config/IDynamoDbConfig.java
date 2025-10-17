package com.johnnyb.config;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public interface IDynamoDbConfig {
    DynamoDbClient dynamoDbClient();
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient);
}
