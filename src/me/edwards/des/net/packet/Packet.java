package me.edwards.des.net.packet;

import java.text.NumberFormat;

/**
 * Class handling packaging and unpackaging of Packets in the DES system.
 * Created on: Oct 17, 2015 at 9:08:41 AM
 * @author Matthew Edwards
 */
public abstract class Packet
{
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789!@#$%^&*()-_=+"
            + "[]{}\\/|:;\"'<>,.?~` ";
    
    private byte id;
    
    /**
     * Creates new Packet from a PacketType
     * @param id ID of Packet
     */
    public Packet(byte id)
    {
        this.id = id;
    }
    
    /**
     * Returns this Packet's id
     * @return Packet Type ID
     */
    public byte getID()
    {
        return id;
    }
    
    /**
     * Returns the binary data payload of this Packet
     * @return
     */
    public abstract byte[] getBinary();
    
    @Override
    public String toString()
    {
        return toString(getBinary());
    }
    
    /**
     * Returns the string representation of binary packet data
     * @param data
     * @return
     */
    public static String toString(byte[] data)
    {
        StringBuffer out = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(4);
        int line = 0;
        int index = 0;
        
        out.append(nf.format(line++) + ": 0x" + toHex(data[index++])
                + "  " + toHex(data[index++]) + toHex(data[index++])
                + " " + toHex(data[index++]) + toHex(data[index++]));
        out.append("                         HEADER\n");
        
        while (index < data.length)
        {
            out.append(nf.format(line++) + ": ");
            
            StringBuffer chars = new StringBuffer();
            
            for (int i = 0; 16 > i; i++)
            {
                if (index >= data.length)
                {
                    for (int j = 0; 16 - i > j; j++)
                    {
                        out.append("  ");
                        
                        if (j % 2 != 0)
                        {
                            out.append(" ");
                        }
                    }
                    if ((16 - i) % 2 != 0)
                    {
                        out.append(" ");
                    }
                    break;
                }
                
                String c = (char) data[index] + "";
                
                if (CHARS.contains(c))
                {
                    chars.append(c);
                }
                else
                {
                    chars.append(".");
                }

                out.append(toHex(data[index++]));
                
                if (i % 2 != 0)
                {
                    out.append(" ");
                }
            }
            
            out.append(chars.toString() + "\n");
        }
        out.deleteCharAt(out.length() - 1);
        
        return out.toString();
    }
    
    /**
     * Returns a byte as a Hex String
     * @param b byte
     * @return Hex String
     */
    public static String toHex(byte b)
    {
        return HEX[(b & 0xF0) >> 4] + "" + HEX[b & 0x0F];
    }

    private static int total = 0;
    
    /**
     * Types of Packets
     * Created on: Jan 4, 2015 at 10:50:45 AM
     * @author Matthew Edwards
     */
    public static enum PacketTypes
    {
        /**
         * Invalid Packet Type, used for internal purposes
         */
        INVALID(),
        
        /**
         * Ping Packet Type, used to ensure that connections are alive
         */
        PING(),
        
        /**
         * Pong Packet Type, used to ensure that connections are alive
         */
        PONG(),
        
        /**
         * Version Packet Type, used for handshake protocol
         */
        VERSION(),
        
        /**
         * Version Acknowledge Packet Type, used to complete handshake protocol
         */
        VERACK(),
        
        /**
         * Get Address Packet Type, used to request address information
         */
        GETADDR(),
        
        /**
         * Address Packet Type, used to send information about all known peers
         */
        ADDR(),
        
        /**
         * Inventory Packet Type, advertises knowledge of a particular piece of data
         */
        INV(),
        
        /**
         * Not Found Packet Type, signals that a specific piece of data that was requested could not be found
         */
        NOTFOUND(),
        
        /**
         * Get Data Packet Type, used to request a particular piece of data
         */
        GETDATA(),
        
        /**
         * Ballot Packet Type, used to transfer ballot information between nodes
         */
        BALLOT(),
        
        /**
         * Block Packet Type, used to transfer block information between nodes
         */
        BLOCK(),

        /**
         * Get Blocks Packet Type, used to request blocks
         */
        GETBLOCKS();
        
        private byte id;
        
        private PacketTypes()
        {
            this.id = (byte) total++;
        }
        
        /**
         * Returns the Type ID
         * @return
         */
        public byte getID()
        {
            return id;
        }
    }

    /**
     * Returns the packet type from a numeric Type ID
     * @param id
     * @return
     */
    public static PacketTypes lookup(byte id)
    {
        for (PacketTypes p : PacketTypes.values())
        {
            if (p.getID() == id)
            {
                return p;
            }
        }
        return PacketTypes.INVALID;
    }
}
