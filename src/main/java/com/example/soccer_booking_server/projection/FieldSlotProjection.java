package com.example.soccer_booking_server.projection;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface FieldSlotProjection {
    Integer getSlotId();
    Integer getSlotNumber();
    LocalTime getSlotStart();
    LocalTime getSlotEnd();
    BigDecimal getPrice();
    Boolean getIsPeak();
}
