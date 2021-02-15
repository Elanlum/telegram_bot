package com.elanlum.ecs.map.service;

import com.elanlum.ecs.map.exceptions.MapException;
import com.elanlum.ecs.ride.model.values.Position;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapService {

  private final GraphHopper hopper;

  /**
   * PathWrapper object that contains info about distance, time, path.
   */
  public PathWrapper getDistance(Position from, Position to) {

    GHRequest req = map(from, to);

    GHResponse rsp = hopper.route(req);

    if (rsp.hasErrors()) {
      throw new MapException(rsp.getErrors().get(0));
    }

    return rsp.getBest();
  }

  private GHRequest map(Position from, Position to) {
    GHRequest req = new GHRequest(from.getLatitude(), from.getLongitude(),
        to.getLatitude(), to.getLongitude())
        .setWeighting("fastest")
        .setVehicle("car")
        .setLocale(Locale.US);

    return req;
  }
}