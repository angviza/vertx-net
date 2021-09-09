package org.legomd.demo.proto.jt708;

import io.netty.util.AttributeKey;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.exp.net.NetServer;
import io.vertx.exp.net.impl.NetServerImpl;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.legomd.demo.msg.RawPacket;
import org.legomd.demo.proto.jt708.codec.JT708Decoder;
import org.legomd.demo.proto.jt708.codec.JT708Encoder;

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
        JsonObject cfg = config();

        NetServerOptions nso = new NetServerOptions().setIdleTimeout(cfg.getInteger("timeout", 3)).setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setPort(cfg.getInteger("port", 9998));// .setSsl(true)

//        io.vertx.rxjava3.core.net.NetServer v = vertx.createNetServer();
        server = NetServerImpl.create((VertxInternal) vertx.getDelegate(), nso,
                JT708Decoder.class,
                JT708Encoder.class
        );
        server
                .exceptionHandler(Throwable::printStackTrace)
//        server = NetServer.newInstance(ser);
                .connectHandler(socket -> {
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
