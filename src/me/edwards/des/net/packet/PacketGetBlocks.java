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
import me.edwards.des.block.Block;
import me.edwards.des.util.ByteUtil;

// -----------------------------------------------------------------------------
/**
 * <strong>Get Blocks Packet</strong><br>
 * <br>
 * This packet is sent to request {@link Block Block} information from other
 * Nodes during the bootstrap process.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketGetBlocks
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private String hash;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketGetBlocks
     * 
     * @param hash
     *            Hash of the oldest {@link Block Block} requested
     */
    public PacketGetBlocks(String hash)
    {
        super(PacketTypes.GETBLOCKS.getID());
        this.hash = hash;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketGetBlocks from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketGetBlocks(byte[] binary)
    {
        super(PacketTypes.GETBLOCKS.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        byte[] bytes = new byte[32];
        data.get(bytes, 0, 32);
        this.hash = ByteUtil.bytesToHex(bytes);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the oldest {@link Block#getHash() hash} of the {@link Block
     * Block} requested by this packet.
     * 
     * @return 32-digit hexadecimal hash of the oldest Block requested
     */
    public String getHash()
    {
        return hash;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 32;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.put(ByteUtil.hexToBytes(hash));
        return data.array();
    }
}
