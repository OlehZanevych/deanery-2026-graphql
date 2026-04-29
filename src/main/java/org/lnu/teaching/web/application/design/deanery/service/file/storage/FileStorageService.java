package org.lnu.teaching.web.application.design.deanery.service.file.storage;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStorageService {
    Mono<Void> saveFile(String fileName, FilePart filePart);
    Flux<DataBuffer> readFile(String fileName);
    Mono<Boolean> checkIfFileExists(String fileName);
    Mono<Boolean> removeFileIfExists(String fileName);
}
