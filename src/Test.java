import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

class Config {
    private String homework;
    private String description;
    private List<TestType> testTypes;

    public String getHomework() {
        return homework;
    }

    public void setHomework(String homework) {
        this.homework = homework;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestType> getTestTypes() {
        return testTypes;
    }

    public void setTestTypes(List<TestType> testTypes) {
        this.testTypes = testTypes;
    }
}

class TestType {
    private Integer score;
    private String type;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

public class Test {
    private static final String IN_FOLDER = "in/";
    private static final String REF_FOLDER = "ref/";
    private static final String CHECKER_RESOURCES_FOLDER = "checker/resources/";
    private static final File TEST_INPUTS_FILE = new File(CHECKER_RESOURCES_FOLDER + IN_FOLDER);

    private static final String OUT_FILE = "results.out";
    private static final File TEST_OUT_FILE = new File(OUT_FILE);

    private static final File CONFIG_FILE = new File(CHECKER_RESOURCES_FOLDER + "config.json");

    private static final int MAX_MILLISECONDS_PER_TEST = 1000;

    private static int score = 0;
    private static int totalScore = 0;

    public static void main(String[] argv) {
        runTests();
        preTestCleanUp();
    }

    private static Config loadConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(CONFIG_FILE, Config.class);
        } catch (IOException e) {
            System.out.println("Could not find config file.");
            System.exit(-1);
        }

        return null;
    }

    private static void runTests() {
        Config config = loadConfig();

        for (final File testFile: Objects.requireNonNull(TEST_INPUTS_FILE.listFiles())) {
            String testFileName = testFile.getName();

            preTestCleanUp();

            final String[] testArgv = createTestArgv(testFile);
            final Future<Object> future = createTimerTask(testArgv);

            runTest(testFileName, config, future);
        }

        System.out.println("Total score: .......................... " + score +"/" + totalScore);
        System.exit(0);
    }

    private static void runTest(String testFileName, Config config, Future<Object> task) {
        ObjectMapper objectMapper = new ObjectMapper();
        File refFile = new File(CHECKER_RESOURCES_FOLDER + REF_FOLDER + testFileName);

        try {
            task.get(MAX_MILLISECONDS_PER_TEST, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            printMessage(testFileName, "Timeout");
            return;
        } catch (Exception e) {
            printMessage(testFileName, "Program ended with exception: " + e.getMessage());
            return;
        } finally {
            task.cancel(true);
        }

        if (!TEST_OUT_FILE.exists()) {
            printMessage(testFileName, "Output file not found. Skipping test...");
        } else {
            try {
                var actual = objectMapper.readTree(TEST_OUT_FILE);
                var expected = objectMapper.readTree(refFile);

                final int testScore = testMaxScore(config, testFileName);
                totalScore += testScore;

                if (expected.equals(actual)) {
                    score += testScore;
                    printMessage(testFileName, Integer.toString(score), true);
                } else {
                    printMessage(testFileName, Integer.toString(0), true);
                }
            } catch(IOException e) {
                printMessage(testFileName, "Output file badly formatted. Skipping test...");
            }
        }
    }

    private static Future<Object> createTimerTask(final String[] argv) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() throws Exception {
                Main.main(argv);
                return null;
            }
        };

        return executor.submit(task);
    }

    private static String[] createTestArgv(File testFile) {
        List<String> listArgv = new ArrayList<>();
        listArgv.add(testFile.getAbsolutePath());
        listArgv.add(OUT_FILE);
        String[] argv = new String[0];
        return listArgv.toArray(argv);
    }

    private static void preTestCleanUp() {
        TEST_OUT_FILE.delete();
    }

    private static void printMessage(String testFileName, String message) {
        printMessage(testFileName, message, false);
    }

    private static void printMessage(String testFileName, String message, boolean trail) {
        String fileName = testFileName.split("\\.")[0];
        if(trail) {
            System.out.println("[" + fileName + "]: ..................... " + message);
        } else {
            System.out.println("[" + fileName + "]: " + message);
        }
    }

    private static int testMaxScore(Config config, String testFileName) {
        for (TestType testType: config.getTestTypes()) {
            if (testFileName.contains(testType.getType())) {
                return testType.getScore();
            }
        }

        printMessage(testFileName, "Test score not found. Skipping test...");
        return 0;
    }
}
