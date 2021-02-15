package com.elanlum.ecs.ride.mapper;

import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.RideRequestFieldParser;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RideRequestMapper {

  private final RideRequestFieldParser parser;

  /**
   * Method processes a Map with String representations of parameters to fill arguments of {@link
   * DriverRideRequest} constructor and returns new DriverRideRequest.
   *
   * @param fields - {@link Map} of {@link FieldName} with DriverRideRequest parameters
   * @return new DriverRideRequest
   */
  public DriverRideRequest mapDriverRideRequest(Map<FieldName, String> fields) {
    return new DriverRideRequest(null, fields.get(FieldName.TELEGRAM_ID),
        new Interval(
            LocalDateTime.of(parser.getParsedDate(fields.get(FieldName.RIDE_DATE)),
                parser.getParsedTime(fields.get(FieldName.RIDE_TIME))),
            LocalDateTime.of(parser.getParsedDate(fields.get(FieldName.RIDE_DATE)),
                parser.getParsedTime(fields.get(FieldName.RIDE_TIME))).plusMinutes(
                parser.getParsedExpectationTime(fields.get(FieldName.EXPECTATION_PERIOD)))),
        parser.getParsedPosition(fields.get(FieldName.DEPARTURE_POSITION)),
        parser.getParsedPosition(fields.get(FieldName.DESTINATION_POSITION)),
        RideRequestStatus.AVAILABLE);
  }

  /**
   * Method processes a Map with String representations of parameters to fill arguments of {@link
   * PassengerRideRequest} constructor and returns new PassengerRideRequest.
   *
   * @param fields - {@link Map} of {@link FieldName} with PassengerRideRequest parameters
   * @return new PassengerRideRequest
   */
  public PassengerRideRequest mapPassengerRideRequest(Map<FieldName, String> fields) {
    return new PassengerRideRequest(null, fields.get(FieldName.TELEGRAM_ID),
        new Interval(
            LocalDateTime.of(parser.getParsedDate(fields.get(FieldName.RIDE_DATE)),
                parser.getParsedTime(fields.get(FieldName.RIDE_TIME))),
            LocalDateTime.of(parser.getParsedDate(fields.get(FieldName.RIDE_DATE)),
                parser.getParsedTime(fields.get(FieldName.RIDE_TIME))).plusMinutes(
                parser.getParsedExpectationTime(fields.get(FieldName.EXPECTATION_PERIOD)))),
        parser.getParsedPosition(fields.get(FieldName.DEPARTURE_POSITION)),
        parser.getParsedPosition(fields.get(FieldName.DESTINATION_POSITION)),
        RideRequestStatus.AVAILABLE);
  }
}
