package com.johnnyb.infrastructure.modules.storage;

import com.pulumi.aws.s3.Bucket;
import com.pulumi.aws.s3.BucketArgs;
import com.pulumi.aws.s3.BucketPublicAccessBlock;
import com.pulumi.aws.s3.BucketPublicAccessBlockArgs;
import com.pulumi.aws.s3.BucketVersioningV2;
import com.pulumi.aws.s3.BucketVersioningV2Args;
import com.pulumi.aws.s3.inputs.BucketVersioningV2VersioningConfigurationArgs;
import com.pulumi.core.Output;

import java.util.Map;

/**
 * S3 Module - Creates S3 buckets for assets and backups
 */
public class S3Module {
    private final String name;
    private final String environment;
    
    private Bucket assetsBucket;
    private Bucket backupsBucket;

    /**
     * Create S3 buckets
     * 
     * @param name Base name for resources
     * @param environment Environment name
     */
    public S3Module(String name, String environment) {
        this.name = name;
        this.environment = environment;
        
        createAssetsBucket();
        createBackupsBucket();
    }

    /**
     * Create S3 bucket for application assets
     */
    private void createAssetsBucket() {
        this.assetsBucket = new Bucket(name + "-assets", BucketArgs.builder()
            .bucket(name + "-assets")
            .tags(Map.of(
                "Name", name + "-assets",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Application assets"
            ))
            .build());

        // Block public access
        new BucketPublicAccessBlock(name + "-assets-public-access-block",
            BucketPublicAccessBlockArgs.builder()
                .bucket(assetsBucket.id())
                .blockPublicAcls(true)
                .blockPublicPolicy(true)
                .ignorePublicAcls(true)
                .restrictPublicBuckets(true)
                .build());

        // Enable versioning for assets in production
        if (environment.equals("prod")) {
            new BucketVersioningV2(name + "-assets-versioning",
                BucketVersioningV2Args.builder()
                    .bucket(assetsBucket.id())
                    .versioningConfiguration(BucketVersioningV2VersioningConfigurationArgs.builder()
                        .status("Enabled")
                        .build())
                    .build());
        }
    }

    /**
     * Create S3 bucket for backups
     */
    private void createBackupsBucket() {
        this.backupsBucket = new Bucket(name + "-backups", BucketArgs.builder()
            .bucket(name + "-backups")
            .tags(Map.of(
                "Name", name + "-backups",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Database and configuration backups"
            ))
            .build());

        // Block public access
        new BucketPublicAccessBlock(name + "-backups-public-access-block",
            BucketPublicAccessBlockArgs.builder()
                .bucket(backupsBucket.id())
                .blockPublicAcls(true)
                .blockPublicPolicy(true)
                .ignorePublicAcls(true)
                .restrictPublicBuckets(true)
                .build());

        // Enable versioning for backups
        new BucketVersioningV2(name + "-backups-versioning",
            BucketVersioningV2Args.builder()
                .bucket(backupsBucket.id())
                .versioningConfiguration(BucketVersioningV2VersioningConfigurationArgs.builder()
                    .status("Enabled")
                    .build())
                .build());
    }

    // Getters
    public Bucket getAssetsBucket() {
        return assetsBucket;
    }

    public Bucket getBackupsBucket() {
        return backupsBucket;
    }

    public Output<String> getAssetsBucketName() {
        return assetsBucket.bucket();
    }

    public Output<String> getBackupsBucketName() {
        return backupsBucket.bucket();
    }

    public Output<String> getAssetsBucketArn() {
        return assetsBucket.arn();
    }

    public Output<String> getBackupsBucketArn() {
        return backupsBucket.arn();
    }
}
