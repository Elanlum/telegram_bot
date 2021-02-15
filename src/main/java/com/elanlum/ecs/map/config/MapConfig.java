package com.elanlum.ecs.map.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class MapConfig {

  @Value("${graphhopper.source}")
  private String graphSource;
  @Value("${graphhopper.workingDirectory}")
  private String graphWorkingDirectory;

  /**
   * GraphHopper bean.
   */
  @SneakyThrows
  @Bean
  public GraphHopper graphHopper() {
    GraphHopper hopper = new GraphHopperOSM().forServer();
    try (InputStream resource = new ClassPathResource(graphSource).getInputStream()) {
      File file = new File("map.osm.pbf");
      if (!file.exists()) {
        Files.copy(resource, file.toPath());
      }
      hopper.setDataReaderFile(file.getPath());
    }

    hopper.setGraphHopperLocation(graphWorkingDirectory);
    hopper.setEncodingManager(new EncodingManager("car"));
    hopper.importOrLoad();
    return hopper;
  }
}
