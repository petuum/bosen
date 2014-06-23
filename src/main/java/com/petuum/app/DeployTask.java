package com.petuum.app;
import ch.ethz.ssh2.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by suyuxin on 14-6-11.
 */
public class DeployTask {

    public static final String username = "petuum";
    public static final String password = "123456";

    private HashMap<Integer, String> hostMap = new HashMap<Integer, String>();
    private String remotePath = "~";
    private File packageFile;
    private File dataLibraryFile;
    private Vector<String> localRelativeFilesList = new Vector<String>();

    public DeployTask(String hostfile) throws IOException {
        //read machine list
        Iterator<String> iter = Files.readAllLines(FileSystems.getDefault().getPath(hostfile), StandardCharsets.US_ASCII).iterator();
        while(iter.hasNext()) {
            String[] line = iter.next().split(" ");
            int id = Integer.valueOf(line[0]);
            if(id ==1) {
                continue;
            }
            hostMap.put(id, line[1]);
        }
    }

    public void deploy(String mainClass) throws IOException {
        Iterator<Map.Entry<Integer, String>> iter = hostMap.entrySet().iterator();
        Session sessionClient0 = null;
        int client_id = 0;
        while(iter.hasNext()) {
            Map.Entry<Integer, String> entry = iter.next();
            Connection conn = new Connection(entry.getValue());
            conn.connect();
            if(! conn.authenticateWithPassword(username, password)) {
                throw new IOException("Authentication failed.");
            }
            //send packages
            sendPackages(conn);
            //run
	        Session session = conn.openSession();
            if(entry.getKey() == 0)
            {
                sessionClient0 = session;
            }
            String command = "export LD_LIBRARY_PATH=.:third_party/lib;java -classpath " + getClassPath() + " " + mainClass + " " + client_id++;
            session.execCommand(command);
        }
        if(sessionClient0 != null) {
            waitSession(sessionClient0);
        }
    }

    private void sendPackages(Connection conn) throws IOException {
        String home = System.getProperty("user.dir") + "/";
        //zip data and library files
        if(dataLibraryFile == null) {
            String[] localPaths = new String[3];
            localPaths[0] = home + "lib";
            localPaths[1] = home + "third_party/lib";
            localPaths[2] = home + "datasets";
            dataLibraryFile = zipLocalFiles(localPaths, "build/data_library.zip");
        }
        //zip package files
	    if(packageFile == null)
	    {
            String[] localPaths = new String[4];
            localPaths[0] = home + "build/libs";
            localPaths[1] = home + "config";
            localPaths[2] = home + "src/main/java/log4j2.xml";
            localPaths[3] = home + "machinefiles";
          	packageFile = zipLocalFiles(localPaths, "build/package.zip");
        }
	    //transfer
        SCPClient client = new SCPClient(conn);
        System.out.println("sending " + packageFile.getName() + " to " + conn.getHostname() + " ...");
        client.put(packageFile.getAbsolutePath(), remotePath);
        System.out.println("sending " + dataLibraryFile.getName() + " to " + conn.getHostname() + " ...");
        client.put(dataLibraryFile.getAbsolutePath(), remotePath);
        //unzip
        Session session = conn.openSession();
        session.execCommand("unzip -o " + packageFile.getName() + ";rm " + packageFile.getName() + ";unzip -n " + dataLibraryFile.getName());
        waitSession(session);
        session.close();
    }

    private void waitSession(Session session) {
        InputStream stdout = session.getStdout();
        InputStream stderr = session.getStderr();

        byte[] buffer = new byte[8192];
        try {
        while (true)
        {
            if ((stdout.available() == 0) && (stderr.available() == 0))
            {
                int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                        | ChannelCondition.EOF, 30000);

                if ((conditions & ChannelCondition.TIMEOUT) != 0)
                {
                    throw new IOException("Timeout while waiting for data from peer.");
                }
                if ((conditions & ChannelCondition.EOF) != 0)
                {
                    if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0)
                    {
                        break;
                    }
                }
            }

            if (stdout.available() > 0)
            {
                int len = stdout.read(buffer);
                if (len > 0)
                    System.out.write(buffer, 0, len);
            }

            if (stderr.available() > 0)
            {
                int len = stderr.read(buffer);
                if (len > 0)
                    System.err.write(buffer, 0, len);
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getClassPath() {
        String jarPath = ".";
        Iterator<String> iter = localRelativeFilesList.iterator();
        while(iter.hasNext()) {
            String filePath = iter.next();
            if(filePath.endsWith(".jar")) {
                jarPath += ":" + filePath;
            }
        }
        return jarPath;
    }

    private File zipLocalFiles(String[] localPaths, String packageName) throws IOException {
        File packageFile = new File(packageName);
        FileOutputStream fileOutput = new FileOutputStream(packageFile);
        ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);
        String home = System.getProperty("user.dir") + "/";
        //read local files
        for(int i = 0; i < localPaths.length; i++) {
            Iterator<String> fileIter = getFileListInDirectory(new File(localPaths[i])).iterator();
            while (fileIter.hasNext()) {
                File file = new File(fileIter.next());
                String relativePath = file.getAbsolutePath().replace(home, "");
                localRelativeFilesList.add(relativePath);
                if(relativePath.endsWith("log4j2.xml")) {//temp statement, need to modify in next turn
                    relativePath = "log4j2.xml";
                }
                zipOutput.putNextEntry(new ZipEntry(relativePath));
                FileInputStream fileInput = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = fileInput.read(buffer)) > 0) {
                    zipOutput.write(buffer, 0, bytesRead);
                }
                zipOutput.closeEntry();
            }
        }
        zipOutput.close();
        fileOutput.close();
        return packageFile;
    }

    private Vector<String> getFileListInDirectory(File dir) {
        Vector<String> fileList = new Vector<String>();
        if(dir.isDirectory()) {
            File[] list = dir.listFiles();
            for(int i = 0; i < list.length; i++) {
                fileList.addAll(getFileListInDirectory(list[i]));
            }
        } else {
            fileList.add(dir.getAbsolutePath());
        }
        return fileList;
    }
}
