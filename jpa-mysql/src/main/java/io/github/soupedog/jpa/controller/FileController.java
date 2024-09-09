package io.github.soupedog.jpa.controller;

import hygge.commons.constant.enums.GlobalHyggeCodeEnum;
import hygge.commons.exception.InternalRuntimeException;
import hygge.commons.exception.ParameterRuntimeException;
import hygge.commons.exception.main.HyggeRuntimeException;
import hygge.util.UtilCreator;
import hygge.util.definition.UnitConvertHelper;
import hygge.util.template.HyggeJsonUtilContainer;
import hygge.web.template.definition.HyggeController;
import hygge.web.util.log.annotation.ControllerLog;
import io.github.soupedog.jpa.domain.po.FileEntity;
import io.github.soupedog.jpa.repository.FileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.soupedog.jpa.domain.bo.SystemCode.FILE_NAME_CAN_NOT_BE_EMPTY;
import static io.github.soupedog.jpa.domain.bo.SystemCode.FILE_NOT_FOUND;
import static io.github.soupedog.jpa.domain.bo.SystemCode.UNEXPECTED_FILE_EXTENSION;

/**
 * 写这块儿时 debug {@link RequestMappingHandlerAdapter#invokeHandlerMethod(HttpServletRequest, HttpServletResponse, HandlerMethod)} 而确定的代码
 *
 * @author Xavier
 * @date 2024/8/25
 * @since 1.0
 */
@RestController
public class FileController extends HyggeJsonUtilContainer implements HyggeController<ResponseEntity<?>> {
    @Autowired
    private FileDao fileDao;

    private UnitConvertHelper unitConvertHelper = UtilCreator.INSTANCE.getDefaultInstance(UnitConvertHelper.class);

    @PostMapping(value = "/file-entity", consumes = "multipart/form-data")
    @ControllerLog(inputParamEnable = false)
    public ResponseEntity<ArrayList<FileEntity>> saveTimeEntity(@RequestPart("files") List<MultipartFile> filesList) {
        ArrayList<FileEntity> result = new ArrayList<>();

        filesList.forEach(item -> {
            String fileName = item.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                throw new ParameterRuntimeException(FILE_NAME_CAN_NOT_BE_EMPTY.getPublicMessage(), FILE_NAME_CAN_NOT_BE_EMPTY);
            }

            String name;
            String extension;

            int indexOfLastPoint = fileName.lastIndexOf(".");
            if (indexOfLastPoint > 0 && indexOfLastPoint < fileName.length() - 1) {
                extension = fileName.substring(indexOfLastPoint + 1);
                name = fileName.substring(0, indexOfLastPoint);
            } else {
                // 没有扩展名或扩展名为''（如：.git）
                throw new ParameterRuntimeException("fileName:" + fileName, UNEXPECTED_FILE_EXTENSION);
            }

            try {
                FileEntity resultItem = FileEntity.builder()
                        .fileNo(randomHelper.getUniversallyUniqueIdentifier(true))
                        .name(name)
                        .extension(extension)
                        .fileSize(unitConvertHelper.storageSmartFormatAsString(item.getSize()))
                        .content(item.getBytes())
                        .build();

                fileDao.save(resultItem);

                // 已入库保存后无需返回给请求方
                resultItem.setContent(null);

                result.add(resultItem);
            } catch (IOException e) {
                throw new InternalRuntimeException("上传的文件保存失败。", e);
            }
        });

        return (ResponseEntity<ArrayList<FileEntity>>) success(result);
    }

    @GetMapping(value = "/file-entity/{fileNo}")
    @ControllerLog(outputParamEnable = false)
    public ResponseEntity<byte[]> getTimeEntity(@PathVariable("fileNo") String fileNo) {
        Optional<FileEntity> resultTemp = fileDao.findOne(Example.of(FileEntity.builder().fileNo(fileNo)
                .build()));

        if (!resultTemp.isPresent()) {
            return (ResponseEntity<byte[]>) fail(HttpStatus.NOT_FOUND, new HyggeRuntimeException(FILE_NOT_FOUND));
        }

        HttpHeaders headers = new HttpHeaders();

        boolean displayDirectly = true;

        switch (resultTemp.get().getExtension()) {
            case "png":
                headers.setContentType(MediaType.IMAGE_PNG);
                break;
            case "jpg":
            case "jpeg":
                headers.setContentType(MediaType.IMAGE_JPEG);
                break;
            case "gif":
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            case "pdf":
                headers.setContentType(MediaType.APPLICATION_PDF);
                break;
            case "mp3":
                headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
                break;
            default:
                displayDirectly = false;
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        // 影响鼠标右键图片另存为默认文件名称
        if (displayDirectly) {
            // inline() 是在浏览器直接展示
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(resultTemp.get().getFileName(), StandardCharsets.UTF_8)
                    .build()
            );
        } else {
            // attachment() 模式则是浏览器直接调用下载
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(resultTemp.get().getFileName(), StandardCharsets.UTF_8)
                    .build()
            );
        }

        return (ResponseEntity<byte[]>) successWithWrapper(HttpStatus.OK, headers, GlobalHyggeCodeEnum.SUCCESS, null, resultTemp.get().getContent(), emptyResponseWrapper);
    }

}
