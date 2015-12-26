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
 * <strong>Pong Packet</strong><br>
 * <br>
 * This packet is sent as a response to a {@link PacketPing}.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketPong
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private long ping;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketPong
     * 
     * @param ping
     *            Nonce for checking pong
     */
    public PacketPong(long ping)
    {
        super(PacketTypes.PONG.getID());
        this.ping = ping;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketPong from binary data
     * 
     * @param binary
     *            Packet binary data
     */
    public PacketPong(byte[] binary)
    {
        super(PacketTypes.PONG.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.ping = data.getLong();
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the Nonce for checking pong. Should be one more than the nonce
     * used by the {@link PacketPing Ping Packet}.
     * 
     * @return Nonce for checking pong
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
