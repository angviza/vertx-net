package org.legomd.demo.proto.modbus;

import io.netty.channel.Channel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServerOptions;
import io.vertx.exp.net.NetServer;
import io.vertx.exp.net.impl.NetServerImpl;
import lombok.extern.slf4j.Slf4j;
import org.legomd.net.modbus.codec.*;
import org.legomd.net.modbus.core.ExceptionCode;
import org.legomd.net.modbus.core.requests.ModbusRequest;
import org.legomd.net.modbus.core.responses.ExceptionResponse;
import org.legomd.net.modbus.core.responses.ModbusResponse;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:21
 * @Description:
 * @Url https://www.cnblogs.com/ioufev/articles/10830028.html
 * @Url git@github.com:digitalpetri/modbus.git
 * @Url https://www.modbustools.com/download.html
 */

@Slf4j
public class ModbusRouter extends AbstractVerticle {
    NetServer<ModbusTcpPayload> server;
    private final AtomicReference<ServiceRequestHandler> requestHandler =
            new AtomicReference(new ServiceRequestHandler(){});

    @Override
    public void start() throws Exception {
        NetServerOptions nso = new NetServerOptions().setIdleTimeout(config().getInteger("timeout", 3000))
                .setPort(config().getInteger("port", 9997));// .setSsl(true)

        server = NetServerImpl.create((VertxInternal) vertx, nso,
                Arrays.asList(
                        new LoggingHandler(LogLevel.TRACE),
                        new ModbusTcpCodec(new ModbusResponseEncoder(), new ModbusRequestDecoder())
                )
        );
        server.connectHandler(ctx -> {
            ctx.handler(payload -> {
                ServiceRequestHandler handler = requestHandler.get();
                if (handler == null) return;

                switch (payload.getModbusPdu().getFunctionCode()) {
                    case ReadCoils:
                        handler.onReadCoils(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case ReadDiscreteInputs:
                        handler.onReadDiscreteInputs(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case ReadHoldingRegisters:
                        handler.onReadHoldingRegisters(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case ReadInputRegisters:
                        handler.onReadInputRegisters(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case WriteSingleCoil:
                        handler.onWriteSingleCoil(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case WriteSingleRegister:
                        handler.onWriteSingleRegister(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case WriteMultipleCoils:
                        handler.onWriteMultipleCoils(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case WriteMultipleRegisters:
                        handler.onWriteMultipleRegisters(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case MaskWriteRegister:
                        handler.onMaskWriteRegister(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    case ReadWriteMultipleRegisters:
                        handler.onReadWriteMultipleRegisters(ModbusTcpServiceRequest.of(payload, ctx.channel()));
                        break;

                    default:
                        /* Function code not currently supported */
                        ExceptionResponse response = new ExceptionResponse(
                                payload.getModbusPdu().getFunctionCode(),
                                ExceptionCode.IllegalFunction);

                        ctx.channel().writeAndFlush(new ModbusTcpPayload(payload.getTransactionId(), payload.getUnitId(), response));
                        break;
                }
            });
            // active
            ctx.drainHandler(buf -> {
                System.out.println(ctx.channel());

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
            ctx.exceptionHandler(e -> {
                log.error("{},{},{}", ctx.channel().remoteAddress(), "exceptionHandler", e.getMessage());
            });
            // read write idle time out end
            ctx.endHandler(end -> {
                ctx.close();
                log.debug("{}>{},{}", ctx.channel().remoteAddress(), "endHandler", end);
            });
            ctx.closeHandler(buf -> {

//                unRegister(socket.channel());
                log.debug("{}>{},{}", ctx.channel().remoteAddress(), "closeHandler", buf);
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

    private static class ModbusTcpServiceRequest<Request extends ModbusRequest, Response extends ModbusResponse>
            implements ServiceRequestHandler.ServiceRequest<Request, Response> {

        private final short transactionId;
        private final short unitId;
        private final Request request;
        private final Channel channel;

        private ModbusTcpServiceRequest(short transactionId, short unitId, Request request, Channel channel) {
            this.transactionId = transactionId;
            this.unitId = unitId;
            this.request = request;
            this.channel = channel;
        }

        @Override
        public short getTransactionId() {
            return transactionId;
        }

        @Override
        public short getUnitId() {
            return unitId;
        }

        @Override
        public Request getRequest() {
            return request;
        }

        @Override
        public Channel getChannel() {
            return channel;
        }

        @Override
        public void sendResponse(Response response) {
            channel.writeAndFlush(new ModbusTcpPayload(transactionId, unitId, response));
        }

        @Override
        public void sendException(ExceptionCode exceptionCode) {
            ExceptionResponse response = new ExceptionResponse(request.getFunctionCode(), exceptionCode);

            channel.writeAndFlush(new ModbusTcpPayload(transactionId, unitId, response));
        }

        @SuppressWarnings("unchecked")
        public static <Request extends ModbusRequest, Response extends ModbusResponse>
        ModbusTcpServiceRequest<Request, Response> of(ModbusTcpPayload payload, Channel channel) {

            return new ModbusTcpServiceRequest<>(
                    payload.getTransactionId(),
                    payload.getUnitId(),
                    (Request) payload.getModbusPdu(),
                    channel
            );
        }

    }
}
