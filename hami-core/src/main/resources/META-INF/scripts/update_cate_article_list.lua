local old_cate_list_key = KEYS[1]
local new_cate_list_key = KEYS[2]

local article_id = tonumber(ARGV[1])
local ctime = tonumber(ARGV[2])
local max_count = 8000

if (old_cate_list_key == new_cate_list_key) then
    return 0
end


-- 加入新的列表
redis.call("zadd", new_cate_list_key, ctime, article_id)
redis.call("zrem", old_cate_list_key, article_id)


local new_cate_count = redis.call("zcard", new_cate_list_key)

if (new_cate_count > max_count) then
    redis.call("zpopmin", new_cate_list_key)
end
return 1