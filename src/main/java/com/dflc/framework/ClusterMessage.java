package com.dflc.framework;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
public class ClusterMessage implements Serializable {
    private int id;
    private String data;
}
