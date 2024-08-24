package io.github.soupedog.jpa.domain.bo;

import hygge.commons.template.definition.HyggeCode;

/**
 * @author Xavier
 * @date 2024/8/25
 * @since 1.0
 */
public enum SystemCode implements HyggeCode {
    FILE_NAME_CAN_NOT_BE_EMPTY(false, false, "文件名不可为空。", 400001, null),
    UNEXPECTED_FILE_EXTENSION(false, false, "不符合预期的文件扩展名。", 400002, null),
    FILE_NOT_FOUND(false, false, "目标文件未找到。", 404001, null),
    ;

    private boolean codeDuplicateEnable;
    private final boolean serious;
    private final String publicMessage;
    private Object code;
    private Object extraInfo;

    public boolean isCodeDuplicateEnable() {
        return this.codeDuplicateEnable;
    }

    public boolean isSerious() {
        return this.serious;
    }

    public String getPublicMessage() {
        return this.publicMessage;
    }

    public <C> C getCode() {
        return (C) this.code;
    }

    public <E> E getExtraInfo() {
        return (E) this.extraInfo;
    }

    SystemCode(boolean codeDuplicateEnable, boolean serious, String publicMessage, Object code, Object extraInfo) {
        this.codeDuplicateEnable = codeDuplicateEnable;
        this.serious = serious;
        this.publicMessage = publicMessage;
        this.code = code;
        this.extraInfo = extraInfo;
    }

    public void setCodeDuplicateEnable(boolean codeDuplicateEnable) {
        this.codeDuplicateEnable = codeDuplicateEnable;
    }

    public void setCode(Object code) {
        this.code = code;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }
}
