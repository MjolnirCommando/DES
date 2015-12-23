package me.edwards.des.net.packet;

import java.nio.ByteBuffer;
import me.edwards.des.block.Ballot;

/**
 * Ballot Packet<br>
 * This packet is used to transfer ballot data between nodes. Created on: Nov 3,
 * 2015 at 10:28:29 AM
 * 
 * @author Matthew Edwards
 */
public class PacketBallot
    extends Packet
{
    private Ballot ballot;


    /**
     * Creates new PacketBallot
     * 
     * @param ballot
     */
    public PacketBallot(Ballot ballot)
    {
        super(PacketTypes.BALLOT.getID());
        this.ballot = ballot;
    }


    /**
     * Creates new PacketBallot
     * 
     * @param binary
     *            Packet binary data
     */
    public PacketBallot(byte[] binary)
    {
        super(PacketTypes.BALLOT.getID());
        ByteBuffer data = ByteBuffer.wrap(binary);
        data.position(1);
        int size = data.getInt();
        byte[] bytes = new byte[size - 5];
        data.get(bytes, 0, size - 5);
        this.ballot = new Ballot(bytes);
    }


    /**
     * Returns the ballot transferred by this packet
     * 
     * @return
     */
    public Ballot getBallot()
    {
        return ballot;
    }


    @Override
    public byte[] getBinary()
    {
        byte[] bytes = ballot.getBytes();
        int size = 1 + 4 + bytes.length;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.put(getID());
        data.putInt(size);
        data.put(bytes);
        return data.array();
    }
}
