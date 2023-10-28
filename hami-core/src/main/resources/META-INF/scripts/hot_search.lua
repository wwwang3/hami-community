local key = KEYS[1]
local keyword = "\""..ARGV[1].."\""


local res = redis.call("zincrby", key, 1, keyword)
local timeout = redis.call("expire", key, 86400)
local size = redis.call("zcard", key)

if (size > 10) then
    redis.call("zpopmin", key)
end

return res
