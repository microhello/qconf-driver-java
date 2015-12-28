package net.qihoo.qconf;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * the entrance class of qconf
 * @version 1.0.0
 */
public class Qconf
{
    private static final String QCONF_DRIVER_JAVA_VERSION = "1.2.0";
    static
    {
        // 1.load library
        try
        {
            Qconf.loadLib();
        }
        catch(IOException e)
        {
            System.err.println("load qconf library failed");
            System.exit(1);
        }
        // 2. env initial
        try
        {
            Qconf.init();
        }
        catch(QconfException e)
        {
            System.err.println("initial qconf environment failed");
            System.exit(1);
        }
        // 3.regist shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run()
        {
            Qconf.destroy();
        }
        });
    }
    static String getOSArch() {
        String arch = System.getProperty("os.arch");
        arch = arch.toLowerCase().trim();
        if ("i386".equals(arch) || "i686".equals(arch)) {
            arch = "x86";
        }
        else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            arch = "x86-64";
        }
        return arch;
    }
    static String getOSName() {
        String osName = System.getProperty("os.name");

        if (osName.startsWith("Linux")) {
            return "linux";
        }
        else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            return "darwin";
        }
        else if (osName.startsWith("Windows")) {
            return "win32";
        }
        return osName.toLowerCase().trim();
    }
    static String getNativeLibraryResourcePath(){
        return getOSName()+"-"+getOSArch();
    }
    private synchronized static void loadLib() throws IOException 
    {  
        String libFullName = getNativeLibraryResourcePath()+File.separator+"libqconf.so";
        InputStream in = null;
        BufferedInputStream reader;
        FileOutputStream writer = null;  

        File extractedLibFile = File.createTempFile("libqconf",".so");
        try { 
            in = Qconf.class.getResourceAsStream(File.separator + libFullName);  
            reader = new BufferedInputStream(in);
            writer = new FileOutputStream(extractedLibFile);  
            byte[] buffer = new byte[1024];  
            while (reader.read(buffer) > 0){  
                writer.write(buffer);  
                buffer = new byte[1024];  
            }  
        } catch (IOException e){  
            e.printStackTrace();  
        } finally {  
            if(in!=null)  
                in.close();  
            if(writer!=null)  
                writer.close();  
        }  
        System.load(extractedLibFile.toString());  
        if (extractedLibFile.exists())
        {
            boolean deleted = extractedLibFile.delete();
            if(!deleted){
                System.err.println("Can't delete extracted temp lib file "+extractedLibFile.toString());
            }
        }
    }  

    private static void exceptionCallback(String errmsg) throws QconfException
    {
        throw new QconfException(errmsg);
    }

    private native static int init() throws QconfException; 
    private native static int destroy();

    /**
     * get qconf java driver version<br>
     *
     * @return version
     * @since version 0.4.0
     */
    public static String version()
    {
        return QCONF_DRIVER_JAVA_VERSION;
    }
    
    /**
     * get value configure from Qconf by the key and idc<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @param idc   server room name, use 'null' if get configure from the one of current client machine
     * @return value of the configure, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public native static String getConf(String key, String idc) throws QconfException;

    /**
     * get value configure from Qconf by the key in current server room<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @return value of the configure, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public static String getConf(String key) throws QconfException
    {
        return getConf(key, null);
    }

    /**
     * get one host configure available from Qconf by the key and idc<br>
     *  it will pick one randomly if multi-host available<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @param idc   server room name, use 'null' if get configure from the one of current client machine
     * @return one host available, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public native static String getHost(String key, String idc) throws QconfException; 

    /**
     * get one host configure available from Qconf by the key in current server room<br>
     *  it will pick one randomly if multi-host available<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @return one host available, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public static String getHost(String key) throws QconfException
    {
        return getHost(key, null);
    }

    /**
     * get all the hosts available from Qconf by the key and idc<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @param idc   server room name, use 'null' if get configure from the one of current client machine
     * @return all hosts  available, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public native static ArrayList<String> getAllHost(String key, String idc) throws QconfException;

    /**
     * get all the hosts available from Qconf by the key in current server room<br>
     *  it will wait for a while if the qconf-agent not get the configure yet, at most 100 * 5 millisecond
     *
     * @param key   the key indicate one configure item
     * @return all childrent conf, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.3.1
     */
    public static ArrayList<String> getAllHost(String key) throws QconfException
    {
        return getAllHost(key, null);
    }

    /**
     * get all children conf with its value
     *
     * @param key   the key indicate one configure item
     * @param idc   server room name
     * @return all children conf, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.4.0
     */
    public native static Map<String, String> getBatchConf(String key, String idc) throws QconfException;
    /**
     * get all children conf with its value
     *
     * @param key   the key indicate one configure item
     * @return all children conf, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.4.0
     */
    public static Map<String, String> getBatchConf(String key) throws QconfException
    {
        return getBatchConf(key, null);
    }

    /**
     * get all children conf keys
     *
     * @param key   the key indicate one configure item
     * @param idc   server room name
     * @return all children keys, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.4.0
     */
    public native static ArrayList<String> getBatchKeys(String key, String idc) throws QconfException;
    /**
     * get all children keys
     *
     * @param key   the key indicate one configure item
     * @return all children keys, return null if failed
     * @exception QconfException 
     *              if any exception of qconf happend during the operation
     * @since version 0.4.0
     */
    public static ArrayList<String> getBatchKeys(String key) throws QconfException
    {
        return getBatchKeys(key, null);
    }
}
