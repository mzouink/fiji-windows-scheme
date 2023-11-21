package sc.fiji.links;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Plugin(type = Service.class)
public class WindowsSchemeRegisterUsingCommand extends AbstractService implements SciJavaService {

    private static final String SCHEME = "fiji";
    private static final long TIMEOUT_SECONDS = 100;

    @Override
    public void initialize() {
        new Thread(() -> {
            try {
//            HKCU = HKEY_CURRENT_USER mean using the current user registry, no need for admin rights
                String keyPath = "HKCU\\Software\\Classes\\" + SCHEME;
                String commandPath = keyPath + "\\shell\\open\\command";
                String APP_PATH = getFijiPath();
                // Check if the key already exists and the application path is the same
                if (alreadyExists(keyPath)) {
                    String existingPath = getRegistryValue(commandPath);
                    if (APP_PATH.equals(existingPath)) {
                        return;
                    }
                }
                System.out.println("The registry key does not exist or the application path is different.");
                String[][] commands = {
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\"", "/f"},
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\"", "/ve", "/d", "\"URL:" + SCHEME + "\"", "/f"},
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\"", "/v", "\"URL Protocol\"", "/f"},
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\\shell\"", "/f"},
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\\shell\\open\"", "/f"},
                        {"reg", "add", "\"HKCU\\Software\\Classes\\" + SCHEME + "\\shell\\open\\command\"", "/ve", "/d", "\"" + APP_PATH + " %1\"", "/f"}
                };

                for (String[] command : commands) {
                    System.out.println(String.join(" ", command));
                    if (!executeCommand(command)) {
                        System.out.println("Command " + String.join(" ", command) + " failed.");
                        return;
                    }
                    System.out.println("Command " + String.join(" ", command) + " executed successfully.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }
    }

    private static boolean executeCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.MILLISECONDS);
            if (!finished) {
                System.out.println("Command timed out.");
                return false;
            }
            System.out.println("Command finished.");
            int result = process.exitValue();
            if (result == 0) {
                System.out.println("Command successful.");
                return true;
            } else {
                System.out.println("Command failed.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return false;
        }
    }

    private static String[] deleteSchemeCommand(String scheme) {
        return new String[]{"reg", "delete", "\"HKCU\\Software\\Classes\\" + scheme + "\"", "/f"};
    }


    private static String getRegistryValue(String keyPath) {
        try {
            String command = "reg query \"" + keyPath + "\" /ve";
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.MILLISECONDS);
            if (!finished) {
                return "";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("REG_SZ") || line.contains("REG_EXPAND_SZ")) {
                    String currentPath = line.substring(line.lastIndexOf("    ") + 4).trim().replace(" %1", "");
                    return currentPath;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private boolean alreadyExists(String keyPath) {
        String[] command = {"reg", "query", keyPath};
        return executeCommand(command);
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
}
