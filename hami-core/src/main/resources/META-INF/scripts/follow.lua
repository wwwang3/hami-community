local following_list_key = KEYS[1] -- 用户关注列表对应的zset Key
local follower_list_key = KEYS[2] -- 被关注用户的粉丝列表对应的zset key

local following_id = tonumber(ARGV[1]) -- 关注的用户ID
local follower_id = tonumber(ARGV[2]) -- 粉丝ID

local score1 = tonumber(ARGV[3])
local score2 = tonumber(ARGV[4])
local max_size = tonumber(ARGV[5])

local res1 = redis.call("zadd", following_list_key, score1, following_id)
local res2 = redis.call("zadd", follower_list_key, score2, follower_id)

local following_size = redis.call("zcard", following_list_key)
local follower_size = redis.call("zcard", follower_list_key)

local res3
local res4

if (following_size > max_size) then
    redis.call("zpopmin", following_list_key)
end
if (follower_size > max_size) then
    redis.call("zpopmin", follower_list_key)
end
return {res1, res2, res3, res4}