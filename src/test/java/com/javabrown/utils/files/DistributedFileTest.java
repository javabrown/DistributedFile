package com.javabrown.utils.files;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistributedFileTest {

    @Test
    public void testReadWriteOperations() throws IOException, NoSuchAlgorithmException {
        // Set up test data
        String inputFilePath = "src/test/resources/testFile.txt";
        String outputDirectory = "target/testOutput/";
        String outputFilePath = "target/testOutput/reconstructedFile.txt";

        // Clean up previous test output, if any
        cleanupTestOutput(outputDirectory);

        // Run write operation
        DistributedFile.main(new String[]{"write", inputFilePath, outputDirectory});

        // Run read operation
        DistributedFile.main(new String[]{"read", outputDirectory, outputFilePath});

        // Verify output file exists and has the same content as the input file
        assertTrue(Files.exists(Paths.get(outputFilePath)));
        assertTrue(Files.readAllLines(Paths.get(inputFilePath)).equals(Files.readAllLines(Paths.get(outputFilePath))));

        // Clean up test output
        cleanupTestOutput(outputDirectory);
    }

    private void cleanupTestOutput(String outputDirectory) throws IOException {
        Path outputDirPath = Paths.get(outputDirectory);
        if (Files.exists(outputDirPath)) {
            Files.walk(outputDirPath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
