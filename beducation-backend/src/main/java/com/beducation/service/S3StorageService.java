package com.beducation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    @Value("${aws.access-key}")
    private String accessKey;

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

            // Modo Simulación para Desarrollo: Si las credenciales son placeholders, no intentamos subir a AWS S3 real
            if ("your-access-key".equals(accessKey)) {
                log.warn("AWS S3 en modo simulación (credenciales no configuradas). Saltando subida real del archivo: {}", originalFilename);
                return s3Key;
            }

            // Ejecutar la subida real
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
     * Elimina un archivo de S3.
     */
    public void deleteFile(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return;
        
        try {
            if ("your-access-key".equals(accessKey)) {
                log.warn("AWS S3 en modo simulación. Saltando eliminación real: {}", s3Key);
                return;
            }

            s3Client.deleteObject(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build());
            
            log.info("Archivo eliminado de S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Error al eliminar archivo de S3: {}", e.getMessage());
            // No lanzamos excepción para no romper el flujo si la eliminación falla (cleanup opcional)
        }
    }
}
