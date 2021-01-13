import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> pathsOfJavaClasses;
        try (Stream<Path> paths = Files.walk(Paths.get("C:\\Study\\2BALASHOVMO-171\\2BALASHOVMO-171"))) {
            pathsOfJavaClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
        CountDownLatch countDownLatch = new CountDownLatch(pathsOfJavaClasses.size());
        for (Path path : pathsOfJavaClasses) {
            new MyThread("Thread #" + path, countDownLatch).run(path);
        }

        countDownLatch.await();
    }


    static class MyThread extends Thread {

        private final CountDownLatch countDownLatch;

        public MyThread(String s, CountDownLatch countDownLatch) {
            super(s);
            this.countDownLatch = countDownLatch;
        }

        public void run(Path path) {
            Map<String, List<String>> classesWithInheritors = new HashMap<>();
            try {
                Files.lines(path).forEach(s -> {
                    int indexOfClass = s.indexOf(" class ");
                    if (indexOfClass == -1) {
                        indexOfClass = s.indexOf(" interface ");
                    }
                    if (indexOfClass != -1) {
                        int index = s.indexOf(" extends ");
                        if (index != -1) {
                            String[] lineWithClassDeclaration = s.substring(indexOfClass).replaceAll("[^A-Za-z0-9\\p{L}$]", " ").split("\\s");
                            String child = lineWithClassDeclaration[2];
                            for (int i = 3; i < lineWithClassDeclaration.length; i++) {
                                String parent = lineWithClassDeclaration[i];
                                if (!parent.equals("extends") && !parent.equals("implements") && !parent.equals("")) {
                                    ArrayList<String> inheritors = (ArrayList<String>) classesWithInheritors.getOrDefault(parent, new ArrayList<>());
                                    inheritors.add(child);
                                    classesWithInheritors.put(parent, inheritors);
                                }
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            classesWithInheritors.forEach((s, strings) -> System.out.println(s + ": " + strings.toString()));
            countDownLatch.countDown();
        }
    }

}
