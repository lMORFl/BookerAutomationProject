package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreatePutDeleteTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking; //Храним созданное бронирование
    private CreatedBooking createdBooking; //Новый объект для создания бронирования

    @BeforeEach
    public void setup() throws JsonProcessingException {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        // Создаем объект Booking с необходимыми данными
        newBooking = new NewBooking();
        newBooking.setFirstname("Ann");
        newBooking.setLastname("Any");
        newBooking.setTotalprice(990);
        newBooking.setDepositpaid(true);
        newBooking.setBookingDates(new BookingDates("2024-04-13", "2024-04-20"));
        newBooking.setAdditionalneeds("breakfast+dinner");

        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);

        //Десериализуем тело ответа в объект Booking
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        // Проверяем, что тело ответа содержит объект нового бронирования
        assertThat(createdBooking).isNotNull();
        assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
        assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
        assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
        assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid());
        // здесь сравнение дат по отдельности и компиляция проходит
        assertEquals(createdBooking.getBooking().getBookingDates().getCheckin(), newBooking.getBookingDates().getCheckin());
        assertEquals(createdBooking.getBooking().getBookingDates().getCheckout(), newBooking.getBookingDates().getCheckout());

    }
    @Test
    public void testUpdateBooking() throws Exception {
        NewBooking updatedBooking = new NewBooking();
        updatedBooking.setFirstname("Edgar");
        updatedBooking.setLastname("Po");
        updatedBooking.setTotalprice(990);
        updatedBooking.setDepositpaid(true);
        updatedBooking.setAdditionalneeds("without food");
        updatedBooking.setBookingDates(new BookingDates("1845-12-12", "1846-01-12"));

        String requestBody = objectMapper.writeValueAsString(updatedBooking);

        // отправляем id и тело запроса
        Response updatedResponse = apiClient.updateById(createdBooking.getBookingid(), requestBody);
        assertThat(updatedResponse.getStatusCode()).isEqualTo(200);
    }
    @AfterEach
    public void tearDown() {
        apiClient.deleteBooking(createdBooking.getBookingid());

        // Проверяем, что бронирование успешно удалено
        assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}
