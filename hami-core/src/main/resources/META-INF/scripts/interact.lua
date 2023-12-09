local zset_key = KEYS[1]

local member = tonumber(ARGV[1]) -- 成员
local score = tonumber(ARGV[2]) -- 分数
local state = tonumber(ARGV[3]) -- 状态 决定删除还是添加
local timeout = tonumber(ARGV[4]) -- 过期时间

local expired = redis.call("pexpire", zset_key, math.max(1, timeout))

if expired == 0 then
    -- 过期
    return -1
end

if state == 1 then
    -- 不存在则添加 返回添加的元素数量 为0表示失败
    return redis.call("zadd", zset_key, "NX", score, member)
else
    -- 删除, 返回删除的元素个数, 为0表示member不存在或者失败
    return redis.call("zrem", zset_key, member)
end

