package sc.fiji.links;

import org.scijava.Priority;
import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

@Plugin(type = ConsoleArgument.class, priority = Priority.VERY_HIGH)
public class FijiSchemeArgumentHandleExample extends AbstractConsoleArgument {

    @Override
    public void handle(final LinkedList<String> args) {
        print("Got handel request");
        print(String.join(" ", args));

        // Do something with URL...
//        if (!supports(args)) return;
//        String url = args.removeFirst(); // --run
    }

    @Override
    public boolean supports(final LinkedList<String> args) {
        print("check support");
        print(String.join(" ", args));
        return args.size() > 0 && args.get(0).startsWith("fiji:");
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
