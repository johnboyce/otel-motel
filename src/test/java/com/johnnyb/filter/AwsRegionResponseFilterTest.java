package com.johnnyb.filter;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(AwsRegionResponseFilterTest.TestAwsRegionProfile.class)
class AwsRegionResponseFilterTest {

    /**
     * Test profile that configures the AWS region for testing
     */
    public static class TestAwsRegionProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("aws.region", "us-east-1");
        }
    }

    @Test
    void testAwsRegionHeaderPresentInGraphQLResponse() {
        // GraphQL query to test - use introspection query which always works
        String query = "{ \"query\": \"{ __typename }\" }";

        given()
            .contentType("application/json")
            .body(query)
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .header("X-AWS-Region", equalTo("us-east-1"));
    }

    @Test
    void testAwsRegionHeaderWithHelloQuery() {
        // Test with the simple hello query that doesn't require DB
        String query = "{ \"query\": \"{ sayHello(name: \\\"Test\\\") }\" }";

        given()
            .contentType("application/json")
            .body(query)
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .header("X-AWS-Region", equalTo("us-east-1"));
    }
}
