package top.wang3.hami.mail.model;

import jakarta.activation.FileDataSource;
import jakarta.activation.FileTypeMap;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;
import top.wang3.hami.mail.manager.MailSenderManager;
import top.wang3.hami.mail.sender.CustomMailSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * MimeMessageHelper包装类
 */
@Slf4j
@SuppressWarnings(value = {"unused"})
public class PrepareMimeMessageHelper {

    private final MailSenderManager manager;
    private final CustomMailSender boundCustomSender;
    @Getter
    private final JavaMailSenderImpl mailSender;
    private final List<MessageWrapper> messages = new ArrayList<>();

    public PrepareMimeMessageHelper(MailSenderManager manager) {
       this.manager = manager;
       this.boundCustomSender = manager.getMailSender();
       this.mailSender = boundCustomSender.getJavaMailSender();
    }

    public PrepareMimeMessageHelper(MailSenderManager manager, CustomMailSender boundCustomSender) {
        this.manager = manager;
        this.boundCustomSender = boundCustomSender;
        this.mailSender = boundCustomSender.getJavaMailSender();
    }

    private FileTypeMap getBoundFileTypeMap() {
        return mailSender.getDefaultFileTypeMap();
    }

    private String getBoundEncoding() {
        return mailSender.getDefaultEncoding();
    }

    @SuppressWarnings(value = {"unused"})
    public class MessageWrapper {
        private final List<Attachment> attachments = new ArrayList<>();
        private final List<Address> bccs = new ArrayList<>();
        private final List<Address> ccs = new ArrayList<>();
        private Date date = new Date();
        private String subject;

        private final List<Address> tos = new ArrayList<>();
        private Address replyTo;
        //纯文本内容
        private final StringBuilder plainText = new StringBuilder();
        //html内容 (若同时设置了纯文本内容和HTML内容，邮件客户端若支持HTMl则不会显示纯文本内容)
        private final StringBuilder htmlText = new StringBuilder();

        public MessageWrapper() {
        }


        public MessageWrapper bcc(String ...bcc) {
            return bcc(Arrays.asList(bcc));
        }

        public MessageWrapper bcc(String bcc, String personal) {
            bccs.add(new Address(bcc, personal));
            return this;
        }

        public MessageWrapper bcc(Collection<String> bcc) {
            List<Address> list = bcc.stream().map(Address::new).toList();
            bccs.addAll(list);
            return this;
        }

        public MessageWrapper cc(String ...cc) {
            return cc(Arrays.asList(cc));
        }

        public MessageWrapper cc(String cc, String personal) {
            ccs.add(new Address(cc, personal));
            return this;
        }

        public MessageWrapper cc(Collection<String> cc) {
            List<Address> list = cc.stream().map(Address::new).toList();
            ccs.addAll(list);
            return this;
        }

        public MessageWrapper to(String to) {
            tos.add(new Address(to));
            return this;
        }

        public MessageWrapper to(String ...tos) {
            if (tos != null) {
                return to(Arrays.asList(tos));
            }
            return this;
        }

        public MessageWrapper to(List<String> tos) {
            if (tos != null && !tos.isEmpty()) {
                List<Address> addresses = tos.stream().map(Address::new).toList();
                this.tos.addAll(addresses);
            }
            return this;
        }

        public MessageWrapper to(String to, String personal) {
            tos.add(new Address(to, personal));
            return this;
        }

        public MessageWrapper replyTo(String replyTo) {
            return replyTo(replyTo, null);
        }

        public MessageWrapper replyTo(String replyTo, String personal) {
            this.replyTo = new Address(replyTo, personal);
            return this;
        }

        public MessageWrapper subject(String subject) {
            this.subject = subject;
            return this;
        }

        public MessageWrapper date(Date date) {
            this.date = date;
            return this;
        }

        public MessageWrapper text(String text) {
            this.plainText.append(text);
            return this;
        }

        public MessageWrapper html(String html) {
            this.htmlText.append(html);
            return this;
        }

        public MessageWrapper addAttachment(File file) {
            if (file != null) {
                var source = new FileDataSource(file);
                source.setFileTypeMap(getBoundFileTypeMap());
                Attachment attachment = new Attachment(file.getName(), source);
                attachments.add(attachment);
            }
            return this;
        }

