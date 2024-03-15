local list_key = KEYS[1]


local article_id = tonumber(ARGV[1])
local ctime = tonumber(ARGV[2]) -- 分数
local timeout = tonumber(ARGV[3]) -- 过期时间
local max_count = tonumber(ARGV[4]) -- 最大元素数量

if redis.call("pexpire", list_key, timeout) == 0 then
    -- 过期
    return 0
end

redis.call("zadd", list_key, ctime, article_id) -- 总的文章列表

local current_list_count = redis.call("zcard", list_key)

if current_list_count > max_count then
    redis.call("zpopmin", list_key)
end

return 1

