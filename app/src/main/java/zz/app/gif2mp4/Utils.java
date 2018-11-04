package zz.app.gif2mp4;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zz.app.gif2mp4.beans.Mp4Info;
import zz.app.gif2mp4.managers.ActivityTransitionManager;

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

    public native static String getAnalyseLine();

    public enum AnalyseState{
        None,Gif2Mp4,Mp42Gif
    }
    private static AnalyseState analyseState=AnalyseState.None;

    public  static  AnalyseState getAnalyseState() {
        return analyseState;
    }

    public static void setAnalyseState(AnalyseState analyseState) {
        Utils.analyseState = analyseState;
    }

    public static Double getTotalTime() {
        return totalTime;
    }

    public static void setTotalTime(Double totalTime) {
        Utils.totalTime = totalTime;
    }

    private static Double totalTime;

    public static ActivityTransitionManager getManager() {
        return manager;
    }
    private static ActivityTransitionManager manager;
    public static void initManager(){
        if(manager==null){
            manager=new ActivityTransitionManager();
        }
    }
    public static final int SORTTYPE_TIME = 0;
    public static final int SORTTYPE_SIZE = 1;
    public static final int SORTTYPE_NAME = 2;

    public static final int GIF2MP4_UNKNOWN_ERROR = 0;
    public static final int GIF2MP4_NOT_ACTION = 2;
    public static final int SUCCESSCODE = 233;
    public static final int FAILURECODE = 235;
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

    public static ArrayList<File> getFiles(String type, int sortType, boolean ascending, boolean expecterror) {
        ArrayList<File> files;
        files = new ArrayList<>(FileUtils.listFiles(Environment.getExternalStorageDirectory(), FileFilterUtils.suffixFileFilter(type), DirectoryFileFilter.INSTANCE));
        Log.d(TAG, "getFiles: size="+files.size());
        sortFiles(files, sortType, ascending);
        if (expecterror && type.equals("gif"))
            files = exceptErrorGif(files);
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

    private static boolean createDir(String name) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDirectory.getAbsoluteFile() + "/" + name);
        if (!dir.exists()) {
            return !dir.mkdir();
        }
        return false;
    }

    private static String getDir(String name) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDirectory.getAbsoluteFile() + "/Gif_Mp4/" + name);
        return dir.getAbsolutePath() + "/";
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
        if (Utils.createDir("Gif_Mp4")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (Utils.createDir("Gif_Mp4/Gif")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4/Gif目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (Utils.createDir("Gif_Mp4/Mp4")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4/Mp4目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
        if (Utils.createDir("Gif_Mp4/.Cache")) //主目录
        {
            Toast.makeText(context, "创建Gif_Mp4/.Cache目录失败?", Toast.LENGTH_SHORT).show();
            context.finish();
        }
    }

    public static native void welcome();

    public static native boolean checkgif(String gifpath);

    public static native int gifframes(String gifpath);

    public static native float gifavgrate(String gifpath);

    public static native int gif2mp4(String gifpath, String mp4path, int encodertypenum, double bitrate, double outputtime, int framecnt);

    public static int gif2mp42(String gifpath, String mp4path, int encodertypenum, double bitrate, double outputtime){
        FFMpegCommand command=new FFMpegCommand();
        command.addCmd("-i");
        command.addCmd(gifpath);
        command.addCmd("-c:v");
        if(encodertypenum==0)
            command.addCmd("h264");
        else
            command.addCmd("mpeg4");
        command.addCmd("-an");
        command.addCmd("-b:v");
        command.addCmd(Utils.bitrate2String( bitrate));
        double scale=outputtime*Utils.gifavgrate(gifpath)/Utils.gifframes(gifpath);
        command.addCmd("-filter_complex");
        command.addCmd(String.format(Locale.getDefault(),"[mid]setpts=PTS*%f;crop=floor(in_w/2)*2:floor(in_h/2)*2[mid]",scale));
        command.addCmd("-pix_fmt");
        command.addCmd("yuv420p");
        command.addCmd("-y");
        command.addCmd(mp4path);
        ffmpeg_native(command.convertCmd());
        return 0;
    }

    public static native int ffmpeg_native(String[] cmd);

    public static void analyseProgress(String line){
        if(analyseState==AnalyseState.Gif2Mp4) {
            Pattern pattern = Pattern.compile("(?:.*)PTS(?:.*)T:(.*)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    Double d = Double.parseDouble(matcher.group(1));
                    setProgress2((int) (d * 100 / totalTime));
                } catch (NumberFormatException ignored) {

                }
            }
        }else if (analyseState==AnalyseState.Mp42Gif){
            Pattern pattern = Pattern.compile("(?:.*)PTS(?:.*)T:(.*)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    Double d = Double.parseDouble(matcher.group(1));
                    setProgress2((int) (d * 100 / totalTime));
                } catch (NumberFormatException ignored) {

                }
            }
        }
    }

    public static native int mp42gif(String mp4path, String gifpath, double fps, int rotate, int width, int height, double start, double timeScale);

    public static int mp42gif2(String mp4path, String gifpath, double fps, int rotate, int width, int height,
                               int x,int y,int w,int h, double start, double end,double totalTime){
        FFMpegCommand command=new FFMpegCommand();
        command.addCmd("-i");
        command.addCmd(mp4path);
        command.addCmd("-filter_complex");
        String temp=String.format(Locale.getDefault(),"crop=%d:%d:%d:%d[mid];",w,h,x,y);
        switch (rotate) {
            case 0:temp+="[mid]rotate=0";break;
            case 1:temp+="[mid]rotate=PI/2";break;
            case 2:temp+="[mid]rotate=PI";break;
            case 3:temp+="[mid]rotate=-PI/2";break;
        }
        temp+="[mid2];";
        double scale=totalTime/(end-start);
        temp+=String.format(Locale.getDefault(),"[mid2]setpts=PTS*%f",scale);
        command.addCmd(temp);
        command.addCmd("-r");
        command.addCmd(fps+"");
        command.addCmd("-s");
        command.addCmd(String.format(Locale.getDefault(),"%dx%d",width,height));
        command.addCmd("-t");
        command.addCmd(totalTime+"");
        command.addCmd("-y");
        command.addCmd(gifpath);
        ffmpeg_native(command.convertCmd());
        return 0;
    }

    public static native int[] getMp4Size(String path);

    public static native long[] getMp4Info(String info);

    public static ArrayList<Mp4Info> getThumbnailMap(Context context, ArrayList<File> files) {
        ArrayList<Mp4Info> ret = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("mp42gif", Context.MODE_PRIVATE);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        for (File f : files) {
            String path = f.getAbsolutePath();
            if (!preferences.contains(path)) {
                String md5 = Utils.MD5(path);
                String output = getDir(".cache") + md5 + ".jpg";
                File f2 = new File(output);
                if (!f2.exists()) {
                    try {
                        boolean b = f2.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "getThumbnailMap: path=" + path);
                    try {
                        retriever.setDataSource(path);
                    } catch (RuntimeException ex) {
                        continue;
                    }
                    Bitmap bitmap = retriever.getFrameAtTime(0);
                    if (bitmap != null) {
                        try {
                            FileOutputStream outputStream = new FileOutputStream(f2);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                            Bitmap bitmap1 = BitmapFactory.decodeFile(output);
                            preferences.edit().putString(path, md5).apply();
                            int[] size=getMp4Size(path);
                            if(size!=null){
                                Mp4Info info=new Mp4Info(path,size[0],size[1]);
                                info.setThumbnail(bitmap1);
                                ret.add(info);
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        preferences.edit().putString(path, md5).apply();

                    }
                } else {
                    preferences.edit().putString(path, md5).apply();
                    int[] size=getMp4Size(path);
                    if(size!=null){
                        Mp4Info info=new Mp4Info(path,size[0],size[1]);
                        info.setThumbnail(BitmapFactory.decodeFile(f2.getAbsolutePath()));
                        ret.add(info);
                    }
                }
            } else {
                String md5 = preferences.getString(path, "");
                String output = getDir(".cache") + md5 + ".jpg";
                Bitmap bitmap = BitmapFactory.decodeFile(output);
                int[] size=getMp4Size(path);
                if(size!=null){
                    Mp4Info info=new Mp4Info(path,size[0],size[1]);
                    info.setThumbnail(bitmap);
                    ret.add(info);
                }
            }

        }
        return ret;
    }

    public static float dp2px(Context context, int i) {
        return (context.getResources().getDisplayMetrics().density * i);
    }


    public static String mills2Str(int position) {
        int milli=position%1000;
        int second=position/1000%60;
        int minus=(position/1000)/60;
        return String.format(Locale.getDefault(),"%d:%02d.%03d",minus,second,milli);
    }
}
