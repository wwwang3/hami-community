local key = KEYS[1]

local time_window = tonumber(ARGV[1]) -- 单位s
local capacity = tonumber(ARGV[2])

local last_requests = tonumber(redis.call("get", key) or "0")
local remain_requests = capacity - last_requests;
if (last_requests == 0) then
    redis.call("set", key, 1)
    redis.call("expire", key, time_window)
elseif (remain_requests > 0) then
    redis.call("incrby", key, 1)
else
    return { 0,  remain_requests }
end
return { 1, remain_requests }