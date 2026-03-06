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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class GetBookingsWithFilterTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking; //Храним созданное бронирование
    private NewBooking newBooking1; //Храним второе созданное бронирование
    private CreatedBooking createdBooking; //Новый объект для создания бронирования
    private CreatedBooking createdBooking1; //Новый объект для создания бронирования

    @BeforeEach
    public void setup() throws JsonProcessingException {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

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

        // Создаю вторую бронь
        newBooking1 = new NewBooking();
        newBooking1.setFirstname("Jonh");
        newBooking1.setLastname("Anybody");
        newBooking1.setTotalprice(690);
        newBooking1.setDepositpaid(true);
        newBooking1.setBookingDates(new BookingDates("2025-05-15", "2025-05-25"));
        newBooking1.setAdditionalneeds("dinner");


        String requestBody1 = objectMapper.writeValueAsString(newBooking1);
        Response response1 = apiClient.createBooking(requestBody1);

        //Десериализуем тело ответа в объект Booking
        String responseBody1 = response1.asString();
        createdBooking1 = objectMapper.readValue(responseBody1, CreatedBooking.class);

        // Проверяем, что тело ответа содержит объект нового бронирования
        assertThat(createdBooking1).isNotNull();
        assertEquals(createdBooking1.getBooking().getFirstname(), newBooking1.getFirstname());
        assertEquals(createdBooking1.getBooking().getLastname(), newBooking1.getLastname());
        assertEquals(createdBooking1.getBooking().getTotalprice(), newBooking1.getTotalprice());
        assertEquals(createdBooking1.getBooking().isDepositpaid(), newBooking1.isDepositpaid());
        // здесь сравнение дат по отдельности и компиляция проходит
        assertEquals(createdBooking1.getBooking().getBookingDates().getCheckin(), newBooking1.getBookingDates().getCheckin());
        assertEquals(createdBooking1.getBooking().getBookingDates().getCheckout(), newBooking1.getBookingDates().getCheckout());

    }
    @Test
    public void testFilterByFirstNameWithFieldLastName() {
        // Получаем имя из первой брони
        String firstName = createdBooking.getBooking().getFirstname();
        int expectedId = createdBooking.getBookingid(); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по имени
        Response response = apiClient.bookingFilterFirstnameLastName(firstName, null); // Передаем параметр firstname и nullовое lastname

        assertEquals(200, response.getStatusCode());

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid) с приведением к числам
        List<Integer> bookingIds = response.jsonPath().getList("bookingid", Integer.class);

        assertTrue(bookingIds.contains(expectedId));
    }

    @Test
    public void testFilterByLastNameWithFieldFirstName() {
        // Получаем фамилию из второй брони
        String lastName = createdBooking1.getBooking().getLastname();
        int expectedId = createdBooking1.getBookingid(); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по фамилии
        Response response = apiClient.bookingFilterFirstnameLastName(null, lastName); // Передаем параметр lastname и nullовое firstname

        assertEquals(200, response.getStatusCode());

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid)
        List<Integer> bookingIds = response.jsonPath().getList("bookingid", Integer.class);

        assertTrue(bookingIds.contains(expectedId));
    }

    @Test
    public void testFilterByOnlyFirstName() {
        // Получаем имя из первой брони
        String firstName = createdBooking.getBooking().getFirstname();
        int expectedId = createdBooking.getBookingid(); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по имени
        Response response = apiClient.bookingFilterFirstname(firstName); // Передаем параметр firstname

        assertEquals(200, response.getStatusCode());

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid) с приведением к числам
        List<Integer> bookingIds = response.jsonPath().getList("bookingid", Integer.class);

        assertTrue(bookingIds.contains(expectedId));
    }

    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());
        // Проверяем, что первое бронирование успешно удалено
        assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
        apiClient.deleteBooking(createdBooking1.getBookingid());
        // Проверяем, что второе бронирование успешно удалено
        assertThat(apiClient.bookingByID(createdBooking1.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}

