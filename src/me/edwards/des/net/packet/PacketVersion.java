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
import me.edwards.des.net.Connection;
import me.edwards.des.util.Version;

// -----------------------------------------------------------------------------
/**
 * <strong>Version Packet</strong><br>
 * <br>
 * This is used in the handshake protocol for the DES system and contains
 * version, block count, and time.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketVersion
    extends Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private Version version;
    private int     port;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new PacketVersion
     * 
     * @param version
     *            Node DES Version
     * @param port
     *            Node port
     */
    public PacketVersion(Version version, int port)
    {
        super(PacketTypes.VERSION.getID());
        this.version = version;
        this.port = port;
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new PacketVersion from binary data
     * 
     * @param binary
     *            Packet binary data as byte array
     */
    public PacketVersion(byte[] binary)
    {
        super(PacketTypes.VERSION.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        int length = data.getInt();
        byte[] temp = new byte[length];
        data.get(temp);
        version = new Version(new String(temp));
        port = data.getInt();
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns the Version of the sender Node.
     * 
     * @return Version of the sender Node
     */
    public Version getVersion()
    {
        return version;
    }


    // -------------------------------------------------------------------------
    /**
     * Return the sender Node's port. Used to set the correct port of the
     * {@link Connection Connection}.
     * 
     * @return Port of the sender Node
     */
    public int getPort()
    {
        return port;
    }


    // -------------------------------------------------------------------------
    @Override
    public byte[] getBinary()
    {
        byte[] versionData = version.toString().getBytes();
        int size = 1 + 4 + 4 + versionData.length + 4;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.putInt(versionData.length);
        data.put(versionData);
        data.putInt(port);
        return data.array();
    }

}
