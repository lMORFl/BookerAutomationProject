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

public class CreatePatchDeleteTest {
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
    @Feature("Смена в бронировании имени и фамилии (PATCH запрос)")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Polyakov Semyon")
    public void testPatchBooking() throws Exception {
//        NewBooking updatedBooking = new NewBooking();
//        updatedBooking.setFirstname("Edgar");  новый объект создавать не нужно!!!
//        updatedBooking.setLastname("Po"); работаем с тем, что создали
        step("Меняем имя в созданной брони на \"Edgar\"", () -> newBooking.setFirstname("Edgar"));
        step("Меняем фамилию в созданной брони на \"Po\"", () -> newBooking.setLastname("Po"));
        String requestBody = step("Строку переводим в JSON", () ->
                objectMapper.writeValueAsString(newBooking));

        // отправляем id и тело запроса
        Response updatedResponse = step("Отправляем запрос PATCH", () ->
                apiClient.updatePatchById(createdBooking.getBookingid(), requestBody));
        step("Проверяем код статус == 200", () ->
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
