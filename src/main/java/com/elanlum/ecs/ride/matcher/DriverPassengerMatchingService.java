package com.elanlum.ecs.ride.matcher;

import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainerFactory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DriverPassengerMatchingService {

  private final ScoringContainerFactory containerFactory;
  private final PassengerRideRequestService passengerRideRequestService;

  /**
   * Method that pairs driver's and passenger's ride requests.
   *
   * @param driverRideRequestMono - {@link reactor.core.publisher.Mono} of {@link
   *     DriverRideRequest} which is used to get driver position
   * @param passengerRideRequestFlux - {@link reactor.core.publisher.Flux} of {@link
   *     PassengerRideRequest} with all the passenger ride requests
   */
  protected Flux<ScoringContainer> getDriverPassengerPairs(
      Mono<DriverRideRequest> driverRideRequestMono,
      Flux<PassengerRideRequest> passengerRideRequestFlux) {

    return driverRideRequestMono
        .flatMapMany(driverRequest -> passengerRideRequestFlux
            .map(passengerRequest -> containerFactory.create(driverRequest, passengerRequest))
        );
  }

  /**
   * Method for getting passengers for driver.
   *
   * @param driverRideRequestMonoIn request from Driver.
   * @return Flux<ScoringContainer></ScoringContainer>
   */
  Flux<ScoringContainer> getNearPassengers(Mono<DriverRideRequest> driverRideRequestMonoIn) {

    Flux<PassengerRideRequest> passengerRideRequestFluxIn = driverRideRequestMonoIn.flatMapMany(
        driverRideRequest -> passengerRideRequestService
            .getAvailablePassengerRequestsInTime(driverRideRequest.getRideDate().getStart(),
                driverRideRequest.getRideDate().getEnd(), driverRideRequest.getUserId()));

    Flux<ScoringContainer> containerFlux = getDriverPassengerPairs(driverRideRequestMonoIn,
        passengerRideRequestFluxIn);

    Mono<List<ScoringContainer>> collected = containerFlux
        .collect(
            Collector.of(ArrayList::new, //supplier
                getListScoringContainerBiConsumer(),  // accumulator
                getListBinaryOperator()  //merging method
            ));

    return collected.flatMapMany(Flux::fromIterable);
  }

  private BiConsumer<List<ScoringContainer>, ScoringContainer> getListScoringContainerBiConsumer() {
    return (listAcc, scoringContainer) -> {
      if (listAcc.isEmpty()) {
        listAcc.add(scoringContainer);
      } else {
        boolean inserted = false;
        int n = 3;
        for (int i = 0; i < listAcc.size(); i++) {
          if (scoringContainer.getScore() > listAcc.get(i).getScore()) {
            listAcc.add(i, scoringContainer);
            inserted = true;
            if (listAcc.size() > n) {
              listAcc.remove(n);
            }
            break;
          }
        }
        if (!inserted & listAcc.size() < n) {
          listAcc.add(scoringContainer);
        }
      }
    };
  }

  BinaryOperator<List<ScoringContainer>> getListBinaryOperator() {
    return (list1, list2) -> {
      List<ScoringContainer> scoringContainers = new ArrayList<>();

      int i1 = 0;
      int i2 = 0;
      for (int i = 0; i < list1.size() + list2.size(); i++) {
        if (i2 >= list2.size() || (i1 < list1.size()
            && list1.get(i1).getScore() >= list2.get(i2).getScore())) {
          scoringContainers.add(list1.get(i1));
          i1++;
        } else {
          scoringContainers.add(list2.get(i2));
          i2++;
        }
      }
      return scoringContainers;
    };
  }
}
