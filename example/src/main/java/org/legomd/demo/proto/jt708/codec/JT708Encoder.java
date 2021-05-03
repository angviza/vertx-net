package org.legomd.demo.proto.jt708.codec;

import org.legomd.demo.msg.RawPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:28
 * @Description:
 */
public class JT708Encoder extends MessageToByteEncoder<RawPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RawPacket msg, ByteBuf out) throws Exception {

    }
}
