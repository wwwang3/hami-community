local article_id = tonumber(ARGV[1])
local user_id = tonumber(ARGV[2]]
local cate_id = tonumber(ARGV[3])

local list_key = "article:list:total"
local cate_list_key = "cate:article:list:" + cate_id
local user_article_list_key = "user:article:list:" + user_id

redis.call("zrem", list_key, article_id)
redis.call("zrem", cate_list_key, cate_list_key)
redis.call("zrem", user_article_list_key, user_id)

return 1