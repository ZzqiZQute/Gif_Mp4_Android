package zz.app.gif2mp4;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;

public class Utils {
    static {
        System.loadLibrary("x264");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avdevice-58");
        System.loadLibrary("postproc-55");
        System.loadLibrary("swscale-5");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("avutil-56");
        System.loadLibrary("avfilter-7");
        System.loadLibrary("zzutils");
    }

    public static final int SORTTYPE_TIME = 0;
    public static final int SORTTYPE_SIZE = 1;
    public static final int SORTTYPE_NAME = 2;

    public static final int GIF2MP4_UNKNOWN_ERROR = 0;
    public static final int GIF2MP4_H264_NOTFOUND = 1;
    public static final int GIF2MP4_NOT_ACTION = 2;
    public static final int SUCCESSCODE = 233;

    private static final String TAG = "Utils";

    public static int getProgress2() {
        return progress2;
    }

    public static void setProgress2(int progress2) {
        Utils.progress2 = progress2;
    }

    private static int progress2;

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            ret.append(HEX_DIGITS[(b >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[b & 0x0f]);
        }
        return ret.toString();
    }

    public static ArrayList<File> getFiles(String type, int sortType, boolean ascending, boolean excepterrorgif) {
        ArrayList<File> files;
        files = new ArrayList<>(FileUtils.listFiles(Environment.getExternalStorageDirectory(), FileFilterUtils.suffixFileFilter("gif"), DirectoryFileFilter.INSTANCE));
        sortFiles(files, sortType, ascending);
        if (excepterrorgif) {
            files = exceptErrorGif(files);
        }
        return files;
    }

    private static ArrayList<File> exceptErrorGif(ArrayList<File> files) {
        ArrayList<File> ret = new ArrayList<>();
        for (File file : files) {
            if (checkgif(file.getAbsolutePath())) {
                ret.add(file);
            }
        }
        return ret;
    }

    private static void sortFiles(ArrayList<File> files, int type, boolean ascending) {
        if (type == SORTTYPE_TIME) {
            for (int i = 1; i < files.size(); i++) {
                int k = -1;
                for (int j = i; j >= 0; j--) {
                    if (ascending) {
                        if (files.get(j).lastModified() > files.get(i).lastModified()) {
                            k = j;
                        }
                    } else {
                        if (files.get(j).lastModified() < files.get(i).lastModified()) {
                            k = j;
                        }
                    }
                }
                if (k != -1) {
                    File f = files.get(i);
                    for (int m = i; m > k; m--) {
                        files.set(m, files.get(m - 1));
                    }
                    files.set(k, f);
                }
            }
        } else if (type == SORTTYPE_NAME) {
            for (int i = 1; i < files.size(); i++) {
                int k = -1;
                for (int j = i; j >= 0; j--) {
                    if (ascending) {
                        if (files.get(j).getName().compareTo(files.get(i).getName()) > 0) {
                            k = j;
                        }
                    } else {
                        if (files.get(j).getName().compareTo(files.get(i).getName()) < 0) {
                            k = j;
                        }
                    }
                }
                if (k != -1) {
                    File f = files.get(i);
                    for (int m = i; m > k; m--) {
                        files.set(m, files.get(m - 1));
                    }
                    files.set(k, f);
                }
            }
        } else if (type == SORTTYPE_SIZE) {
            for (int i = 1; i < files.size(); i++) {
                int k = -1;
                for (int j = i; j >= 0; j--) {
                    if (ascending) {
                        if (FileUtils.sizeOf(files.get(j)) > FileUtils.sizeOf(files.get(i))) {
                            k = j;
                        }
                    } else {
                        if (FileUtils.sizeOf(files.get(j)) < FileUtils.sizeOf(files.get(i))) {
                            k = j;
                        }
                    }
                }
                if (k != -1) {
                    File f = files.get(i);
                    for (int m = i; m > k; m--) {
                        files.set(m, files.get(m - 1));
                    }
                    files.set(k, f);
                }
            }
        }

    }

    public static boolean createDir(String name) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File gifdir = new File(externalStorageDirectory.getAbsoluteFile() + "/" + name);
        if (!gifdir.exists()) {
            boolean b = gifdir.mkdir();
            if (!b)
                return false;
        }
        return true;
    }

    public static String bitrate2String(double bitrate) {
        if (bitrate < 1000)
            return String.valueOf(bitrate);
        else if (bitrate < 1000 * 1000)
            return String.valueOf((float) bitrate / 1000) + "K";
        else
            return String.valueOf((float) (bitrate / 1000) / 1000) + "M";

    }

    public static double string2Bitrate(String s) {
        if (s.endsWith("M") || s.endsWith("m")) {
            try {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000 * 1000;
            } catch (Exception ignored) {
                return -1;
            }

        } else if (s.endsWith("K") || s.endsWith("k")) {
            try {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000;
            } catch (Exception ignored) {
                return -1;
            }
        } else {
            try {
                return Double.parseDouble(s);
            } catch (Exception ignored) {
                return -1;
            }
        }
    }

    public static String size2String(long size) {
        if (size < 1000)
            return String.format(Locale.getDefault(), "%dB", size);
        else if (size < 1000 * 1000)
            return String.format(Locale.getDefault(), "%.3fKB", (float) size / 1000);
        else
            return String.format(Locale.getDefault(), "%.3fMB", (float) (size) / 1000 / 1000);
    }
    public static void makeFolders(AppCompatActivity context) {
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (!sdCardExist) {
            Toast.makeText(context, "请检查SD卡?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (!Utils.createDir("Gif_Mp4")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (!Utils.createDir("Gif_Mp4/Gif")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4/Gif目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (!Utils.createDir("Gif_Mp4/Mp4")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4/Mp4目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
    }

    public static native void welcome();

    public static native boolean checkgif(String gifpath);

    public static native int gifframes(String gifpath);

    public static native float gifavgrate(String gifpath);

    public static native int gif2mp4(String gifpath, String mp4path, int encodertypenum, double bitrate, double outputtime, int framecnt);

    public static native int mp42gif(String mp4path,String gifpath,int fps,int rot,int width,int height,double start,double end);


}
