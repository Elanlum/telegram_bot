package com.elanlum.ecs.notification;

import com.elanlum.ecs.notification.values.Notification;

import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Slf4j
@Component
public class NotificationMessageQueue {

  private final FluxSinkHolder fluxSinkHolder = new FluxSinkHolder();

  @Getter
  private final Flux<Notification> notificationStream = Flux.create(fluxSinkHolder)
      .publish()
      .autoConnect(1);

  public void sendNotification(Notification notification) {
    this.fluxSinkHolder.sendNotification(notification);
  }

  public void shutdown() {
    this.fluxSinkHolder.shutdown();
    log.debug("The fluxSinkHolder was shutdown");
  }

  private class FluxSinkHolder implements Consumer<FluxSink<Notification>> {

    private FluxSink<Notification> fluxSink;

    @Override
    public void accept(FluxSink<Notification> notificationFluxSink) {
      this.fluxSink = notificationFluxSink;
    }

    void shutdown() {
      fluxSink.complete();
    }

    void sendNotification(Notification notification) {
      Objects.requireNonNull(notificationStream);
      fluxSink.next(notification);
    }
  }
}
