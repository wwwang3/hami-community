package top.wang3.hami.common.constant;

public enum ContentState {

    // 原始草稿, 用户还未发布的草稿
    ORIGIN_DRAFT(Constants.ZERO),

    // 正常
    NORMAL(Constants.ONE),

    // 审核中 用户点击发布或者更新按钮进入审核状态
    REVIEW(Constants.TWO),

    // 审核失败
    REVIEW_FAILED(Constants.THREE);


    public final Byte value;

    ContentState(byte value) {
        this.value = value;
    }

}
