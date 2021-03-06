// BlockingQueueTraverseDirectory.java
// 2019-3-17

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class BlockingQueueTraverseDirectory {
    private static final int FILE_QUEUE_SIZE = 10;
    private static final int SEARCH_THREADS = 100;
    private static final File DUUMY = new File("");
    private static ConcurrentLinkedQueue<File> results = new ConcurrentLinkedQueue<>();
    private static BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);

    public static void main(String[] args) throws InterruptedException {
        // https://blog.csdn.net/u011277123/article/details/54090304
        // 使用CountDownLatch或者同步屏障或Thread.join()或Thread.sleep()等待
        // 其余线程结束再运行另一个线程

        String directory = "C:\\Study\\github\\Lookoops\\java\\CoreJava_I";
        String keyword = ".java";

        // 线程任务
        Runnable enumerator = () -> {
            try {
                enumerate(new File(directory));
                queue.put(DUUMY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        new Thread(enumerator).start();
        for (int i = 1; i <= SEARCH_THREADS; ++i) {
            Runnable searcher = () -> {
                try {
                    boolean done = false;
                    while (!done) {
                        File file = queue.take();
                        if (file == DUUMY) {
                            queue.put(file);
                            done = true;
                        } else {
                            patternSearch(file, keyword);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            new Thread(searcher).start();
        }

        System.out.println("[INFO] waiting for 5 sec to see results");
        Thread.sleep(5000);
        System.out.println(results.toString());
    }

    /**
     * 遍历文件
     * @param directory 从该目录开始遍历
     */
    public static void enumerate(File directory) throws InterruptedException {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) // 如果是目录则递归查询文件
                enumerate(file);
            else
                queue.put(file);    // 将文件放入列队
        }
    }

    /**
     * 文件中是否存在关键字
     * @param file 待匹配文件
     * @param keyword 关键词
     */
    public static void search(File file, String keyword) throws IOException {
        try (Scanner in = new Scanner(file, "utf-8")) {
            int lineNumber = 0;
            while (in.hasNextLine()) {
                lineNumber++;
                String line = in.nextLine();
                if (line.contains(keyword)) {
                    results.offer(file);
                    System.out.printf("[INFO] %s:%d:%s%n", file.getPath(), lineNumber, line);
                }
            }
        }
    }

    /**
     * 指定文件名
     * @param file 待匹配文件
     * @param keyword 关键词
     */
    public static void patternSearch(File file, String keyword) {
        try {
            String fileName = file.getName();
            if (fileName.contains(keyword)) {
                results.offer(file);
                System.out.printf("[INFO] %s\n", file.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}