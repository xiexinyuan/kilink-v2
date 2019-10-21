package cn.xlink.sdk.common;

import cn.xlink.sdk.common.handler.XHandlerable;
import cn.xlink.sdk.common.handler.XLinkHandlerHelper;
import cn.xlink.sdk.common.handler.XMessageable;
import cn.xlink.sdk.common.handler.XMsgHandleAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 基础的日志输出类 Created by taro on 2017/12/6.
 */
public class BaseLog {
  //////////////////////////////////////////////////////////////////////

  private static final String TAG = "BaseLog";
  // 添加log消息,输出到文件中
  private static final int MSG_ADD_LOG_ITEM = 100;
  // 压缩log文件
  private static final int MSG_ARCHIVE_LOG_ITEM = 101;
  private static final Date sDate = new Date();
  private static final SimpleDateFormat sDateFormat =
      new SimpleDateFormat("yyyy_MM_dd HH:mm:ss:SSS", Locale.getDefault());
  // 是否已经启用了log
  volatile boolean mIsStarted;
  // log输出通道
  WritableByteChannel mLogFileChannel;
  // handler
  XHandlerable mHandler;
  // 当前日志输出文件地址
  String mTargetLogFilePath = "";
  StringBuilder mBufferCache;
  //////////////////////////////////////////////////////////////////////
  private Config mConfig;

  public BaseLog(@NotNull Config config) {
    mConfig = config;
    mBufferCache = new StringBuilder(200);
  }

  /**
   * 获取日志等级描述
   *
   * @param level 来自{@link Loggable}
   * @return
   */
  public static String getLogLevel(int level) {
    switch (level) {
      case Loggable.VERBOSE:
        return "VERBOSE";
      case Loggable.DEBUG:
        return "DEBUG";
      case Loggable.WARN:
        return "WARN";
      case Loggable.INFO:
        return "INFO";
      case Loggable.ERROR:
        return "ERROR";
      case Loggable.NONE:
        return "NONE";
      default:
        return "UNKNOWN";
    }
  }

  /**
   * get a format date for now
   *
   * @return 24 hours date format
   */
  public static String getFormatDate() {
    sDate.setTime(System.currentTimeMillis());
    return sDateFormat.format(sDate);
  }

  /**
   * get a format date by time
   *
   * @param time the timemillis for format
   * @return 24 hours date format
   */
  public static String getFormatDate(long time) {
    sDate.setTime(time);
    return sDateFormat.format(sDate);
  }

  /**
   * contact tag and msg together for printing
   *
   * @param tag
   * @param msg
   * @return
   */
  public static String getPrintLogMsg(@Nullable String tag, @NotNull String msg) {
    String subTag = StringUtil.subFixLengthString(tag, 24);
    // 日期-线程ID-tag-消息内容
    return String.format("%1$s %2$03d %3$-24s  %4$s", getFormatDate(),
        Thread.currentThread().getId(), subTag, msg);
  }

  /**
   * create a default log config for edit, notice that you have to set the path for log file output,
   * unless disable log file<br>
   * <li>debug level is DEBUG
   * <li>buffer level is DEBUG
   * <li>log file is ENABLE
   * <li>log prefix is DEBUG
   *
   * @return
   */
  @NotNull
  public static Config defaultDebugConfig() {
    Config config = new Config().setDebugLevel(Loggable.DEBUG).setBufferLevel(Loggable.DEBUG)
        .setEnableLogFile(true).setLogPreFix("DEBUG");
    return config;
  }

  // 以上为静态方法
  ///////////////////////////////////////////////////////////////////////////

