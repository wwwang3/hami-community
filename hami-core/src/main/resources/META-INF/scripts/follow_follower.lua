local follower_list_key = KEYS[1] -- 用户关注列表对应的zset Key


local follower_id = tonumber(ARGV[1]) -- 关注的用户ID
local score1 = tonumber(ARGV[2])
local max_size = tonumber(ARGV[3])

local res1 = redis.call("zadd", follower_list_key, score1, follower_id)

local follower_size = redis.call("zcard", follower_list_key)


if (follower_size > max_size) then
    return redis.call("zpopmin", follower_list_key)
end

return res1;