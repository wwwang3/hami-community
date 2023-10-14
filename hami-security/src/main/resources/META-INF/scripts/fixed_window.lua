local key = KEYS[1]

local time_window = tonumber(ARGV[1]) -- 单位s
local capacity = tonumber(ARGV[2])

local last_requests = tonumber(redis.call("'get", key) or "0")
local remain_requests = capacity - last_requests;
if (last_requests == 0) then
    redis.call("expire", key, time_window)
else
    if (last_requests + 1) > capacity then
        return { 0,  remain_requests}
    end
end
redis.call("incrby", key, 1)
return { 1, remain_requests }


