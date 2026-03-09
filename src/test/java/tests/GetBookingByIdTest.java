package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.GetBookingById;
import core.models.NewBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    //  Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }
    @Test
    @Feature("Получение бронирования по ID")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testGetBookingById() throws Exception {
        // Выполняем запрос к эндпоинту /bookig через APIClient

        Response response =  step("Получаем бронирование по ID == 5", () ->
                        apiClient.bookingByID(5));

        // Проверям, что статус-код ответа равен 200
        step("Проверяем статус код == 200", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = step("Переводим JSON в JAVA объект", () ->
                response.getBody().asString());
        GetBookingById infoBooking = step("Парсим строку в нужные типы данных", () ->
                objectMapper.readValue(responseBody, GetBookingById.class));

        // Проверяем, что тело ответа не пустое
        step("Проверяем, что объект и его поля не пустые", () -> {
                assertThat(infoBooking).isNotNull();
        // Проверяем, что поля не пустые
        assertThat(infoBooking.getFirstname()).isNotNull();
        assertThat(infoBooking.getLastname()).isNotNull();
        assertThat(infoBooking.getBookingDates()).isNotNull();
        assertThat(infoBooking.getBookingDates().getCheckin()).isNotNull();
        assertThat(infoBooking.getBookingDates().getCheckout()).isNotNull();
        });
    }
}