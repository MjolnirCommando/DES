package me.edwards.des.util;

/**
 * Utility class for converting Java data structures and primitives into byte arrays
 * Created on: Oct 17, 2015 at 9:21:05 AM
 * @author Matthew Edwards
 */
public class ByteUtil
{
    /**
     * Translates int to byte array
     * @param i Integer to translate
     * @return Byte array representing integer
     */
    public static byte[] intToBytes(int i)
    {
        byte[] result = new byte[4];
        for (int j = 3; j >= 0; j--)
        {
            result[j] = (byte) (i & 0xFF);
            i >>= 8;
        }
        return result;
    }
    
    /**
     * Translates byte array to int
     * @param data Data to translate
     * @return Integer represented by byte array
     */
    public static int bytesToInt(byte[] data)
    {
        int result = new Integer(
                (int) (0xff & data[0]) << 24 |
                (int) (0xff & data[1]) << 16 |
                (int) (0xff & data[2]) << 8 |
                (int) (0xff & data[3]) << 0
                );
        return result;
    }
    
    /**
     * Translates long to byte array
     * @param l Long integer to translate
     * @return Byte array representing long integer
     */
    public static byte[] longToBytes(long l)
    {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--)
        {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
