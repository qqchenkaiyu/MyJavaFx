package ch.makery.address.view;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    @SneakyThrows
    public static void writeObject(File file, Object object) {
        writeContent(file, JSON.toJSONString(object));
    }

    @SneakyThrows
    public static void writeContent(File file, String str) {
        Path path = file.toPath();
        Files.deleteIfExists(path);
        file.createNewFile();
        Files.write(path, str.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
    }

    @SneakyThrows
    public static <T> T readObject(File file, Class<T> clazz) {
        if (!file.exists()) {
            file.createNewFile();
            return clazz.newInstance();
        }
        byte[] readAllBytes = Files.readAllBytes(file.toPath());
        T parseObject = JSON.parseObject(new String(readAllBytes, StandardCharsets.UTF_8), clazz);
        return parseObject == null ? clazz.newInstance() : parseObject;
    }

    @SneakyThrows
    public static <T> List<T> readArray(File file, Class<T> clazz) {
        if (!file.exists()) {
            file.createNewFile();
            return Arrays.asList(clazz.newInstance());
        }
        byte[] readAllBytes = Files.readAllBytes(file.toPath());
        return JSON.parseArray(new String(readAllBytes, StandardCharsets.UTF_8), clazz);
    }
}
