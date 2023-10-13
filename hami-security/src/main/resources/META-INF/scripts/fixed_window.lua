local key = KEYS[1]

local time_window = tonumber(ARGV[1]) -- 单位s
local capacity = tonumber(ARGV[2])

local current_requests = tonumber(redis.call("'get", key) or "0")
if (current_requests == 0) then
    redis.call("expire", key, time_window)
else
    if (current_requests + 1) > capacity then
        return -1;
    end
end
redis.call("incrby", key, 1)
return current_requests + 1;


