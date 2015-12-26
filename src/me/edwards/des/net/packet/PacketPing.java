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
 * <strong>Ping Packet</strong><br>
 * <br>
 * This packet is sent to request a response from its destination Node.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketPing
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private long ping;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketPing
     * 
     * @param ping
     *            Nonce for checking ping
     */
    public PacketPing(long ping)
    {
        super(PacketTypes.PING.getID());
        this.ping = ping;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketPing from binary data
     * 
     * @param binary
     *            Packet binary data
     */
    public PacketPing(byte[] binary)
    {
        super(PacketTypes.PING.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.ping = data.getLong();
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the Nonce for checking ping. Should be incremented by one when
     * returned using a {@link PacketPong Pong Packet}.
     * 
     * @return Nonce for checking ping
     */
    public long getPing()
    {
        return ping;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 8;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.putLong(ping);
        return data.array();
    }
}
