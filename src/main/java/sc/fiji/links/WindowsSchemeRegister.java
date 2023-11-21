package sc.fiji.links;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// This code is not working. the script keep hanging on the line: 33
//@Plugin(type = Service.class)
//extends AbstractService implements SciJavaService
public class WindowsSchemeRegister  {

    private static final String SCHEME = "fiji";

//    @Override
    public void initialize() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }
        try {

            String keyPath = "Software\\Classes\\" + SCHEME;
            String commandPath = keyPath + "\\shell\\open\\command";
            String APP_PATH = getFijiPath();

            print("APP_PATH: " + APP_PATH);
            print(String.valueOf(WinReg.HKEY_CURRENT_USER));
            boolean exists = Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, "\"Software\\Microsoft\"");
            print("Key exists: " + exists);
            // Check if the key already exists and the application path is the same
            if (Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, keyPath)) {
                print("The registry key already exists.");
                String existingPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, commandPath, "");
                if (APP_PATH.equals(existingPath)) {
                    print("The registry key already exists and the application path is the same.");
                    return;
                }
            }

            print("The registry key does not exist or the application path is different.");

            // Create registry keys and set values
            Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, keyPath);
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, keyPath, "", "URL:" + SCHEME);
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, keyPath, "URL Protocol", "");
            Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, keyPath + "\\shell");
            Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, keyPath + "\\shell\\open");
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, commandPath, "", "\"" + APP_PATH + "\" \"%1\"");

            print("Custom URI scheme added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFijiPath() throws IOException {
        String currentPath = System.getProperty("user.dir");
        File file = new File(currentPath, "ImageJ-win64.exe");
        if (file.exists())
            return file.getAbsolutePath();
        file = new File(currentPath, "ImageJ-win32.exe");
        if (file.exists())
            return file.getAbsolutePath();
        throw new IOException("ImageJ-win64.exe nor ImageJ-win32.exe was found.");
    }


    static void print(String text) {
        System.out.println(text);
        String path = System.getProperty("user.home") + "\\Desktop\\tmp.txt";
        System.out.println(path);
        try {
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
