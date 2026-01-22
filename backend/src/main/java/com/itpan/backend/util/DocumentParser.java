package com.itpan.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class DocumentParser {

    public String parse(InputStream inputStream) {
        try {
            // 1. 创建解析器 (AutoDetectParser 会自动猜测文件类型)
            Parser parser = new AutoDetectParser();

            // 2. 创建处理器 (BodyContentHandler 用于接收提取的文本)
            // -1 表示不限制字符长度 (默认有限制，大文件会报错)
            BodyContentHandler handler = new BodyContentHandler(-1);

            // 3. 元数据容器 (虽然我们可以不读取，但必须传)
            Metadata metadata = new Metadata();

            // 4. 解析上下文
            ParseContext context = new ParseContext();

            // 5. 执行解析
            parser.parse(inputStream, handler, metadata, context);

            // 6. 返回提取的文本
            String content = handler.toString();

            // 简单的清洗：去除多余的空行和制表符，压缩空白字符
            return content.replaceAll("\\s+", " ").trim();

        } catch (Exception e) {
            log.error("文档解析失败: {}", e.getMessage());
            throw new RuntimeException("文档解析失败", e);
        }
    }

}
