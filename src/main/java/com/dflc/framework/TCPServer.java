package com.dflc.framework;

import com.dflc.framework.codec.HJ212Decoder;
import com.dflc.framework.net.NetServerImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/2 20:03
 * @Description:
 */
@Slf4j
public class TCPServer extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions opt=new DeploymentOptions();
        opt.setInstances(Runtime.getRuntime().availableProcessors());
        vertx.deployVerticle(TCPServer.class.getName());

    }

    NetServerImpl<ClusterMessage> server;

    @Override
    public void start() throws Exception {
        NetServerOptions nso = new NetServerOptions().setIdleTimeout(config().getInteger("timeout", 3000))
                .setPort(config().getInteger("port",9999));// .setSsl(true)

        server = new NetServerImpl<ClusterMessage>((VertxInternal) vertx, nso, HJ212Decoder.class, HJ212Decoder.class);
        server.connectHandler(socket -> {
            socket.handler(msg -> {
                System.out.println(msg.data());
//                socket.write(new ClusterMessage(1,""),r->{});
//                if (buf != null)
//                    socket.
            });
            // active
            socket.drainHandler(buf -> {
                System.out.println(socket.channel());
//                String ip = socket.channel().remoteAddress().toString().split("[:]")[0];
//                ClusterMapService.get(GameCostHelper.C_M_K_BLACKIPLIST, ip, ipv -> {
//                    if (ipv != null) {
//                        log.info("active channel,faild ip[{}] in blacklist", ip);
//                        socket.end();
//                    } else
//                        log.debug("active channel,success {}", socket.channel().remoteAddress());
//                });
            });
            // exception
            socket.exceptionHandler(e -> {
//                log.error("{},{},{}", socket.channel().remoteAddress(), "exceptionHandler", e.getMessage());
            });
            // read write idle time out end
            // socket.endHandler(end -> {
            // socket.close();
            // log.debug("{}>{},{}", socket.channel().remoteAddress(), "endHandler", end);
            // });
            socket.closeHandler(buf -> {

//                unRegister(socket.channel());
//                log.debug("{}>{},{}", socket.channel().remoteAddress(), "closeHandler", buf);
            });
        });

        server.listen(res -> {
            if (res.succeeded()) {
                log.info("TCP Server is started on port: {}", server.actualPort());
            } else {
                log.info("Failed tobind!");
            }
        });
        super.start();
    }
}
