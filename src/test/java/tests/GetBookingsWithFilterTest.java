package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.Allure.step;
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
        step("Создаем нового пользователя", () ->
                newBooking = new NewBooking());
        step( "Имя: Ann", () ->
                newBooking.setFirstname("Ann"));
        step("Фамилия: Any", () ->
                newBooking.setLastname("Any"));
        step("Общая стоймость : 990", () ->
                newBooking.setTotalprice(990));
        step("Статус оплаты: true" + newBooking.isDepositpaid(), () ->
                newBooking.setDepositpaid(true));
        step("Дата въезда/выезда: 2024-04-13/2024-04-20", () ->
                newBooking.setBookingDates(new BookingDates("2024-04-13", "2024-04-20")));
        step("Дополнительно: breakfast+dinner", () ->
                newBooking.setAdditionalneeds("breakfast+dinner"));


        String requestBody = step("Переделываем java объект в JSON", () -> objectMapper.writeValueAsString(newBooking));
        Response response = step("Отправляем запрос на сервер", () -> apiClient.createBooking(requestBody));

        //Десериализуем тело ответа в объект Booking
        String responseBody = step("JSON переводим в строку", () ->
                response.asString());
        createdBooking = step("Десериализуем тело JSON в Java объект", () ->
                objectMapper.readValue(responseBody, CreatedBooking.class));

        // Проверяем, что тело ответа содержит объект нового бронирования
        step("Проверяем, что тело ответа содержит объект нового бронирования", () -> {
                    assertThat(createdBooking).isNotNull();
                    assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname(), "Проверка имени");
                    assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname(), "Проверка фамилии");
                    assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice(), "Проверка общей стоимости");
                    assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid(), "Проверка статуса оплаты");
                    // здесь сравнение дат по отдельности и компиляция проходит
                    assertEquals(createdBooking.getBooking().getBookingDates().getCheckin(), newBooking.getBookingDates().getCheckin(), "Проверка даты въезда");
                    assertEquals(createdBooking.getBooking().getBookingDates().getCheckout(), newBooking.getBookingDates().getCheckout(), "Проверка даты выезда");
                }
        );

        // Создаю вторую бронь
        step("Создаем нового пользователя", () ->
                newBooking1 = new NewBooking());
        step( "Имя: John", () ->
                newBooking1.setFirstname("John"));
        step("Фамилия: Anybody", () ->
                newBooking1.setLastname("Anybody"));
        step("Общая стоймость : 1290", () ->
                newBooking1.setTotalprice(1290));
        step("Статус оплаты: true" + newBooking.isDepositpaid(), () ->
                newBooking1.setDepositpaid(true));
        step("Дата въезда/выезда: 2024-05-13/2024-05-20", () ->
                newBooking1.setBookingDates(new BookingDates("2024-05-13", "2024-05-20")));
        step("Дополнительно: breakfast", () ->
                newBooking1.setAdditionalneeds("breakfast"));


        String requestBody1 = step("Переделываем java объект в JSON", () -> objectMapper.writeValueAsString(newBooking1));
        Response response1 = step("Отправляем запрос на сервер", () -> apiClient.createBooking(requestBody1));

        //Десериализуем тело ответа в объект Booking
        String responseBody1 = step("JSON переводим в строку", () ->
                response1.asString());
        createdBooking1 = step("Десериализуем тело JSON в Java объект", () ->
                objectMapper.readValue(responseBody1, CreatedBooking.class));

        // Проверяем, что тело ответа содержит объект нового бронирования
        step("Проверяем, что тело ответа содержит объект нового бронирования", () -> {
                    assertThat(createdBooking1).isNotNull();
                    assertEquals(createdBooking1.getBooking().getFirstname(), newBooking1.getFirstname(), "Проверка имени");
                    assertEquals(createdBooking1.getBooking().getLastname(), newBooking1.getLastname(), "Проверка фамилии");
                    assertEquals(createdBooking1.getBooking().getTotalprice(), newBooking1.getTotalprice(), "Проверка общей стоимости");
                    assertEquals(createdBooking1.getBooking().isDepositpaid(), newBooking1.isDepositpaid(), "Проверка статуса оплаты");
                    // здесь сравнение дат по отдельности и компиляция проходит
                    assertEquals(createdBooking1.getBooking().getBookingDates().getCheckin(), newBooking1.getBookingDates().getCheckin(), "Проверка даты въезда");
                    assertEquals(createdBooking1.getBooking().getBookingDates().getCheckout(), newBooking1.getBookingDates().getCheckout(), "Проверка даты выезда");
                }
        );
    }
    @Test
    @Feature("просмотр списка ID с фильтрацией ?firstname=Ann&lastname=")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testFilterByFirstNameWithFieldLastName() {
        // Получаем имя из первой брони
        String firstName = step("Получаем имя из первого бронирования", () ->
        createdBooking.getBooking().getFirstname());

        int expectedId = step("Получаем ID первого бронирования", () ->
                createdBooking.getBookingid()); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по имени
        Response response =  step("Запрос с фильтром ?firstname=Ann&lastname=", () ->
                apiClient.bookingFilterFirstnameLastName(firstName, null)); // Передаем параметр firstname и nullовое lastname

        step("Проверем статус код == 200", () ->
                assertEquals(200, response.getStatusCode()));

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid) с приведением к числам
        List<Integer> bookingIds = step("Получаем список ID с явным приведением к числам", () ->
                response.jsonPath().getList("bookingid", Integer.class));

        step("Проверяем, что ID из нашего бронирования в списке", () ->
                assertTrue(bookingIds.contains(expectedId)));
    }

    @Test
    @Feature("просмотр списка ID с фильтрацией ?firstname=&lastname=Anybody")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testFilterByLastNameWithFieldFirstName() {
        // Получаем фамилию из второй брони
        String lastName = step("Получаем фамилию из второго бронирования", () ->
                createdBooking1.getBooking().getLastname());
        int expectedId = step("Получаем ID второго бронирования", () ->
                createdBooking1.getBookingid()); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по фамилии
        Response response = step("Запрос с фильтром ?firstname=&lastname=Anybody", () ->
                apiClient.bookingFilterFirstnameLastName(null, lastName)); // Передаем параметр lastname и nullовое firstname

        step("Проверяем статус код == 200", () ->
                assertEquals(200, response.getStatusCode()));

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid)
        List<Integer> bookingIds = step("Получаем список ID с явным приведением к числам", () ->
                response.jsonPath().getList("bookingid", Integer.class));

        step("Проверяем, что ID из нашего бронирования в списке", () ->
        assertTrue(bookingIds.contains(expectedId)));
    }

    @Test
    @Feature("просмотр списка ID с фильтрацией ?firstname=Ann")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testFilterByOnlyFirstName() {
        // Получаем имя из первой брони
        String firstName = step("Получаем имя из первого бронирования", () ->
                createdBooking.getBooking().getFirstname());

        int expectedId = step("Получаем ID первого бронирования", () ->
                createdBooking.getBookingid()); // ID который мы ожидаем найти

        // Отправляем запрос с фильтром по имени
        Response response =  step("Запрос с фильтром ?firstname=Ann&lastname=", () ->
                apiClient.bookingFilterFirstnameLastName(firstName, null)); // Передаем параметр firstname и nullовое lastname

        step("Проверем статус код == 200", () ->
                assertEquals(200, response.getStatusCode()));

        // Получаем список ID из ответа (API возвращает список объектов с полем bookingid) с приведением к числам
        List<Integer> bookingIds = step("Получаем список ID с явным приведением к числам", () ->
                response.jsonPath().getList("bookingid", Integer.class));

        step("Проверяем, что ID из нашего бронирования в списке", () ->
                assertTrue(bookingIds.contains(expectedId)));
    }

    @AfterEach
    public void tearDown() {
        step("Получаем токен", () ->
        apiClient.createToken("admin", "password123"));

        step("Удаление первого созданного бронирования", () ->
        apiClient.deleteBooking(createdBooking.getBookingid()));
        // Проверяем, что первое бронирование успешно удалено
        step("Проверяем статус код == 404", () ->
                assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404));

        step("Удаление второго созданного бронирования", () ->
        apiClient.deleteBooking(createdBooking1.getBookingid()));
        // Проверяем, что второе бронирование успешно удалено
        step("Проверяем статус код == 404", () ->
                assertThat(apiClient.bookingByID(createdBooking1.getBookingid()).getStatusCode()).isEqualTo(404));
    }
}

