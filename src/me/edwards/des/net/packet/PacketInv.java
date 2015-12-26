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
import java.util.ArrayList;
import me.edwards.des.block.Ballot;
import me.edwards.des.block.Block;
import me.edwards.des.util.ByteUtil;

// -----------------------------------------------------------------------------
/**
 * <strong>Inventory Packet</strong><br>
 * <br>
 * This packet is sent to advertise knowledge of a particular piece, or pieces,
 * of data.<br>
 * <br>
 * Created on: Nov 3, 2015 at 9:17:44 AM
 * 
 * @author Matthew Edwards
 */
public class PacketInv
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    /**
     * The Vector type for a Ballot Object
     */
    public static final int      VECTOR_BALLOT = 1;

    /**
     * The Vector type for a Block Object
     */
    public static final int      VECTOR_BLOCK  = 2;

    
    // -------------------------------------------------------------------------
    private ArrayList<InvVector> vectors;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketInv
     */
    public PacketInv()
    {
        super(PacketTypes.INV.getID());
        this.vectors = new ArrayList<InvVector>();
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketInv from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
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


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the number of vectors in this Packet
     * 
     * @return Number of vectors in this Packet
     */
    public int getSize()
    {
        return vectors.size();
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the type of the vector at the specified index.
     * 
     * @param index
     *            Index of inventory vector
     * @return Type of the specified vector if the index exists, otherwise -1.
     */
    public int getType(int index)
    {
        if (index < vectors.size())
        {
            return vectors.get(index).type;
        }
        return -1;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the hash of the vector at the specified index.
     * 
     * @param index
     *            Index of inventory vector
     * @return Hash of the specified vector if the index exists as a String,
     *         otherwise null.
     */
    public String getHash(int index)
    {
        if (index < vectors.size())
        {
            return ByteUtil.bytesToHex(vectors.get(index).hash);
        }
        return null;
    }


    // -------------------------------------------------------------------------
    /**
     * Adds an inventory object to this Packet.
     * 
     * @param invObject
     *            Object to add to inventory
     * @throws RuntimeException
     *             Thrown if the invObject is not a valid type
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


    // -------------------------------------------------------------------------
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


    // -------------------------------------------------------------------------
    /**
     * Inventory Vector contains the hash of the inventory object and its type.
     * <br>
     * <br>
     * Created on: Nov 3, 2015 at 9:18:31 AM
     * 
     * @author Matthew Edwards
     */
    protected static class InvVector
    {
        // ~ Static/Instance variables .........................................

        // ---------------------------------------------------------------------
        protected int    type;
        protected byte[] hash;
    }
}
