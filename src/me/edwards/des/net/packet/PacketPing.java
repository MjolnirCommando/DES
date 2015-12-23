package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

/**
 * Ping Packet<br>
 * This packet is sent to request a response from its target node. Created on:
 * Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketPing
    extends Packet
{
    private long ping;


    /**
     * Creates new PacketPing
     * 
     * @param ping
     *            Nonce for checking pong
     */
    public PacketPing(long ping)
    {
        super(PacketTypes.PING.getID());
        this.ping = ping;
    }


    /**
     * Creates new PacketPing from binary data
     * 
     * @param binary
     *            Packet binary data
     */
    public PacketPing(byte[] binary)
    {
        super(PacketTypes.PING.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.ping = data.getLong();
    }


    /**
     * Returns the Nonce for checking pong
     * 
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
