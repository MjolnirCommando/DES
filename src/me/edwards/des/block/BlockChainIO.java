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

package me.edwards.des.block;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import me.edwards.des.Launcher;
import me.edwards.des.util.ByteUtil;

// -----------------------------------------------------------------------------
/**
 * Handles reading and writing of BlockChains to and from the file system.<br>
 * <br>
 * Created on: Dec 21, 2015 at 11:28:44 AM
 * 
 * @author Matthew Edwards
 */
public class BlockChainIO
{
    // ~ Static/Instance variables .............................................

    /**
     * Size of the read/write Buffer
     */
    private static final int BUFFER_SIZE = 4096;


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Saves the BlockChain to the specified file.
     * 
     * @param bc
     *            BlockChain object to save
     * @param fileName
     *            Full path of file in which to save
     * @throws IOException
     *             Thrown if file writing encounters an error
     */
    public static void save(BlockChain bc, String fileName)
        throws IOException
    {
        if (fileName.endsWith(".block"))
        {
            fileName = fileName.substring(0, fileName.length() - 6);
        }

        long time = System.currentTimeMillis();
        
        byte[][] bytes = bc.getBytes();

        Launcher.GLOBAL.info("Saving BlockChain to \"" + fileName
            + ".block\" (" + bc.getSize() + " blocks, " + bytes.length
            + " partitions)...");

        ZipOutputStream zout =
            new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                new File(fileName + ".block"))));
        for (int i = -1; bytes.length > i; i++)
        {
            BufferedOutputStream bout =
                new BufferedOutputStream(new FileOutputStream(new File(fileName
                    + ".temp")));
            ByteBuffer data;
            if (i == -1)
            {
                data = ByteBuffer.allocate(8);
                data.putInt(bc.getSize());
                data.putInt(bytes.length);
            }
            else
            {
                data = ByteBuffer.allocate(4 + bytes[i].length);
                data.putInt(bytes[i].length);
                data.put(bytes[i]);
            }
            bout.write(data.array());
            bout.close();

            zout.putNextEntry(new ZipEntry("block" + i));
            byte[] buffer = new byte[BUFFER_SIZE];
            FileInputStream in =
                new FileInputStream(new File(fileName + ".temp"));

            int len;
            while ((len = in.read(buffer)) > 0)
            {
                zout.write(buffer, 0, len);
            }
            in.close();
            new File(fileName + ".temp").delete();
        }
        zout.close();

        Launcher.GLOBAL.info("BlockChain saved in "
            + (System.currentTimeMillis() - time) / 1000 + " seconds!");
    }


    // -------------------------------------------------------------------------
    /**
     * Loads a BlockChain from the specified file.
     * 
     * @param fileName
     *            Full path of file from which to load
     * @return BlockChain loaded from file
     * @throws IOException
     *             Thrown if file reading encounters an error or the fileName
     *             contains an invalid extension. (Only ".block" accepted)
     */
    public static BlockChain load(String fileName)
        throws IOException
    {
        if (fileName.endsWith(".block"))
        {
            fileName = fileName.substring(0, fileName.length() - 6);

            Launcher.GLOBAL.info("Loading BlockChain from \"" + fileName
                + ".block\"...");
            long time = System.currentTimeMillis();

            int num = 0;
            int size = 0;

            ZipInputStream zin =
                new ZipInputStream(new BufferedInputStream(new FileInputStream(
                    new File(fileName + ".block"))));
            byte[][] bytes = null;
            for (int i = -1; num > i; i++)
            {
                BufferedOutputStream bout =
                    new BufferedOutputStream(new FileOutputStream(new File(
                        fileName + ".temp")));
                zin.getNextEntry();
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = zin.read(buffer)) > 0)
                {
                    bout.write(buffer, 0, len);
                }
                bout.close();

                FileInputStream fin =
                    new FileInputStream(new File(fileName + ".temp"));
                BufferedInputStream bis = new BufferedInputStream(fin);

                if (i == -1)
                {
                    byte[] bin = new byte[8];
                    bis.read(bin);
                    ByteBuffer data = ByteBuffer.wrap(bin);
                    size = data.getInt();
                    num = data.getInt();
                    Launcher.GLOBAL.info("Found " + size + " blocks and " + num
                        + " partitions...");
                    bytes = new byte[num][];
                }
                else
                {
                    byte[] binI = new byte[4];
                    bis.read(binI);

                    byte[] bin = new byte[ByteUtil.bytesToInt(binI)];
                    bis.read(bin);
                    bytes[i] = bin;
                }

                bis.close();
                fin.close();

                new File(fileName + ".temp").delete();
            }
            zin.close();

            BlockChain bc = new BlockChain(size, bytes);
            Launcher.GLOBAL.info("BlockChain loaded in "
                + (System.currentTimeMillis() - time) / 1000 + " seconds!");
            return bc;
        }
        else
        {
            throw new IOException("Invalid File Extension");
        }
    }
}
