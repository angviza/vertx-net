package io.vertx.iot.mqtt.container;


import io.vertx.iot.mqtt.domain.MqttSession;
import io.vertx.rxjava3.mqtt.MqttEndpoint;
import io.vertx.rxjava3.mqtt.MqttTopicSubscription;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MqttSessionContainer extends ConcurrentHashMap<String, MqttSession> {
    public static MqttSessionContainer mqttSessionContainer = null;
    static {
        mqttSessionContainer = new MqttSessionContainer();
    }
    public static MqttSessionContainer getAndPut(String clientId, MqttTopicSubscription mqttTopicSubscription) {
        MqttSession mqttSession;
        if (!MqttSessionContainer.mqttSessionContainer.containsKey(clientId)) {
            mqttSession = new MqttSession();
        } else {
            mqttSession = mqttSessionContainer.get(clientId);
            Set<MqttTopicSubscription> mqttTopicSubscriptions = mqttSession.mqttTopicSubscriptions();
            mqttTopicSubscriptions.add(mqttTopicSubscription);
        }
        return mqttSessionContainer;
    }

    public static MqttEndpoint getByClientId(String clientId) {
        MqttSession mqttSession = mqttSessionContainer.get(clientId);
        return mqttSession.endpoint();
    }
}
