package io.github.soupedog.jpa.domain.bo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import hygge.util.UtilCreator;
import io.github.soupedog.jpa.utils.XmlHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 标记属性序列化顺序
@JsonPropertyOrder(value = {"name", "age", "sex", "balance", "phoneNumber", "roleList"})
@JacksonXmlRootElement(namespace = "asdasdasd", localName = "User")
public class UserXml {
    @JacksonXmlProperty(localName = "name_zh")
    private String name;
    private Integer age;
    private SexEnum sex;
    private BigDecimal balance;
    @JacksonXmlElementWrapper(localName = "phoneNumber")
    @JacksonXmlProperty(localName = "item")
    private List<String> phoneNumber;
    private List<Role> roleList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JacksonXmlRootElement(localName = "Role")
    public static class Role {
        // 作为上一层标签的属性而不是子标签
        @JacksonXmlProperty(isAttribute = true)
        private Integer rid;
        private String type;
    }

    public enum SexEnum {
        MALE, FEMALE
    }

    public static void main(String[] args) {
        UserXml user = UserXml.builder()
                .name("张三")
                .age(24)
                .sex(SexEnum.MALE)
                .balance(new BigDecimal("3.14"))
                .phoneNumber(Arrays.asList("130********", "131********"))
                .roleList(Arrays.asList(
                        Role.builder()
                                .rid(1)
                                .type("管理员")
                                .build()))
                .build();

        String xml = XmlHelper.formatAsString(true, user, UserXml.class);

        System.out.println(xml);

        System.out.println(UtilCreator.INSTANCE.getDefaultJsonHelperInstance(true).formatAsString(XmlHelper.readAsObject(xml, UserXml.class)));
    }
}
