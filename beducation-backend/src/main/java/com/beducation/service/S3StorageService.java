package com.beducation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * ============================================================
 * Servicio: S3StorageService
 * ============================================================
 * Gestiona la subida de archivos (documentos de estudiantes y 
 * logos de empresas) a un bucket de Amazon S3.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Sube un archivo a S3 y devuelve la clave (key) generada.
     *
     * @param file     archivo MultipartFile procedente del endpoint
     * @param folder   carpeta destino en S3 (ej. "students/123/cv")
     * @return la clave S3 del archivo subido
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Generar un nombre de archivo único usando UUID
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String s3Key = folder + "/" + UUID.randomUUID() + extension;

            // Configurar la petición de subida a S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

            // Ejecutar la subida
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Archivo subido a S3 con éxito: {}", s3Key);
            return s3Key;
            
        } catch (IOException e) {
            log.error("Error al leer el archivo MultipartFile: {}", e.getMessage());
            throw new RuntimeException("Error al procesar el archivo a subir.", e);
        } catch (Exception e) {
            log.error("Error al subir archivo a S3: {}", e.getMessage());
            throw new RuntimeException("Fallo en el servicio de almacenamiento (AWS S3).", e);
        }
    }

    /**
     * Obtiene la URL pública de lectura de un objeto en S3.
     */
    public String getFileUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return null;
        try {
            return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucketName).key(s3Key).build())
                .toString();
        } catch (Exception e) {
            log.warn("No se pudo generar la URL para el recurso S3: {}", s3Key);
            return null;
        }
    }
}
