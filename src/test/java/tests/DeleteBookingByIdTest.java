package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    //  Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        step("Получаем токен", () ->
                apiClient.createToken("admin", "password123"));
    }

    @Test
    @Feature("Удаление бронирования по ID")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testDeleteBookingById() throws Exception {
        // Выполняем запрос к эндпоинту /booking через APIClient
        Response response = step("Получаем список с ID", () ->
                apiClient.booking());

        // Проверяем, что статус-код ответа равен 200
        step("Проверяем статус код == 200", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = step("JSON переводи в объект JAVA", () ->
                response.getBody().asString());
        List<Booking> bookings = step("Объект JAVA переводим в список", () ->
                objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        }));

        // Проверяем, что тело ответа содержит объекты Booking
        step("Проверям что список не пуст", () ->
                assertThat(bookings).isNotEmpty()); // Проверяем, что список не пуст

        // Проверяем, что каждый объект Booking содержит валидное начение bookingid
        for (Booking booking : bookings) {
            step("Проверяем, что ID != 0", () ->
                    assertThat(booking.getBookingid()).isGreaterThan(0)); // bookingid должен быть больше чем 0
        }

        // Берем пятый ID из списка
        int fifthBookingId = bookings.get(4).getBookingid();

        // Удаляем
        Response deleteResponse = step("Удаляем пятый пункт в списке", () ->
                apiClient.deleteBooking(fifthBookingId));
        step("Проверяем статус код == 201", () ->
                assertThat(deleteResponse.getStatusCode()).isEqualTo(201));

        // Проверяем, что удалено
        step("Проверяем, что удаление действительно произошло, статус код == 404", () -> {
        Response checkResponse = apiClient.bookingByID(fifthBookingId);
        assertThat(checkResponse.getStatusCode()).isEqualTo(404);
        });
    }
}