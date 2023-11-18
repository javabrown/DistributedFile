package com.javabrown.utils.files;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class DistributedFile {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar distributed-file.jar read <inputDirectory> <outputFilePath>");
            System.out.println("       java -jar distributed-file.jar write <inputFilePath> <outputDirectory>");
            System.exit(1);
        }

        String operation = args[0].toLowerCase();
        String inputPath = args[1];
        String outputPath = args[2];

        try {
            if ("read".equals(operation)) {
                readFiles(inputPath, outputPath);
            } else if ("write".equals(operation)) {
                writeFiles(inputPath, outputPath);
            } else {
                System.out.println("Invalid operation. Use 'read' or 'write'.");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void readFiles(String inputDirectory, String outputFilePath)
            throws IOException, NoSuchAlgorithmException {

        List<File> files = Files.walk(Paths.get(inputDirectory))
                .filter(Files::isRegularFile)
                .collect(java.util.stream.Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (File file : files) {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            outputStream.write(fileBytes);
        }

        byte[] joinedBytes = outputStream.toByteArray();
        byte[] hash = getHash(joinedBytes);

        if (verifyHash(inputDirectory, hash)) {
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                fos.write(joinedBytes);
            }
            System.out.println("File successfully read and written.");
        } else {
            System.out.println("Hash verification failed. The files may have been tampered with.");
        }
    }

    private static void writeFiles(String inputFilePath, String outputDirectory)
            throws IOException, NoSuchAlgorithmException {

        File inputFile = new File(inputFilePath);

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int partNumber = 1;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] hash = getHash(buffer);
                String subFolder = outputDirectory + bytesToHex(hash) + "/";
                String outputFile = subFolder + "part" + partNumber + ".dat";

                createFolder(subFolder);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(buffer, 0, bytesRead);
                }

                partNumber++;
            }
            System.out.println("File successfully split and stored.");
        }
    }

    private static byte[] getHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    private static boolean verifyHash(String inputDirectory, byte[] expectedHash)
            throws NoSuchAlgorithmException, IOException {
        for (File file : Files.walk(Paths.get(inputDirectory))
                .filter(Files::isRegularFile)
                .collect(java.util.stream.Collectors.toList())) {

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] actualHash = getHash(fileBytes);

            if (!Arrays.equals(actualHash, expectedHash)) {
                return false;
            }
        }
        return true;
    }

    private static void createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
