package io.vertx.iot.mqtt;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.reactivex.Completable;
import io.reactivex.Single;
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
    private static final int MQTT_SERVER_PORT = 1883;
    private static final String MQTT_SERVER_HOST = "0.0.0.0";

    @Override
    public Completable rxStart() {
        JsonObject cfg = config();
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
        // 接受远程客户端连接
        endpoint.accept(false);
        grantedQosLevels = new ArrayList<>();
        String clientId = endpoint.clientIdentifier();
//        // 注册下线事件监听
//        EndPointConnentHandler.disconnect(clientId,endpoint);
//        // 心跳检测
//        EndPointConnentHandler.ping(clientId,endpoint);
//        // 注册订阅Borker
//        EndPointSubscribeHandler.subscribeHandler(clientId,endpoint,grantedQosLevels);
//        // 取消注册Borker
//        EndPointSubscribeHandler.unSubscribeHandler(clientId,endpoint,grantedQosLevels);
//        // 监听客户端Topic
//        EndPointMessageHandler.received(endpoint);

        // 显示主要连接信息
        System.out.println("MQTT client [" + clientId + "] request to connect, clean session = " + endpoint.isCleanSession());
        if (endpoint.auth() != null) {
            System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        }

        if (endpoint.will() != null) {
            System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes() == null ? new byte[]{} : endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
        }
        log.info("MQTT client [{}] connected", endpoint.clientIdentifier());


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

//
//        endpoint.publishHandler(message -> {
//
//            log.info("Message [{}] received with topic={}, qos={}, payload={}",
//                    message.messageId(), message.topicName(), message.qosLevel(), message.payload());
//
//            vertx.eventBus().publish("dashboard", String.valueOf(message.payload()));
//
//            if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
//                endpoint.publishAcknowledge(message.messageId());
//            } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
//                endpoint.publishRelease(message.messageId());
//            }
//
//        });


        /**
         * 断开连接时调用
         */
        endpoint.disconnectHandler(v -> {
            System.out.println("Received disconnect from client");
        });

        /**
         * 客户端订阅时调用
         */
        endpoint.subscribeHandler(subscribe -> {
            List<MqttQoS> grantedQosLevels = new ArrayList<>();
            for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                grantedQosLevels.add(s.qualityOfService());
            }
            // 确认订阅请求
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
        });

        /**
         * 客户端取消订阅时调用
         */
        endpoint.unsubscribeHandler(unsubscribe -> {
            for (String t : unsubscribe.topics()) {
                System.out.println("Unsubscription for " + t);
            }
            // 确认订阅请求
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