        public MessageWrapper addAttachment(byte[] bytes, String name, String type) {
            if (bytes != null && bytes.length > 0) {
                var dataSource = new ByteArrayDataSource(bytes, type);
                attachments.add(new Attachment(name, dataSource));
            }
            return this;
        }

        public MessageWrapper addAttachment(InputStream is, String name, String type) {
            if (is != null) {
                try {
                    var dataSource = new ByteArrayDataSource(is, type);
                    attachments.add(new Attachment(name, dataSource));
                } catch (IOException e) {
                    throw new MailPreparationException(e);
                }
            }
            return this;
        }

        /**
         * 独立开辟一个新的MessWrapper, 并将之前的MessageWrapper添加进messages
         * @return MessageWrapper
         */
        public MessageWrapper free() {
            PrepareMimeMessageHelper.this.messages.add(this);
            return PrepareMimeMessageHelper.this.create();
        }

        public MailSendResult send() {
            PrepareMimeMessageHelper.this.messages.add(this);
            return PrepareMimeMessageHelper.this.doSend();
        }

        public MimeMessage transfer() {
            //使用绑定的MailSender构建MimeMessage
            return transfer(PrepareMimeMessageHelper.this.getMailSender());
        }

        /**
         * 转换为MimeMessageHepler,不会检查属性的合法性
         * @param sender JavaMailSenderImpl
         * @return MimeMessage
         */
        @SuppressWarnings(value = {"all"})
        public MimeMessage transfer(JavaMailSenderImpl sender) {
            try {
                var message = sender.createMimeMessage();
                var from = sender.getUsername();
                var helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED);
                var  encoding = helper.getEncoding();
                var bcc = convertToInternetAddress(this.bccs, encoding);
                var cc = convertToInternetAddress(this.ccs, encoding);
                var to = convertToInternetAddress(this.tos, encoding);
                helper.setFrom(from);
                if (bcc != null) {
                    helper.setBcc(bcc);
                }
                if (cc != null) {
                    helper.setCc(cc);
                }
                helper.setTo(to);
                if (replyTo != null) {
                    helper.setReplyTo(this.replyTo.getAddress(), this.replyTo.getPersonal());
                }
                helper.setSentDate(date == null ? new Date() : date);
                helper.setSubject(subject);
                if (htmlText != null) {
                    helper.setText(htmlText.toString(), true);
                }
                if (plainText != null) {
                    helper.setText(plainText.toString());
                }
                for (Attachment attachment : attachments) {
                    if (attachment.getName() != null && attachment.getDataSource() != null) {
                        helper.addAttachment(attachment.getName(), attachment.getDataSource());
                    }
                }
                return message;
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.error(e.getMessage());
                throw new MailPreparationException(e.getMessage(), e);
            }
        }

        private InternetAddress[] convertToInternetAddress(List<Address> addresses, String encoding) {
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }
            try {
                List<InternetAddress> list = new ArrayList<>();
                for (Address address : addresses) {
                    if (address != null && StringUtils.hasText(address.getAddress())) {
                        InternetAddress internetAddress = new InternetAddress(address.getAddress(), address.getPersonal());
                        list.add(internetAddress);
                    }
                }
                return list.toArray(new InternetAddress[0]);
            } catch (UnsupportedEncodingException e) {
                log.error("unsupported encoding, ignore address");
                return null;
            }
        }

    }

    public MessageWrapper create() {
        return new MessageWrapper();
    }

    public CustomMailSender getCustomMailSender() {
        return this.boundCustomSender;
    }

    public MimeMessage[] convertToMimeMessage(JavaMailSenderImpl sender) {
        var wrappers = this.messages;
        int size = wrappers.size();
        if (size == 0) {
            log.warn("no message need to send");
            return new MimeMessage[0];
        }
        var mimeMessages = new MimeMessage[size];
        for (int i = 0; i < wrappers.size(); i++) {
            mimeMessages[i] = wrappers.get(i).transfer(sender);
        }
        return mimeMessages;
    }

    public MimeMessage[] build() {
        return convertToMimeMessage(getMailSender());
    }

    public MimeMessage[] rebuild(CustomMailSender otherService) {
        return convertToMimeMessage(otherService.getJavaMailSender());
    }

    public MailSendResult doSend() {
        return manager.send(this);
    }
}
