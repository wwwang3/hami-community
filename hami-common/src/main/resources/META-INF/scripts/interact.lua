local zset_key = KEYS[1]

local timeout = tonumber(ARGV[1])
local member = tonumber(ARGV[2]) -- 成员
local score = tonumber(ARGV[3]) -- 分数
local state = tonumber(ARGV[4]) -- 状态 决定删除还是添加

if redis.call("pexpire", zset_key, timeout) == 0 then
     -- 缓存过期
    return -1
end

local exist = redis.call("zscore", zset_key, member)

if state == 1 then
    if exist then
        -- 要添加但已经存在
        return 2
    else
        -- 要添加但不存在 成功返回1
        return redis.call("zadd", zset_key, score, member)
    end
else
    if exist then
        -- 要删除同时存在 成功返回1
        return redis.call("zrem", zset_key, member)
    else
        -- 要删除但是不存在
        return 3
    end
end


