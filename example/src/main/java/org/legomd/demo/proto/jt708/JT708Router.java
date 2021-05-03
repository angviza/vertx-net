package org.legomd.demo.proto.jt708;

import org.legomd.demo.proto.jt708.codec.JT708Decoder;
import org.legomd.demo.proto.jt708.codec.JT708Encoder;
import org.legomd.demo.msg.RawPacket;
import io.netty.util.AttributeKey;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.tcp.NetServer;
import io.vertx.core.tcp.impl.NetServerImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:21
 * @Description:
 */
@Slf4j
public class JT708Router extends AbstractVerticle {
    NetServer<RawPacket> server;

    @Override
    public void start() throws Exception {
        JsonObject cfg=config();
        NetServerOptions nso = new NetServerOptions().setIdleTimeout(cfg.getInteger("timeout", 3)).setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setPort(cfg.getInteger("port",9998));// .setSsl(true)

        server = NetServerImpl.create((VertxInternal) vertx, nso,
                JT708Decoder.class,
                JT708Encoder.class
        );
        server.connectHandler(socket -> {
            socket.handler(msg -> {
                socket.channel().attr(AttributeKey.newInstance("ce"));
                System.out.println(msg.data());
//                socket.write(new ClusterMessage(1,""),r->{});
//                if (buf != null)
//                    socket.
            });
            // active
            socket.drainHandler(buf -> {
                System.out.println(socket.channel());
            });
            // exception
            socket.exceptionHandler(e -> {
                log.error("{},{},{}", socket.channel().remoteAddress(), "exceptionHandler", e.getMessage());
            });
            // read write idle time out end
            socket.endHandler(end -> {
                socket.close();
                log.debug("{}>{},{}", socket.channel().remoteAddress(), "endHandler", end);
            });
            socket.closeHandler(buf -> {

//                unRegister(socket.channel());
                log.debug("{}>{},{}", socket.channel().remoteAddress(), "closeHandler", buf);
            });
        });

        server.listen(res -> {
            if (res.succeeded()) {
                log.info("TCP Server is started on port: {}", server.actualPort());
            } else {
                log.info("Failed tobind!");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        server.close();
    }
}
