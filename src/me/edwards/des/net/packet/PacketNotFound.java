package me.edwards.des.net.packet;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import me.edwards.des.util.HashUtil;

/**
 * Not Found Packet<br>
 * This packet is sent to signal that a specified resource in a GETDATA could
 * not be found. Created on: Nov 3, 2015 at 10:19:25 AM
 * 
 * @author Matthew Edwards
 */
public class PacketNotFound
    extends Packet
{
    private int    type;
    private String hash;
    private byte[] hashData;


    /**
     * Creates new PacketNotFound
     * 
     * @param type
     * @param hash
     */
    public PacketNotFound(int type, String hash)
    {
        super(PacketTypes.NOTFOUND.getID());
        this.type = type;
        this.hash = hash;
        this.hashData =
            new BigInteger(hash.replaceFirst("0{0,31}", ""), 16).toByteArray();
    }


    /**
     * Creates new PacketNotFound
     * 
     * @param binary
     *            Packet binary data
     */
    public PacketNotFound(byte[] binary)
    {
        super(PacketTypes.NOTFOUND.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.type = data.getInt();
        this.hashData = new byte[32];
        data.get(hashData);
        this.hash =
            HashUtil.generateLeadingZeros(new BigInteger(1, hashData)
                .toString(16));
    }


    /**
     * Returns the type of data
     * 
     * @return
     */
    public int getType()
    {
        return type;
    }


    /**
     * Returns the hash of data
     * 
     * @return
     */
    public String getHash()
    {
        return hash;
    }


    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 36;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.putInt(type);
        data.put(hashData);
        return data.array();
    }
}
