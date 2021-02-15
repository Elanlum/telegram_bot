package com.elanlum.ecs.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.values.Position;
import com.graphhopper.PathWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag(TestCategory.INTEGRATION)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestsConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MapServiceIntegrationTest {

  @Autowired
  MapService hopper;

  Position from = new Position(59.906842f, 30.298719f);
  Position to = new Position(59.888854f, 30.322629f);

  PathWrapper answer;

  @BeforeEach
  void init() {
    answer = hopper.getDistance(from, to);
  }

  @Test
  void hasErrors() {
    assertFalse(answer.hasErrors());
  }

  @Test
  void getTime() {
    assertEquals(537050L, answer.getTime());
  }

  @Test
  void getDistance() {
    assertTrue(answer.getDistance() > 0);
  }

  @Test
  void otherFieldNotNull() {
    assertNotNull(answer.getPoints());
    assertNotNull(answer.getWaypoints());
    assertNotNull(answer.getInstructions());
    assertNotNull(answer.getDescription());
  }
}
