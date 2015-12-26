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

package me.edwards.des.util;

import javax.xml.bind.DatatypeConverter;

// -----------------------------------------------------------------------------
/**
 * Utility class for converting Java data structures and primitives into byte
 * arrays, or hexadecimal Strings, or vice-versa.<br>
 * <br>
 * Created on: Oct 17, 2015 at 9:21:05 AM
 * 
 * @author Matthew Edwards
 */
public class ByteUtil
{
    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Converts an integer into a byte array
     * 
     * @param i
     *            Integer to convert
     * @return Byte array representing the integer
     */
    public static byte[] intToBytes(int i)
    {
        byte[] result = new byte[4];
        for (int j = 3; j >= 0; j--)
        {
            result[j] = (byte)(i & 0xFF);
            i >>= 8;
        }
        return result;
    }


    // -------------------------------------------------------------------------
    /**
     * Converts a byte array into an integer
     * 
     * @param bytes
     *            Byte array to convert
     * @return Integer representing the byte array
     */
    public static int bytesToInt(byte[] bytes)
    {
        int result =
            new Integer((int)(0xff & bytes[0]) << 24
                | (int)(0xff & bytes[1]) << 16 | (int)(0xff & bytes[2]) << 8
                | (int)(0xff & bytes[3]) << 0);
        return result;
    }


    // -------------------------------------------------------------------------
    /**
     * Converts a long integer into a byte array
     * 
     * @param l
     *            Long integer to convert
     * @return Byte array representing the long integer
     */
    public static byte[] longToBytes(long l)
    {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--)
        {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }


    // -------------------------------------------------------------------------
    /**
     * Converts a hexadecimal String into a byte array
     * 
     * @param hex
     *            Hex String to convert
     * @return Byte array representing the hexadecimal String
     */
    public static byte[] hexToBytes(String hex)
    {
        return DatatypeConverter.parseHexBinary(hex);
    }


    // -------------------------------------------------------------------------
    /**
     * Converts a byte array into a hexadecimal String
     * 
     * @param bytes
     *            Byte array to convert
     * @return A hexadecimal String representing the byte array
     */
    public static String bytesToHex(byte[] bytes)
    {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
