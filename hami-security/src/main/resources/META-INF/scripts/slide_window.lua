local key = KEYS[1]
local member = KEYS[2]

local rate = tonumber(ARGV[1])  -- 速率
local capacity = tonumber(ARGV[2]) -- 最大容量
local current = tonumber(ARGV[3]) -- 当前时间戳(单位s)

local window_size = tonumber(capacity / rate) -- 窗口大小(时间间隔)
-- 比如capacity=200，rate=20，window_size = 10 表示10秒内的最大请求数为200

local last_requests = 0
local exists = redis.call("exists", key)

if (exists == 1) then
    last_requests = redis.call("zcard", key)
end

local remain_requests = capacity - last_requests
local allowed = 0
if (last_requests < capacity) then
    allowed = 1
    redis.call("zadd", key, current, member)
end


redis.call('zremrangebyscore', key, 0, current - window_size)
redis.call("expire", key, window_size)

return { allowed, remain_requests }