  /**
   * 当创建文件失败时,创建一个特殊文件名的空文件作为记录
   *
   * @param path 目标路径
   * @param fileName 文件名
   */
  private static void createFailFileForRecord(String path, String fileName) {
    fileName = fileName.concat("create file fail");
    File file = new File(path, fileName);
    try {
      file.createNewFile();
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * if the log utils is started and log msg
   *
   * @return
   */
  public boolean isStarted() {
    return mIsStarted;
  }

  /**
   * is can log msg into file
   *
   * @return
   */
  public boolean isEnableLogFile() {
    return mConfig.mEnableLogFile;
  }

  /**
   * is can log msg with debug level
   *
   * @return
   */
  public boolean isEnableDebugLog() {
    return mConfig.mDebugLevel >= Loggable.DEBUG;
  }

  /**
   * is can log msg with error level
   *
   * @return
   */
  public boolean isEnableErrorLog() {
    return mConfig.mDebugLevel == Loggable.ERROR;
  }

  /**
   * get debug level, see {@link Loggable}
   *
   * @return
   */
  public int getDebugLevel() {
    return mConfig.mDebugLevel;
  }

  /**
   * print log with tag ,msg and throwable
   *
   * @param level level requested for logging
   * @param tag tag for msg
   * @param msg msg
   * @param e throwable exception
   * @return anything you like
   */
  public int log(int level, @Nullable String tag, @Nullable String msg, @Nullable Throwable e) {
    if (e != null) {
      e.printStackTrace();
    }
    if (mConfig.mLoggable != null) {
      if (msg == null) {
        msg = "";
      }
      return mConfig.mLoggable.log(level, tag, msg, e);
    }
    return -1;
  }

  /**
   * Handy function to get a loggable stack trace from a Throwable
   *
   * @param tr An exception to log
   */
  public String getStackTraceString(Throwable tr) {
    if (tr != null) {
      StringWriter strWrite = new StringWriter();
      PrintWriter printWrite = new PrintWriter(strWrite);
      tr.printStackTrace(printWrite);
      printWrite.flush();
      return strWrite.toString();
    } else {
      return null;
    }
  }

  public synchronized void start() {
    debug(TAG, "XLog starting...");
    if (mHandler == null) {
      mHandler = XLinkHandlerHelper.getInstance()
          .getHandlerable(XLinkHandlerHelper.getInstance().newThreadLooperable());
      mHandler.setXHandleMsgAction(new MsgHandleAction());
      XLinkHandlerHelper.getInstance().prepareLooperable(mHandler, mHandler.getXLooper());
      mHandler.sendEmptyXMessage(MSG_ARCHIVE_LOG_ITEM);
    }
    if (mConfig.mEnableLogFile) {
      if (mLogFileChannel == null || !mLogFileChannel.isOpen()) {
        if (mLogFileChannel != null) {
          // 确保IO通道是会正常关闭的
          stop();
        }
        RandomAccessFile logFile = createLogFile();
        if (logFile != null) {
          mLogFileChannel = logFile.getChannel();
          mIsStarted = true;
          debug(TAG, "XLog started : create file success");
        } else {
          log(Loggable.ERROR, TAG, "start XLog fail: create saving file Loggable.ERROR", null);
        }
      }
    }
  }

  public synchronized void stop() {
    debug(TAG, "XLog stopping...");
    mIsStarted = false;
    if (mHandler != null) {
      mHandler.removeXMessages(MSG_ADD_LOG_ITEM);
      mHandler.removeXMessages(MSG_ARCHIVE_LOG_ITEM);
      mHandler = null;
    }
    if (mLogFileChannel != null) {
      try {
        if (mLogFileChannel.isOpen()) {
          ((FileChannel) mLogFileChannel).force(true);
        }
      } catch (IOException e) {
        log(Loggable.ERROR, TAG, "save XLog fail:", e);
      } finally {
        try {
          mLogFileChannel.close();
        } catch (IOException ignore) {
        } finally {
          mLogFileChannel = null;
        }
      }
    }
  }

  /**
   * Send a {@link Loggable#VERBOSE} log message.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public int verbose(String tag, String msg) {
    return innerlog(Loggable.VERBOSE, tag, msg, null);
  }

  /**
   * Send a {@link Loggable#VERBOSE} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public int verbose(String tag, String msg, Throwable tr) {
    return innerlog(Loggable.VERBOSE, tag, msg, tr);
  }

  /**
   * Send a {@link Loggable#DEBUG} log message.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public int debug(String tag, String msg) {
    return innerlog(Loggable.DEBUG, tag, msg, null);
  }

  /**
   * Send a {@link Loggable#DEBUG} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public int debug(String tag, String msg, Throwable tr) {
    return innerlog(Loggable.DEBUG, tag, msg, tr);
  }

  /**
   * Send an {@link Loggable#INFO} log message.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public int info(String tag, String msg) {
    return innerlog(Loggable.INFO, tag, msg, null);
  }

  /**
   * Send a {@link Loggable#INFO} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public int info(String tag, String msg, Throwable tr) {
    return innerlog(Loggable.INFO, tag, msg, tr);
  }

  /**
   * Send a {@link Loggable#WARN} log message.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public int warn(String tag, String msg) {
    return innerlog(Loggable.WARN, tag, msg, null);
  }

  /**
   * Send a {@link Loggable#WARN} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public int warn(String tag, String msg, Throwable tr) {
    return innerlog(Loggable.WARN, tag, msg, tr);
  }

  /**
   * Send a {@link Loggable#WARN} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param tr An exception to log
   */
  public int warn(String tag, Throwable tr) {
    return innerlog(Loggable.WARN, tag, null, tr);
  }

  /**
   * Send an {@link Loggable#ERROR} log message.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public int error(String tag, String msg) {
    return innerlog(Loggable.ERROR, tag, msg, null);
  }

  /**
   * Send a {@link Loggable#ERROR} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually identifies the class or
   *        activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public int error(String tag, String msg, Throwable tr) {
    return innerlog(Loggable.ERROR, tag, msg, tr);
  }


  ////////////////////////////// packet methods /////////////////////////////////////////////

  /**
   * 更新配置文件
   *
   * @param config
   * @return
   */
  protected BaseLog setConfig(Config config) {
    if (config != null) {
      mConfig = config;
    }
    return this;
  }

  ///////////////////////////////////////////////////////////////////////////
  // buffer
  ///////////////////////////////////////////////////////////////////////////

  private int innerlog(int level, String tag, String msg, Throwable tr) {
    if (mConfig.mEnableLogFile && mConfig.mBufferLevel <= level) {
      writeLog(getLogLevel(level), tag, msg, null);
    }
    if (mConfig.mDebugLevel <= level) {
      return log(level, tag, msg, tr);
    } else {
      return 0;
    }
  }

  private void writeLog(String level, String tag, String msg, Throwable tr) {
    if (!mIsStarted) {
      return;
    }
    String logMsg = String.valueOf(msg);
    if (tr != null) {
      logMsg = logMsg.concat("\n").concat(getStackTraceString(tr));
    }
    LogItem newItem = new LogItem(System.currentTimeMillis(), tag, logMsg, level, -1,
        // 在android中使用该方式获取进程号会崩溃
        // CommonUtil.getProcessID(),
        (int) Thread.currentThread().getId());
    addItemToLogChannel(newItem);
  }

  //////////////////////////////////////////////////////////////////////

  private void addItemToLogChannel(LogItem newItem) {
    XMessageable msg = XLinkHandlerHelper.getInstance().getMessageable(MSG_ADD_LOG_ITEM, newItem);
    mHandler.sendXMessage(msg);
  }

  private String stringifyLogItem(LogItem item) {
    if (mBufferCache.length() > 0) {
      mBufferCache.delete(0, mBufferCache.length());
    }
    sDate.setTime(item.ts);
    mBufferCache.append(sDateFormat.format(sDate));
    mBufferCache.append(" ").append(StringUtil.rightPad(String.valueOf(item.pid), 5));
    mBufferCache.append(" ").append(StringUtil.rightPad(String.valueOf(item.tid), 5));
    mBufferCache.append(" ").append(item.level).append(" ");
    mBufferCache.append(StringUtil.rightPad(item.tag, 25)).append(": ");
    mBufferCache.append(item.msg);
    mBufferCache.append("\n");
    return mBufferCache.toString();
  }
  //////////////////////////////////////////////////////////////////////

  private String checkFileAndCreateNew(String path, String fileName, int index) {
    try {
      File file = new File(path, fileName.concat(".txt"));
      if (file.exists()) {
        index++;
        fileName = fileName.concat("_").concat(String.valueOf(index));
        return checkFileAndCreateNew(path, fileName, index);
      } else {
        file.getParentFile().mkdirs();
      }
      if (file.createNewFile()) {
        return file.getAbsolutePath();
      } else {
        createFailFileForRecord(path, fileName);
        return null;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      createFailFileForRecord(path, fileName);
      return null;
    }
  }

  private RandomAccessFile createLogFile() {
    SimpleDateFormat simpledateformat =
        new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
    String filename = "/" + mConfig.mLogPrefix + "_log_" + simpledateformat.format(new Date());
    String logFile = checkFileAndCreateNew(mConfig.mLogoutPath, filename, 0);
    if (logFile != null) {
      try {
        log(Loggable.DEBUG, TAG, "createLogFile: " + logFile, null);
        mTargetLogFilePath = logFile;
        return new RandomAccessFile(logFile, "rw");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  //////////////////////////////////////////////////////////////////////
  // 内部类

  private static class LogItem {
    long ts;
    String tag;
    String msg;
    String level;
    int pid;
    int tid;

    public LogItem(long ts, String tag, String msg, String level, int pid, int tid) {
      this.ts = ts;
      this.tag = tag;
      this.msg = msg;
      this.level = level;
      this.pid = pid;
      this.tid = tid;
    }
  }

  //////////////////////////////////////////////////////////////////////
  // 内部静态类

  private static class DefaultLogger implements Loggable {
    @Override
    public int log(int level, String tag, @NotNull String msg, Throwable e) {
      switch (level) {
        case ERROR:
          System.err.println(getPrintLogMsg(tag, msg));
          break;
        default:
          System.out.println(getPrintLogMsg(tag, msg));
          break;
      }
      return level;
    }
  }

  public static class Config {
    private String mDefaultTag;
    private String mLogPrefix;
    private String mLogoutPath;
    private boolean mEnableLogFile;
    private int mDebugLevel;
    private int mBufferLevel;
    private Loggable mLoggable;

    public Config() {
      mDefaultTag = "unknownTag";
      mLogoutPath = "/xlink/";
      mLogPrefix = "xlink";
      mEnableLogFile = false;
      mDebugLevel = Loggable.ERROR;
      mBufferLevel = Loggable.ERROR;
      mLoggable = new DefaultLogger();
    }

    public Config setDefaultTag(@NotNull String tag) {
      mDefaultTag = tag;
      return this;
    }

    public Config setLogPreFix(@NotNull String prefix) {
      mLogPrefix = prefix;
      return this;
    }

    public Config setLogoutPath(@NotNull String logoutPath) {
      mLogoutPath = logoutPath;
      return this;
    }

    public Config setEnableLogFile(boolean isEnable) {
      mEnableLogFile = isEnable;
      return this;
    }

    public Config setDebugLevel(int level) {
      mDebugLevel = level;
      return this;
    }

    public Config setBufferLevel(int level) {
      mBufferLevel = level;
      return this;
    }

    public Config setLoggable(Loggable loggable) {
      if (loggable != null) {
        mLoggable = loggable;
      }
      return this;
    }

    @Override
    public String toString() {
      String format =
          "{\"mDefaultTag\":%s,\"mLogPrefix\":%s,\"mLogoutPath\":%s,\"mEnableLogFile\":%s,\"mDebugLevel\":%s,\"mBufferLevel\":%s}";
      return String.format(format, mDefaultTag, mLogPrefix, mLogoutPath,
          String.valueOf(mEnableLogFile), getLogLevel(mDebugLevel), getLogLevel(mBufferLevel));
    }
  }

  private class MsgHandleAction implements XMsgHandleAction {
    @Override
    public boolean handleMessage(@NotNull XHandlerable handlerable, @NotNull XMessageable msg) {
      switch (msg.getMsgId()) {
        case MSG_ADD_LOG_ITEM:
          LogItem newItem = (LogItem) msg.getObj();
          if (mLogFileChannel != null) {
            try {
              if (!mLogFileChannel.isOpen()) {
                log(Loggable.DEBUG, TAG, "log file channel is not opened, dont write any bytes",
                    null);
                stop();
              }
              if (mLogFileChannel != null) {
                byte[] data = stringifyLogItem(newItem).getBytes();
                mLogFileChannel.write(ByteBuffer.wrap(data, 0, data.length));
              }
            } catch (Exception e) {
              // 这里之前一直会出现错误,因为catch异常只是处理了IO异常,并没有处理其它异常
              log(Loggable.ERROR, TAG,
                  "error! write log file exception, maybe log file channel closed", e);
            }
          }
          break;
        case MSG_ARCHIVE_LOG_ITEM:
          CommonUtil.archivePreviousLogs(mConfig.mLogoutPath, mConfig.mLogPrefix);
          break;
      }
      return true;
    }
  }
}
