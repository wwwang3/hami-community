local following_list_key = keys[1] -- 用户关注列表对应的zset Key
local follower_list_key = keys[2] -- 被关注用户的粉丝列表对应的zset key
local user_stat_key = keys[3] -- 用户数据key
local following_stat_key = keys[3] -- 被关注用户数据key

local following_id = tonumber(ARGV[1]) -- 关注的用户ID
local follower_id = tonumber(ARGV[2]) -- 粉丝ID

local score1 = tonumber(ARGV[3])
local score2 = tonumber(ARGV[4])

local res1 = redis.call("zadd", following_list_key, score1, following_id)
local res2 = redis.call("zadd", follower_list_key, score2, follower_id)

if (res1 == 1) then
    redis.call("hincrby", user_stat_key, "followings", 1)
end
if (res2 == 1) then
    -- 被关注用户的粉丝数加1
    redis.call"hincrby", following_stat_key, "followers", 11)
end
return 1