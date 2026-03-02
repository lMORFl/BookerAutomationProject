package core.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewBooking {
    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    @JsonProperty("bookingdates") private BookingDates bookingDates;
    private String additionalneeds;

    public String getFirstname() {
        return firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public boolean isDepositpaid() {
        return depositpaid;
    }

    public void setDepositpaid(boolean depositpaid) {
        this.depositpaid = depositpaid;
    }

    @JsonProperty("bookingdates") public BookingDates getBookingDates() {
        return bookingDates;
    }

    @JsonProperty("bookingdates") public void setBookingDates(BookingDates bookingDates) {
        this.bookingDates = bookingDates;
    }

    public String getAdditionalneeds() {
        return additionalneeds;
    }

    public void setAdditionalneeds(String additionalneeds) {
        this.additionalneeds = additionalneeds;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
}
