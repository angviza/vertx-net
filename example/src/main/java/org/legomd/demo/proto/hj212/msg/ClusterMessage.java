package org.legomd.demo.proto.hj212.msg;


import org.legomd.demo.msg.RawPacket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/2 20:03
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class ClusterMessage extends RawPacket {
    private int id;
    private String data;
}
