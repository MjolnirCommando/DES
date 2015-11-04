package me.edwards.des.util;

import java.security.InvalidParameterException;

/**
 * Data structure representing a version for an application or class
 * Created on: May 28, 2015 at 11:19:51 AM
 * @author Matthew Edwards
 */
public class Version
{
    private static final int[] primes = new int[] {3, 5, 7, 11, 13, 17, 23, 29};
    
    private int[] versionNum;
    private int versionHash;
    private String versionString;
    private String versionToString;
    
    /**
     * Creates new version (0.0)
     */
    public Version()
    {
        this("0.0");
    }
    
    /**
     * Creates new Version from parsed string
     * @param version Version in format "12.34 Version_String" with a version 
     * number first (seperated with periods), a space, and a version ID string (Alpha, etc.)
     * @throws InvalidParameterException Throws InvalidParameterException if 
     * "version" string is improperly formatted
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
                throw new InvalidParameterException("Could not parse Version string (Version number too long)");
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
            throw new InvalidParameterException("Could not parse Version string (Misplaced character)");
        }
    }
    
    /**
     * Returns true if both versions are equal
     * @param v
     * @return
     */
    public boolean isEqualTo(Version v)
    {
        return v.versionToString.equals(versionToString);
    }
    
    /**
     * Returns true if this version is newer than the specified version
     * @param v
     * @return
     */
    public boolean isNewerThan(Version v)
    {
        int limit = v.versionNum.length > versionNum.length ? versionNum.length : v.versionNum.length;
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
    
    /**
     * Returns true if this version is older than the specified version
     * @param v
     * @return
     */
    public boolean isOlderThan(Version v)
    {
        int limit = v.versionNum.length > versionNum.length ? versionNum.length : v.versionNum.length;
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
