package io.github.soupedog.mybatis.domain.enums;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
public enum UserSexEnum {
    /**
     * 保密
     */
    SECRET(-1, "SECRET"),
    /**
     * 女
     */
    WOMAN(0, "WOMAN"),
    /**
     * 男
     */
    MAN(1, "MAN");

    UserSexEnum(Integer index, String value) {
        this.index = index;
        this.value = value;
    }

    public static UserSexEnum parse(Integer index) {
        if (index == null) {
            throw new IllegalArgumentException("Unexpected index of UserSexEnum,it can't be null.");
        }
        switch (index) {
            case -1:
                return UserSexEnum.SECRET;
            case 1:
                return UserSexEnum.MAN;
            case 0:
                return UserSexEnum.WOMAN;
            default:
                throw new IllegalArgumentException("Unexpected index of UserSexEnum,it can't be " + index + ".");
        }
    }

    public static UserSexEnum parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unexpected value of UserSexEnum,it can't be null.");
        }
        switch (value) {
            case "SECRET":
                return UserSexEnum.SECRET;
            case "MAN":
                return UserSexEnum.MAN;
            case "WOMAN":
                return UserSexEnum.WOMAN;
            default:
                throw new IllegalArgumentException("Unexpected value of UserSexEnum,it can't be " + value + ".");
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
