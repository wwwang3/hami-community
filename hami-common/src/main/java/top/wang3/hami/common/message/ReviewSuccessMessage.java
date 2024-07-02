package top.wang3.hami.common.message;

public record ReviewSuccessMessage(Long draftId, Integer userId, String title) implements RabbitMessage {

    @Override
    public String getRoute() {
        return "content.review.success";
    }
}
