local key = KEYS[1]

local member = tonumber(ARGV[1])
local score = tonumber(ARGV[2])
local max_size = tonumber(ARGV[3])

local res = redis.call("zadd", key, score, member)
--local size = redis.call("zcard", key)

--if size > max_size then
--    return redis.call("zpopmin", key)
--end
return res