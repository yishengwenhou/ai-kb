package com.itpan.backend.service.impl;

import com.itpan.backend.mapper.DocumentMapper;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.util.DocumentParser;
import com.itpan.backend.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAsyncServiceImpl {

    private final OssUtil ossUtil;
    private final DocumentParser documentParser;
    private final DocumentMapper documentMapper;

    @Async // 必须在启动类开启 @EnableAsync
    public void parseDocument(Long docId) {
        log.info("开始异步解析文档: {}", docId);
        Document doc = documentMapper.selectById(docId);
        if (doc == null) return;

        try {
            // 1. 下载
            InputStream inputStream = ossUtil.download(doc.getFilePath());
            // 2. 解析
            String content = documentParser.parse(inputStream);
            inputStream.close();

            if (content == null || content.trim().isEmpty()) {
                log.warn("文档解析结果为空: {}", docId);
                // 也可以考虑标记为特殊状态，或者 just log warning
                content = "(该文档未提取到可读文本，可能是纯图片或加密文档)";
            }

            // 3. 更新数据库
            doc.setContent(content);
            doc.setCharCount(content.length());
            doc.setStatus(0); // 0 = 解析完成/就绪
            documentMapper.updateById(doc);

            log.info("文档解析成功，字符数: {}", content.length());
        } catch (Exception e) {
            log.error("文档解析失败", e);
            doc.setStatus(3); // 3 = 失败
            documentMapper.updateById(doc);
        }
    }
}