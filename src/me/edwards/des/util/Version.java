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

import java.security.InvalidParameterException;

// -----------------------------------------------------------------------------
/**
 * Data structure representing a version for an application or class.<br>
 * <br>
 * Created on: May 28, 2015 at 11:19:51 AM
 * 
 * @author Matthew Edwards
 */
public class Version
{
    // ~ Static/Instance variables .............................................
    
    private static final int[] primes =
                                          new int[] { 3, 5, 7, 11, 13, 17, 23,
        29                               };

    private int[]              versionNum;
    private int                versionHash;
    private String             versionString;
    private String             versionToString;


    // ~ Constructors ..........................................................

    // -------------------------------------------------------------------------
    /**
     * Creates new Version (Initialized as 0.0)
     */
    public Version()
    {
        this("0.0");
    }


    // -------------------------------------------------------------------------
    /**
     * Creates new Version from parsed string.
     * 
     * @param version
     *            Version in format "12.34 Version_String" with a version number
     *            first (separated with periods), a space, and a version ID
     *            string (Alpha, etc.)
     * @throws InvalidParameterException
     *             Throws InvalidParameterException if "version" string is
     *             improperly formatted
     */
    public Version(String version)
    {
        try
        {
            String[] sec = version.split(" ");

            if (sec.length > 1)
            {
                versionString = sec[1];
            }

            String[] num = sec[0].split("\\.");

            if (num.length > 8)
            {
                throw new InvalidParameterException(
                    "Could not parse Version string (Version number too long)");
            }

            versionNum = new int[num.length];

            for (int i = 0; num.length > i; i++)
            {
                versionNum[i] = Integer.parseInt(num[i]);
            }

            versionHash = 0;
            versionToString = "";

            for (int i = 0; versionNum.length > i; i++)
            {
                versionHash += versionNum[i] * primes[versionNum.length - i];
                versionToString += versionNum[i];

                if (i < versionNum.length - 1)
                {
                    versionToString += ".";
                }
            }

            if (versionString != null)
            {
                versionToString += " " + versionString;
            }
        }
        catch (Exception e)
        {
            throw new InvalidParameterException(
                "Could not parse Version string (Misplaced character)");
        }
    }


    // ~ Methods ...............................................................

    // -------------------------------------------------------------------------
    /**
     * Compares this version and the specified version for equality.
     * 
     * @param v
     *            Version to compare
     * @return True if both versions are equal, False otherwise
     */
    public boolean isEqualTo(Version v)
    {
        return v.versionToString.equals(versionToString);
    }


    // -------------------------------------------------------------------------
    /**
     * Compares this version and the specified version to find the newer one.
     * 
     * @param v
     *            Version to compare
     * @return True if this version is newer than the specified version, False
     *         otherwise
     */
    public boolean isNewerThan(Version v)
    {
        int limit =
            v.versionNum.length > versionNum.length
                ? versionNum.length
                : v.versionNum.length;
        for (int i = 0; limit > i; i++)
        {
            if (versionNum[i] > v.versionNum[i])
            {
                return true;
            }
            else if (v.versionNum[i] > versionNum[i])
            {
                return false;
            }
        }

        return versionNum.length > v.versionNum.length;
    }


    // -------------------------------------------------------------------------
    /**
     * Compares this version and the specified version to find the older one.
     * 
     * @param v
     *            Version to compare
     * @return True if this version is older than the specified version, False
     *         otherwise
     */
    public boolean isOlderThan(Version v)
    {
        int limit =
            v.versionNum.length > versionNum.length
                ? versionNum.length
                : v.versionNum.length;
        for (int i = 0; limit > i; i++)
        {
            if (versionNum[i] < v.versionNum[i])
            {
                return true;
            }
            else if (v.versionNum[i] < versionNum[i])
            {
                return false;
            }
        }

        return versionNum.length < v.versionNum.length;
    }


    @Override
    public int hashCode()
    {
        return versionHash;
    }


    @Override
    public String toString()
    {
        return versionToString;
    }
}
