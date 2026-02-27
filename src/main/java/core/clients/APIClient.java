package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {

    private final String baseUrl;

    public APIClient() {

        this.baseUrl = determineBaseUrl();
    }

    // Определение базового URL на основе файла конфигурации
    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Фаил конфигурации не найден: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить конфигурационный фаил: " + configFileName, e);
        }

        return properties.getProperty("baseUrl");
    }

    // Настройка базовых параметров HTTP-запросов
    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-type", "application/json")
                .header("Accept", "application/json");
    }

    // GET запрос на эндпоинт /ping
    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) // Используем ENUM для эндпоинта /ping
                .then()
                .statusCode(201) // Ожидаемый статус-код 201 Created
                .extract()
                .response();
    }
    public Response booking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response bookingByID() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKINGBYID.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
