package top.wang3.hami.common.message;


public record ReviewFailedMessage(Long draftId, Integer userId, String title, String msg) implements RabbitMessage {

    @Override
    public String getRoute() {
        return "content.review.failed";
    }
}
