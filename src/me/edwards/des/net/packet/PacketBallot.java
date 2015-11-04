package me.edwards.des.net.packet;

import java.nio.ByteBuffer;

import me.edwards.des.block.Ballot;

/**
 * Ballot Packet<br>
 * This packet is used to transfer ballot data between nodes.
 * Created on: Nov 3, 2015 at 10:28:29 AM
 * @author Matthew Edwards
 */
public class PacketBallot extends Packet
{
    /**
     * Creates new PacketBallot
     * @param ballot
     */
    public PacketBallot(Ballot ballot)
    {
        super(PacketTypes.BALLOT.getID());
    }
    
    /**
     * Creates new PacketBallot
     * @param binary Packet binary data
     */
    public PacketBallot(byte[] binary)
    {
        super(PacketTypes.BALLOT.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(5);
    }

    @Override
    public byte[] getBinary()
    {
        int size = 1 + 4;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        return data.array();
    }
}
