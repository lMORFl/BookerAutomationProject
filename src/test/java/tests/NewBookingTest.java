package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import core.clients.APIClient;
import core.models.CreatedBooking;
import core.models.BookingDates;
import core.models.NewBooking;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking; //Храним созданное бронирование
    private CreatedBooking createdBooking; //Новый объект для создания бронирования

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем объект Booking с необходимыми данными
        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(190);
        newBooking.setDepositpaid(true);
        newBooking.setBookingDates(new BookingDates("2025-04-13", "2025-04-20"));
        newBooking.setAdditionalneeds("dinner");
    }

    @Test
    public void createBooking() throws JsonProcessingException {
        // Выполняем запрос к эндпоинту /booking через APIClient
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);

        // Проверяем статус -код 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        //Десериализуем тело ответа в объект Booking
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        // Проверяем, что тело ответа содержит объект нового бронирования
        assertThat(createdBooking).isNotNull();
        assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
        assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
        assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
        assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid());
//        assertEquals(createdBooking.getBooking().getBookingdates(), newBooking.getBookingdates());   при таком сравнении ошибка выдает два разных объекта

        // здесь сравнение дат по отдельности и компиляция проходит
        assertEquals(
                createdBooking.getBooking().getBookingDates().getCheckin(),
                newBooking.getBookingDates().getCheckin()
        );
        assertEquals(
                createdBooking.getBooking().getBookingDates().getCheckout(),
                newBooking.getBookingDates().getCheckout()
        );
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), newBooking.getAdditionalneeds());
    }
        @AfterEach
                public void tearDown() {
            // Удаление созданного бронирования
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(createdBooking.getBookingid());

            // Проверяем, что бронирование успешно удалено
            assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
        }
    }

