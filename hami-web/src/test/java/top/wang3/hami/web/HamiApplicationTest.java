package top.wang3.hami.web;


import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.Like;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.service.article.ArticleCollectService;
import top.wang3.hami.core.service.like.LikeService;
import top.wang3.hami.core.service.user.UserFollowService;
import top.wang3.hami.core.service.user.UserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ContextConfiguration(classes = HamiCommunityApplication.class)
public class HamiApplicationTest {


    @Resource
    UserService userService;

    @Resource
    ArticleCollectService articleCollectService;

    @Resource
    LikeService likeService;

    @Resource
    UserFollowService userFollowService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Test
    void testUserService() {
        List<Integer> userIds = ChainWrappers.queryChain(userService.getBaseMapper())
                .select("user_id")
                .list().stream().map(User::getUserId).toList();

        int start = 10;
        int end = 20;
        int size = userIds.size();
        Random random1 = new Random();
        Random random2 = new Random();
        System.out.println(userIds.size());
        ArrayList<UserFollow> list1 = new ArrayList<>(2560);
        for (int userId : userIds) {
            int rounds = start + random1.nextInt(end - start);
            HashSet<Integer> set = new HashSet<>();
            while (rounds > 0) {
                int index = random2.nextInt(size);
                int value = userIds.get(index);
                if (value != userId && !set.contains(value)) {
                    set.add(value);
                    list1.add(new UserFollow(null, userId, value, 1, null, null));
                    rounds--;
                }
            }
            if (list1.size() >= 2000) {
                userFollowService.saveBatch(list1);
                list1.clear();
            }
        }
        if (!list1.isEmpty()) {
            userFollowService.saveBatch(list1);
        }

        int seq = 1;
        ArrayList<Like> list2 = new ArrayList<>(2560);
        for (int userId : userIds) {
            int rounds = start + random1.nextInt(end - start);
            while (rounds > 0) {
                list2.add(new Like(null, seq++, (byte) 1, userId, (byte) 1, null, null));
                rounds--;
            }
            if (list2.size() >= 1000) {
                likeService.saveBatch(list2);
                list2.clear();
            }
        }
        if (!list2.isEmpty()) {
            likeService.saveBatch(list2);
        }

        seq = 1;
        ArrayList<ArticleCollect> list3 = new ArrayList<>(2560);
        for (int userId : userIds) {
            int rounds = start + random1.nextInt(end - start);
            while (rounds > 0) {
                list3.add(new ArticleCollect(null, null, userId, seq++, (byte) 1, null, null, null));
                rounds--;
            }
            if (list3.size() >= 1000) {
                articleCollectService.saveBatch(list3);
                list3.clear();
            }
        }
        if (!list3.isEmpty()) {
            articleCollectService.saveBatch(list3);
        }

    }



}
