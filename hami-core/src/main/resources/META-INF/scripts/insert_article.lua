local list_key = KEYS[1]
local cate_list_key = KEYS[2]
local user_article_list_key = KEYS[3]

local article_id = tonumber(ARGV[1])
local ctime = tonumber(ARGV[2]) -- 分数
local max_count = 5000; -- 5000篇文章 20篇每页，250页基本满足需求了


redis.call("zadd", list_key, ctime, article_id) -- 总的文章列表
redis.call("zadd", cate_list_key, ctime, article_id) -- 分类文章列表
redis.call("zadd", user_article_list_key, ctime, article_id) -- 用户文章

local current_list_count = redis.call("zcard", list_key)

if (current_list_count > max_count) then
    redis.call("zpopmin", list_key)
end

local current_cate_list_count = redis.call("zcard", cate_list_key)

if (current_cate_list_count > max_count) then
    redis.call("zpopmin", cate_list_key)
end
return 1

