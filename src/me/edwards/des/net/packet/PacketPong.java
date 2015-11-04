package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

/**
 * Pong Packet<br>
 * This packet is sent as a response to its target node.
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * @author Matthew Edwards
 */
public class PacketPong extends Packet
{
    private long ping;
    
    /**
     * Creates new PacketPong
     * @param ping Nonce for checking pong
     */
    public PacketPong(long ping)
    {
        super(PacketTypes.PONG.getID());
        this.ping = ping;
    }
    
    /**
     * Creates new PacketPong from binary data
     * @param binary Packet binary data
     */
    public PacketPong(byte[] binary)
    {
        super(PacketTypes.PONG.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.ping = data.getLong();
    }
    
    /**
     * Returns the Nonce for checking pong
     * @return
     */
    public long getPing()
    {
        return ping;
    }

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
