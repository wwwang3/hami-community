package top.wang3.hami.security.storage;


/**
 * 退出登录后jwt黑名单存储接口
 */
public interface BlacklistStorage {

    /**
     * 将jwt添加进黑名单
     * @param jwtId jwt的Id
     * @param expireAt jwt过期时间(毫秒级时间戳)
     */
    boolean add(String jwtId, long expireAt);

    /**
     * 黑名单是否包含该jwtId
     * @param jwtId jwt的Id
     * @return true -包含 false -不包含
     */
    boolean contains(String jwtId);
}
