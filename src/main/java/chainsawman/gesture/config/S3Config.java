package chainsawman.gesture.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Value("${aws.region}")
    private String region;

    @Value("${aws.disable-ec2-metadata:false}")
    private boolean disableEc2Metadata;

    @Bean
    public AmazonS3 amazonS3() {
        if (disableEc2Metadata) {
            System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true");
        }
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(region)
                .build();
    }
}

