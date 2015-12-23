package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

/**
 * Get Address Packet<br>
 * This packet is sent to request address information from other nodes during
 * the bootstrap process. Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketGetAddr
    extends Packet
{
    /**
     * Creates new PacketGetAddr
     */
    public PacketGetAddr()
    {
        super(PacketTypes.GETADDR.getID());
    }


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
