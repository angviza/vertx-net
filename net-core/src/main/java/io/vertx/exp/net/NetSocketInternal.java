package io.vertx.exp.net;

import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.lang.rx.RxGen;

public interface NetSocketInternal<T> extends NetSocket<T> {

    ChannelHandlerContext channelHandlerContext();

    /**
     * Write a message in the channel pipeline.
     * <p/>
     * When a read operation is in progress, the flush operation is delayed until the read operation completes.
     *
     * @param message the message to write, it should be handled by one of the channel pipeline handlers
     * @return a future completed with the result
     */
    Future<Void> writeMessage(Object message);

    /**
     * Like {@link #writeMessage(Object)} but with an {@code handler} called when the message has been written
     * or failed to be written.
     */
    NetSocketInternal<T> writeMessage(Object message, Handler<AsyncResult<Void>> handler);

    /**
     * Set a {@code handler} on this socket to process the messages produced by this socket. The message can be
     * {@link io.netty.buffer.ByteBuf} or other messages produced by channel pipeline handlers.
     * <p/>
     * The {@code} handler should take care of releasing pooled / direct messages.
     * <p/>
     * The handler replaces any {@link #handler(Handler)} previously set.
     *
     * @param handler the handler to set
     * @return a reference to this, so the API can be used fluently
     */
    NetSocketInternal<T> messageHandler(Handler<Object> handler);
}
