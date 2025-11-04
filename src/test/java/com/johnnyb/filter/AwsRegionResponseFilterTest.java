package com.johnnyb.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AwsRegionResponseFilterTest {

    private AwsRegionResponseFilter filter;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;
    private UriInfo uriInfo;
    private MultivaluedMap<String, Object> headers;

    @BeforeEach
    void setUp() {
        filter = new AwsRegionResponseFilter();
        filter.awsRegion = "us-east-1";
        
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);
        uriInfo = mock(UriInfo.class);
        headers = new MultivaluedHashMap<>();
        
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(responseContext.getHeaders()).thenReturn(headers);
    }

    @Test
    void testAwsRegionHeaderAddedForGraphQLRequest() throws IOException {
        // Mock a GraphQL request
        when(uriInfo.getPath()).thenReturn("graphql");
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.containsKey("X-AWS-Region"));
        assertEquals("us-east-1", headers.getFirst("X-AWS-Region"));
    }

    @Test
    void testAwsRegionHeaderAddedForGraphQLSubPath() throws IOException {
        // Mock a GraphQL UI or schema request
        when(uriInfo.getPath()).thenReturn("graphql/schema.graphql");
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.containsKey("X-AWS-Region"));
        assertEquals("us-east-1", headers.getFirst("X-AWS-Region"));
    }

    @Test
    void testAwsRegionHeaderNotAddedForNonGraphQLRequest() throws IOException {
        // Mock a non-GraphQL request
        when(uriInfo.getPath()).thenReturn("api/hotels");
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.isEmpty());
    }

    @Test
    void testAwsRegionHeaderWithDefaultValue() throws IOException {
        // Test with default "none" value
        filter.awsRegion = "none";
        when(uriInfo.getPath()).thenReturn("graphql");
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.containsKey("X-AWS-Region"));
        assertEquals("none", headers.getFirst("X-AWS-Region"));
    }

    @Test
    void testAwsRegionHeaderWithNullPath() throws IOException {
        // Test with null path (shouldn't happen, but test defensive coding)
        when(uriInfo.getPath()).thenReturn(null);
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.isEmpty());
    }
}
