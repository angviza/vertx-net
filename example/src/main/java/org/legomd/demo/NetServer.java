package org.legomd.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.iot.mqtt.MqttVerticle;
import org.legomd.demo.proto.jt708.JT708Router;
import org.legomd.demo.proto.modbus.ModbusRouter;
import org.legomd.demo.proto.mqtt.MQTTRouter;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/2 20:03
 * @Description:
 */
public class NetServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions opt = new DeploymentOptions();
        opt.setInstances(Runtime.getRuntime().availableProcessors());
//        vertx.deployVerticle(MQTTRouter.class.getName());
//        vertx.deployVerticle(ModbusRouter.class.getName());
//        vertx.deployVerticle(JT708Router.class.getName());
//        vertx.deployVerticle(MqttVerticle.class.getName());
    }


}
