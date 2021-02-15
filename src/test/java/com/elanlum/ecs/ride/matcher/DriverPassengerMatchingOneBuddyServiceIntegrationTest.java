package com.elanlum.ecs.ride.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.RideMatchingNotification;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag(TestCategory.INTEGRATION)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestsConfig.class)
class DriverPassengerMatchingOneBuddyServiceIntegrationTest {

  @Autowired
  private PassengerRideRequestRepo passengerRideRequestRepo;
  @Autowired
  private DriverPassengerMatchingOneBuddyService oneBuddyService;
  @Autowired
  private NotificationMessageQueue notificationMessageQueue;
  @Autowired
  private UserService userService;
  @Autowired
  private RideService rideService;
  @Autowired
  private PassengerRideRequestService passengerRideRequestService;
  @Autowired
  private DriverRideRequestService driverRideRequestService;

  @Test
  @DisplayName("matchAndNotify method produces Notification and changes statuses of participants")
  void matchAndNotifyIsComplete() {
    User passenger = new User(
        null, "passenger", "Ivan", "1", 1L);
    User driver = new User(
        null, "driver", "driver", "2", 2L);

    Position driverPosition = new Position(59.833562f, 30.347907f);  //zvyozdnaya metro
    Position passengerPositionOne = new Position(59.835168f,
        30.345053f); //lensoveta  ~250m car by yandex rank-1
    Position destination = new Position(59.851937f, 30.268448f);

    String passId = userService.save(passenger).block().getId();
    String driverId = userService.save(driver).block().getId();

    PassengerRideRequest passRideRequestOne = new PassengerRideRequest(null, passId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        passengerPositionOne, destination, RideRequestStatus.AVAILABLE);

    DriverRideRequest driverRideRequest = new DriverRideRequest(null, driverId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        driverPosition, destination, RideRequestStatus.AVAILABLE);

    PassengerRideRequest savedPassengerRequest = passengerRideRequestService
        .save(passRideRequestOne).block();
    DriverRideRequest savedDriverRequest = driverRideRequestService
        .save(driverRideRequest).block();

    oneBuddyService.matchAndNotify(savedDriverRequest);

    RideMatchingNotification firstNotification =
        (RideMatchingNotification) notificationMessageQueue.getNotificationStream().blockFirst();

    assertEquals(RideRequestStatus.MATCHED,
        passengerRideRequestService.findById(savedPassengerRequest.getId()).block().getStatus());
    assertEquals(RideRequestStatus.MATCHED,
        driverRideRequestService.findById(savedDriverRequest.getId()).block().getStatus());
    assertEquals(firstNotification.getRide().getDriver().getName(),
        rideService.findById(firstNotification.getRide().getId()).block().getDriver().getName());
    assertEquals("Ivan", firstNotification.getRide().getPassenger().getName());
    assertTrue(firstNotification.getMessage().contains("We organized a ride for you."));
    assertTrue(firstNotification.getMessage().contains("Ivan"));
    assertNotNull(firstNotification.getRide().getStartingPosition());
  }
}
