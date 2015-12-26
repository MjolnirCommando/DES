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

// -----------------------------------------------------------------------------
/**
 * <strong>Block Packet</strong><br>
 * <br>
 * This packet is used to transfer {@link Block Block} data between Nodes.<br>
 * <br>
 * Created on: Nov 3, 2015 at 10:28:29 AM
 * 
 * @author Matthew Edwards
 */
public class PacketBlock
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private Block block;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketBlock
     * 
     * @param block
     *            Block payload
     */
    public PacketBlock(Block block)
    {
        super(PacketTypes.BLOCK.getID());
        this.block = block;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketBlock from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketBlock(byte[] binary)
    {
        super(PacketTypes.BLOCK.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(1);
        int size = data.getInt();
        byte[] bytes = new byte[size - 5];
        data.get(bytes, 0, size - 5);
        this.block = new Block(bytes);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the {@link Block Block} transferred by this Packet
     * 
     * @return Block payload
     */
    public Block getBlock()
    {
        return block;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        byte[] bytes = block.getBytes();
        int size = 1 + 4 + bytes.length;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.put(bytes);
        return data.array();
    }
}
