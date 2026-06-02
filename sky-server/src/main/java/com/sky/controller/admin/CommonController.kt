package com.sky.controller.admin

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.exception.IllegalException
import com.sky.result.Result
import com.sky.utils.AliOssUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Tag(name = "通用接口")
@RestController
@RequestMapping("/admin/common")
class CommonController(
    private val aliOssUtil: AliOssUtil
) {
    @AutoLog(msg = "文件上传")
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    fun upload(file: MultipartFile): Result<String> {
        val objectName = file.let {
            val orgName =
                it.originalFilename ?: throw IllegalException(MessageConstant.Param.FILE_NO_NAME)
            var extension = orgName.substringAfterLast('.')
            extension = if (extension == orgName) ".unknown" else ".${extension}"
            UUID.randomUUID().toString() + extension//返回拼接后的文件名:
        }
        val filePath = aliOssUtil.upload(file.bytes, objectName) ?: return Result.error(MessageConstant.Server.UPLOAD_FAILED)
        return Result.success(filePath)
    }
}