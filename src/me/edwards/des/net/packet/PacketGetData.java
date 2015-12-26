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
import me.edwards.des.net.packet.PacketInv.InvVector;
import me.edwards.des.util.ByteUtil;
import me.edwards.des.util.HashUtil;

// -----------------------------------------------------------------------------
/**
 * <strong>Get Data Packet</strong><br>
 * <br>
 * This packet is sent to request a particular piece, or pieces, of data.<br>
 * <br>
 * Created on: Nov 3, 2015 at 10:06:19 AM
 * 
 * @author Matthew Edwards
 */
public class PacketGetData
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private ArrayList<InvVector> vectors;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketGetData
     */
    public PacketGetData()
    {
        super(PacketTypes.GETDATA.getID());
        this.vectors = new ArrayList<InvVector>();
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketGetData from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
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


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the number of vectors in this Packet.
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
     * @param type
     *            {@link PacketInv Type} of data
     * @param hash
     *            Data's 32-digit hexadecimal hash
     */
    public void addInv(int type, String hash)
    {
        InvVector vector = new InvVector();
        vector.type = type;
        vector.hash = ByteUtil.hexToBytes(HashUtil.generateLeadingZeros(hash));
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
}
