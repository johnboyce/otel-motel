package com.johnnyb.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;

@Provider
public class AwsRegionResponseFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(AwsRegionResponseFilter.class);
    private static final String AWS_REGION_HEADER = "X-AWS-Region";

    @ConfigProperty(name = "aws.region", defaultValue = "none")
    private String awsRegion;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Add the AWS region header to all GraphQL requests
        String path = requestContext.getUriInfo().getPath();
        if (path != null && path.startsWith("graphql")) {
            responseContext.getHeaders().add(AWS_REGION_HEADER, awsRegion);
            LOG.debugf("Added %s header with value: %s", AWS_REGION_HEADER, awsRegion);
        }
    }

    // Package-private setter for testing
    void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }
}
