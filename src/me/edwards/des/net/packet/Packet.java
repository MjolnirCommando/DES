package me.edwards.des.net.packet;

import java.text.NumberFormat;
import me.edwards.des.Node;

// -----------------------------------------------------------------------------
/**
 * Handles packaging and unpackaging of Packets sent between {@link Node Nodes}
 * in the DES system.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:08:41 AM
 * 
 * @author Matthew Edwards
 */
public abstract class Packet
{
    // ~ Static/Instance variables .............................................

    // -------------------------------------------------------------------------
    private static final char[] HEX   = "0123456789ABCDEF".toCharArray();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                          + "abcdefghijklmnopqrstuvwxyz"
                                          + "0123456789!@#$%^&*()-_=+"
                                          + "[]{}\\/|:;\"'<>,.?~` ";

    
    // -------------------------------------------------------------------------
    private byte                id;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Initializes a new Packet using an ID defined in {@linkplain PacketTypes}.
     * 
     * @param id
     *            ID of Packet
     */
    public Packet(byte id)
    {
        this.id = id;
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Returns this Packet's ID.
     * 
     * @see PacketTypes
     * @return Packet Type ID
     */
    public byte getID()
    {
        return id;
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the binary payload of this Packet as a byte array.
     * 
     * @return Binary payload as a byte array
     */
    public abstract byte[] getBinary();


    // -------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return toString(getBinary());
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the String representation of a Packet's binary data.
     * 
     * @param data Packet binary payload as byte array
     * @return String representation of binary payload (Byte dump)
     */
    public static String toString(byte[] data)
    {
        StringBuffer out = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(4);
        int line = 0;
        int index = 0;

        out.append(nf.format(line++) + ": 0x" + toHex(data[index++]) + "  "
            + toHex(data[index++]) + toHex(data[index++]) + " "
            + toHex(data[index++]) + toHex(data[index++]));
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

                String c = (char)data[index] + "";

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


    // -------------------------------------------------------------------------
    /**
     * Returns a byte as a 2-digit hexadecimal String
     * 
     * @param b
     *            Byte to convert
     * @return 2-digit hexadecimal String representation of converted byte
     */
    public static String toHex(byte b)
    {
        return HEX[(b & 0xF0) >> 4] + "" + HEX[b & 0x0F];
    }


    // -------------------------------------------------------------------------
    /**
     * Value used to automatically assign unique integer ID's to Packet Types.
     */
    private static int total = 0;


    // -------------------------------------------------------------------------
    /**
     * Enumerates all the types of Packets and their ID's.<br>
     * <br>
     * Created on: Jan 4, 2015 at 10:50:45 AM
     * 
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
         * 
         * @see PacketPing
         */
        PING(),

        /**
         * Pong Packet Type, used to ensure that connections are alive
         * 
         * @see PacketPong
         */
        PONG(),

        /**
         * Version Packet Type, used for handshake protocol
         * 
         * @see PacketVersion
         */
        VERSION(),

        /**
         * Version Acknowledge Packet Type, used to complete handshake protocol
         * 
         * @see PacketVerack
         */
        VERACK(),

        /**
         * Get Address Packet Type, used to request address information
         * 
         * @see PacketGetAddr
         */
        GETADDR(),

        /**
         * Address Packet Type, used to send information about all known peers
         * 
         * @see PacketAddr
         */
        ADDR(),

        /**
         * Inventory Packet Type, advertises knowledge of a particular piece of
         * data
         * 
         * @see PacketInv
         */
        INV(),

        /**
         * Not Found Packet Type, signals that a specific piece of data that was
         * requested could not be found
         * 
         * @see PacketNotFound
         */
        NOTFOUND(),

        /**
         * Get Data Packet Type, used to request a particular piece of data
         * 
         * @see PacketGetData
         */
        GETDATA(),

        /**
         * Ballot Packet Type, used to transfer ballot information between nodes
         * 
         * @see PacketBallot
         */
        BALLOT(),

        /**
         * Block Packet Type, used to transfer block information between nodes
         * 
         * @see PacketBlock
         */
        BLOCK(),

        /**
         * Get Blocks Packet Type, used to request blocks
         * 
         * @see PacketGetBlocks
         */
        GETBLOCKS();

        // ~ Static/Instance variables .........................................

        // ---------------------------------------------------------------------
        private byte id;


        // ~ Constructors ......................................................

        // ---------------------------------------------------------------------
        private PacketTypes()
        {
            this.id = (byte) total++;
        }


        // ~ Methods ...........................................................

        // ---------------------------------------------------------------------
        /**
         * Returns the Packet Type ID
         * 
         * @return Packet Type ID
         */
        public byte getID()
        {
            return id;
        }
    }


    // -------------------------------------------------------------------------
    /**
     * Returns the {@link PacketTypes Packet Type} from a numeric Type ID.
     * 
     * @param id
     *            Packet Type ID
     * @return PacketTypes object corresponding to the Packet Type ID. If the ID
     *         can not be found, the {@link PacketTypes#INVALID Invalid Packet
     *         Type} is returned
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
