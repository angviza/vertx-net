package io.vertx.exp.net;

import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface NetSocketInternal<T> extends NetSocket<T> {
    ChannelHandlerContext channelHandlerContext();

    NetSocketInternal writeMessage(Object var1);

    NetSocketInternal writeMessage(Object var1, Handler<AsyncResult<Void>> var2);

    NetSocketInternal messageHandler(Handler<Object> var1);
}
