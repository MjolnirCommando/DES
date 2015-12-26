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
import java.util.ArrayList;
import me.edwards.des.net.Connection;

// -----------------------------------------------------------------------------
/**
 * <strong>Address Packet</strong><br>
 * <br>
 * This packet is sent as a response to a {@link PacketGetAddr PacketGetAddr}
 * with a list of the responding node's known peers. It is used in the
 * bootstrapping process.<br>
 * <br>
 * Created on: Oct 19, 2015 at 10:46:14 AM
 * 
 * @author Matthew Edwards
 */
public class PacketAddr
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private String[] peerList;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketAddr
     * 
     * @param peers
     *            List of current peers
     */
    public PacketAddr(ArrayList<Connection> peers)
    {
        super(PacketTypes.ADDR.getID());
        peerList = new String[peers.size()];
        for (int i = 0; peerList.length > i; i++)
        {
            peerList[i] = peers.get(i).getHostName();
        }
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketAddr from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketAddr(byte[] binary)
    {
        super(PacketTypes.ADDR.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        int length = data.getInt();
        peerList = new String[length];
        for (int i = 0; length > i; i++)
        {
            byte[] strBytes = new byte[data.getInt()];
            data.get(strBytes);
            peerList[i] = new String(strBytes);
        }
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns this Packet's peer list
     * 
     * @return Peer list as an array of Strings
     */
    public String[] getPeers()
    {
        return peerList;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 4;
        for (String p : peerList)
        {
            size += 4 + p.getBytes().length;
        }
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.putInt(peerList.length);
        for (String p : peerList)
        {
            byte[] pBytes = p.getBytes();
            data.putInt(pBytes.length);
            data.put(pBytes);
        }
        return data.array();
    }
}
