package top.wang3.hami.security.model;

public class LoginUser {

    private final int id;

    public LoginUser(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LoginUserBuilder withId(int id) {
        return builder().id(id);
    }

    public static LoginUserBuilder builder() {
        return new LoginUserBuilder();
    }

    public static class LoginUserBuilder {
        int id;

        public LoginUserBuilder id(int id) {
            this.id = id;
            return this;
        }

        public LoginUser build() {
            return new LoginUser(id);
        }
    }
}
