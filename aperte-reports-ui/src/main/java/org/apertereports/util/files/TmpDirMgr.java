package org.apertereports.util.files;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class creates simple manager for temporary directories
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class TmpDirMgr {

    private static final Logger logger = LoggerFactory.getLogger(TmpDirMgr.class);
    private File baseDir;
    private static final int MAX_TRIES_NO = 10;

    /**
     * Creates temporary directory manager. Directory
     * _user_home_dir_/_bs_dir_/tmp is used as base directory
     */
    public TmpDirMgr() {
        this(System.getProperty("user.home") + File.separator + ".BlueSoft" + File.separator + "tmp");
    }

    /**
     * Creates temporary directory manager
     *
     * @param baseDirPath Path to base dir selected for temporary resources
     */
    public TmpDirMgr(String baseDirPath) {
        this(new File(baseDirPath));
    }

    /**
     * Creates temporary directory manager
     *
     * @param baseDir File object representing base dir selected for temporary
     * resources
     */
    public TmpDirMgr(File baseDir) {
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                throw new RuntimeException("error creating base dir: " + baseDir);
            }
        }
        if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException("file " + baseDir.getAbsolutePath() + " is not a directory");
        }

        this.baseDir = baseDir;
    }

    /**
     * Tries to clear base dir
     */
    public void clearBaseDir() {
        for (File f : baseDir.listFiles()) {
            boolean deleted = f.delete();
            if (!deleted) {
                logger.warn("error while deleting base dir: " + baseDir);
            }
        }
    }

    /**
     * Creates new temporary directory
     *
     * @return Created temporary directory
     */
    public File createNewTmpDir() {
        return createNewTmpDir(null);
    }

    /**
     * Creates new temporary directory with one subdirectory
     *
     * @param subdir Subdirectory name
     * @return Created temporary directory
     */
    public File createNewTmpDir(String subdir) {
        int i = MAX_TRIES_NO;
        while (i > 0) {
            String name = "" + System.currentTimeMillis();
            File d = new File(baseDir.getAbsolutePath() + File.separator + name);
            if (!d.exists()) {
                boolean created = d.mkdirs();
                if (!created) {
                    throw new RuntimeException("error while creating dir: " + d.getAbsolutePath());
                }
                if (subdir != null && subdir.length() > 0) {
                    File sd = new File(d.getAbsolutePath() + File.separator + subdir);
                    created = sd.mkdir();
                    if (!created) {
                        throw new RuntimeException("error while creating dir: " + d.getAbsolutePath());
                    }
                }
                return d;
            }

            i--;
            try {
                Thread.sleep(2);
            } catch (Exception e) {
                logger.error("sleep error: ", e);
            }
        }
        throw new RuntimeException("cannot create tmp directory");
    }

    /**
     * Returns base directory
     *
     * @return Base directory
     */
    public File getBaseDir() {
        return baseDir;
    }
}
