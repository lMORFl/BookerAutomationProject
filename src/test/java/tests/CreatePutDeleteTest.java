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

import static io.qameta.allure.Allure.step;
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
        step("Получаем токен", () ->
                apiClient.createToken("admin", "password123"));

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
    @Feature("Смена в бронировании всех полей (PUT запрос)")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testUpdateBooking() throws Exception {

        NewBooking updatedBooking = step("Создаем объект, которым будем заменять инфу в старом бронировании", () -> new NewBooking());
        step("Меняем имя на Edgar", () ->
                updatedBooking.setFirstname("Edgar"));
        step("Меняем фамилию на Po", () ->
                updatedBooking.setLastname("Po"));
        step("Меняем полную стоимость на 990", () ->
                updatedBooking.setTotalprice(990));
        step("Статус оплаты true", () ->
                updatedBooking.setDepositpaid(true));
        step("Меняем допы на \"without food\"", () ->
                updatedBooking.setAdditionalneeds("without food"));
        step("Меняем даты заеда/выезда на 1845-12-12 / 1846-01-12", () ->
                updatedBooking.setBookingDates(new BookingDates("1845-12-12", "1846-01-12")));

        String requestBody = step("Переводим объект в JSON", () ->
                objectMapper.writeValueAsString(updatedBooking));

        // отправляем id и тело запроса
        Response updatedResponse = step("Отправляем запрос PUT", () ->
                apiClient.updateById(createdBooking.getBookingid(), requestBody));
        step("Проверяем статус код == 200", () ->
                assertThat(updatedResponse.getStatusCode()).isEqualTo(200));
    }
    @AfterEach
    public void tearDown() {
        step("Удаление созданного бронирования", () ->
                apiClient.deleteBooking(createdBooking.getBookingid()));


        // Проверяем, что бронирование успешно удалено
        step("Проверяем код статус == 404", () ->
                assertThat(apiClient.bookingByID(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404));
    }
}
