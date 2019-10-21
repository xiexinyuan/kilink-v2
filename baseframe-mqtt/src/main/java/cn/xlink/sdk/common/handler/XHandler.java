package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * xlink 内部使用的 java handler Created by link on 2017/7/7.
 */

public class XHandler implements XHandlerable {

  private XMsgHandleAction mHandleAction;
  private XLooper mLooper;
  private Map<Integer, List<XMessage>> mFutureMap = new HashMap<>();
  private Lock mLock = new ReentrantLock();

  public XHandler(XLooper looper) {
    if (looper == null) {
      throw new NullPointerException("create handler with a null looper");
    }
    mLooper = looper;
  }

  @Override
  public XLooperable getXLooper() {
    return mLooper;
  }

  @Override
  public void releaseXHandler() {
    mLock.lock();
    mHandleAction = null;
    if (mFutureMap.size() > 0) {
      for (List<XMessage> msgList : mFutureMap.values()) {
        for (XMessage msg : msgList) {
          if (msg.mScheduledFuture != null) {
            msg.mScheduledFuture.cancel(false);
          }
        }
      }
      mFutureMap.clear();
      mFutureMap = null;
    }
    if (mLooper != null) {
      mLooper = null;
    }
    mLock.unlock();
  }

  @Override
  public boolean hasXMessage(int msgWhat) {
    boolean has = false;
    mLock.lock();
    has = mFutureMap.containsKey(msgWhat);
    mLock.unlock();
    return has;
  }

  @Override
  public void removeXMessages(int msgWhat) {
    mLock.lock();
    if (mFutureMap.containsKey(msgWhat)) {
      List<XMessage> msgList = mFutureMap.get(msgWhat);
      for (XMessage msg : msgList) {
        if (msg != null && msg.mScheduledFuture != null) {
          msg.mScheduledFuture.cancel(false);
        }
      }
      mFutureMap.get(msgWhat).clear();
      mFutureMap.remove(msgWhat);
    }
    mLock.unlock();
  }

  @Override
  public void removeCallbacksAndXMessages(Object msgObj) {
    removeCallbacksAndMessages(this, msgObj);
  }

  @Override
  public void sendXMessage(@NotNull XMessageable msg) {
    sendXMessageDelayed(msg, 0);
  }

  @Override
  public void sendEmptyXMessage(int what) {
    XMessageable msg = XLinkHandlerHelper.getInstance().getMessageable(what);
    sendXMessage(msg);
  }

  @Override
  public void postXRunnable(Runnable runnable) {
    postXRunnableDelayed(runnable, null, 0);
  }

  @Override
  public void postXRunnableDelayed(Runnable runnable, long delayMills) {
    postXRunnableDelayed(runnable, null, delayMills);
  }

  @Override
  public void postXRunnableDelayed(Runnable runnable, Object token, long delayMills) {
    if (runnable == null || delayMills < 0) {
      return;
    }
    XMessageable msg = XLinkHandlerHelper.getInstance().getMessageable(0, token, runnable, null);
    sendXMessageDelayed(msg, delayMills);
  }

  @Override
  public void sendXMessageDelayed(@NotNull final XMessageable msg, long delay) {
    if (!(msg instanceof XMessage) || mLooper == null || mLooper.getExecutorService() == null) {
      return;
    }
    XMessage xMsg = (XMessage) msg;

    // 此处加锁是必要的,不能去掉
    mLock.lock();
    final int msgId = xMsg.what;
    Runnable executeRunnable = null;

    if (xMsg.callback != null) {
      // 若是存在runnable回调事件,使用runnable进行处理
      executeRunnable = xMsg.callback;
    } else {
      // 若不存在runnable,则调用handleMessage()进行处理
      executeRunnable = new Runnable() {
        @Override
        public void run() {
          // 处理消息,不能将处理消息放在锁中,因为可能在处理消息回调中开发者会调用到相关的handler方法,要防止死锁
          handleMessage(msg);
          // 这里注意,在sendMessageDelayed中也使用了同一个锁,因为需要创建执行体并提交到线程执行器中进行处理
          // 有可能任务是不需要延迟的,如果不需要可能线程马上就处理了这个任务,但是此时不一定已经将执行任务引用保存到消息中
          // 所以需要加锁确保执行当前任务时,任务确实已经被记录到消息中,后面才能移除掉该任务的记录
          mLock.lock();
          if (mFutureMap.containsKey(msgId)) {
            mFutureMap.get(msgId).remove(msg);
          }
          // 释放消息所有的携带的数据资源
          msg.release();
          mLock.unlock();
        }
      };
    }
    // 创建了执行的任务操作
    ScheduledFuture<?> scheduledFuture =
        mLooper.getExecutorService().schedule(executeRunnable, delay, TimeUnit.MILLISECONDS);
    if (msgId != 0) {
      List<XMessage> msgList;
      if (!mFutureMap.containsKey(msgId)) {
        // 保存当前执行的任务操作
        msgList = new ArrayList<>();
        mFutureMap.put(msgId, msgList);
      } else {
        // 已经存在对应msgId的消息时,直接获取其对应的消息执行队列
        msgList = mFutureMap.get(msgId);
      }
      // 保存当前消息的执行任务
      xMsg.mScheduledFuture = scheduledFuture;
      msgList.add(xMsg);
    }
    mLock.unlock();
  }

  @Override
  public void setXHandleMsgAction(XMsgHandleAction action) {
    mHandleAction = action;
  }


  private void removeCallbacksAndMessages(XHandler handler, Object token) {
    mLock.lock();
    Iterator<List<XMessage>> itMsgList = mFutureMap.values().iterator();
    while (itMsgList.hasNext()) {
      List<XMessage> msgList = itMsgList.next();
      Iterator<XMessage> it = msgList.iterator();
      while (it.hasNext()) {
        XMessage msg = it.next();
        if (msg.obj == token && msg.target == handler) {
          it.remove();
        }
      }
      if (msgList.size() <= 0) {
        itMsgList.remove();
      }
    }
    mLock.unlock();
  }

  public void handleMessage(@NotNull XMessageable msg) {
    if (mHandleAction != null) {
      mHandleAction.handleMessage(this, msg);
    }
  }
}
