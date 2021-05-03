/*
 * Copyright 2016 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.legomd.net.modbus.core.requests;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import org.legomd.net.modbus.core.FunctionCode;

public abstract class ByteBufModbusRequest extends DefaultByteBufHolder implements ModbusRequest {

    private final FunctionCode functionCode;

    public ByteBufModbusRequest(ByteBuf data, FunctionCode functionCode) {
        super(data);

        this.functionCode = functionCode;
    }

    @Override
    public FunctionCode getFunctionCode() {
        return functionCode;
    }

}