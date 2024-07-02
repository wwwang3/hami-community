package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.SiteStat;
import top.wang3.hami.common.model.UserStat;

import java.util.*;

@Mapper
public interface StatConverter {

    StatConverter INSTANCE = Mappers.getMapper(StatConverter.class);

    ArticleStatDTO toArticleStatDTO(ArticleStat stat);
    List<ArticleStatDTO> toArticleStatDTOS(List<ArticleStat> stat);

    UserStatDTO toUserStatDTO(UserStat userStat);

    List<UserStatDTO> toUserStatDTO(List<UserStat> userStats);

    default UserStatDTO mapToUserStatDTO(Map<String, Integer> stat) {
        UserStatDTO statDTO = new UserStatDTO();
        statDTO.setUserId(stat.get(RedisConstants.USER_STAT_ID));
        statDTO.setTotalArticles(stat.get(RedisConstants.USER_STAT_ARTICLES));
        statDTO.setTotalViews(stat.get(RedisConstants.USER_STAT_VIEWS));
        statDTO.setTotalLikes(stat.get(RedisConstants.USER_STAT_LIKES));
        statDTO.setTotalComments(stat.get(RedisConstants.USER_STAT_COMMENTS));
        statDTO.setTotalCollects(stat.get(RedisConstants.USER_STAT_COLLECTS));
        statDTO.setTotalFollowers(stat.get(RedisConstants.USER_STAT_FOLLOWERS));
        statDTO.setTotalFollowings(stat.get(RedisConstants.USER_STAT_FOLLOWINGS));
        return statDTO;
    }

    default List<UserStatDTO> mapToUserStatDTO(List<Map<String, Integer>> mapList) {
        if (CollectionUtils.isEmpty(mapList)) return Collections.emptyList();
        ArrayList<UserStatDTO> stats = new ArrayList<>(mapList.size());
        for (Map<String, Integer> map : mapList) {
            stats.add(mapToUserStatDTO(map));
        }
        return stats;
    }

    default Map<String, Integer> userStatToMap(UserStat stat) {
        Objects.requireNonNull(stat);
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(RedisConstants.USER_STAT_ID, stat.getUserId());
        map.put(RedisConstants.USER_STAT_ARTICLES, stat.getTotalArticles());
        map.put(RedisConstants.USER_STAT_VIEWS, stat.getTotalViews());
        map.put(RedisConstants.USER_STAT_LIKES, stat.getTotalLikes());
        map.put(RedisConstants.USER_STAT_COMMENTS, stat.getTotalComments());
        map.put(RedisConstants.USER_STAT_COLLECTS, stat.getTotalCollects());
        map.put(RedisConstants.USER_STAT_FOLLOWERS, stat.getTotalFollowers());
        map.put(RedisConstants.USER_STAT_FOLLOWINGS, stat.getTotalFollowings());
        return map;
    }

    default List<Map<String, Integer>> userStatToMap(List<UserStat> stats) {
        if (CollectionUtils.isEmpty(stats)) {
            return Collections.emptyList();
        }
        ArrayList<Map<String, Integer>> results = new ArrayList<>(stats.size());
        for (UserStat stat : stats) {
            results.add(userStatToMap(stat));
        }
        return results;
    }

    default Map<String, Integer> siteStatToMap(SiteStat stat) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(6);
        map.put("users", stat.getUsers());
        map.put("articles", stat.getArticles());
        map.put("views", stat.getViews());
        map.put("likes", stat.getLikes());
        map.put("comments", stat.getComments());
        map.put("collects", stat.getCollects());
        map.put("pv", stat.getPv());
        map.put("uv", stat.getUv());
        return map;
    }

    default SiteStat maptoSiteStat(Map<String, Integer> data) {
        SiteStat stat = new SiteStat();
        stat.setUsers(data.get("users"));
        stat.setArticles(data.get("articles"));
        stat.setViews(data.get("views"));
        stat.setLikes(data.get("likes"));
        stat.setComments(data.get("comments"));
        stat.setCollects(data.get("collects"));
        stat.setPv(data.get("pv"));
        stat.setUv(data.get("uv"));
        return stat;
    }
}
