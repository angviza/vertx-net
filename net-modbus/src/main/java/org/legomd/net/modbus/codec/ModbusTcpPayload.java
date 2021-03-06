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

package org.legomd.net.modbus.codec;


import org.legomd.net.modbus.core.ModbusPdu;

public class ModbusTcpPayload {

    private final short transactionId;
    private final short unitId;
    private final ModbusPdu modbusPdu;

    public ModbusTcpPayload(short transactionId, short unitId, ModbusPdu modbusPdu) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.modbusPdu = modbusPdu;
    }

    public short getTransactionId() {
        return transactionId;
    }

    public short getUnitId() {
        return unitId;
    }

    public ModbusPdu getModbusPdu() {
        return modbusPdu;
    }

}
