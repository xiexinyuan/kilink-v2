package com.konka.iot.baseframe.common.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by legendmohe on 16/3/29.
 */
public class CommonUtil {

    /**
     * 获取Collection泛型对象类型的第一个参数类型
     *
     * @param obj
     * @return 当参数为null或者该参数不为泛型时, 返回null
     */
    public static Class getCollectionValueType(Object obj) {
        if (obj != null && obj instanceof Collection) {
            Collection col = (Collection) obj;
            if (col.size() > 0) {
                Iterator it = col.iterator();
                while (it.hasNext()) {
                    Object value = it.next();
                    if (value != null) {
                        return value.getClass();
                    }
                }
            }
        }
        return null;
    }

    public static boolean isEmpty(byte[] t) {
        return !(t != null && t.length != 0);
    }

    public static <T> boolean isEmpty(T[] t) {
        return !(t != null && t.length != 0);
    }

    public static boolean isEmpty(Collection collection) {
        return !(collection != null && collection.size() != 0);
    }

    public static boolean isEmpty(WeakReference weakReference) {
        return !(weakReference != null && weakReference.get() != null);
    }

    /**
     * 获取当前的进程号
     *
     * @return 获取失败时返回-1
     */
    public static final int getProcessID() {
        try {
            //TODO:安卓环境中使用会找不到该类
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            System.out.println(runtimeMXBean.getName());
            return Integer.valueOf(runtimeMXBean.getName().split("@")[0]);
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * 合并压缩已经存在的log信息
     *
     * @param logoutPath log文件保存的地址
     * @param logPrefix  log文件的前缀
     */
    public static boolean archivePreviousLogs(String logoutPath, String logPrefix) {
        if (StringUtil.isEmpty(logoutPath)) {
            return false;
        }
        File directory = new File(logoutPath);
        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().startsWith(logPrefix + "_log_")) {
                fileList.add(file);
            }
        }
        // 每10个文件压缩一次
        if (fileList.size() > 10) {
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
            String filename = "/" + logPrefix + "_archive_" + simpledateformat.format(new Date()) + ".zip";
            zipIt(new File(directory.getAbsolutePath() + filename), fileList);

            for (File deletingFile :
                    fileList) {
                deletingFile.delete();
            }
        }
        return true;
    }

    /**
     * 将多个文件压缩一起
     *
     * @param outputFile 文件输出地址
     * @param files      需要压缩的文件
     */
    public static void zipIt(File outputFile, List<File> files) {
        byte[] buffer = new byte[1024];
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(outputFile));
            FileInputStream in = null;
            for (File file : files) {
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(file.getAbsolutePath());
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }

            zos.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ignore) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignore) {
                }
            }
        }
        return line;
    }

    public static <T> List<T> toList(T[] objects) {
        List<T> result = new ArrayList<>();
        Collections.addAll(result, objects);
        return result;
    }

    public static <T> boolean in(T index, T[] target) {
        for (T t :
                target) {
            if (index.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> String dumpObjectArray(T[] src) {
        StringBuffer sb = new StringBuffer();
        for (T item :
                src) {
            sb.append(item.toString()).append(", ");
        }
        if (sb.length() > 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public static String dumpStacktrace() {
        StringWriter sw = new StringWriter();
        new Throwable("").printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String dumpTopStackElement(String prefix) {
        return dumpTopStackElement(prefix, 8, 2);
    }

    private static String dumpTopStackElement(String prefix, int limit) {
        return dumpTopStackElement(prefix, limit, 2);
    }

    private static String dumpTopStackElement(String prefix, int limit, int offset) {
        // exclude self
        offset += 1;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("\n    ");
            for (int i = 0, j = 0; i < stackTrace.length && j < limit; i++) {
                StackTraceElement element = stackTrace[i];
                if (!element.getClassName().startsWith(prefix))
                    continue;
                if (offset-- >= 0) {
                    continue;
                }
                sb.append(element.getClassName()).append(".");
                sb.append(element.getMethodName()).append("(");
                sb.append(element.getFileName()).append(":");
                sb.append(element.getLineNumber()).append(")");
                sb.append("\n    ");
                j++;
            }
            return sb.toString();
        }
        return null;
    }

    public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.<T>emptyList() : iterable;
    }

    /**
     * 判断某个对象是否为某个类的类型或者其子类,null对象一定返回false
     *
     * @param clazz 类型
     * @param obj   对象
     * @return
     */
    public static boolean isClassEquals(@NotNull Class clazz, Object obj) {
        return obj != null && clazz.isInstance(obj);
    }

    /**
     * 判断两个对象是否相同,对象可null
     *
     * @param arg1
     * @param arg2
     * @return
     */
    public static boolean isObjEquals(@Nullable Object arg1, @Nullable Object arg2) {
        if (arg1 == arg2) {
            return true;
        } else if (arg1 == null || arg2 == null) {
            return false;
        } else {
            return arg1.equals(arg2);
        }
    }

}
