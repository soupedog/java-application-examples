package io.github.soupedog.jpa.domain.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hygge.commons.template.definition.HyggeLogInfoObject;
import hygge.util.template.HyggeJsonUtilContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author Xavier
 * @date 2024/8/25
 * @since 1.0
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_entity")
public class FileEntity extends HyggeJsonUtilContainer implements HyggeLogInfoObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileId;
    private String fileNo;
    /**
     * fileName = name + extension
     */
    private String name;
    private String extension;
    private String fileSize;
    @Lob
    // @Lob 默认在 mysql 中会认为是 longblob GB 级大小，下面是手动改成 MB 级大小
    // @Column(columnDefinition = "mediumblob")
    private byte[] content;

    @Override
    public String toJsonLogInfo() {
        return jsonHelper.formatAsString(FileEntity.builder()
                .fileId(fileId)
                .fileNo(fileNo)
                .name(name)
                .extension(extension)
                .fileSize(fileSize)
                .build());
    }

    @JsonIgnore
    public String getFileName() {
        return name + "." + extension;
    }
}
