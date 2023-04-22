//package com.example.emotrak.config;
//
//import com.amazonaws.AmazonClientException;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.PutObjectResult;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//
//import java.io.File;
//
///*
// * 단위 테스트는 Mock 객체를 사용하여 개별 컴포넌트를 독립적으로 테스트하기 때문에,
// * 설정 클래스와 같이 여러 컴포넌트 간의 상호 작용을 정의하는 경우에는 적합하지 않을 수 있음
// * 따라서 Spring Boot 테스트 환경에서 실행 ( 실제 애플리케이션과 유사한 환경에서 통합 테스트를 진행)
// * LocalStack 을 사용하여 로컬 환경에서 AWS 서비스를 테스트하는 방법...알아보는중..
// */
//@SpringBootTest
//@TestPropertySource(properties = {
//        "cloud.aws.credentials.access-key=testAccessKey",
//        "cloud.aws.credentials.secret-key=testSecretKey",
//        "cloud.aws.region.static=testRegion",
//        "cloud.aws.s3.bucket=testbucket"})
//
//class FileUploadConfigTest {
//
//    @Autowired
//    private AmazonS3 amazonS3Client;
//
//    @Test
//    @DisplayName("AmazonS3 클라이언트 객체가 정상적으로 생성되는지 확인하는 테스트")
//    void testAmazonS3Client1() {
//        // given
//        String expectedAccessKey = "testAccessKey";
//        String expectedSecretKey = "testSecretKey";
//        String expectedRegion = "testRegion";
//
//        // when
//        AWSCredentialsProvider awsCreds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(expectedAccessKey, expectedSecretKey));
//        AmazonS3 expectedS3Client = AmazonS3ClientBuilder.standard()
//                .withRegion(expectedRegion)
//                .withCredentials(awsCreds)
//                .build();
//
//        // then
//        Assertions.assertEquals(expectedS3Client.getClass(), amazonS3Client.getClass(), "AmazonS3 클라이언트 객체 생성에 실패하였습니다.");
//    }
//
//    @Test
//    @DisplayName("잘못된 인증 정보와 지정한 리전 정보로 AmazonS3 클라이언트 객체 생성에 실패하는지 확인하는 테스트")
//    void testAmazonS3ClientFail() {
//        // given
//        String accessKey = "invalidAccessKey";
//        String secretKey = "invalidSecretKey";
//        String region = "invalidRegion";
//
//        // when, then
//        Assertions.assertThrows(AmazonClientException.class, () -> {
//            AWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
//            AmazonS3ClientBuilder.standard()
//                    .withRegion(region)
//                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//                    .build();
//            amazonS3Client.listBuckets();
//        });
//    }
//
//    @Test
//    @DisplayName("AWS 인증 정보와 지정한 리전 정보를 이용하여 AmazonS3 클라이언트 객체 생성하는 테스트")
//     void testAmazonS3Client2() {
//        // AmazonS3 클라이언트 객체 생성
//        AmazonS3 s3Client = amazonS3Client;
//
//        // 생성된 클라이언트 객체가 null이 아닌지 확인
//        Assertions.assertNotNull(s3Client);
//    }
//
//    @Test
//    @DisplayName("AmazonS3 클라이언트 객체를 이용하여 파일을 업로드할 때, 존재하지 않는 파일을 업로드하면 AmazonClientException 예외가 발생하는지 확인하는 테스트")
//    public void testUploadNonexistentFile() {
//        // given
//        String bucketname = "testbucket";
//        String key = "testKey";
//        File file = new File("nonexistentFile.txt");
//
//        // when, then
//        Assertions.assertThrows(AmazonClientException.class, () -> {
//            amazonS3Client.putObject(bucketname, key, file);
//        });
//    }
//
//    @Test
//    @DisplayName("AmazonS3 클라이언트 객체 생성 후, Bucket 객체를 생성하는지 확인하는 테스트")
//    void testCreateBucket() {
//        // given
//        String bucketname = "testbucket";
//
//        // when
//        amazonS3Client.createBucket(bucketname);
//
//        // then
//        Assertions.assertTrue(amazonS3Client.doesBucketExistV2(bucketname), "Bucket 객체 생성에 실패하였습니다.");
//    }
//
//    @Test
//    @DisplayName("AmazonS3 클라이언트 객체를 이용하여 파일을 업로드하는지 확인하는 테스트")
//     void testUploadFile() {
//        // given
//        String bucketName = "testBucket";
//        String key = "testKey";
//        File file = new File("https://emotraks3bucket.s3.ap-northeast-2.amazonaws.com/f8111e61-44dc-4305-bfd1-1538be48f9f5_129577095.jpeg");
//
//        // when
//        PutObjectResult result = amazonS3Client.putObject(bucketName, key, file);
//
//        // then
//        Assertions.assertNotNull(result.getETag(), "파일 업로드에 실패하였습니다.");
//    }
//
//    @Test
//    @DisplayName("AmazonS3 클라이언트 객체를 이용하여 파일을 삭제하는지 확인하는 테스트")
//    void testDeleteFile() {
//        // given
//        String bucketName = "testBucket";
//        String key = "testKey";
//
//        // when
//        amazonS3Client.deleteObject(bucketName, key);
//
//        // then
//        Assertions.assertFalse(amazonS3Client.doesObjectExist(bucketName, key), "파일 삭제에 실패하였습니다.");
//    }
//
//
//}