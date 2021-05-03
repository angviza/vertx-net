package org.legomd.demo.proto.hj212.codec;

import org.legomd.demo.proto.hj212.msg.ClusterMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 00:21
 * @Description:
 */
public class HJ212Encoder extends MessageToByteEncoder<ClusterMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ClusterMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.data().getBytes());
    }
}
