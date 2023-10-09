local following_list_key = KEYS[1] -- 用户关注列表对应的zset Key


local following_id = tonumber(ARGV[1]) -- 关注的用户ID
local score1 = tonumber(ARGV[2])
local max_size = tonumber(ARGV[3])

local res1 = redis.call("zadd", following_list_key, score1, following_id)

local following_size = redis.call("zcard", following_list_key)


if (following_size > max_size) then
    return redis.call("zpopmin", following_list_key)
end

return res1;