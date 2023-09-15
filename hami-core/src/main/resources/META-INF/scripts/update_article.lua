local article_id = tonumber(ARGV[1])
local old_cate_id = tonumber(ARGV[2])
local new_cate_id = tonumber(ARGV[3])

local ctime = tonumber(ARGV[4])
local max_count = 1000

if (old_cate_id == new_cate_id) then
    return 0
end

local old_cate_list_key = "cate:article:list:" + old_cate_id
local new_cate_list_key = "cate:article:list:" + new_cate_id

-- 加入新的列表
redis.call("zadd", new_cate_list_key, ctime, new_cate_id)
redis.call("zrem", old_cate_list_key, old_cate_id)


local new_cate_count = redis.call("zcard", new_cate_list_key)

if (new_cate_count > max_count) then
    redis.call("zpopmin", new_cate_list_key)
end
return 1