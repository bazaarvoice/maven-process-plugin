package com.bazaarvoice.maven.plugin.process;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.MessageDigest;

import static org.testng.Assert.assertEquals;


public class StdOutRedirectorTest {

    @Test
    public void testRedirector(){
        ClassLoader classLoader = getClass().getClassLoader();
        String inputFile_string=classLoader.getResource("input.txt").getFile();
        String outputFile_string=System.getProperty("tmpDir") + "redirected_input.txt";
        FileReader inputFile = null;
        OutputStream outputFile = null;

        try {
            inputFile = new FileReader(inputFile_string);
            outputFile = new BufferedOutputStream(new FileOutputStream(outputFile_string));
            StdoutRedirector redirector = new StdoutRedirector(inputFile, outputFile);

            redirector.run();

            assertTextFileEquals(outputFile_string, inputFile_string);

            Files.deleteIfExists(FileSystems.getDefault().getPath(outputFile_string));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        finally {
            IOUtils.closeQuietly(inputFile);
        }
    }

    @Test
    public void testRedirector_stopped(){
        ClassLoader classLoader = getClass().getClassLoader();
        String inputFile_string=classLoader.getResource("input2.txt").getFile();
        String expectedFile_string=classLoader.getResource("empty_file.txt").getFile();
        String outputFile_string=System.getProperty("tmpDir") + "redirected_empty.txt";
        FileReader inputFile = null;
        OutputStream outputFile = null;

        try {
            inputFile = new FileReader(inputFile_string);
            outputFile = new BufferedOutputStream(new FileOutputStream(outputFile_string));
            StdoutRedirector redirector = new StdoutRedirector(inputFile, outputFile);

            redirector.stopIt();
            redirector.run();

            inputFile.close();

            assertTextFileEquals(outputFile_string, expectedFile_string);
            Files.deleteIfExists(FileSystems.getDefault().getPath(outputFile_string));

        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        finally {
            IOUtils.closeQuietly(inputFile);
        }
    }

    public void assertTextFileEquals(String actualFile_string, String expectedFile_string) throws Exception {
        MessageDigest actualMD = MessageDigest.getInstance("MD5");
           MessageDigest expectedMD = MessageDigest.getInstance("MD5");

        FileInputStream actualFileInputStream = new FileInputStream(actualFile_string);
        FileInputStream expectedFileInputStream = new FileInputStream(expectedFile_string);

        byte[] dataBytes = new byte[1024];
        int nread;

        while ((nread = actualFileInputStream.read(dataBytes)) != -1) {
            actualMD.update(dataBytes, 0, nread);
        };

        dataBytes = new byte[1024];
        while ((nread = expectedFileInputStream.read(dataBytes)) != -1) {
            expectedMD.update(dataBytes, 0, nread);
        };
        
        actualFileInputStream.close();
        expectedFileInputStream.close();

        assertEquals(actualMD.getDigestLength(), expectedMD.getDigestLength());
        assertEquals(actualMD.digest(), expectedMD.digest());
    }
}
