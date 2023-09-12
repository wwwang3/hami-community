local key = keys[0]

local total_views = "total_views"
local likes = "total_likes"
local comments = "total_comments"
local collects = "total_collects"

local argv1 = tonumber(ARGV[1])
local argv2 = tonumber(ARGV[2])
local argv3 = tonumber(ARGV[3])
local argv4 = tonumber(ARGV[4])

if (argv1 != 0) then
    redis.call("hincrby", key, total_views, argv1)
end
if (argv2 != 0) then
    redis.call("hincrby", key, total_likes, argv2)
end
if (argv3 != 0) then
    redis.call("hincrby", key, total_comments, argv3)
end
if (argv4 != 0) then
    redis.call("hincrby", key, total_collects argv4)
end
return 1