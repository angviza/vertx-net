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


import org.legomd.net.modbus.core.FunctionCode;

/**
 * This function is used to read from 1 to 125 contiguous input registers in a remote device. The Request PDU
 * specifies the starting register address and the number of registers. In the PDU Registers are addressed starting at
 * zero. Therefore input registers numbered 1-16 are addressed as 0-15.
 */
public class ReadInputRegistersRequest extends SimpleModbusRequest {

    private final int address;
    private final int quantity;

    /**
     * @param address  0x0000 to 0xFFFF (0 to 65535)
     * @param quantity 0x0001 to 0x007D (1 to 125)
     */
    public ReadInputRegistersRequest(int address, int quantity) {
        super(FunctionCode.ReadInputRegisters);

        this.address = address;
        this.quantity = quantity;
    }

    public int getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

}
