package restfulbooker.base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import restfulbooker.config.ConfigLoader;

public abstract class BaseTest {

    protected RequestSpecification requestSpec;

    @BeforeEach
    void setUpRequestSpec() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ConfigLoader.baseUrl())
                .setContentType(ContentType.JSON)
                .build();
    }
}
