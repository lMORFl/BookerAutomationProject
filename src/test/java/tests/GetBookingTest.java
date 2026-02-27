package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    //  Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBooking() throws Exception {
        // Выполняем запрос к эндпоинту /bookig через APIClient
        Response response = apiClient.booking();

        // Проверям, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {});

        // Проверяем, что тело ответа содержит объекты Booking
        assertThat(bookings).isNotEmpty(); // Промеряем, что список не пуст

        // Проверяем, что каждый объект Booking содержит валидное начение bookingid
        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0); // bookingid должен быть больше чем 0
        }
    }
}
