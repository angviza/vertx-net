package io.vertx.exp.net.impl;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCounted;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.net.impl.SslHandshakeCompletionHandler;
import io.vertx.core.net.impl.VertxHandler;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.exp.net.NetSocket;
import io.vertx.exp.net.NetSocketInternal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 *
 * This class is optimised for performance when used on the same event loop that is was passed to the handler with.
 * However it can be used safely from other threads.
 *
 * The internal state is protected using the synchronized keyword. If always used on the same event loop, then
 * we benefit from biased locking which makes the overhead of synchronized near zero.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class NetSocketImpl<T> extends ConnectionBase implements NetSocketInternal<T> {

    private static final Handler<Object> DEFAULT_MSG_HANDLER = new InvalidMessageHandler();

    private static final Logger log = LoggerFactory.getLogger(NetSocketImpl.class);

    private final String writeHandlerID;
    private final SSLHelper helper;
    private final SocketAddress remoteAddress;
    private final TCPMetrics metrics;
    private Handler<Void> endHandler;
    private Handler<Void> drainHandler;
    private InboundBuffer<Object> pending;
    private MessageConsumer<T> registration;
    private Handler<Object> messageHandler;

    public NetSocketImpl(VertxInternal vertx, ChannelHandlerContext channel, ContextInternal context,
                         SSLHelper helper, TCPMetrics metrics) {
        this(vertx, channel, null, context, helper, metrics);
    }

    public NetSocketImpl(VertxInternal vertx, ChannelHandlerContext channel, SocketAddress remoteAddress, ContextInternal context,
                         SSLHelper helper, TCPMetrics metrics) {
        super(vertx, channel, context);
        this.helper = helper;
        this.writeHandlerID = "__vertx.net." + UUID.randomUUID().toString();
        this.remoteAddress = remoteAddress;
        this.metrics = metrics;
        this.messageHandler = DEFAULT_MSG_HANDLER;
        pending = new InboundBuffer<>(context);
        pending.drainHandler(v -> doResume());
        pending.exceptionHandler(context::reportException);
        pending.handler(obj -> {
            if (obj == InboundBuffer.END_SENTINEL) {
                Handler<Void> handler = endHandler();
                if (handler != null) {
                    handler.handle(null);
                }
            } else {
                Handler<Object> handler = messageHandler();
                if (handler != null) {
                    handler.handle(obj);
                }
            }
        });
    }

    synchronized void registerEventBusHandler() {
        Handler<Message<T>> writeHandler = msg -> write(msg.body());
        registration = vertx.eventBus().<T>localConsumer(writeHandlerID).handler(writeHandler);
    }

    @Override
    public TCPMetrics metrics() {
        return metrics;
    }

    @Override
    public String writeHandlerID() {
        return writeHandlerID;
    }

    @Override
    public synchronized NetSocketInternal writeMessage(Object message) {
        writeToChannel(message);
        return this;
    }

    @Override
    public NetSocketInternal writeMessage(Object message, Handler<AsyncResult<Void>> handler) {
        writeToChannel(message, toPromise(handler));
        return this;
    }

    @Override
    public NetSocket write(T data) {
//        write(data.getByteBuf(), null);
        super.writeToChannel(data);
        return this;
    }

//    @Override
//    public NetSocket write(String str) {
//        return write(str, (Handler<AsyncResult<Void>>) null);
//    }

//    @Override
//    public NetSocket write(String str, Handler<AsyncResult<Void>> handler) {
//        write(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8), handler);
//        return this;
//    }

//    @Override
//    public NetSocket write(String str, String enc) {
//        return write(str, enc, null);
//    }

//    @Override
//    public NetSocket write(String str, String enc, Handler<AsyncResult<Void>> handler) {
//        if (enc == null) {
//            write(str);
//        } else {
//            write(Unpooled.copiedBuffer(str, Charset.forName(enc)), handler);
//        }
//        return this;
//    }

    @Override
    public NetSocket write(T message, Handler<AsyncResult<Void>> handler) {
        super.writeToChannel(message);
//        write(message, handler);
        return this;
    }

    private void write(ByteBuf buff, Handler<AsyncResult<Void>> handler) {
        reportBytesWritten(buff.readableBytes());
        writeMessage(buff, handler);
    }

    @Override
    public synchronized NetSocket handler(Handler<T> dataHandler) {
        if (dataHandler != null) {
            messageHandler(new DataMessageHandler(channelHandlerContext().alloc(), dataHandler));
        } else {
            messageHandler(null);
        }
        return this;
    }

    private synchronized Handler<Object> messageHandler() {
        return messageHandler;
    }

    @Override
    public synchronized NetSocketInternal messageHandler(Handler<Object> handler) {
        messageHandler = handler;
        return this;
    }

    @Override
    public synchronized NetSocket pause() {
        pending.pause();
        return this;
    }

    @Override
    public NetSocket fetch(long amount) {
        pending.fetch(amount);
        return this;
    }

    @Override
    public synchronized NetSocket resume() {
        return fetch(Long.MAX_VALUE);
    }

    @Override
    public NetSocket setWriteQueueMaxSize(int maxSize) {
        doSetWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return isNotWritable();
    }

    private synchronized Handler<Void> endHandler() {
        return endHandler;
    }

    @Override
    public synchronized NetSocket endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    @Override
    public synchronized NetSocket drainHandler(Handler<Void> drainHandler) {
        this.drainHandler = drainHandler;
        vertx.runOnContext(v -> callDrainHandler()); //If the channel is already drained, we want to call it immediately
        return this;
    }

    @Override
    public NetSocket sendFile(String filename, long offset, long length) {
        return sendFile(filename, offset, length, null);
    }

    @Override
    public NetSocket sendFile(String filename, long offset, long length, final Handler<AsyncResult<Void>> resultHandler) {
        File f = vertx.resolveFile(filename);
        if (f.isDirectory()) {
            throw new IllegalArgumentException("filename must point to a file and not to a directory");
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "r");
            ChannelFuture future = super.sendFile(raf, Math.min(offset, f.length()), Math.min(length, f.length() - offset));
            if (resultHandler != null) {
                future.addListener(fut -> {
                    final AsyncResult<Void> res;
                    if (future.isSuccess()) {
                        res = Future.succeededFuture();
                    } else {
                        res = Future.failedFuture(future.cause());
                    }
                    vertx.runOnContext(v -> resultHandler.handle(res));
                });
            }
        } catch (IOException e) {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException ignore) {
            }
            if (resultHandler != null) {
                vertx.runOnContext(v -> resultHandler.handle(Future.failedFuture(e)));
            } else {
                log.error("Failed to send file", e);
            }
        }
        return this;
    }

    public NetSocketImpl exceptionHandler(Handler<Throwable> handler) {
        return (NetSocketImpl) super.exceptionHandler(handler);
    }

    @Override
    public NetSocketImpl closeHandler(Handler<Void> handler) {
        return (NetSocketImpl) super.closeHandler(handler);
    }

    @Override
    public NetSocket upgradeToSsl(Handler<Void> handler) {
        return upgradeToSsl(null, handler);
    }

    @Override
    public NetSocket upgradeToSsl(String serverName, Handler<Void> handler) {
        ChannelOutboundHandler sslHandler = (ChannelOutboundHandler) chctx.pipeline().get("ssl");
        if (sslHandler == null) {
            chctx.pipeline().addFirst("handshaker", new SslHandshakeCompletionHandler(ar -> {
                if (ar.succeeded()) {
                    handler.handle(null);
                } else {
                    chctx.channel().closeFuture();
                    handleException(ar.cause());
                }
            }));
            if (remoteAddress != null) {
                sslHandler = new SslHandler(helper.createEngine(vertx, remoteAddress, serverName));
                ((SslHandler) sslHandler).setHandshakeTimeout(helper.getSslHandshakeTimeout(), helper.getSslHandshakeTimeoutUnit());
            } else {
                if (helper.isSNI()) {
                    sslHandler = new SniHandler(helper.serverNameMapper(vertx));
                } else {
                    sslHandler = new SslHandler(helper.createEngine(vertx));
                    ((SslHandler) sslHandler).setHandshakeTimeout(helper.getSslHandshakeTimeout(), helper.getSslHandshakeTimeoutUnit());
                }
            }
            chctx.pipeline().addFirst("ssl", sslHandler);
        }
        return this;
    }

    @Override
    protected synchronized void handleInterestedOpsChanged() {
        checkContext();
        callDrainHandler();
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        close(handler);
    }

    @Override
    public void end() {
        close();
    }

    @Override
    protected void handleClosed() {
        MessageConsumer consumer;
        synchronized (this) {
            consumer = registration;
            registration = null;
        }
        pending.write(InboundBuffer.END_SENTINEL);
        super.handleClosed();
        if (consumer != null) {
            consumer.unregister();
        }
    }

    public void handleMessage(Object msg) {
        if (msg instanceof ByteBuf) {
            msg = VertxHandler.safeBuffer((ByteBuf) msg, chctx.alloc());
        }
        if (!pending.write(msg)) {
            doPause();
        }
    }

    private class DataMessageHandler<T> implements Handler<T> {

        private final Handler<T> dataHandler;
        private final ByteBufAllocator allocator;

        DataMessageHandler(ByteBufAllocator allocator, Handler<T> dataHandler) {
            this.allocator = allocator;
            this.dataHandler = dataHandler;
        }

        @Override
        public void handle(T event) {
//            if (event instanceof ByteBuf) {
//                Buffer data = Buffer.buffer((ByteBuf) event);
//                reportBytesRead(data.length());
                dataHandler.handle(event);
//            }
        }
    }

    private static class InvalidMessageHandler implements Handler<Object> {
        @Override
        public void handle(Object msg) {
            // ByteBuf are eagerly released when the message is processed
            if (msg instanceof ReferenceCounted && (!(msg instanceof ByteBuf))) {
                ReferenceCounted refCounter = (ReferenceCounted) msg;
                refCounter.release();
            }
        }
    }

    private synchronized void callDrainHandler() {
        if (drainHandler != null) {
            if (!writeQueueFull()) {
                drainHandler.handle(null);
            }
        }
    }
}