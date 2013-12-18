package com.sysgears.grain;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Site launcher, that checks for classpath being up to day and if it is the case,
 * launches Grain, otherwise exits with non-zero error code.
 */
public class SiteLauncher {
    
    /** Dependency file */
    private File dependencyFile;
    
    /** Requested Grain version */
    private String grainVersion;
    
    /** Grain snapshots repo URL */
    private String snapshotsRepoUrl;
    
    /** Cmdline arguments passed to Grain */
    private String[] grainArgs = new String[0];

    /**
     * Entry point into site launcher.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args) throws Throwable {
        final SiteLauncher launcher = new SiteLauncher();
        launcher.run(args);        
    }

    /**
     * Checks for classpath being up to day and if it is the case,
     * launches Grain, otherwise exits with non-zero error code.
     * 
     * @param args command line arguments
     */
    private void run(final String[] args) throws Throwable {
        parseCmdline(args);

        final BufferedReader br = new BufferedReader(new FileReader(dependencyFile));
        final Set<File> dependencies = new HashSet<File>();
        File grainJar = null;
        try {
            String line;
            
            line = br.readLine();
            grainJar = new File(line);
            dependencies.add(grainJar);
            
            while ((line = br.readLine()) != null) {
                dependencies.add(new File(line));
            }
        } finally {
            try {
                br.close();
            } catch (Throwable ignored) {
            }
        }
        dependencies.add(new File("build.gradle"));
        
        final long lastModified = dependencyFile.lastModified();
        int exitValue = 0;

        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        for (File dependency : dependencies) {
            if (!dependency.exists() || dependency.lastModified() > lastModified) {
                exitValue = 2;
                break;
            }
            if (!dependency.equals(grainJar)) {
                method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{dependency.toURI().toURL()});
            }
        }
        
        if (exitValue == 0 && grainVersion.endsWith("-SNAPSHOT")) {
            try {
                String metadata = downloadFromRepo("maven-metadata.xml");
                Matcher m1 = Pattern.compile("<timestamp>([0-9\\.]+)").matcher(metadata);
                Matcher m2 = Pattern.compile("<buildNumber>([0-9\\.]+)").matcher(metadata);
                if (!m1.find() || !m2.find()) {
                    throw new RuntimeException("Wrong metadata file on snapshots repo:\n" + metadata);
                } else {
                    String timestamp = m1.group(1);
                    String buildNumber = m2.group(1);
                    String jarMd5 = downloadFromRepo("grain-" + grainVersion.replaceAll("-SNAPSHOT", "") + "-" + timestamp + "-" + buildNumber + ".jar.md5");
                    exitValue = jarMd5.equals(calculateMD5(grainJar)) ? 0 : 2;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(0);
            }
        }
        
        if (exitValue == 0) {
            Method main = Class.forName("com.sysgears.grain.Main").getMethod("main", String[].class);
            main.invoke(null, (Object)grainArgs);
        } else {
            System.exit(exitValue);
        }
    }

    /**
     * Calculates MD5 checksum of a file.
     * 
     * @param file a file 
     * 
     * @return MD5 checksum
     */
    private String calculateMD5(File file) throws Throwable {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[4096];

        int count;

        while ((count = fis.read(buf)) != -1) {
            md.update(buf, 0, count);
        }

        byte[] bytes = md.digest();

        StringBuilder sb = new StringBuilder("");
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }

    /**
     * Downloads file from Grain snapshot repository.
     * 
     * @param filename filename
     *                 
     * @return filename text
     * 
     * @throws Throwable in case of error
     */
    private String downloadFromRepo(String filename) throws Throwable {
        final URL url = new URL(snapshotsRepoUrl + "com/sysgears/grain/grain/" + grainVersion + "/" + filename);
        final HttpURLConnection huc = (HttpURLConnection)url.openConnection();
        huc.setConnectTimeout(3000);
        huc.setReadTimeout(3000);
        huc.connect();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        BufferedInputStream bis = new BufferedInputStream(huc.getInputStream());
        try {
            int count;
            while ((count = bis.read(buf, 0, buf.length)) != -1)
            {
                bos.write(buf, 0, count);
            }
        } finally {
            bis.close();
        }
        return new String(bos.toByteArray(), "UTF-8");
    }  

    /**
     * Parses command line arguments and sets corresponding class fields.
     * 
     * @param args command line arguments
     */
    private void parseCmdline(final String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        } else {
            grainVersion = args[0];
            dependencyFile = new File(".site-" + grainVersion + ".dep");
            if (!dependencyFile.exists()) {
                System.err.println("Dependency file " + dependencyFile + " not found");
                printUsage();
                System.exit(1);
            }
            if (args.length > 1 && !args[1].equals("--")) {
                snapshotsRepoUrl = args[1];
            } else {
                snapshotsRepoUrl = "http://repo.sysgears.com/snapshots/";
            }
            int idx;
            for (idx = 1; idx < args.length; idx++) {
                if (args[idx].equals("--")) {
                    grainArgs = new String[args.length - (idx + 1)];
                    System.arraycopy(args, idx + 1, grainArgs, 0, grainArgs.length);
                }
            }
        }
    }

    /**
     * Prints command-line usage of SiteLauncher.
     */
    private void printUsage() {
        System.out.println("Syntax: " + this.getClass().getCanonicalName() + " grain_version [Grain snapshots repo url] -- grain_args");
    }
}
