package me.edwards.des.net.packet;

import java.nio.ByteBuffer;
import me.edwards.des.util.Version;

/**
 * Version Packet<br>
 * This is used in the handshake protocol for the DES system and contains
 * version, block count, and time. Created on: Oct 17, 2015 at 9:12:01 AM
 * 
 * @author Matthew Edwards
 */
public class PacketVersion
    extends Packet
{
    private Version version;
    private int     port;


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


    /**
     * Creates new PacketVersion from binary data
     * 
     * @param binary
     *            Packet binary data
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


    /**
     * Returns the Version of the sender Node
     * 
     * @return
     */
    public Version getVersion()
    {
        return version;
    }


    /**
     * Return the sender Node's port
     * 
     * @return
     */
    public int getPort()
    {
        return port;
    }


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
