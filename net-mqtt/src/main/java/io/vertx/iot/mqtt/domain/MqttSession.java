package io.vertx.iot.mqtt.domain;

import io.vertx.reactivex.mqtt.MqttEndpoint;
import io.vertx.reactivex.mqtt.MqttTopicSubscription;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(fluent = true)
public class MqttSession {
    private MqttEndpoint endpoint;
    private Set<MqttTopicSubscription> mqttTopicSubscriptions = new HashSet<>();

}
