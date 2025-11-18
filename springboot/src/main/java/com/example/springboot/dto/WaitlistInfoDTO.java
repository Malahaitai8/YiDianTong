// 文件路径: src/main/java/com/example/springboot/dto/WaitlistInfoDTO.java
package com.example.springboot.dto;

public class WaitlistInfoDTO {

    private Long scheduleId;
    private Long rank; // 您的排名 (从 0 开始)
    private Long queueSize; // 当前总排队人数

    // 您可以根据需要添加更多排班信息 (例如科室、医生)，但需要额外查询，暂时从简

    public WaitlistInfoDTO(Long scheduleId, Long rank, Long queueSize) {
        this.scheduleId = scheduleId;
        this.rank = rank;
        this.queueSize = queueSize;
    }

    // --- 省略 Getter 和 Setter ---
    // (您可以自行添加，或者使用 @Data LOMBOK 注解)
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
    public Long getQueueSize() { return queueSize; }
    public void setQueueSize(Long queueSize) { this.queueSize = queueSize; }
}