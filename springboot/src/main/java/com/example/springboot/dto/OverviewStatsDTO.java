package com.example.springboot.dto;

public class OverviewStatsDTO {
    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private int noShowAppointments;

    private int totalSlots;
    private int availableSlots;
    private int usedSlots;

    private double completionRate; // 0-1
    private double utilization;    // 0-1

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

    public int getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public int getNoShowAppointments() { return noShowAppointments; }
    public void setNoShowAppointments(int noShowAppointments) { this.noShowAppointments = noShowAppointments; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public int getUsedSlots() { return usedSlots; }
    public void setUsedSlots(int usedSlots) { this.usedSlots = usedSlots; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public double getUtilization() { return utilization; }
    public void setUtilization(double utilization) { this.utilization = utilization; }
}

