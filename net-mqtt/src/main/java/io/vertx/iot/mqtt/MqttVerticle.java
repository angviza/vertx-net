package io.vertx.iot.mqtt;


import io.netty.handler.codec.mqtt.MqttQoS;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.iot.mqtt.container.MqttSessionContainer;
import io.vertx.iot.mqtt.domain.MqttSession;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.mqtt.MqttEndpoint;
import io.vertx.rxjava3.mqtt.MqttServer;
import io.vertx.rxjava3.mqtt.MqttTopicSubscription;
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
                        .listen(cfg.getInteger("port", MQTT_SERVER_PORT), cfg.getString("host", MQTT_SERVER_HOST));
            } catch (Throwable e) {
                emitter.onError(e);
            }
        }).ignoreElement();

    }

    public void endpointHandler(MqttEndpoint endpoint) {
        MqttSessionContainer.mqttSessionContainer.put(endpoint.clientIdentifier(), new MqttSession().endpoint(endpoint));
        //
        // 接受远程客户端连接
        endpoint.accept(false).publishAutoAck(true);
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
         * 断开连接时调用
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
            mqttServer.close();
        }
        return super.rxStop();
    }
}
