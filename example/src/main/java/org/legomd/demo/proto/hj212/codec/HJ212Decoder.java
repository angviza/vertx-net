package org.legomd.demo.proto.hj212.codec;

import org.legomd.demo.proto.hj212.msg.ClusterMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 00:21
 * @Description:
 */
public class HJ212Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int len = in.readableBytes();
        if (len < 10) {
            return;
        }
        byte[] data = new byte[len];
        in.markReaderIndex();
        in.readBytes(data);
        String msg = new String(data);
        System.out.println("HJ212Decoder:"+msg);
        if (!msg.startsWith("aa")) {
            in.resetReaderIndex();
            return;
        }
        out.add(new ClusterMessage(0, msg));
    }
}
