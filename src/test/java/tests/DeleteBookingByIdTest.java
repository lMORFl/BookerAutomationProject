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

public class DeleteBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    //  Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");
    }

    @Test
    public void testDeleteBookingById() throws Exception {
        // Выполняем запрос к эндпоинту /bookig через APIClient
        Response response = apiClient.booking();

        // Проверям, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });

        // Проверяем, что тело ответа содержит объекты Booking
        assertThat(bookings).isNotEmpty(); // Проверяем, что список не пуст

        // Проверяем, что каждый объект Booking содержит валидное начение bookingid
        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0); // bookingid должен быть больше чем 0
        }

        // Берем пятый ID из списка
        int fifthBookingId = bookings.get(4).getBookingid();

        // Удаляем
        Response deleteResponse = apiClient.deleteBooking(fifthBookingId);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(201);

        // Проверяем, что удалено
        Response checkResponse = apiClient.bookingByID(fifthBookingId);
        assertThat(checkResponse.getStatusCode()).isEqualTo(404);
    }
}