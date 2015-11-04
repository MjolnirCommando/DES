package me.edwards.des.net.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.edwards.des.net.Connection;

/**
 * Address Packet<br>
 * This packet is sent as a response to a GETADDR Packet with a list of the responding
 * node's known peers. It is used in the bootstrapping process.
 * Created on: Oct 19, 2015 at 10:46:14 AM
 * @author Matthew Edwards
 */
public class PacketAddr extends Packet
{
    private String[] peerList;
    
    /**
     * Creates new PacketAddr
     * @param peers List of current peers
     */
    public PacketAddr(ArrayList<Connection> peers)
    {
        super(PacketTypes.ADDR.getID());
        peerList = new String[peers.size()];
        for (int i = 0; peerList.length > i; i++)
        {
            peerList[i] = peers.get(i).getHostName();
        }
    }
    
    /**
     * Creates new PacketAddr
     * @param binary Packet binary data
     */
    public PacketAddr(byte[] binary)
    {
        super(PacketTypes.ADDR.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
        int length = data.getInt();
        peerList = new String[length];
        for (int i = 0; length > i; i++)
        {
            byte[] strBytes = new byte[data.getInt()];
            data.get(strBytes);
            peerList[i] = new String(strBytes);
        }
    }
    
    /**
     * Returns this packet's peer list
     * @return
     */
    public String[] getPeers()
    {
        return peerList;
    }

    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4 + 4;
        for (String p : peerList)
        {
            size += 4 + p.getBytes().length;
        }
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.putInt(peerList.length);
        for (String p : peerList)
        {
            byte[] pBytes = p.getBytes();
            data.putInt(pBytes.length);
            data.put(pBytes);
        }
        return data.array();
    }
}
