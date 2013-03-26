package EnterpriseSystems.CloudManager.Model;

// class for virtual machines

import java.util.List;

public class VM {

    private Long ID;

    private final double memoryUsage_MB;
    private final double diskUsage_GB;

    private List<Double> cpuUsage_history;
    private List<Double> networkUsage_history;

    private long hostId;
    public static final int windowSize = 100;


    public VM(Long ID, double memoryUsage, double diskUsage, long hostId) {
        this.hostId = hostId;
        this.ID = ID;
        this.memoryUsage_MB = memoryUsage;
        this.diskUsage_GB = diskUsage;
        this.cpuUsage_history = new MetricQueue<Double>(windowSize);
        this.networkUsage_history = new MetricQueue<Double>(windowSize);
    }

    public VM(VM vm) {
        this.ID = vm.ID;

        this.memoryUsage_MB = vm.getMemoryUsage_MB();
        this.diskUsage_GB = vm.getDiskUsage_GB();

        this.cpuUsage_history = new MetricQueue<Double>(windowSize);
        for (Double aDouble : vm.cpuUsage_history) cpuUsage_history.add(aDouble);
        this.networkUsage_history = new MetricQueue<Double>(windowSize);
        for (Double aDouble : cpuUsage_history)  networkUsage_history.add(aDouble);

        this.hostId = vm.hostId;
    }


    public void addCpuDataPoint(Double cpuUsage_current) {
        cpuUsage_history.add(cpuUsage_current);
    }

    public Double getAvNetworkUsage() {
        if (this.networkUsage_history.isEmpty())
            return new Double(0);

        Double total = new Double(0);
        for (Double value : networkUsage_history)
            total += value;

        return total / networkUsage_history.size();
    }

    public double getAvCpuUsage() {
        if (cpuUsage_history.isEmpty())
            return 0;

        double total = 0;
        for (Double value : cpuUsage_history) {
            total += value;
        }

        return total / cpuUsage_history.size();
    }


    public long getHostId() {
        return hostId;
    }

    public void setHost(long hostId) {
        this.hostId = hostId;
    }
    public void addNetworkUsageDataPoint(Double networkUsage_current) {
        networkUsage_history.add(networkUsage_current);
    }



    public Long getID() {
        return ID;
    }

    public void setID(Long iD) {
        ID = iD;
    }

    public double getMemoryUsage_MB() {
        return memoryUsage_MB;
    }

    public double getDiskUsage_GB() {
        return diskUsage_GB;
    }

    @Override
    public String toString() {
        return "VM{" +
                "networkUsage=" + getAvNetworkUsage() +
                ", hostId=" + hostId +
                ", cpuUsage=" + getAvCpuUsage() +
                ", diskUsage_GB=" + diskUsage_GB +
                ", memoryUsage_MB=" + memoryUsage_MB +
                ", ID=" + ID +
                '}';
    }
}
