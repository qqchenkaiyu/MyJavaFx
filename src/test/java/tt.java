import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class tt {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        // numbers.parallelStream() .forEach(num->System.out.println(Thread.currentThread().getName()+">>"+num));
        numbers.stream().forEach(num -> {
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + ">>" + num);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        Thread.sleep(5000);

    }

    @Test
    public void tt() throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession("root", "192.168.110.129", 22);
        session.setConfig("StrictHostKeyChecking", "no");
        //    java.util.Properties config = new java.util.Properties();
        //   config.put("StrictHostKeyChecking", "no");
        session.setPassword("qq634691");
        session.connect();

        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        channelShell.connect();
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                    IOUtils.copy(channelShell.getInputStream(), System.out);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        // channelShell.setCommand("su root");
        PrintWriter printWriter = new PrintWriter(channelShell.getOutputStream());
        printWriter.println("tcpdump -i any   -w /home/chenkaiyu/抓包.cap");
        printWriter.flush();
        Thread.sleep(5000);
        // Thread.sleep(1000);
        printWriter.println("exit");//加上个就是为了，结束本次交互
        printWriter.flush();
//        CompletableFuture.runAsync(()->{
//            while (true){
//                try {
//                    Thread.sleep(1000);
//                channelShell.getOutputStream().write("ll".getBytes());
//                channelShell.getOutputStream().flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//      /  });
        //   channelShell.getInputStream().transferTo(System.out);

        // Thread.sleep(999999);
        //  channelShell.disconnect();
        // session.disconnect();
    }
}
