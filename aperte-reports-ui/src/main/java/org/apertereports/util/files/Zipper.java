package org.apertereports.util.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class creates .zip resource from a file or a directory
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class Zipper {

    private final static Logger logger = LoggerFactory.getLogger(Zipper.class);

    /**
     * Creates .zip file from element pointed by source path
     *
     * @param srcPath Source
     * @param destPath Destination
     * @throws Exception
     */
    static public void zip(String srcPath, String destPath) throws Exception {

        logger.info("SRC: " + srcPath);
        logger.info("DST: " + destPath);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destPath));

        File f = new File(srcPath);
        if (f.isDirectory()) {
            addFolder(f, f.getName(), zos);
        } else {
            addFile(f, "", zos);
        }

        zos.flush();
        zos.close();
    }

    static private void addFolder(File f, String zipPath, ZipOutputStream zos) throws Exception {
        if (!f.isDirectory()) {
            throw new IllegalArgumentException("folder File object exected");
        }

        for (File e : f.listFiles()) {
            if (e.isDirectory()) {
                String newZipPath = (zipPath.length() == 0 ? "" : zipPath + File.separator) + e.getName();
                addFolder(e, newZipPath, zos);
            } else {
                addFile(e, zipPath, zos);
            }
        }
    }

    static private void addFile(File f, String zipPath, ZipOutputStream zos) throws Exception {
        if (f.isDirectory()) {
            throw new IllegalArgumentException("regular file expected");
        }

        logger.debug("adding file: " + f.getAbsolutePath() + " -> " + zipPath);
        FileInputStream in = new FileInputStream(f);
        zos.putNextEntry(new ZipEntry(zipPath + (zipPath.length() == 0 ? "" : File.separator) + f.getName()));
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            zos.write(buf, 0, len);
        }
    }
}