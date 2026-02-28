package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {

    private final String baseUrl;
    private String token;

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
                .header("Accept", "application/json")
                .filter(addAutTokenFilter());
    }


    // Метод получения токена
    public void createToken(String username, String password) {
        // Тело запроса для получения токена
        String requestBody = String.format("{\"username\": \"%s\",\"password\":\"%s\"}", username, password);
        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post(ApiEndpoints.AUTH.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Извлекаем токен из ответа
        token = response.jsonPath().getString("token");
    }

    // Фильтр для добавления токена в заголовок Authorization
    private Filter addAutTokenFilter() {
        return (FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
        }
            return ctx.next(requestSpec, responseSpec);
        };
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

    public Response bookingByID(int infoBookingId) {
        return getRequestSpec()
                .pathParam("id", infoBookingId)
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .extract()
                .response();
    }

    public Response deleteBooking(int bookingID) {
        return getRequestSpec()
                .pathParam("id", bookingID) // Указываем path parameter для ID
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}") // Используем параметр пути в запросе
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .response();
    }

}
