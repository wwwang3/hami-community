package top.wang3.hami.core.service.mail.template;


import java.util.List;

public interface MailTemplate<T> {

    List<String> getReceivers(T item);

    String getSubject(T item);

    String render(T item);

    boolean html();
}
