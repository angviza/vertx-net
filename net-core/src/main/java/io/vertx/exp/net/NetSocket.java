package io.vertx.exp.net;

import io.netty.channel.Channel;
import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

/**
 * Represents a socket-like interface to a TCP connection on either the
 * client or the server side.
 * <p>
 * Instances of this class are created on the client side by an {@link }
 * when a connection to a server is made, or on the server side by a {@link NetServer}
 * when a server accepts a connection.
 * <p>
 * It implements both {@link ReadStream} and {@link WriteStream} so it can be used with
 * {@link io.vertx.core.streams.Pump} to pump data with flow control.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface NetSocket<T> extends ReadStream<T>, WriteStream<T> {
    @GenIgnore
    Channel channel();
    @Override
    NetSocket exceptionHandler(Handler<Throwable> handler);

    @Override
    NetSocket handler(Handler<T> handler);

    @Override
    NetSocket pause();

    @Override
    NetSocket resume();

    @Override
    NetSocket fetch(long amount);

    /**
     * {@inheritDoc}
     * <p>
     * This handler might be called after the close handler when the socket is paused and there are still
     * buffers to deliver.
     */
    @Override
    NetSocket endHandler(Handler<Void> endHandler);

    @Override
    NetSocket write(T data);

    @Override
    NetSocket setWriteQueueMaxSize(int maxSize);

    @Override
    NetSocket drainHandler(Handler<Void> handler);

    /**
     * When a {@code NetSocket} is created it automatically registers an event handler with the event bus, the ID of that
     * handler is given by {@code writeHandlerID}.
     * <p>
     * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
     * that buffer will be received by this instance in its own event loop and written to the underlying connection. This
     * allows you to write data to other connections which are owned by different event loops.
     *
     * @return the write handler ID
     */
    String writeHandlerID();

    /**
     * Same as {@link #write(String)} but with an {@code handler} called when the operation completes
     */
//    @Fluent
//    NetSocket write(String str, Handler<AsyncResult<Void>> handler);

    /**
     * Write a {@link String} to the connection, encoded in UTF-8.
     *
     * @param str  the string to write
     * @return a reference to this, so the API can be used fluently
     */
//    @Fluent
//    NetSocket write(String str);

    /**
     * Same as {@link #write(String, String)} but with an {@code handler} called when the operation completes
     */
//    @Fluent
//    NetSocket write(String str, String enc, Handler<AsyncResult<Void>> handler);

    /**
     * Write a {@link String} to the connection, encoded using the encoding {@code enc}.
     *
     * @param str  the string to write
     * @param enc  the encoding to use
     * @return a reference to this, so the API can be used fluently
     */
//    @Fluent
//    NetSocket write(String str, String enc);

    /**
     * Like  but with an {@code handler} called when the message has been written
     * or failed to be written.
     */
    @Fluent
    NetSocket write(T message, Handler<AsyncResult<Void>> handler);

    /**
     * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
     * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
     *
     * @param filename  file name of the file to send
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    default NetSocket sendFile(String filename) {
        return sendFile(filename, 0, Long.MAX_VALUE);
    }

    /**
     * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
     * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
     *
     * @param filename  file name of the file to send
     * @param offset offset
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    default NetSocket sendFile(String filename, long offset) {
        return sendFile(filename, offset, Long.MAX_VALUE);
    }

    /**
     * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
     * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
     *
     * @param filename  file name of the file to send
     * @param offset offset
     * @param length length
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    NetSocket sendFile(String filename, long offset, long length);

    /**
     * Same as {@link #sendFile(String)} but also takes a handler that will be called when the send has completed or
     * a failure has occurred
     *
     * @param filename  file name of the file to send
     * @param resultHandler  handler
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    default NetSocket sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
        return sendFile(filename, 0, Long.MAX_VALUE, resultHandler);
    }

    /**
     * Same as {@link #sendFile(String, long)} but also takes a handler that will be called when the send has completed or
     * a failure has occurred
     *
     * @param filename  file name of the file to send
     * @param offset offset
     * @param resultHandler  handler
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    default NetSocket sendFile(String filename, long offset, Handler<AsyncResult<Void>> resultHandler) {
        return sendFile(filename, offset, Long.MAX_VALUE, resultHandler);
    }

    /**
     * Same as {@link #sendFile(String, long, long)} but also takes a handler that will be called when the send has completed or
     * a failure has occurred
     *
     * @param filename  file name of the file to send
     * @param offset offset
     * @param length length
     * @param resultHandler  handler
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    NetSocket sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler);

    /**
     * @return the remote address for this connection, possibly {@code null} (e.g a server bound on a domain socket)
     */
    @CacheReturn
    SocketAddress remoteAddress();

    /**
     * @return the local address for this connection, possibly {@code null} (e.g a server bound on a domain socket)
     */
    @CacheReturn
    SocketAddress localAddress();

    /**
     * Calls {@link #close()}
     */
    @Override
    void end();

    /**
     * Calls {@link #end(Handler)}
     */
    @Override
    void end(Handler<AsyncResult<Void>> handler);

    /**
     * Close the NetSocket
     */
    void close();

    /**
     * Close the NetSocket and notify the {@code handler} when the operation completes.
     */
    void close(Handler<AsyncResult<Void>> handler);

    /**
     * Set a handler that will be called when the NetSocket is closed
     *
     * @param handler  the handler
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    NetSocket closeHandler(@Nullable Handler<Void> handler);

    /**
     * Upgrade channel to use SSL/TLS. Be aware that for this to work SSL must be configured.
     *
     * @param handler  the handler will be notified when it's upgraded
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    NetSocket upgradeToSsl(Handler<Void> handler);

    /**
     * Upgrade channel to use SSL/TLS. Be aware that for this to work SSL must be configured.
     *
     * @param serverName the server name
     * @param handler  the handler will be notified when it's upgraded
     * @return a reference to this, so the API can be used fluently
     */
    @Fluent
    NetSocket upgradeToSsl(String serverName, Handler<Void> handler);

    /**
     * @return true if this {@link io.vertx.core.net.NetSocket} is encrypted via SSL/TLS.
     */
    boolean isSsl();

    /**
     * @return SSLSession associated with the underlying socket. Returns null if connection is
     *         not SSL.
     * @see javax.net.ssl.SSLSession
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    SSLSession sslSession();

    /**
     * Note: Java SE 5+ recommends to use javax.net.ssl.SSLSession#getPeerCertificates() instead of
     * of javax.net.ssl.SSLSession#getPeerCertificateChain() which this method is based on. Use {@link #sslSession()} to
     * access that method.
     *
     * @return an ordered array of the peer certificates. Returns null if connection is
     *         not SSL.
     * @throws javax.net.ssl.SSLPeerUnverifiedException SSL peer's identity has not been verified.
     * @see javax.net.ssl.SSLSession#getPeerCertificateChain()
     * @see #sslSession()
     */
    @GenIgnore
    X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException;

    /**
     * Returns the SNI server name presented during the SSL handshake by the client.
     *
     * @return the indicated server name
     */
    String indicatedServerName();
}