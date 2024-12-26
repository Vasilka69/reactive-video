package ru.vasili4.reactive_video.s3;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
class S3Test {

	@Autowired
	private MinioClient minioClient;

	@Test
	void test() throws Exception {
		System.out.println();

		String bucketName = "test222";

//		minioClient.makeBucket(
//				MakeBucketArgs.builder()
//						.bucket(bucketName)
//						.build()
//		);

		String stringPath = "src/test/resources/tmp/1.xlsx";
		Path path = Path.of(stringPath);
		File file = new File(path.toString());
		FileInputStream fileInputStream = new FileInputStream(file);

		minioClient.putObject(PutObjectArgs
				.builder()
				.bucket(bucketName)
				.object(path.toString().replace("\\", "/"))
//				.stream(fileInputStream, Files.size(path), 1024 * 1024 * 5)
				.stream(fileInputStream, -1, 1024 * 1024 * 5)
				.build());

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}

}
