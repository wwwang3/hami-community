local article_id = tonumber(ARGV[1])
local user_id = tonumber(ARGV[2]]
local cate_id = tonumber(ARGV[3])

local ctime = tonumber(ARGV[4]) -- 分数
local max_count = 1000; -- 1000篇文章 20篇每页，50页基本满足需求了

local list_key = "article:list:total"
local cate_list_key = "cate:article:list:" + cate_id
local user_article_list_key = "user:article:list:" + user_id

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

