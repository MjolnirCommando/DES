package me.edwards.des.net.packet;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.edwards.des.net.packet.PacketInv.InvVector;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

/**
 * Get Data Packet<br>
 * This packet is sent to request a particular piece of data
 * Created on: Nov 3, 2015 at 10:06:19 AM
 * @author Matthew Edwards
 */
public class PacketGetData extends Packet
{
    private ArrayList<InvVector> vectors;
    
    /**
     * Creates new PacketGetData
     */
    public PacketGetData()
    {
        super(PacketTypes.GETDATA.getID());
        this.vectors = new ArrayList<InvVector>();
    }
    
    /**
     * Creates new PacketGetData
     * @param binary Packet binary data
     */
    public PacketGetData(byte[] binary)
    {
        super(PacketTypes.GETDATA.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(1);
        this.vectors = new ArrayList<InvVector>();
        int length = (data.getInt() - 5) / 36;
        for (int i = 0; length > i; i++)
        {
            InvVector v = new InvVector();
            v.type = data.getInt();
            v.hash = new byte[32];
            data.get(v.hash);
            vectors.add(v);
        }
    }
    
    /**
     * Returns the number of vectors in this Inv
     * @return
     */
    public int getSize()
    {
        return vectors.size();
    }
    
    /**
     * Returns the type of the vector at the specified index
     * @param index
     * @return
     */
    public int getType(int index)
    {
        return vectors.get(index).type;
    }
    
    /**
     * Returns the hash of the vector at the specified index
     * @param index
     * @return
     */
    public String getHash(int index)
    {
        BigInteger i = new BigInteger(1, vectors.get(index).hash);
        return HashUtil.generateLeadingZeros(i.toString(16));
    }
    
    /**
     * Adds an object to this packet
     * @param type Type of data
     * @param hash Data hash
     */
    public void addInv(int type, String hash)
    {
        InvVector vector = new InvVector();
        vector.type = type;
        vector.hash = ByteUtil.hexToBytes(HashUtil.generateLeadingZeros(hash));
        vectors.add(vector);
    }

    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 36 * vectors.size();
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        for (int i = 0; vectors.size() > i; i++)
        {
            data.putInt(vectors.get(i).type);
            data.put(vectors.get(i).hash);
        }
        return data.array();
    }
}
