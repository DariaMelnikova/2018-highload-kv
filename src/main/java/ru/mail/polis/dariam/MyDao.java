package ru.mail.polis.dariam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

import ru.mail.polis.KVDao;

public class MyDao implements KVDao {
    private final String absolutePath;
    private final Base64.Encoder encoder;

    public MyDao(File path) {
        this.absolutePath = path.getAbsolutePath();
        this.encoder = Base64.getUrlEncoder();
    }

    @NotNull
    @Override
    public byte[] get(@NotNull byte[] key) throws NoSuchElementException, IOException {
        try {
            return Files.readAllBytes(getPathByKey(key));
        } catch (NoSuchFileException e) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void upsert(@NotNull byte[] key, @NotNull byte[] value) throws IOException {
        Files.write(getPathByKey(key), value);
    }

    @Override
    public void remove(@NotNull byte[] key) throws IOException {
        try {
            Files.delete(getPathByKey(key));
        } catch (NoSuchFileException e) {

        }
    }

    private Path getPathByKey(byte[] key) {
        return Paths.get(absolutePath, encoder.encodeToString(key));
    }

    @Override
    public void close() throws IOException {

    }
}
