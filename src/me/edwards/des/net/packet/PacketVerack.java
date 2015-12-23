package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

/**
 * Version Acknowledge Packet<br>
 * This is used in the handshake protocol for the DES system and confirms that a
 * handshake is complete Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketVerack
    extends Packet
{
    /**
     * Creates new PacketVerack
     */
    public PacketVerack()
    {
        super(PacketTypes.VERACK.getID());
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
