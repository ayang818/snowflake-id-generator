package com.ayang818.github.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 25959
 */
public class IdGenerator {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerator.class);

    /**
     * 起始时间戳
     */
    private static final long START_TIME_STAMP = System.currentTimeMillis();

    /**
     * 数据中心Id长度
     */
    private final long dataCenterIdBits = 5L;

    /**
     * 数据中心最大Id
     */
    private final long maxDataCenterId = (1 << dataCenterIdBits) - 1;

    /**
     * 工作机器Id长度
     */
    private final long workerIdBites = 5L;

    /**
     * 最大工作机器Id
     */
    private final long maxWorkerId = (1 << workerIdBites) - 1;

    /**
     * 序列号长度
     */
    private final long sequenceBites = 12L;

    /**
     * 工作机器Id偏移量
     */
    private final long workerIdOffset = sequenceBites;

    /**
     * 数据中心Id偏移量
     */
    private final long dataCenterIdOffset = sequenceBites + workerIdBites;

    /**
     * 时间戳偏移量
     */
    private final long timestampOffset = sequenceBites + workerIdBites + dataCenterIdBits;

    /**
     * 计算序列的掩码
     */
    private final long sequenceMask = (1 << sequenceBites) - 1;

    private long dataCenterId;

    private long workerId;

    private long lastTimestamp = -1L;

    private long sequence = 0L;


    public IdGenerator(long dataCenterId, long workerId) {
        init(dataCenterId, workerId);
    }

    private void init(long dataCenterId, long workerId) {
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("DataCenter Id must be between %d and %d", 0, maxDataCenterId));
        }
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("WorkerId must be between %d and %d", 0, maxWorkerId));
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 获取下一个snowflakeId
     * @return snowflakeId
     */
    public synchronized long getNextId() {
        long currentTimeStamp = currentTimeStamp();
        // 当前时间比最后的时间戳还要小，说明服务器上的时钟被回调了
        if (currentTimeStamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }
        // 当前时间和最后的时间戳相同
        if (currentTimeStamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // sequence 为sequenceMask + 1时，说明这个毫秒的所有序列都已经用完了
            if (sequence == 0) {
                currentTimeStamp = blockedUntilNextMillis(currentTimeStamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimeStamp;
        // 于起始时间的差
        long reduce = lastTimestamp - START_TIME_STAMP;
        return reduce << timestampOffset
                | dataCenterIdBits << dataCenterIdOffset
                | workerIdBites << workerIdOffset
                | sequence;
    }

    /**
     * 获取当前的时间毫秒
     * @return currentTimeMillis
     */
    private long currentTimeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * 返回大于currentTimeStamp的时间戳
     * @param currentTimeStamp
     * @return nextTimeStamp
     */
    private long blockedUntilNextMillis(long currentTimeStamp) {
        long timeStamp = currentTimeStamp();
        while (timeStamp <= currentTimeStamp) {
            timeStamp = currentTimeStamp();
        }
        return timeStamp;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public long getWorkerId() {
        return workerId;
    }
}