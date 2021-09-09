package io.vertx.exp.net.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;
import io.vertx.core.Handler;
import io.vertx.core.net.impl.VertxEventLoopGroup;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A channel server load balancer that distributes channel processing to a list of workers.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class ServerChannelLoadBalancer extends ChannelInitializer<Channel> {

    private final VertxEventLoopGroup workers;
    private final ConcurrentMap<EventLoop, ServerChannelLoadBalancer.WorkerList> workerMap = new ConcurrentHashMap<>();
    private final ChannelGroup channelGroup;

    // We maintain a separate hasHandlers variable so we can implement hasHandlers() efficiently
    // As it is called for every HTTP message received
    private volatile boolean hasHandlers;

    ServerChannelLoadBalancer(EventExecutor executor) {
        this.workers = new VertxEventLoopGroup();
        this.channelGroup = new DefaultChannelGroup(executor);
    }

    public VertxEventLoopGroup workers() {
        return workers;
    }

    public boolean hasHandlers() {
        return hasHandlers;
    }

    @Override
    protected void initChannel(Channel ch) {
        Handler<Channel> handler = chooseInitializer(ch.eventLoop());
        if (handler == null) {
            ch.close();
        } else {
            channelGroup.add(ch);
            handler.handle(ch);
        }
    }

    private Handler<Channel> chooseInitializer(EventLoop worker) {
        ServerChannelLoadBalancer.WorkerList handlers = workerMap.get(worker);
        return handlers == null ? null : handlers.chooseHandler();
    }

    public synchronized void addWorker(EventLoop eventLoop, Handler<Channel> handler) {
        workers.addWorker(eventLoop);
        ServerChannelLoadBalancer.WorkerList handlers = new ServerChannelLoadBalancer.WorkerList();
        ServerChannelLoadBalancer.WorkerList prev = workerMap.putIfAbsent(eventLoop, handlers);
        if (prev != null) {
            handlers = prev;
        }
        handlers.addWorker(handler);
        hasHandlers = true;
    }

    public synchronized boolean removeWorker(EventLoop worker, Handler<Channel> handler) {
        ServerChannelLoadBalancer.WorkerList handlers = workerMap.get(worker);
        if (handlers == null || !handlers.removeWorker(handler)) {
            return false;
        }
        if (handlers.isEmpty()) {
            workerMap.remove(worker);
        }
        if (workerMap.isEmpty()) {
            hasHandlers = false;
        }
        //Available workers does it's own reference counting -since workers can be shared across different Handlers
        workers.removeWorker(worker);
        return true;
    }

    private static final class WorkerList {
        private int pos;
        private final List<Handler<Channel>> list = new CopyOnWriteArrayList<>();
        Handler<Channel> chooseHandler() {
            Handler<Channel> handler = list.get(pos);
            pos++;
            checkPos();
            return handler;
        }

        void addWorker(Handler<Channel> handler) {
            list.add(handler);
        }

        boolean removeWorker(Handler<Channel> handler) {
            if (list.remove(handler)) {
                checkPos();
                return true;
            } else {
                return false;
            }
        }

        boolean isEmpty() {
            return list.isEmpty();
        }

        void checkPos() {
            if (pos == list.size()) {
                pos = 0;
            }
        }
    }

    /**
     * Close the load-balancer and all registered channels.
     */
    public void close() {
        channelGroup.close();
    }
}
