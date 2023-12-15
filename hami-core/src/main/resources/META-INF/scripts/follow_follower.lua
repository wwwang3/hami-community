local follower_list_key = KEYS[1] -- 用户粉丝列表对应的zset Key


local follower_id = tonumber(ARGV[1]) -- 粉丝ID
local score1 = tonumber(ARGV[2])
local state = tonumber(ARGV[3])
local max_size = tonumber(ARGV[4])

local res1
if state == 1 then
    res1 = redis.call("zadd", follower_list_key, score1, follower_id)

    local follower_size = redis.call("zcard", follower_list_key)
    if (follower_size > max_size) then
        return redis.call("zpopmin", follower_list_key)
    end
else
    res1 = redis.call("zrem", follower_list_key, follower_id)
end


return res1;