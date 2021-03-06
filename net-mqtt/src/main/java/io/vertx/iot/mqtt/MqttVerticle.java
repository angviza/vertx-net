package io.vertx.iot.mqtt;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.iot.mqtt.container.MqttSessionContainer;
import io.vertx.iot.mqtt.domain.MqttSession;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.mqtt.MqttEndpoint;
import io.vertx.reactivex.mqtt.MqttServer;
import io.vertx.reactivex.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MqttVerticle extends AbstractVerticle {
    MqttServer mqttServer;
    List<MqttQoS> grantedQosLevels;
    VertxInternal vertxInt;
    private static final int MQTT_SERVER_PORT = 1883;
    private static final String MQTT_SERVER_HOST = "0.0.0.0";

    @Override
    public Completable rxStart() {
        JsonObject cfg = config();
        vertxInt = (VertxInternal) vertx.getDelegate();
        return Single.create(emitter -> {
            try {
                MqttServerOptions options = new MqttServerOptions();
                options.setTcpFastOpen(MqttServerOptions.DEFAULT_TCP_FAST_OPEN);
                MqttServer mqttServer = MqttServer.create(vertx, options);
                mqttServer
                        .endpointHandler(this::endpointHandler)
                        .listen(cfg.getInteger("port", MQTT_SERVER_PORT), cfg.getString("host", MQTT_SERVER_HOST), done -> {

                            if (done.succeeded()) {
                                log.info("MQTT server started on port {}", done.result().actualPort());
                            } else {
                                log.error("MQTT server not started", done.cause());
                            }
                        });
            } catch (Throwable e) {
                emitter.onError(e);
            }
        }).ignoreElement();

    }

    public void endpointHandler(MqttEndpoint endpoint) {
        MqttSessionContainer.mqttSessionContainer.put(endpoint.clientIdentifier(), new MqttSession().endpoint(endpoint));
        //
        // ???????????????????????????
        endpoint.accept(false).publishAutoAck(true);
        grantedQosLevels = new ArrayList<>();
        String clientId = endpoint.clientIdentifier();
//        // ????????????????????????
//        EndPointConnentHandler.disconnect(clientId,endpoint);
//        // ????????????
//        EndPointConnentHandler.ping(clientId,endpoint);
//        // ????????????Borker
//        EndPointSubscribeHandler.subscribeHandler(clientId,endpoint,grantedQosLevels);
//        // ????????????Borker
//        EndPointSubscribeHandler.unSubscribeHandler(clientId,endpoint,grantedQosLevels);
//        // ???????????????Topic
//        EndPointMessageHandler.received(endpoint);

        // ????????????????????????
        System.out.println("MQTT client [" + clientId + "] request to connect, clean session = " + endpoint.isCleanSession());
        if (endpoint.auth() != null) {
            System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        }

        if (endpoint.will() != null) {
            System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes() == null ? new byte[]{} : endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
        }
        log.info("MQTT client [{}] connected", endpoint.clientIdentifier());

        vertx.setPeriodic(1000, r -> {
            endpoint.publish("/test",
                    Buffer.buffer("test topic publish : " + r),
                    MqttQoS.EXACTLY_ONCE,
                    false,
                    false);
        });
        endpoint.publishHandler(message -> {

            log.info("Message [{}] received with topic={}, qos={}, payload={}",
                    message.messageId(), message.topicName(), message.qosLevel(), message.payload());
            endpoint.publish(message.topicName(),
                    Buffer.buffer(message.payload().toString(Charset.defaultCharset())),
                    MqttQoS.EXACTLY_ONCE,
                    false,
                    false);
            if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                endpoint.publishAcknowledge(message.messageId());
            } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                endpoint.publishReceived(message.messageId());
            }
        }).publishReleaseHandler(messageId -> {
            endpoint.publishComplete(messageId);
        });

        endpoint.pingHandler(v -> {
            //endpoint.pong();
            System.out.println("Ping received from client");
        });

        // specifing handlers for handling QoS 1 and 2
        endpoint.publishAcknowledgeHandler(messageId -> {
            System.out.println("Received ack for message = " + messageId);
        }).publishReceivedHandler(messageId -> {
            endpoint.publishRelease(messageId);
        }).publishCompletionHandler(messageId -> {
            System.out.println("Received ack for message = " + messageId);
        });


        /**
         * ?????????????????????
         */
        endpoint.disconnectHandler(v -> {
            System.out.println("Received disconnect from client");
        });

        /**
         * +--------------------------------------------------------------+
         * +==============================================================+
         * +==============================================================+
         * +--------------------------------------------------------------+
         * /
         /**
         * ????????????????????????
         */
        endpoint.subscribeHandler(subscribe -> {
            List<MqttQoS> grantedQosLevels = new ArrayList<>();
            for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                grantedQosLevels.add(s.qualityOfService());
            }
            // ??????????????????
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
        });

        /**
         * ??????????????????????????????
         */
        endpoint.unsubscribeHandler(unsubscribe -> {
            for (String t : unsubscribe.topics()) {
                System.out.println("Unsubscription for " + t);
            }
            // ??????????????????
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });

    }

    @Override
    public Completable rxStop() {
        if (mqttServer != null) {
            mqttServer.close(res -> {
                if (res.succeeded()) {
                    log.info("Server is nowclosed");
                } else {
                    log.info("closefailed");
                }
            });
        }
        return super.rxStop();
    }
}
