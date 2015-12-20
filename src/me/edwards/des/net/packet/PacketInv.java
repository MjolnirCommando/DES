package me.edwards.des.net.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.edwards.des.block.Ballot;
import me.edwards.des.block.Block;
import me.edwards.des.util.ByteUtil;

/**
 * Inventory Packet<br>
 * This packet is sent to advertise knowledge of a particular piece of data
 * Created on: Nov 3, 2015 at 9:17:44 AM
 * @author Matthew Edwards
 */
public class PacketInv extends Packet
{
    /**
     * The Vector type for a Ballot Object
     */
    public static final int VECTOR_BALLOT = 1;
    
    /**
     * The Vector type for a Block Object
     */
    public static final int VECTOR_BLOCK = 2;
    
    private ArrayList<InvVector> vectors;
    
    /**
     * Creates new PacketInv
     */
    public PacketInv()
    {
        super(PacketTypes.INV.getID());
        this.vectors = new ArrayList<InvVector>();
    }
    
    /**
     * Creates new PacketInv
     * @param binary Packet binary data
     */
    public PacketInv(byte[] binary)
    {
        super(PacketTypes.INV.getID());
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
        return ByteUtil.bytesToHex(vectors.get(index).hash);
    }
    
    /**
     * Adds an object to this Inv
     * @param invObject
     * @throws A Runtime Exception is thrown if the invObject is not a valid type
     */
    public void addInv(Object invObject)
    {
        InvVector vector = new InvVector();
        if (invObject instanceof Ballot)
        {
            vector.type = VECTOR_BALLOT;
            vector.hash = ByteUtil.hexToBytes(((Ballot) invObject).getRoot());
        }
        else if (invObject instanceof Block)
        {
            vector.type = VECTOR_BLOCK;
            vector.hash = ByteUtil.hexToBytes(((Block) invObject).getHash());
        }
        else
        {
            throw new RuntimeException("Invalid invObject type!");
        }
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
    
    protected static class InvVector
    {
        protected int type;
        protected byte[] hash;
    }
}
