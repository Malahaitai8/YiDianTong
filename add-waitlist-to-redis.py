#!/usr/bin/env python3
"""
将候补记录添加到 Redis 队列
需要先执行 add-waitlist.sql 插入数据库记录
"""
import redis
import pymysql
from datetime import datetime

# 数据库配置（根据实际情况修改）
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',  # 请修改为实际密码
    'database': 'yi_dian_tong_2',  # 请修改为实际数据库名
    'charset': 'utf8mb4'
}

# Redis 配置
REDIS_HOST = 'localhost'
REDIS_PORT = 6379

def add_waitlist_to_redis():
    # 连接数据库
    db = pymysql.connect(**DB_CONFIG)
    cursor = db.cursor()
    
    # 连接 Redis
    r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
    
    try:
        # 查询 scheduleId 183-190 的所有候补记录
        sql = """
        SELECT id, schedule_id, patient_id, join_time, status 
        FROM waitlist 
        WHERE schedule_id BETWEEN 183 AND 190 
        AND status = 'WAITING'
        ORDER BY schedule_id, join_time
        """
        cursor.execute(sql)
        results = cursor.fetchall()
        
        print(f"找到 {len(results)} 条候补记录")
        
        # 添加到 Redis
        for row in results:
            waitlist_id, schedule_id, patient_id, join_time, status = row
            
            # Redis key 格式：waitlist:schedule:{scheduleId}
            queue_key = f"waitlist:schedule:{schedule_id}"
            
            # 将 join_time 转换为时间戳（毫秒）
            if isinstance(join_time, datetime):
                score = int(join_time.timestamp() * 1000)
            else:
                # 如果是字符串，需要解析
                dt = datetime.strptime(str(join_time), '%Y-%m-%d %H:%M:%S')
                score = int(dt.timestamp() * 1000)
            
            # 添加到有序集合
            r.zadd(queue_key, {str(waitlist_id): score})
            
            # 添加到患者索引
            patient_key = f"waitlist:patient:{patient_id}"
            r.sadd(patient_key, str(waitlist_id))
            
            print(f"✓ 已添加: scheduleId={schedule_id}, waitlistId={waitlist_id}, patientId={patient_id}")
        
        print(f"\n成功添加 {len(results)} 条候补记录到 Redis")
        
    except Exception as e:
        print(f"错误: {e}")
    finally:
        cursor.close()
        db.close()

if __name__ == '__main__':
    print("开始将候补记录添加到 Redis...")
    add_waitlist_to_redis()

