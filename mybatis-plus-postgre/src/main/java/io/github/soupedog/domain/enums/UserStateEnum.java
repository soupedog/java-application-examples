package io.github.soupedog.domain.enums;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
public enum UserStateEnum {
    /**
     * 禁用
     */
    INACTIVE(0, "禁用"),
    /**
     * 激活
     */
    ACTIVE(1, "激活");

    UserStateEnum(Integer index, String value) {
        this.index = index;
        this.value = value;
    }

    public static UserStateEnum parse(Integer index) {
        if (index == null) {
            throw new IllegalArgumentException("Unexpected index of UserStateEnum,it can't be null.");
        }
        switch (index) {
            case 1:
                return UserStateEnum.ACTIVE;
            case 0:
                return UserStateEnum.INACTIVE;
            default:
                throw new IllegalArgumentException("Unexpected index of UserStateEnum,it can't be " + index + ".");
        }
    }

    public static UserStateEnum parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unexpected value of UserStateEnum,it can't be null.");
        }
        switch (value) {
            case "ACTIVE":
                return UserStateEnum.ACTIVE;
            case "INACTIVE":
                return UserStateEnum.INACTIVE;
            default:
                throw new IllegalArgumentException("Unexpected value of UserStateEnum,it can't be " + value + ".");
        }
    }

    /**
     * 序号
     */
    private Integer index;
    /**
     * 枚举值
     */
    private String value;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
