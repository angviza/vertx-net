package org.legomd.demo;

import org.legomd.demo.proto.modbus.ModbusRouter;
import org.legomd.demo.proto.jt708.JT708Router;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/2 20:03
 * @Description:
 */
@Slf4j
public class TCPServer  {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions opt=new DeploymentOptions();
        opt.setInstances(Runtime.getRuntime().availableProcessors());
        vertx.deployVerticle(ModbusRouter.class.getName());
        vertx.deployVerticle(JT708Router.class.getName());

    }


}
