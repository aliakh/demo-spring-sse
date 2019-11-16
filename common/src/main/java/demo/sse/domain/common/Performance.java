package demo.sse.domain.common;

import java.util.StringJoiner;

public class Performance {

    private long time;

    private long committedVirtualMemorySize;

    private long totalSwapSpaceSize;
    private long freeSwapSpaceSize;

    private long totalPhysicalMemorySize;
    private long freePhysicalMemorySize;

    private double systemCpuLoad;
    private double processCpuLoad;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    public void setCommittedVirtualMemorySize(long committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
        this.totalSwapSpaceSize = totalSwapSpaceSize;
    }

    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
        this.freeSwapSpaceSize = freeSwapSpaceSize;
    }

    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Performance.class.getSimpleName() + "[", "]")
                .add("time=" + time)
                .add("committedVirtualMemorySize=" + committedVirtualMemorySize)
                .add("totalSwapSpaceSize=" + totalSwapSpaceSize)
                .add("freeSwapSpaceSize=" + freeSwapSpaceSize)
                .add("totalPhysicalMemorySize=" + totalPhysicalMemorySize)
                .add("freePhysicalMemorySize=" + freePhysicalMemorySize)
                .add("systemCpuLoad=" + systemCpuLoad)
                .add("processCpuLoad=" + processCpuLoad)
                .toString();
    }
}
