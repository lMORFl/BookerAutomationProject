package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateGetBookingsTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking; //Храним созданное бронирование
    private CreatedBooking createdBooking; //Новый объект для создания бронирования

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
    }

    @Test
    @Feature("Получение списка ID пользователей с созданием нового пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void GetBooking() throws Exception {
        // Выполняем запрос к эндпоинту /bookig через APIClient
        Response responseBooking = step("Получаем список бронирований", () -> apiClient.booking());

        // Проверяем, что статус-код ответа равен 200
        step("Проверяем статус код == 200", () ->
                assertThat(responseBooking.getStatusCode())
                        .as("Код ответа не совпал с ожидаемым. Ответ: " + responseBooking.getStatusCode())
                        .isEqualTo(200)
        );

        // Десериализуем тело ответа в список объектов Booking
        String responseBodyBooking = step ("Десериализуем тело", () ->
                responseBooking.getBody().asString());
        List<Booking> bookings = step ("Cоздаем список", () ->
                objectMapper.readValue(responseBodyBooking, new TypeReference<List<Booking>>() {}));

        // Проверяем, что тело ответа содержит объекты Booking
        step ("Проверяем, что список не пустой", () ->
                assertThat(bookings).isNotEmpty());

        // Проверяем, что каждый объект Booking содержит валидное значение bookingid
        for (Booking booking : bookings) {
            step ("Проверяем, что каждый ID не равен 0", () ->
                    assertThat(booking.getBookingid()).isGreaterThan(0)); // bookingid должен быть больше чем 0
        }
    }
    @AfterEach
    public void tearDown() {
        // Удаление созданного бронирования
        apiClient.createToken("admin", "password123");
        step ("Удаляем созданного пользователя", () ->
                apiClient.deleteBooking(createdBooking.getBookingid()));

        // Проверяем, что бронирование успешно удалено
        step ("Проверяем, что созданный пользователь действительно удален", () ->
                assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404));
    }
}