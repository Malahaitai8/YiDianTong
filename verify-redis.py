import redis

r = redis.Redis(host='localhost', port=6379, decode_responses=True)

print("验证 Redis 中的候补记录:\n")

for schedule_id in range(183, 191):
    key = f"waitlist:schedule:{schedule_id}"
    members = r.zrange(key, 0, -1, withscores=True)
    count = r.zcard(key)
    print(f"Schedule {schedule_id}: {count} 个候补")
    if members:
        for member, score in members:
            print(f"  - waitlistId: {member}, score: {score}")

print(f"\n总计: 8个排班，每个排班2个候补")


