package com.elanlum.ecs.ride.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.RideRequestFieldParser;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideRequestMapperTest {

  private static final String TELEGRAM_ID = "telegramId";
  private static final String DEPARTURE_PARAMS = "59.906842 30.298719";
  private static final String DESTINATION_PARAMS = "59.888854 30.322629";
  private static final String DATE_PARAMS = "02-01-2093";
  private static final String TIME_PARAMS = "4:20";
  private static final String DURATION_PARAMS = "15";

  @InjectMocks
  RideRequestMapper mapper;
  private LocalDate testDate = LocalDate.of(2093, 1, 2);
  private LocalTime testTime = LocalTime.of(4, 20);
  private LocalTime testTimeEnd = testTime.plusMinutes(15);
  private LocalDateTime testEndDateTime = LocalDateTime.of(testDate, testTime).plusMinutes(15);
  private long testDuration = 15L;
  private Position testDeparture = new Position(59.906842f, 30.298719f);
  private Position testDestination = new Position(59.888854f, 30.322629f);

  @Mock
  RideRequestFieldParser parser;
  private Map<FieldName, String> fields = new HashMap<>();

  @BeforeEach
  void beforeAll() {
    fields.put(FieldName.TELEGRAM_ID, TELEGRAM_ID);
    fields.put(FieldName.DEPARTURE_POSITION, DEPARTURE_PARAMS);
    fields.put(FieldName.DESTINATION_POSITION, DESTINATION_PARAMS);
    fields.put(FieldName.RIDE_DATE, DATE_PARAMS);
    fields.put(FieldName.RIDE_TIME, TIME_PARAMS);
    fields.put(FieldName.EXPECTATION_PERIOD, DURATION_PARAMS);

    doReturn(testDeparture).when(parser).getParsedPosition(DEPARTURE_PARAMS);
    doReturn(testDestination).when(parser).getParsedPosition(DESTINATION_PARAMS);
    doReturn(testDate).when(parser).getParsedDate(DATE_PARAMS);
    doReturn(testTime).when(parser).getParsedTime(TIME_PARAMS);
    doReturn(testDuration).when(parser).getParsedExpectationTime(DURATION_PARAMS);
  }

  @Test
  void mapDriverRideRequest_returnsDriverRideRequest() {
    DriverRideRequest testRideRequest = mapper.mapDriverRideRequest(fields);

    assertNull(testRideRequest.getId());
    assertEquals("telegramId", testRideRequest.getUserId());
    assertEquals(Role.DRIVER, testRideRequest.getRole());
    assertEquals(59.906842f, testRideRequest.getDeparturePoint().getLatitude(), 0.000005f);
    assertEquals(30.298719f, testRideRequest.getDeparturePoint().getLongitude(), 0.000005f);
    assertEquals(59.888854f, testRideRequest.getDestinationPoint().getLatitude(), 0.000005f);
    assertEquals(30.322629f, testRideRequest.getDestinationPoint().getLongitude(), 0.000005f);
    assertEquals(testEndDateTime, testRideRequest.getRideDate().getEnd());
    assertEquals("2093-01-02T04:20", testRideRequest.getRideDate().getStart().toString());
  }

  @Test
  void mapDriverRideRequest_returnsPassengerRideRequest() {
    PassengerRideRequest testRideRequest = mapper.mapPassengerRideRequest(fields);

    assertNull(testRideRequest.getId());
    assertEquals("telegramId", testRideRequest.getUserId());
    assertEquals(Role.PASSENGER, testRideRequest.getRole());
    assertEquals(59.906842f, testRideRequest.getDeparturePoint().getLatitude(), 0.000005f);
    assertEquals(30.298719f, testRideRequest.getDeparturePoint().getLongitude(), 0.000005f);
    assertEquals(59.888854f, testRideRequest.getDestinationPoint().getLatitude(), 0.000005f);
    assertEquals(30.322629f, testRideRequest.getDestinationPoint().getLongitude(), 0.000005f);
    assertEquals(testEndDateTime, testRideRequest.getRideDate().getEnd());
    assertEquals("2093-01-02T04:20", testRideRequest.getRideDate().getStart().toString());
  }
}