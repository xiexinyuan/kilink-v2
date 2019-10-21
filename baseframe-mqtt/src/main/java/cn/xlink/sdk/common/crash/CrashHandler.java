package cn.xlink.sdk.common.crash;

import cn.xlink.sdk.common.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler mHandler;
    private WeakReference<CrashHandlerListener> mCrashHandlerListener;
    private SimpleDateFormat mDateFormat;
    private Date mDate;
    private CrashInfoProviderable mProviderable;

    public CrashHandler(CrashInfoProviderable providerable) {
        if (providerable == null) {
            throw new RuntimeException("crash info provider can not be null");
        }
        mHandler = Thread.getDefaultUncaughtExceptionHandler();
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SS", Locale.getDefault());
        mDate = new Date();
        mProviderable = providerable;
    }

    public void setCrashHandlerListener(CrashHandlerListener listener) {
        if (listener != null) {
            mCrashHandlerListener = new WeakReference<>(listener);
        } else {
            mCrashHandlerListener = null;
        }
    }

    /**
     * 获取异常信息的提供者对象
     *
     * @return
     */
    public CrashInfoProviderable getProviderable() {
        return mProviderable;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        mDate.setTime(System.currentTimeMillis());

        // 日期、app版本信息
        StringBuilder buff = new StringBuilder();
        buff.append("Date: ");
        buff.append(mDateFormat.format(mDate));
        buff.append("\n");
        buff.append("===========\n");
        buff.append(mProviderable.provideEnvironment());
        buff.append(" \n");
        buff.append("===========\n");

        // 崩溃的堆栈信息
        buff.append("Stacktrace:\n\n");
        StringWriter stringwriter = new StringWriter();
        PrintWriter printwriter = new PrintWriter(stringwriter);
        throwable.printStackTrace(printwriter);
        buff.append(stringwriter.toString());
        buff.append("===========\n");
        printwriter.close();

        write2ErrorLog(buff.toString());

        if (mHandler != null) {
            // 交给还给系统处理
            mHandler.uncaughtException(thread, throwable);
        }
        if (mCrashHandlerListener != null) {
            CrashHandlerListener listener = mCrashHandlerListener.get();
            if (listener != null) {
                listener.onProcessedCrashInfo(thread, throwable);
            }
        }
//        System.exit(0);
    }

    /**
     * 创建总文件夹
     */
    public String getFilePath() {
        //由接口去提供crash文件缓存的路径
        // /mnt/sdcard判断有无SD卡
        String cachePath = mProviderable.provideCrashFileStoragePath();
        File file = new File(cachePath);
        if (!file.exists()) {
            // 创建文件夹
            file.mkdirs();
        }
        return cachePath;
    }

    private void write2ErrorLog(String content) {
        mDate.setTime(System.currentTimeMillis());
        String time = mDateFormat.format(mDate);
        String path = getFilePath();
        String filename = mProviderable.provideCrashFileName(time);
        if (StringUtil.isEmpty(filename)) {
            filename = time.concat("_crash");
        }
        File file = new File(path, filename.concat(".txt"));
        FileOutputStream fos = null;
        try {
            if (file.exists()) {
                // 清空之前的记录
                file.delete();
            } else {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
