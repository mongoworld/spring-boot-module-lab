plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "site.gukho"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

// dependencies 블록 위에
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }   // 에이전트로 쓸 jar 하나만

}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")   // 테스트 JVM 시작 시 정식 출입증
}
