local key = KEYS[1]
local total_hash = KEYS[2]
local cate_hash = KEYS[3]

local timeout = tonumber(ARGV[1])
local delta = tonumber(ARGV[2])

if redis.call("pexpire", key, timeout) == 0 then
    -- 缓存过期
    return 0
end

redis.call("hincrby", key, total_hash, delta)
redis.call("hincrby", key, cate_hash, delta)
return 1