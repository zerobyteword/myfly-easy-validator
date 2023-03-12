package io.github.zerobyteword.easyvalidator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiffItem {
    private String sourcePath;
    private String targetPath;
    private Object source;
    private Object target;
}
