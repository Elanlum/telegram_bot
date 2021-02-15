package com.elanlum.ecs.ride.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Position;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  DriverRideRequest driverRideRequest;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  PassengerRideRequest passengerRideRequest;
  @InjectMocks
  Ride ride;

  private Position testPassengerDeparture = new Position(55.000000f, 35.000000f);
  private LocalDateTime testDriverEarlierLdt = LocalDateTime.of(2006, Month.JUNE, 6, 6, 6);
  private LocalDateTime testDriverLaterLdt = LocalDateTime.of(2012, Month.DECEMBER, 12, 12, 12);
  private LocalDateTime testPassengerLdt = LocalDateTime.of(2009, Month.SEPTEMBER, 9, 9, 9);

  @Test
  void getStartingPosition_returnsCorrectPosition() {
    doReturn(testPassengerDeparture).when(passengerRideRequest).getDeparturePoint();
    assertEquals(testPassengerDeparture, ride.getStartingPosition());
  }

  @Test
  void givenDriversRequestIsEarlier_returnsPassengerRequestLdt() {
    when(driverRideRequest.getRideDate().getStart()).thenReturn(testDriverEarlierLdt);
    when(passengerRideRequest.getRideDate().getStart()).thenReturn(testPassengerLdt);

    assertEquals(testPassengerLdt, ride.getRideDateTime());
  }

  @Test
  void givenDriversRequestIsLater_returnsDriverRequestLdt() {
    when(driverRideRequest.getRideDate().getStart()).thenReturn(testDriverLaterLdt);
    when(passengerRideRequest.getRideDate().getStart()).thenReturn(testPassengerLdt);

    assertEquals(testDriverLaterLdt, ride.getRideDateTime());
  }
}