package com.johnnyb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Hotel {
    
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String description;
    private Integer starRating;
    private List<String> roomIds;
    
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
