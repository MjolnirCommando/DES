/*============================================================================*\
 | Copyright (C) 2015 Matthew Edwards                                         |
 |                                                                            |
 | Licensed under the Apache License, Version 2.0 (the "License"); you may    |
 | not use this file except in compliance with the License. You may obtain a  |
 | copy of the License at                                                     |
 |                                                                            |
 |     http://www.apache.org/licenses/LICENSE-2.0                             |
 |                                                                            |
 | Unless required by applicable law or agreed to in writing, software        |
 | distributed under the License is distributed on an "AS IS" BASIS,          |
 | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   |
 | See the License for the specific language governing permissions and        |
 | limitations under the License.                                             |
\*============================================================================*/

package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

// -----------------------------------------------------------------------------
/**
 * <strong>Version Acknowledge Packet</strong><br>
 * <br>
 * This is used in the handshake protocol for the DES system and confirms that a
 * handshake is complete.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketVerack
    extends Packet
{
    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketVerack
     */
    public PacketVerack()
    {
        super(PacketTypes.VERACK.getID());
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        return data.array();
    }

}
