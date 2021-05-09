package org.legomd.demo.proto.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:21
 * @Description:
 * @Url http://www.jensd.de/apps/mqttfx/1.7.1/
 */

@Slf4j
public class MQTTRouter extends AbstractVerticle {
    MqttServer mqttServer;

    @Override
    public void start() throws Exception {
        MqttServerOptions options = new MqttServerOptions()
                .setPort(1883);
//                .setKeyCertOptions(new PemKeyCertOptions()
//                        .setKeyPath("./src/test/resources/tls/server-key.pem")
//                        .setCertPath("./src/test/resources/tls/server-cert.pem"))
//                .setSsl(true);

        mqttServer = MqttServer.create(vertx, options);
        mqttServer.endpointHandler(endpoint -> {

            // shows main connect info
            System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

            if (endpoint.auth() != null) {
                System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
            }
            if (endpoint.will() != null) {
                try {
                    System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
                            " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

            // accept connection from the remote client
            endpoint.accept(true);
            endpoint.subscribeHandler(subscribe -> {

                List<MqttQoS> grantedQosLevels = new ArrayList<>();
                for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
                    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                    grantedQosLevels.add(s.qualityOfService());
                }
                // ack the subscriptions request
                endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);

            });
            endpoint.disconnectHandler(v -> {

                System.out.println("Received disconnect from client");
            });
        });

        mqttServer.listen(ar -> {

            if (ar.succeeded()) {

                System.out.println("MQTT server is listening on port " + ar.result().actualPort());
            } else {

                System.out.println("Error on starting the server");
                ar.cause().printStackTrace();
            }
        });

    }
}
