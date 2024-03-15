local key = KEYS[1]
local old_cate_hash = KEYS[2]
local new_cate_hash = KEYS[3]

local timeout = tonumber(ARGV[1])

if redis.call("pexpire", key, timeout) == 0 then
    -- 缓存过期
    return 0
end

redis.call("hincrby", key, old_cate_hash, -1)
redis.call("hincrby", key, new_cate_hash, 1)
return 1