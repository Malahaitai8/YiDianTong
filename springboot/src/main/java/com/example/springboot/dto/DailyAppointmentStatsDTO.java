package com.example.springboot.dto;

/**
 * 按天统计预约数据，用于管理端折线图等可视化
 */
public class DailyAppointmentStatsDTO {

    private String date; // yyyy-MM-dd
    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private int noShowAppointments;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(int totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public int getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(int completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public int getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(int cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public int getNoShowAppointments() {
        return noShowAppointments;
    }

    public void setNoShowAppointments(int noShowAppointments) {
        this.noShowAppointments = noShowAppointments;
    }
}


