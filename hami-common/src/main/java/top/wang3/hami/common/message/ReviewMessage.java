package top.wang3.hami.common.message;


public record ReviewMessage(Long draftId) implements RabbitMessage {

    @Override
    public String getRoute() {
        return "content.review.need";
    }
}
