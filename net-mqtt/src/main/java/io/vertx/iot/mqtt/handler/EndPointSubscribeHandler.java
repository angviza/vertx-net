package io.vertx.iot.mqtt.handler;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.iot.mqtt.container.MqttSessionContainer;
import io.vertx.rxjava3.mqtt.MqttEndpoint;
import io.vertx.rxjava3.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EndPointSubscribeHandler {

    public static void subscribeHandler(String clientId, MqttEndpoint endpoint, List<MqttQoS> grantedQosLevels) {
        endpoint.subscribeHandler(subscribe -> {
            for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                log.info("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                grantedQosLevels.add(s.qualityOfService());
                // 添加到缓存
                MqttSessionContainer.getAndPut(clientId, s);
            }
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
        });
    }

    public static void unSubscribeHandler(String clientId, MqttEndpoint endpoint, List<MqttQoS> grantedQosLevels) {

        endpoint.unsubscribeHandler(unsubscribe -> {

            for (String t : unsubscribe.topics()) {
                log.info("Unsubscription for " + t);
            }
            MqttSessionContainer.mqttSessionContainer.remove(clientId);
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });
    }

}