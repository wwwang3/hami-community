local list_key = KEYS[1]
local cate_list_key = KEYS[2]
local user_article_list_key = KEYS[3]

local article_id = tonumber(ARGV[1])

redis.call("zrem", list_key, article_id)
redis.call("zrem", cate_list_key, article_id)
redis.call("zrem", user_article_list_key, article_id)

return 1