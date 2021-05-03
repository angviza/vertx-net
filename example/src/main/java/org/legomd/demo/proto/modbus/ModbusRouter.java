package org.legomd.demo.proto.modbus;

import org.legomd.demo.proto.hj212.codec.HJ212Decoder;
import org.legomd.demo.proto.hj212.codec.HJ212Encoder;
import org.legomd.demo.msg.RawPacket;
import io.netty.util.AttributeKey;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.tcp.NetServer;
import io.vertx.core.tcp.impl.NetServerImpl;
import lombok.extern.slf4j.Slf4j;
import org.legomd.net.modbus.codec.ModbusRequestEncoder;
import org.legomd.net.modbus.codec.ModbusResponseDecoder;
import org.legomd.net.modbus.codec.ModbusTcpCodec;
import org.legomd.net.modbus.codec.ModbusTcpPayload;

import java.util.Arrays;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:21
 * @Description:
 */
@Slf4j
public class ModbusRouter extends AbstractVerticle {
    NetServer<ModbusTcpPayload> server;

    @Override
    public void start() throws Exception {
        NetServerOptions nso = new NetServerOptions().setIdleTimeout(config().getInteger("timeout", 3000))
                .setPort(config().getInteger("port",9999));// .setSsl(true)

        server = NetServerImpl.create((VertxInternal) vertx, nso,
                 Arrays.asList(new ModbusTcpCodec(new ModbusRequestEncoder(), new ModbusResponseDecoder()))
        );
        server.connectHandler(socket -> {
            socket.handler(msg -> {
                socket.channel().attr(AttributeKey.newInstance("ce"));
                System.out.println(msg.getModbusPdu().getFunctionCode());
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
