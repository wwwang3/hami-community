local following_list_key = keys[1] -- 用户关注列表对应的zset Key
local follower_list_key = keys[2] -- 被关注用户的粉丝列表对应的zset key
local user_stat_key = keys[3] -- 用户数据key
local following_stat_key = keys[3] -- 被关注用户数据key

local following_id = tonumber(ARGV[1]) -- 关注的用户ID
local follower_id = tonumber(ARGV[2]) -- 粉丝ID

local res1 = redis.call("zrem", following_list_key, following_id)

local res2 = redis.call("zrem", follower_list_key, follower_id)

-- 用户的关注数减一
if (res1 == 1) then
    redis.call("hincrby", user_stat_key, followings, -1)
end
if (res2 == 1) then
    -- 被关注用户的粉丝数减一
    redis.call"hincrby", following_stat_key, "followers", -1)
end
return 1