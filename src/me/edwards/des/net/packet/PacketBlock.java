package me.edwards.des.net.packet;

import java.nio.ByteBuffer;
import me.edwards.des.block.Block;

/**
 * Block Packet<br>
 * This packet is used to transfer block data between nodes. Created on: Nov 3,
 * 2015 at 10:28:29 AM
 * 
 * @author Matthew Edwards
 */
public class PacketBlock
    extends Packet
{
    private Block block;


    /**
     * Creates new PacketBlock
     * 
     * @param block
     */
    public PacketBlock(Block block)
    {
        super(PacketTypes.BLOCK.getID());
        this.block = block;
    }


    /**
     * Creates new PacketBlock
     * 
     * @param binary
     *            Packet binary data
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


    /**
     * Returns the block transferred by this packet
     * 
     * @return
     */
    public Block getBlock()
    {
        return block;
    }


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
