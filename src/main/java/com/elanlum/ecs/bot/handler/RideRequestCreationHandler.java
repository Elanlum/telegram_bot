package com.elanlum.ecs.bot.handler;

import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.context.exceptions.WrongCommandException;
import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.RideRequestFieldParser;
import com.elanlum.ecs.bot.context.model.UserContext;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.mapper.RideRequestMapper;
import com.elanlum.ecs.ride.model.common.AbstractRideRequest;
import com.elanlum.ecs.user.service.UserService;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideRequestCreationHandler {

  private final UserService userService;
  private final DriverRideRequestService driverRideRequestService;
  private final PassengerRideRequestService passengerRideRequestService;
  private final RideRequestFieldParser parser;
  private final RideRequestMapper mapper;

  /**
   * Method represents each separate step of creating ride request for user. User sends a
   * message(command) and this method handles it, updating internal context of creating certain ride
   * request.
   *
   * @param userContext - context of creating ride request, containing current parameters for a
   *     ride, available commands, user's role, etc.
   * @param telegramUpdate - object, containing anything that users sends through telegram. In
   *     our case it is a command, a map or parameters.
   * @return updated {@link UserContext}
   */
  public Mono<UserContext> handle(UserContext userContext, Update telegramUpdate) {
    String command;
    if (telegramUpdate.getMessage().getLocation() != null) {
      command = (telegramUpdate.getMessage().getLocation().getLatitude()) + " "
          + telegramUpdate.getMessage().getLocation().getLongitude();
    } else {
      command = telegramUpdate.getMessage().getText().trim();
    }

    userContext.setAvailableCommands(fillAvailableCommands(userContext.getFieldsToFill(),
        userContext.getAvailableCommands()));

    if (command.equals(ContextType.CREATE_DRIVER_REQUEST.getCommandName()) || command
        .equals(ContextType.CREATE_PASSENGER_REQUEST.getCommandName())) {
      return Mono.just(userContext);
    }

    if (userContext.getState() == 0) {
      return processCommand(userContext, command);
    } else {
      return processParameters(userContext, command);
    }
  }

  private Set<String> fillCommand(FieldName fieldName,
      Map<FieldName, String> fieldsToFill, Set<String> availableCommands) {
    if (!fieldsToFill.containsKey(fieldName)) {
      availableCommands.add(fieldName.getCommand());
    } else {
      availableCommands.remove(fieldName.getCommand());
    }
    return availableCommands;
  }

  private Mono<UserContext> processCommand(UserContext userContext, String command) {
    if (command.equals("/cancel")) {
      userContext.setCanceled(true);
      return Mono.just(userContext);
    }
    Set<String> availableCommands = userContext.getAvailableCommands();
    if (command.equals("/create")) {
      if (availableCommands.contains("/create")) {
        return saveRideRequest(userContext)
            .doOnNext(saveRideRequest -> userContext.setCompleted(true))
            .map(saveRideRequest -> userContext);
      } else {
        Mono<UserContext> error = Mono
            .error(new WrongCommandException("Not all the parameters are set"));
        return error;
      }
    }
    try {
      Arrays.stream(FieldName.values())
          .filter(fieldName -> (fieldName != FieldName.ROLE) && (fieldName
              != FieldName.TELEGRAM_ID))
          .forEach(fieldName -> {
            if (fieldName.getCommand().equals(command)) {
              if (availableCommands.contains(command)) {
                userContext.setState(fieldName.getState());
              } else if (command.equals(FieldName.RIDE_TIME.getCommand()) && !userContext
                  .getFieldsToFill().containsKey(FieldName.RIDE_DATE)) {
                throw new WrongCommandException("You can't set ride time before date");
              } else {
                throw new WrongCommandException(
                    "Parameter <" + fieldName.name() + "> is already set");
              }
            }
          });
    } catch (WrongCommandException ex) {
      log.debug("Exception caught", ex);
      return Mono.error(ex);
    }
    if (userContext.getState() != 0) {
      return Mono.just(userContext);
    } else {
      return Mono.error(new WrongCommandException("Unknown command: " + command));
    }
  }

  private Mono<? extends AbstractRideRequest> saveRideRequest(UserContext userContext) {
    Map<FieldName, String> filledFields = userContext.getFieldsToFill();
    switch (filledFields.get(FieldName.ROLE)) {
      case "driver":
        return userService.findByTelegramId(filledFields.get(FieldName.TELEGRAM_ID))
            .map(user -> {
              filledFields.put(FieldName.TELEGRAM_ID, user.getId());
              return filledFields;
            })
            .flatMap(fields -> driverRideRequestService.save(mapper.mapDriverRideRequest(fields)));
      case "passenger":
        return userService.findByTelegramId(filledFields.get(FieldName.TELEGRAM_ID))
            .map(user -> {
              filledFields.put(FieldName.TELEGRAM_ID, user.getId());
              return filledFields;
            })
            .flatMap(
                fields -> passengerRideRequestService.save(mapper.mapPassengerRideRequest(fields)));
      default:
        return Mono.error(new UnsupportedOperationException());
    }
  }

  private Set<String> fillAvailableCommands(Map<FieldName, String> fieldsToFill,
      Set<String> availableCommands) {
    Arrays.stream(FieldName.values())
        .filter(fieldName -> (fieldName != FieldName.ROLE) && (fieldName
            != FieldName.TELEGRAM_ID))
        .forEach(fieldName -> fillCommand(fieldName, fieldsToFill, availableCommands));
    if (!fieldsToFill.containsKey(FieldName.RIDE_DATE)) {
      availableCommands.remove(FieldName.RIDE_TIME.getCommand());
    }
    return availableCommands;
  }

  private Mono<UserContext> processParameters(UserContext userContext, String command) {
    return Mono.fromSupplier(() -> {
      if (command.equals("/cancel")) {
        userContext.setCanceled(true);
        return userContext;
      }
      Map<FieldName, String> fieldsToFill = userContext.getFieldsToFill();
      Set<String> availableCommands = userContext.getAvailableCommands();
      switch (userContext.getState()) {
        case 1:
          parser.validateCoordinate(command);
          fieldsToFill.put(FieldName.DEPARTURE_POSITION, command);
          availableCommands.remove(FieldName.DEPARTURE_POSITION.getCommand());
          break;
        case 2:
          parser.validateCoordinate(command);
          fieldsToFill.put(FieldName.DESTINATION_POSITION, command);
          availableCommands.remove(FieldName.DESTINATION_POSITION.getCommand());
          break;
        case 3:
          parser.validateDate(command);
          fieldsToFill.put(FieldName.RIDE_DATE, command);
          availableCommands.remove(FieldName.RIDE_DATE.getCommand());
          availableCommands.add(FieldName.RIDE_TIME.getCommand());
          break;
        case 4:
          parser.validateTime(command, fieldsToFill.get(FieldName.RIDE_DATE));
          fieldsToFill.put(FieldName.RIDE_TIME, command);
          availableCommands.remove(FieldName.RIDE_TIME.getCommand());
          break;
        case 5:
          try {
            Integer.parseInt(command);
            fieldsToFill.put(FieldName.EXPECTATION_PERIOD, command);
            availableCommands.remove(FieldName.EXPECTATION_PERIOD.getCommand());
          } catch (NumberFormatException ex) {
            log.debug("Exception caught", ex);
            throw new UnparsableInputException("Incorrect parameters: " + command);
          }
          break;
        default:
      }
      if (availableCommands.size() == 1) {
        availableCommands.add("/create");
      }
      userContext.setAvailableCommands(availableCommands);
      userContext.setState(0);
      return userContext;
    });
  }
}
