package com.elanlum.ecs.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.elanlum.ecs.map.exceptions.MapException;
import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.values.Position;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
public class MapServiceUnitTest {

  @Mock
  GraphHopper hopper;
  @InjectMocks
  MapService ghService;

  @Mock
  GHResponse ghResponse;

  Position from = new Position(59.906842f, 30.298719f);
  Position to = new Position(59.888854f, 30.322629f);

  @Test
  public void givenGetDistanceMethodMocked_WhenGetDistanceInvoked_ThenMockValueReturned() {

    PathWrapper stubPathWrapper = new PathWrapper().setTime(537075L);

    Mockito.when(hopper.route(any(GHRequest.class))).thenReturn(ghResponse);
    Mockito.when(ghResponse.getBest()).thenReturn(stubPathWrapper);

    PathWrapper testPathWrapper = ghService.getDistance(from, to);

    assertEquals(testPathWrapper.getTime(), stubPathWrapper.getTime());
  }

  @Test
  public void methodInGhServiceThrowsException() {

    Mockito.when(hopper.route(any(GHRequest.class))).thenThrow(new RuntimeException());

    assertThrows(RuntimeException.class, () -> ghService.getDistance(from, to));
  }

  @Test
  public void testGetDistance_setErrorForGhResponse_throwsException() {

    Mockito.when(hopper.route(any(GHRequest.class))).thenReturn(ghResponse);
    Mockito.when(ghResponse.hasErrors()).thenReturn(true);
    Mockito.when(ghResponse.getErrors()).thenReturn(List.of(new RuntimeException()));

    assertThrows(MapException.class, () -> ghService.getDistance(from, to));
  }
}
