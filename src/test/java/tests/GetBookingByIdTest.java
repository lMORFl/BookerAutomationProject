package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.GetBookingById;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void testGetBookingById() throws Exception {
        // Выполняем запрос к эндпоинту /bookig через APIClient
        Response response = apiClient.bookingByID();

        // Проверям, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        GetBookingById infoBooking = objectMapper.readValue(responseBody, GetBookingById.class);

        // Проверяем, что тело ответа не пустое
        assertThat(infoBooking).isNotNull();
        // Проверяем, что поля не пустые
        assertThat(infoBooking.getFirstname()).isNotNull();
        assertThat(infoBooking.getLastname()).isNotNull();
        assertThat(infoBooking.getBookingdates()).isNotNull();
        assertThat(infoBooking.getBookingdates().getCheckin()).isNotNull();
        assertThat(infoBooking.getBookingdates().getCheckout()).isNotNull();
    }
}