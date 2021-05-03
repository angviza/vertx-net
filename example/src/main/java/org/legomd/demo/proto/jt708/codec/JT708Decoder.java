package org.legomd.demo.proto.jt708.codec;

import org.legomd.demo.proto.jt708.msg.LoginMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/3 16:28
 * @Description:
 */
public class JT708Decoder extends ByteToMessageDecoder {
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
        System.out.println("JT708Decoder:"+msg);
        if (!msg.startsWith("bb")) {
            in.resetReaderIndex();
            return;
        }
        out.add(new LoginMsg(1, msg));
    }
}
