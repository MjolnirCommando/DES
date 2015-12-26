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
import me.edwards.des.block.Ballot;

// -----------------------------------------------------------------------------
/**
 * <strong>Ballot Packet</strong><br>
 * <br>
 * This packet is used to transfer {@link Ballot Ballot} data between Nodes.<br>
 * <br>
 * Created on: Nov 3, 2015 at 10:28:29 AM
 * 
 * @author Matthew Edwards
 */
public class PacketBallot
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private Ballot ballot;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketBallot
     * 
     * @param ballot
     *            {@link Ballot Ballot} payload
     */
    public PacketBallot(Ballot ballot)
    {
        super(PacketTypes.BALLOT.getID());
        this.ballot = ballot;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketBallot from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketBallot(byte[] binary)
    {
        super(PacketTypes.BALLOT.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(1);
        int size = data.getInt();
        byte[] bytes = new byte[size - 5];
        data.get(bytes, 0, size - 5);
        this.ballot = new Ballot(bytes);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the {@link Ballot Ballot} transferred by this Packet.
     * 
     * @return Ballot payload
     */
    public Ballot getBallot()
    {
        return ballot;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        byte[] bytes = ballot.getBytes();
        int size = 1 + 4 + bytes.length;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.put(bytes);
        return data.array();
    }
}
