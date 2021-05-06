package io.vertx.iot.mqtt.handler;

import io.vertx.iot.mqtt.container.MqttSessionContainer;
import io.vertx.reactivex.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndPointConnentHandler {

    public static void disconnect(String clientId, MqttEndpoint endpoint) {
        endpoint.disconnectHandler( event -> {
            MqttSessionContainer.mqttSessionContainer.remove(clientId);
            log.info(clientId + " has offline.");
        });
    }

    public static void ping(String clientId,MqttEndpoint endpoint) {
        // Be notified by client keep alive
        endpoint.pingHandler(v -> {
            System.out.println("Ping received from client,Id: " + clientId);
        });
    }
}
