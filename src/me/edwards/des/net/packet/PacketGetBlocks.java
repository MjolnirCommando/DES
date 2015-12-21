package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

import me.edwards.des.util.ByteUtil;

/**
 * Get Blocks Packet<br>
 * This packet is sent to request block information from other nodes during
 * the bootstrap process.
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * @author Matthew Edwards
 */
public class PacketGetBlocks extends Packet
{
    private String hash;
    
    /**
     * Creates new PacketGetBlocks
     * @param hash Hash of the oldest Block requested
     */
    public PacketGetBlocks(String hash)
    {
        super(PacketTypes.GETBLOCKS.getID());
        this.hash = hash;
    }
    
    /**
     * Creates new PacketGetBlocks from binary data
     * @param binary Packet binary data
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
    
    /**
     * Returns the oldest hash of the Block requested by this packet
     * @return
     */
    public String getHash()
    {
        return hash;
    }

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
