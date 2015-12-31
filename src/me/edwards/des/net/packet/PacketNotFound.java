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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import me.edwards.des.net.packet.PacketInv.InvVector;
import me.edwards.des.util.ByteUtil;

//-----------------------------------------------------------------------------
/**
 * <strong>Not Found Packet</strong><br>
 * <br>
 * This packet is sent to signal that a specified resource in a
 * {@link PacketGetData PacketGetData} could not be found.<br>
 * <br>
 * Created on: Nov 3, 2015 at 10:19:25 AM
 * 
 * @author Matthew Edwards
 */
public class PacketNotFound
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private int    type;
    private String hash;
    private byte[] hashData;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketNotFound
     * 
     * @see InvVector
     * @param type
     *            Inventory Vector type
     * @param hash
     *            Inventory Vector hash
     */
    public PacketNotFound(int type, String hash)
    {
        super(PacketTypes.NOTFOUND.getID());
        this.type = type;
        this.hash = hash;
        this.hashData =
            new BigInteger(hash.replaceFirst("0{0,31}", ""), 16).toByteArray();
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketNotFound from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketNotFound(byte[] binary)
    {
        super(PacketTypes.NOTFOUND.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        this.type = data.getInt();
        this.hashData = new byte[32];
        data.get(hashData);
        this.hash = ByteUtil.bytesToHex(hashData);
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the type of the inventory vector.
     * 
     * @see PacketInv
     * @return Type of the inventory vector
     */
    public int getType()
    {
        return type;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the hash of the inventory vector.
     * 
     * @return Hash of the inventory vector
     */
    public String getHash()
    {
        return hash;
    }


    // -------------------------------------------------------------------------
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
