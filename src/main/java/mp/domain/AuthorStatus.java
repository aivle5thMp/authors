package mp.domain;

public enum AuthorStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String value;

    AuthorStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
} 