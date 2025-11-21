// 文件路径: src/main/java/com/example/springboot/dto/WaitlistInfoDTO.java
package com.example.springboot.dto;

public class WaitlistInfoDTO {

    private Long scheduleId;
    private Long rank; // 您的排名 (从 0 开始)
    private Long queueSize; // 当前总排队人数

    // 您可以根据需要添加更多排班信息 (例如科室、医生)，但需要额外查询，暂时从简

    // 展示所需的扩展字段
    private Long doctorId;
    private String doctorName;
    private java.util.Date scheduleDate;
    private String timeSlot;     // morning/afternoon/evening
    private String timeSlotName; // 上午/下午/晚上

    public WaitlistInfoDTO() {}


    public WaitlistInfoDTO(Long scheduleId, Long rank, Long queueSize) {
        this.scheduleId = scheduleId;
        this.rank = rank;
        this.queueSize = queueSize;
    }

    // 新增字段的 Getter/Setter，便于序列化输出给前端
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public java.util.Date getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(java.util.Date scheduleDate) { this.scheduleDate = scheduleDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getTimeSlotName() { return timeSlotName; }
    public void setTimeSlotName(String timeSlotName) { this.timeSlotName = timeSlotName; }


    // --- 省略 Getter 和 Setter ---
    // (您可以自行添加，或者使用 @Data LOMBOK 注解)
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
    public Long getQueueSize() { return queueSize; }
    public void setQueueSize(Long queueSize) { this.queueSize = queueSize; }
}